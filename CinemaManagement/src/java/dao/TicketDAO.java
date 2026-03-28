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
    private CustomerDAO customerDAO = new CustomerDAO();

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
            conn.setAutoCommit(false);

            // 1. Kiểm tra ghế trong cùng connection (tránh deadlock)
            if (isSeatBookedInConn(conn, ticket.getShowtimeId(), ticket.getSeatNumber())) {
                System.out.println("❌ Ghế " + ticket.getSeatNumber() + " đã được đặt!");
                conn.rollback();
                return -1;
            }

            double originalPrice  = ticket.getTicketPrice();
            double discountAmount = 0;
            double pointsUsed     = 0;
            double finalPrice     = originalPrice;

            // 2. Xử lý điểm — FIX: lấy điểm trong cùng connection, tính đúng công thức
            if (ticket.getCustomerId() != null && usePoints) {
                double currentPoints = getCustomerPointsInConn(conn, ticket.getCustomerId());
                if (currentPoints >= 100) {
                    // Làm tròn xuống bội số 100
                    double usablePoints = Math.floor(currentPoints / 100) * 100;
                    // Mỗi 100 điểm = 10,000đ
                    double maxDiscount = usablePoints / 100.0 * 10000.0;
                    discountAmount = Math.min(maxDiscount, originalPrice);
                    // Số điểm dùng = discount / 10000 * 100, làm tròn xuống bội 100
                    pointsUsed = Math.floor(discountAmount / 10000.0) * 100;
                    discountAmount = pointsUsed / 100.0 * 10000.0;
                    finalPrice = Math.max(0, originalPrice - discountAmount);
                    System.out.println("💎 Dùng " + pointsUsed + " điểm, giảm " + discountAmount + "đ, còn " + finalPrice + "đ");
                }
            }

            // 3. INSERT vé
            String insertSql = "INSERT INTO Tickets " +
                     "(customer_id, showtime_id, seat_number, ticket_type, " +
                     "ticket_price, discount_amount, final_price, " +
                     "points_used, points_earned, payment_status, payment_date) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 0, 'Paid', ?)";

            PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
            if (ticket.getCustomerId() != null) {
                ps.setInt(1, ticket.getCustomerId());
            } else {
                ps.setNull(1, Types.INTEGER);
            }
            ps.setInt(2, ticket.getShowtimeId());
            ps.setString(3, ticket.getSeatNumber());
            ps.setString(4, ticket.getTicketType() != null ? ticket.getTicketType() : "Normal");
            ps.setDouble(5, originalPrice);
            ps.setDouble(6, discountAmount);
            ps.setDouble(7, finalPrice);
            ps.setDouble(8, pointsUsed);
            ps.setTimestamp(9, new Timestamp(System.currentTimeMillis()));

            int affected = ps.executeUpdate();
            if (affected == 0) {
                System.out.println("❌ INSERT Tickets thất bại!");
                conn.rollback();
                return -1;
            }

            int ticketId = -1;
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) ticketId = keys.getInt(1);
            System.out.println("✅ INSERT vé thành công, ticket_id = " + ticketId);

            // 4. Cập nhật điểm khách hàng (nếu có)
            if (ticket.getCustomerId() != null && ticketId > 0) {

                // 4a. Trừ điểm đã dùng
                if (pointsUsed > 0) {
                    PreparedStatement deductPs = conn.prepareStatement(
                            "UPDATE Customers SET loyalty_points = loyalty_points - ? WHERE customer_id = ?");
                    deductPs.setDouble(1, pointsUsed);
                    deductPs.setInt(2, ticket.getCustomerId());
                    deductPs.executeUpdate();
                    System.out.println("💳 Đã trừ " + pointsUsed + " điểm");
                }

                // 4b. Tích điểm 5% final_price
                double pointsEarned = Math.floor(finalPrice * 0.05);
                if (pointsEarned > 0) {
                    PreparedStatement updTicket = conn.prepareStatement(
                            "UPDATE Tickets SET points_earned = ? WHERE ticket_id = ?");
                    updTicket.setDouble(1, pointsEarned);
                    updTicket.setInt(2, ticketId);
                    updTicket.executeUpdate();

                    PreparedStatement earnPs = conn.prepareStatement(
                            "UPDATE Customers SET loyalty_points = loyalty_points + ? WHERE customer_id = ?");
                    earnPs.setDouble(1, pointsEarned);
                    earnPs.setInt(2, ticket.getCustomerId());
                    earnPs.executeUpdate();
                    System.out.println("⭐ Đã cộng " + pointsEarned + " điểm");
                }

                // 4c. Ghi lịch sử điểm (KHÔNG rollback nếu thất bại)
                double pointsEarned2 = Math.floor(finalPrice * 0.05);
                tryInsertPointTransaction(conn, ticket.getCustomerId(), ticketId,
                        pointsEarned2 - pointsUsed, "Purchase", "Mua vé #" + ticketId);
            }

            conn.commit();
            System.out.println("✅ Bán vé hoàn tất! ticket_id=" + ticketId);
            return ticketId;

        } catch (SQLException e) {
            System.out.println("❌ SQLException khi bán vé: " + e.getMessage());
            e.printStackTrace();
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return -1;
        } finally {
            try { if (conn != null) { conn.setAutoCommit(true); conn.close(); } }
            catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // ── HỦY VÉ ──────────────────────────────────────────────────
    public boolean cancelTicket(int ticketId) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            Ticket ticket = getTicketById(ticketId);
            if (ticket == null) { conn.rollback(); return false; }

            PreparedStatement ps = conn.prepareStatement(
                    "SELECT show_date, show_time FROM Showtimes WHERE showtime_id = ?");
            ps.setInt(1, ticket.getShowtimeId());
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) { conn.rollback(); return false; }

            LocalDateTime showDateTime = LocalDateTime.of(
                    rs.getDate("show_date").toLocalDate(),
                    rs.getTime("show_time").toLocalTime());
            long hoursUntilShow = ChronoUnit.HOURS.between(LocalDateTime.now(), showDateTime);

            String refundStatus = hoursUntilShow >= 2 ? "Refunded" : "Cancelled";

            PreparedStatement upd = conn.prepareStatement(
                    "UPDATE Tickets SET payment_status = ? WHERE ticket_id = ?");
            upd.setString(1, refundStatus);
            upd.setInt(2, ticketId);
            upd.executeUpdate();

            if (ticket.getCustomerId() != null) {
                double pointDelta = 0;
                if (ticket.getPointsEarned() > 0) pointDelta -= ticket.getPointsEarned();
                if ("Refunded".equals(refundStatus) && ticket.getPointsUsed() > 0) {
                    pointDelta += ticket.getPointsUsed();
                }
                if (pointDelta != 0) {
                    PreparedStatement updPoints = conn.prepareStatement(
                            "UPDATE Customers SET loyalty_points = loyalty_points + ? WHERE customer_id = ?");
                    updPoints.setDouble(1, pointDelta);
                    updPoints.setInt(2, ticket.getCustomerId());
                    updPoints.executeUpdate();
                }
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return false;
        } finally {
            try { if (conn != null) { conn.setAutoCommit(true); conn.close(); } }
            catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // ── Kiểm tra ghế trong cùng connection ──────────────────────
    private boolean isSeatBookedInConn(Connection conn, int showtimeId, String seatNumber)
            throws SQLException {
        String sql = "SELECT COUNT(*) FROM Tickets " +
                     "WHERE showtime_id = ? AND seat_number = ? " +
                     "AND payment_status IN ('Paid', 'Pending')";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, showtimeId);
        ps.setString(2, seatNumber);
        ResultSet rs = ps.executeQuery();
        return rs.next() && rs.getInt(1) > 0;
    }

    // ── Kiểm tra ghế (public — mở connection mới) ───────────────
    public boolean isSeatBooked(int showtimeId, String seatNumber) {
        try (Connection conn = DBConnection.getConnection()) {
            return isSeatBookedInConn(conn, showtimeId, seatNumber);
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    // ── Lấy điểm khách hàng trong cùng connection ───────────────
    private double getCustomerPointsInConn(Connection conn, int customerId) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
                "SELECT loyalty_points FROM Customers WHERE customer_id = ?");
        ps.setInt(1, customerId);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) return rs.getDouble("loyalty_points");
        return 0;
    }

    // ── Ghi lịch sử điểm — KHÔNG throw nếu thất bại ─────────────
    private void tryInsertPointTransaction(Connection conn, int customerId, int ticketId,
                                           double pointDelta, String type, String desc) {
        String[] cols = {"points_change", "points"};
        for (String col : cols) {
            try {
                String sql = "INSERT INTO PointTransactions " +
                        "(customer_id, ticket_id, " + col + ", transaction_type, description, transaction_date) " +
                        "VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, customerId);
                ps.setInt(2, ticketId);
                ps.setDouble(3, pointDelta);
                ps.setString(4, type);
                ps.setString(5, desc);
                ps.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
                ps.executeUpdate();
                return;
            } catch (SQLException e) {
                System.out.println("⚠️ PointTransactions cột '" + col + "': " + e.getMessage());
            }
        }
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

    // ── Extract Ticket từ ResultSet ──────────────────────────────
    private Ticket extractTicketFromResultSet(ResultSet rs) throws SQLException {
        Ticket ticket = new Ticket();
        ticket.setTicketId(rs.getInt("ticket_id"));

        int customerId = rs.getInt("customer_id");
        if (!rs.wasNull()) ticket.setCustomerId(customerId);

        ticket.setShowtimeId(rs.getInt("showtime_id"));
        ticket.setSeatNumber(rs.getString("seat_number"));
        ticket.setTicketType(rs.getString("ticket_type"));
        ticket.setTicketPrice(rs.getDouble("ticket_price"));
        ticket.setDiscountAmount(rs.getDouble("discount_amount"));
        ticket.setFinalPrice(rs.getDouble("final_price"));
        ticket.setPointsUsed(rs.getDouble("points_used"));
        ticket.setPointsEarned(rs.getDouble("points_earned"));
        ticket.setPaymentStatus(rs.getString("payment_status"));
        ticket.setBookingDate(rs.getTimestamp("booking_date"));

        Timestamp paymentDate = rs.getTimestamp("payment_date");
        if (paymentDate != null) ticket.setPaymentDate(paymentDate);

        ticket.setCustomerName(rs.getString("customer_name"));
        ticket.setMovieName(rs.getString("movie_name"));
        ticket.setRoomName(rs.getString("room_name"));
        ticket.setShowDate(rs.getDate("show_date"));
        ticket.setShowTime(rs.getString("show_time"));

        return ticket;
    }
}