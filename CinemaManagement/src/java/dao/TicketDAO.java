package dao;

import model.Ticket;
import model.Customer;
import util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class TicketDAO {

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
    /**
     * Bán 1 vé cho 1 ghế.
     * FIX: Tính đúng giá theo ticketType trước khi áp dụng điểm.
     *   - VIP     : giá gốc × 1.2  (+20%)
     *   - Student : giá gốc × 0.9  (-10%)
     *   - Normal  : giá gốc
     * @return ticketId (>0) nếu thành công, -2 nếu ghế đã đặt, -1 nếu lỗi khác
     */
    public int sellTicket(Ticket ticket, boolean usePoints) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            if (conn == null) {
                System.out.println("❌ sellTicket: không kết nối được DB!");
                return -1;
            }
            conn.setAutoCommit(false);

            String seat       = ticket.getSeatNumber().trim();
            int    showtimeId = ticket.getShowtimeId();

            // 1. Kiểm tra ghế đã đặt chưa
            if (isSeatBooked(conn, showtimeId, seat)) {
                System.out.println("  ⚠️ Ghế " + seat + " đã được đặt!");
                conn.rollback();
                return -2;
            }

            // 2. FIX: Tính giá theo loại vé
            double basePrice     = ticket.getTicketPrice();   // giá gốc từ showtime
            double pricePerSeat  = basePrice;
            String ticketType    = ticket.getTicketType() != null ? ticket.getTicketType() : "Normal";

            switch (ticketType) {
                case "VIP":
                    pricePerSeat = Math.round(basePrice * 1.2);   // +20%
                    break;
                case "Student":
                    pricePerSeat = Math.round(basePrice * 0.9);   // -10%
                    break;
                default:
                    pricePerSeat = basePrice;
            }

            System.out.println("  TicketType=" + ticketType
                + " basePrice=" + basePrice
                + " pricePerSeat=" + pricePerSeat);

            double discountAmount = 0;
            double pointsUsed     = 0;
            double finalPrice     = pricePerSeat;

            // 3. Xử lý điểm tích lũy (tính trên pricePerSeat, không phải basePrice)
            if (ticket.getCustomerId() != null && usePoints) {
                double curPoints = getCustomerPoints(conn, ticket.getCustomerId());
                System.out.println("  KH " + ticket.getCustomerId()
                    + " điểm hiện tại: " + curPoints);
                if (curPoints >= 100) {
                    double usable      = Math.floor(curPoints / 100) * 100;
                    double maxDiscount = usable / 100.0 * 10000.0;
                    discountAmount     = Math.min(maxDiscount, pricePerSeat);
                    pointsUsed         = Math.floor(discountAmount / 10000.0) * 100;
                    discountAmount     = pointsUsed / 100.0 * 10000.0;
                    finalPrice         = Math.max(0, pricePerSeat - discountAmount);
                    System.out.println("  Dùng " + pointsUsed + " điểm → giảm "
                        + discountAmount + " đ → còn " + finalPrice + " đ");
                }
            }

            // 4. INSERT vào Tickets — lưu pricePerSeat (đã tính loại vé) làm ticket_price
            String insertSql =
                "INSERT INTO Tickets " +
                "(customer_id, showtime_id, seat_number, ticket_type, " +
                " ticket_price, discount_amount, final_price, " +
                " points_used, points_earned, payment_status, payment_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 0, 'Paid', ?)";

            PreparedStatement ps = conn.prepareStatement(insertSql,
                Statement.RETURN_GENERATED_KEYS);

            if (ticket.getCustomerId() != null) {
                ps.setInt(1, ticket.getCustomerId());
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            ps.setInt(2, showtimeId);
            ps.setString(3, seat);
            ps.setString(4, ticketType);
            ps.setDouble(5, pricePerSeat);      // FIX: lưu giá đã nhân hệ số
            ps.setDouble(6, discountAmount);
            ps.setDouble(7, finalPrice);
            ps.setDouble(8, pointsUsed);
            ps.setTimestamp(9, new Timestamp(System.currentTimeMillis()));

            int affected = ps.executeUpdate();
            if (affected == 0) {
                System.out.println("  ❌ INSERT Tickets = 0 rows!");
                conn.rollback();
                return -1;
            }

            int ticketId = -1;
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) ticketId = keys.getInt(1);
            System.out.println("  ✅ INSERT Tickets OK, ticket_id=" + ticketId);

            // 5. Cập nhật điểm khách hàng
            if (ticket.getCustomerId() != null && ticketId > 0) {

                // 5a. Trừ điểm đã dùng
                if (pointsUsed > 0) {
                    updatePoints(conn, ticket.getCustomerId(), -pointsUsed);
                    insertPointTx(conn, ticket.getCustomerId(), ticketId,
                        -pointsUsed, "Redeem", "Dùng điểm mua vé #" + ticketId);
                }

                // 5b. Tích điểm 5% finalPrice
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
                    System.out.println("  ⭐ Tích " + earned + " điểm");
                }
            }

            conn.commit();
            System.out.println("  ✅ commit OK");
            return ticketId;

        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("  ⚠️ UNIQUE violation: " + e.getMessage());
            rollback(conn);
            return -2;
        } catch (SQLException e) {
            System.out.println("  ❌ SQLException: " + e.getMessage());
            e.printStackTrace();
            rollback(conn);
            return -1;
        } catch (Exception e) {
            System.out.println("  ❌ Exception: " + e.getMessage());
            e.printStackTrace();
            rollback(conn);
            return -1;
        } finally {
            restoreAndClose(conn);
        }
    }

    // ── HỦY VÉ ──────────────────────────────────────────────────
    public boolean cancelTicket(int ticketId) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            if (conn == null) return false;
            conn.setAutoCommit(false);

            Ticket ticket = getTicketById(ticketId);
            if (ticket == null) { rollback(conn); return false; }

            PreparedStatement ps = conn.prepareStatement(
                "SELECT show_date, show_time FROM Showtimes WHERE showtime_id = ?");
            ps.setInt(1, ticket.getShowtimeId());
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) { rollback(conn); return false; }

            LocalDateTime showDT = LocalDateTime.of(
                rs.getDate("show_date").toLocalDate(),
                rs.getTime("show_time").toLocalTime());
            long hoursLeft = ChronoUnit.HOURS.between(LocalDateTime.now(), showDT);

            String newStatus = hoursLeft >= 2 ? "Refunded" : "Cancelled";

            PreparedStatement upd = conn.prepareStatement(
                "UPDATE Tickets SET payment_status = ? WHERE ticket_id = ?");
            upd.setString(1, newStatus);
            upd.setInt(2, ticketId);
            upd.executeUpdate();

            if (ticket.getCustomerId() != null) {
                double delta = 0;
                if (ticket.getPointsEarned() > 0) delta -= ticket.getPointsEarned();
                if ("Refunded".equals(newStatus) && ticket.getPointsUsed() > 0)
                    delta += ticket.getPointsUsed();
                if (delta != 0) {
                    updatePoints(conn, ticket.getCustomerId(), delta);
                    insertPointTx(conn, ticket.getCustomerId(), ticketId,
                        delta, "Refund", "Hủy vé #" + ticketId);
                }
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            rollback(conn);
            return false;
        } finally {
            restoreAndClose(conn);
        }
    }

    // ── Kiểm tra ghế đã đặt (dùng trong cùng connection) ────────
    private boolean isSeatBooked(Connection conn, int showtimeId, String seatNumber)
            throws SQLException {
        String sql = "SELECT COUNT(*) FROM Tickets " +
                     "WHERE showtime_id = ? AND seat_number = ? " +
                     "AND payment_status = 'Paid'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, showtimeId);
            ps.setString(2, seatNumber.trim());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int cnt = rs.getInt(1);
                System.out.println("    isSeatBooked(" + seatNumber + ")=" + cnt);
                return cnt > 0;
            }
        }
        return false;
    }

    // ── Lấy điểm khách hàng (dùng trong cùng connection) ────────
    private double getCustomerPoints(Connection conn, int customerId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT loyalty_points FROM Customers WHERE customer_id = ?")) {
            ps.setInt(1, customerId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble("loyalty_points");
        }
        return 0;
    }

    // ── Cập nhật điểm (dùng trong cùng connection) ───────────────
    private void updatePoints(Connection conn, int customerId, double delta)
            throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE Customers SET loyalty_points = loyalty_points + ? " +
                "WHERE customer_id = ?")) {
            ps.setDouble(1, delta);
            ps.setInt(2, customerId);
            int rows = ps.executeUpdate();
            System.out.println("    updatePoints KH=" + customerId
                + " delta=" + delta + " rows=" + rows);
            if (rows == 0) throw new SQLException("KH " + customerId + " không tồn tại!");
        }
    }

    // ── Ghi lịch sử điểm ─────────────────────────────────────────
    private void insertPointTx(Connection conn, int customerId, int ticketId,
                               double points, String type, String desc) {
        String sql = "INSERT INTO PointTransactions " +
                     "(customer_id, ticket_id, points_change, " +
                     " transaction_type, description) " +
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
            System.out.println("    ⚠️ PointTransactions insert lỗi: " + e.getMessage());
        }
    }

    // ── Kiểm tra ghế (public, dùng ngoài transaction) ───────────
    public boolean isSeatBooked(int showtimeId, String seatNumber) {
        String sql = "SELECT COUNT(*) FROM Tickets " +
                     "WHERE showtime_id = ? AND seat_number = ? " +
                     "AND payment_status = 'Paid'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, showtimeId);
            ps.setString(2, seatNumber.trim());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    // ── Lấy vé theo khách hàng ──────────────────────────────────
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

    // ── Helpers ──────────────────────────────────────────────────
    private void rollback(Connection conn) {
        if (conn != null) try { conn.rollback(); } catch (SQLException e) { e.printStackTrace(); }
    }

    private void restoreAndClose(Connection conn) {
        if (conn != null) try {
            conn.setAutoCommit(true);
            conn.close();
        } catch (SQLException e) { e.printStackTrace(); }
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