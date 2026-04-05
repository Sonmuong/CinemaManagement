package dao;

import model.Ticket;
import util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class TicketDAO {

    // ── Inner class kết quả hủy vé ───────────────────────────────
    public static class CancelResult {
        public final boolean success;
        public final boolean refunded;       // true = hoàn tiền 80%, false = không hoàn
        public final double  refundAmount;   // số tiền thực tế hoàn lại

        public CancelResult(boolean success, boolean refunded, double refundAmount) {
            this.success      = success;
            this.refunded     = refunded;
            this.refundAmount = refundAmount;
        }
    }

    // ── Lấy tất cả vé ───────────────────────────────────────────
    public List<Ticket> getAllTickets() {
        List<Ticket> tickets = new ArrayList<>();
        String sql = "SELECT t.*, c.full_name as customer_name, m.movie_name, " +
                     "r.room_name, s.show_date, CONVERT(VARCHAR(5), s.show_time, 108) as show_time " +
                     "FROM Tickets t " +
                     "LEFT JOIN Customers c ON t.customer_id = c.customer_id " +
                     "INNER JOIN Showtimes s ON t.showtime_id = s.showtime_id " +
                     "INNER JOIN Movies m ON s.movie_id = m.movie_id " +
                     "INNER JOIN Rooms r ON s.room_id = r.room_id " +
                     "ORDER BY t.booking_date DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) tickets.add(extractTicketFromResultSet(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return tickets;
    }

    // ── Lấy vé theo ID ──────────────────────────────────────────
    public Ticket getTicketById(int ticketId) {
        String sql = "SELECT t.*, c.full_name as customer_name, m.movie_name, " +
                     "r.room_name, s.show_date, CONVERT(VARCHAR(5), s.show_time, 108) as show_time " +
                     "FROM Tickets t " +
                     "LEFT JOIN Customers c ON t.customer_id = c.customer_id " +
                     "INNER JOIN Showtimes s ON t.showtime_id = s.showtime_id " +
                     "INNER JOIN Movies m ON s.movie_id = m.movie_id " +
                     "INNER JOIN Rooms r ON s.room_id = r.room_id " +
                     "WHERE t.ticket_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ticketId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return extractTicketFromResultSet(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // ── BÁN VÉ ──────────────────────────────────────────────────
    public int sellTicket(Ticket ticket, boolean usePoints) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            if (conn == null) { System.out.println("❌ sellTicket: không kết nối được DB!"); return -1; }
            conn.setAutoCommit(false);

            String seat       = ticket.getSeatNumber().trim();
            int    showtimeId = ticket.getShowtimeId();

            if (isSeatBooked(conn, showtimeId, seat)) {
                System.out.println("  ⚠️ Ghế " + seat + " đã được đặt!");
                conn.rollback();
                return -2;
            }

            double basePrice    = ticket.getTicketPrice();
            double pricePerSeat = basePrice;
            String ticketType   = ticket.getTicketType() != null ? ticket.getTicketType() : "Normal";

            switch (ticketType) {
                case "VIP":     pricePerSeat = Math.round(basePrice * 1.2); break;
                case "Student": pricePerSeat = Math.round(basePrice * 0.9); break;
                default:        pricePerSeat = basePrice;
            }

            double discountAmount = 0;
            double pointsUsed     = 0;
            double finalPrice     = pricePerSeat;

            if (ticket.getCustomerId() != null && usePoints) {
                double curPoints = getCustomerPoints(conn, ticket.getCustomerId());
                if (curPoints >= 100) {
                    double usable      = Math.floor(curPoints / 100) * 100;
                    double maxDiscount = usable / 100.0 * 10000.0;
                    discountAmount     = Math.min(maxDiscount, pricePerSeat);
                    pointsUsed         = Math.floor(discountAmount / 10000.0) * 100;
                    discountAmount     = pointsUsed / 100.0 * 10000.0;
                    finalPrice         = Math.max(0, pricePerSeat - discountAmount);
                }
            }

            String insertSql =
                "INSERT INTO Tickets " +
                "(customer_id, showtime_id, seat_number, ticket_type, " +
                " ticket_price, discount_amount, final_price, " +
                " points_used, points_earned, payment_status, payment_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 0, 'Paid', ?)";

            PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
            if (ticket.getCustomerId() != null) ps.setInt(1, ticket.getCustomerId());
            else ps.setNull(1, Types.INTEGER);
            ps.setInt(2, showtimeId);
            ps.setString(3, seat);
            ps.setString(4, ticketType);
            ps.setDouble(5, pricePerSeat);
            ps.setDouble(6, discountAmount);
            ps.setDouble(7, finalPrice);
            ps.setDouble(8, pointsUsed);
            ps.setTimestamp(9, new Timestamp(System.currentTimeMillis()));

            int affected = ps.executeUpdate();
            if (affected == 0) { conn.rollback(); return -1; }

            int ticketId = -1;
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) ticketId = keys.getInt(1);

            if (ticket.getCustomerId() != null && ticketId > 0) {
                if (pointsUsed > 0) {
                    updatePoints(conn, ticket.getCustomerId(), -pointsUsed);
                    insertPointTx(conn, ticket.getCustomerId(), ticketId,
                        -pointsUsed, "Redeem", "Dùng điểm mua vé #" + ticketId);
                }
                double earned = Math.floor(finalPrice * 0.05);
                if (earned > 0) {
                    PreparedStatement updTicket = conn.prepareStatement(
                        "UPDATE Tickets SET points_earned = ? WHERE ticket_id = ?");
                    updTicket.setDouble(1, earned);
                    updTicket.setInt(2, ticketId);
                    updTicket.executeUpdate();
                    updatePoints(conn, ticket.getCustomerId(), earned);
                    insertPointTx(conn, ticket.getCustomerId(), ticketId,
                        earned, "Earn", "Tích điểm mua vé #" + ticketId);
                }
            }

            conn.commit();
            return ticketId;

        } catch (SQLIntegrityConstraintViolationException e) {
            rollback(conn); return -2;
        } catch (Exception e) {
            e.printStackTrace(); rollback(conn); return -1;
        } finally {
            restoreAndClose(conn);
        }
    }

    // ── HỦY VÉ — trả về CancelResult với thông tin hoàn tiền ────
    public CancelResult cancelTicket(int ticketId) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            if (conn == null) return new CancelResult(false, false, 0);
            conn.setAutoCommit(false);

            Ticket ticket = getTicketById(ticketId);
            if (ticket == null) { rollback(conn); return new CancelResult(false, false, 0); }

            // Lấy thời gian chiếu
            PreparedStatement ps = conn.prepareStatement(
                "SELECT show_date, show_time FROM Showtimes WHERE showtime_id = ?");
            ps.setInt(1, ticket.getShowtimeId());
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) { rollback(conn); return new CancelResult(false, false, 0); }

            LocalDateTime showDT = LocalDateTime.of(
                rs.getDate("show_date").toLocalDate(),
                rs.getTime("show_time").toLocalTime());
            long hoursLeft = ChronoUnit.HOURS.between(LocalDateTime.now(), showDT);

            // Hoàn 80% nếu hủy trước 2 giờ chiếu, không hoàn nếu trễ
            boolean isRefund   = hoursLeft >= 2;
            String  newStatus  = isRefund ? "Refunded" : "Cancelled";
            double  refundAmt  = isRefund ? Math.round(ticket.getFinalPrice() * 0.8) : 0;

            PreparedStatement upd = conn.prepareStatement(
                "UPDATE Tickets SET payment_status = ? WHERE ticket_id = ?");
            upd.setString(1, newStatus);
            upd.setInt(2, ticketId);
            upd.executeUpdate();

            // Xử lý điểm khi hủy
            if (ticket.getCustomerId() != null) {
                double delta = 0;
                // Trừ lại điểm đã tích từ vé này
                if (ticket.getPointsEarned() > 0) delta -= ticket.getPointsEarned();
                // Hoàn lại điểm đã dùng (chỉ khi được refund tiền)
                if (isRefund && ticket.getPointsUsed() > 0) delta += ticket.getPointsUsed();
                if (delta != 0) {
                    updatePoints(conn, ticket.getCustomerId(), delta);
                    insertPointTx(conn, ticket.getCustomerId(), ticketId,
                        delta, "Refund",
                        (isRefund ? "Hoàn vé #" : "Hủy vé #") + ticketId);
                }
            }

            conn.commit();
            return new CancelResult(true, isRefund, refundAmt);

        } catch (SQLException e) {
            e.printStackTrace();
            rollback(conn);
            return new CancelResult(false, false, 0);
        } finally {
            restoreAndClose(conn);
        }
    }

    // ── Helpers nội bộ ───────────────────────────────────────────
    private boolean isSeatBooked(Connection conn, int showtimeId, String seatNumber)
            throws SQLException {
        String sql = "SELECT COUNT(*) FROM Tickets " +
                     "WHERE showtime_id = ? AND seat_number = ? AND payment_status = 'Paid'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, showtimeId);
            ps.setString(2, seatNumber.trim());
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    private double getCustomerPoints(Connection conn, int customerId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT loyalty_points FROM Customers WHERE customer_id = ?")) {
            ps.setInt(1, customerId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble("loyalty_points");
        }
        return 0;
    }

    private void updatePoints(Connection conn, int customerId, double delta) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE Customers SET loyalty_points = loyalty_points + ? WHERE customer_id = ?")) {
            ps.setDouble(1, delta);
            ps.setInt(2, customerId);
            int rows = ps.executeUpdate();
            if (rows == 0) throw new SQLException("KH " + customerId + " không tồn tại!");
        }
    }

    private void insertPointTx(Connection conn, int customerId, int ticketId,
                                double points, String type, String desc) {
        String sql = "INSERT INTO PointTransactions " +
                     "(customer_id, ticket_id, points_change, transaction_type, description) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            if (ticketId > 0) ps.setInt(2, ticketId);
            else ps.setNull(2, Types.INTEGER);
            ps.setDouble(3, points);
            ps.setString(4, type);
            ps.setString(5, desc);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("⚠️ PointTransactions insert lỗi: " + e.getMessage());
        }
    }

    public boolean isSeatBooked(int showtimeId, String seatNumber) {
        String sql = "SELECT COUNT(*) FROM Tickets " +
                     "WHERE showtime_id = ? AND seat_number = ? AND payment_status = 'Paid'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, showtimeId);
            ps.setString(2, seatNumber.trim());
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public List<Ticket> getTicketsByCustomer(int customerId) {
        List<Ticket> tickets = new ArrayList<>();
        String sql = "SELECT t.*, c.full_name as customer_name, m.movie_name, " +
                     "r.room_name, s.show_date, CONVERT(VARCHAR(5), s.show_time, 108) as show_time " +
                     "FROM Tickets t " +
                     "INNER JOIN Customers c ON t.customer_id = c.customer_id " +
                     "INNER JOIN Showtimes s ON t.showtime_id = s.showtime_id " +
                     "INNER JOIN Movies m ON s.movie_id = m.movie_id " +
                     "INNER JOIN Rooms r ON s.room_id = r.room_id " +
                     "WHERE t.customer_id = ? ORDER BY t.booking_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) tickets.add(extractTicketFromResultSet(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return tickets;
    }

    private void rollback(Connection conn) {
        if (conn != null) try { conn.rollback(); } catch (SQLException e) { e.printStackTrace(); }
    }

    private void restoreAndClose(Connection conn) {
        if (conn != null) try { conn.setAutoCommit(true); conn.close(); }
        catch (SQLException e) { e.printStackTrace(); }
    }

    private Ticket extractTicketFromResultSet(ResultSet rs) throws SQLException {
        Ticket t = new Ticket();
        t.setTicketId(rs.getInt("ticket_id"));
        int customerId = rs.getInt("customer_id");
        if (!rs.wasNull()) t.setCustomerId(customerId);
        t.setShowtimeId(rs.getInt("showtime_id"));
        t.setSeatNumber(rs.getString("seat_number"));
        t.setTicketType(rs.getString("ticket_type"));
        t.setTicketPrice(rs.getDouble("ticket_price"));
        t.setDiscountAmount(rs.getDouble("discount_amount"));
        t.setFinalPrice(rs.getDouble("final_price"));
        t.setPointsUsed(rs.getDouble("points_used"));
        t.setPointsEarned(rs.getDouble("points_earned"));
        t.setPaymentStatus(rs.getString("payment_status"));
        t.setBookingDate(rs.getTimestamp("booking_date"));
        Timestamp payDate = rs.getTimestamp("payment_date");
        if (payDate != null) t.setPaymentDate(payDate);
        t.setCustomerName(rs.getString("customer_name"));
        t.setMovieName(rs.getString("movie_name"));
        t.setRoomName(rs.getString("room_name"));
        t.setShowDate(rs.getDate("show_date"));
        t.setShowTime(rs.getString("show_time"));
        return t;
    }
}