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

            // 1. Kiểm tra ghế đã đặt chưa
            if (isSeatBooked(ticket.getShowtimeId(), ticket.getSeatNumber())) {
                System.out.println("❌ Ghế " + ticket.getSeatNumber() + " đã được đặt!");
                return -1;
            }

            double originalPrice  = ticket.getTicketPrice();
            double discountAmount = 0;
            double pointsUsed     = 0;
            double finalPrice     = originalPrice;

            // 2. Xử lý sử dụng điểm (100 điểm = 10,000 VNĐ)
            if (ticket.getCustomerId() != null && usePoints) {
                Customer customer = customerDAO.getCustomerById(ticket.getCustomerId());
                if (customer != null && customer.getLoyaltyPoints() >= 100) {
                    // Số điểm khả dụng (phải là bội số 100)
                    double availablePoints = Math.floor(customer.getLoyaltyPoints() / 100) * 100;

                    // Tối đa giảm không quá giá vé
                    // 100 điểm = 10,000đ  →  1 điểm = 100đ
                    double maxDiscount = availablePoints * 100; // tổng tiền có thể giảm
                    discountAmount = Math.min(maxDiscount, originalPrice);

                    // Quy ngược lại số điểm thực sự dùng
                    pointsUsed = Math.ceil(discountAmount / 100); // mỗi 100đ = 1 điểm
                    // Làm tròn xuống bội số 100
                    pointsUsed = Math.floor(pointsUsed / 100) * 100;
                    discountAmount = pointsUsed * 100; // tính lại discount cho chính xác

                    finalPrice = originalPrice - discountAmount;
                    System.out.println("💎 Dùng " + pointsUsed + " điểm, giảm " + discountAmount + "đ");
                }
            }

            // 3. INSERT vé
            String sql = "INSERT INTO Tickets (customer_id, showtime_id, seat_number, " +
                         "ticket_type, ticket_price, discount_amount, final_price, " +
                         "points_used, points_earned, payment_status, payment_date) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
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
            ps.setDouble(9, 0); // points_earned cập nhật sau
            ps.setString(10, "Paid");
            ps.setTimestamp(11, new Timestamp(System.currentTimeMillis()));

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

            // 4. Xử lý điểm tích lũy cho khách hàng
            if (ticket.getCustomerId() != null && ticketId > 0) {

                // 4a. Trừ điểm đã dùng
                if (pointsUsed > 0) {
                    updatePointsDirectly(conn, ticket.getCustomerId(), -pointsUsed);
                    insertPointTransaction(conn, ticket.getCustomerId(), ticketId,
                            -pointsUsed, "Redeem", "Sử dụng điểm mua vé");
                    System.out.println("💳 Đã trừ " + pointsUsed + " điểm");
                }

                // 4b. Tích điểm 5% final_price
                double pointsEarned = Math.floor(finalPrice * 0.05);
                if (pointsEarned > 0) {
                    // Cập nhật points_earned trong vé
                    PreparedStatement upd = conn.prepareStatement(
                            "UPDATE Tickets SET points_earned = ? WHERE ticket_id = ?");
                    upd.setDouble(1, pointsEarned);
                    upd.setInt(2, ticketId);
                    upd.executeUpdate();

                    updatePointsDirectly(conn, ticket.getCustomerId(), pointsEarned);
                    insertPointTransaction(conn, ticket.getCustomerId(), ticketId,
                            pointsEarned, "Earn", "Tích điểm từ mua vé");
                    System.out.println("⭐ Đã cộng " + pointsEarned + " điểm");
                }
            }

            conn.commit();
            System.out.println("✅ Bán vé hoàn tất!");
            return ticketId;

        } catch (SQLException e) {
            System.out.println("❌ SQLException khi bán vé: " + e.getMessage());
            e.printStackTrace();
            rollback(conn);
            return -1;
        } finally {
            restoreAutoCommit(conn);
        }
    }

    // ── HỦY VÉ ──────────────────────────────────────────────────
    public boolean cancelTicket(int ticketId) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            Ticket ticket = getTicketById(ticketId);
            if (ticket == null) return false;

            // Lấy giờ chiếu
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT show_date, show_time FROM Showtimes WHERE showtime_id = ?");
            ps.setInt(1, ticket.getShowtimeId());
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return false;

            LocalDateTime showDateTime = LocalDateTime.of(
                    rs.getDate("show_date").toLocalDate(),
                    rs.getTime("show_time").toLocalTime());
            long hoursUntilShow = ChronoUnit.HOURS.between(LocalDateTime.now(), showDateTime);

            String refundStatus = hoursUntilShow >= 2 ? "Refunded" : "Cancelled";
            double refundAmount  = hoursUntilShow >= 2 ? ticket.getFinalPrice() * 0.8 : 0;

            // Cập nhật trạng thái vé
            PreparedStatement upd = conn.prepareStatement(
                    "UPDATE Tickets SET payment_status = ? WHERE ticket_id = ?");
            upd.setString(1, refundStatus);
            upd.setInt(2, ticketId);
            upd.executeUpdate();

            // Xử lý điểm
            if (ticket.getCustomerId() != null) {
                if (ticket.getPointsEarned() > 0) {
                    updatePointsDirectly(conn, ticket.getCustomerId(), -ticket.getPointsEarned());
                    insertPointTransaction(conn, ticket.getCustomerId(), ticketId,
                            -ticket.getPointsEarned(), "Refund", "Trừ điểm do hủy vé");
                }
                if (refundAmount > 0 && ticket.getPointsUsed() > 0) {
                    updatePointsDirectly(conn, ticket.getCustomerId(), ticket.getPointsUsed());
                    insertPointTransaction(conn, ticket.getCustomerId(), ticketId,
                            ticket.getPointsUsed(), "Refund", "Hoàn điểm do hủy vé");
                }
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            rollback(conn);
            return false;
        } finally {
            restoreAutoCommit(conn);
        }
    }

    // ── Kiểm tra ghế đã đặt chưa ────────────────────────────────
    public boolean isSeatBooked(int showtimeId, String seatNumber) {
        String sql = "SELECT COUNT(*) FROM Tickets " +
                     "WHERE showtime_id = ? AND seat_number = ? " +
                     "AND payment_status IN ('Paid', 'Pending')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, showtimeId);
            ps.setString(2, seatNumber);
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

    // ════════════════════════════════════════════════════════════
    // PRIVATE HELPERS
    // ════════════════════════════════════════════════════════════

    /**
     * Cập nhật điểm trực tiếp trong cùng connection (tránh deadlock).
     * delta > 0: cộng điểm | delta < 0: trừ điểm
     */
    private void updatePointsDirectly(Connection conn, int customerId, double delta)
            throws SQLException {
        String sql = "UPDATE Customers SET loyalty_points = loyalty_points + ? " +
                     "WHERE customer_id = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setDouble(1, delta);
        ps.setInt(2, customerId);
        int rows = ps.executeUpdate();
        if (rows == 0) {
            throw new SQLException("Không tìm thấy khách hàng ID=" + customerId + " để cập nhật điểm!");
        }
    }

    /**
     * Ghi lịch sử giao dịch điểm trong cùng connection.
     */
    private void insertPointTransaction(Connection conn, int customerId, int ticketId,
                                        double points, String type, String description)
            throws SQLException {
        // Thử insert — nếu bảng PointTransactions không tồn tại thì bỏ qua
        try {
            String sql = "INSERT INTO PointTransactions " +
                         "(customer_id, ticket_id, points, transaction_type, description, transaction_date) " +
                         "VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, customerId);
            if (ticketId > 0) ps.setInt(2, ticketId); else ps.setNull(2, Types.INTEGER);
            ps.setDouble(3, points);
            ps.setString(4, type);
            ps.setString(5, description);
            ps.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
            ps.executeUpdate();
        } catch (SQLException e) {
            // Nếu bảng chưa có thì không throw, chỉ log
            System.out.println("⚠️ Không ghi được PointTransactions (bảng chưa tồn tại?): " + e.getMessage());
        }
    }

    private void rollback(Connection conn) {
        if (conn != null) try { conn.rollback(); } catch (SQLException e) { e.printStackTrace(); }
    }

    private void restoreAutoCommit(Connection conn) {
        if (conn != null) try { conn.setAutoCommit(true); conn.close(); }
        catch (SQLException e) { e.printStackTrace(); }
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