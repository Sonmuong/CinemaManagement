package dao;

import model.Showtime;
import util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class ShowtimeDAO {

    public List<Showtime> getAllShowtimes() {
        List<Showtime> showtimes = new ArrayList<>();
        String sql = "SELECT s.*, m.movie_name, r.room_name, r.total_seats, " +
                    "(SELECT COUNT(*) FROM Tickets t WHERE t.showtime_id = s.showtime_id " +
                    "AND t.payment_status IN ('Paid', 'Pending')) as seats_booked " +
                    "FROM Showtimes s " +
                    "INNER JOIN Movies m ON s.movie_id = m.movie_id " +
                    "INNER JOIN Rooms r ON s.room_id = r.room_id " +
                    "ORDER BY s.show_date DESC, s.show_time DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) showtimes.add(extractShowtimeFromResultSet(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return showtimes;
    }

    public Showtime getShowtimeById(int showtimeId) {
        String sql = "SELECT s.*, m.movie_name, r.room_name, r.total_seats, " +
                    "(SELECT COUNT(*) FROM Tickets t WHERE t.showtime_id = s.showtime_id " +
                    "AND t.payment_status IN ('Paid', 'Pending')) as seats_booked " +
                    "FROM Showtimes s " +
                    "INNER JOIN Movies m ON s.movie_id = m.movie_id " +
                    "INNER JOIN Rooms r ON s.room_id = r.room_id " +
                    "WHERE s.showtime_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, showtimeId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return extractShowtimeFromResultSet(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<Showtime> getShowtimesByMovie(int movieId) {
        List<Showtime> showtimes = new ArrayList<>();
        String sql = "SELECT s.*, m.movie_name, r.room_name, r.total_seats, " +
                    "(SELECT COUNT(*) FROM Tickets t WHERE t.showtime_id = s.showtime_id " +
                    "AND t.payment_status IN ('Paid', 'Pending')) as seats_booked " +
                    "FROM Showtimes s " +
                    "INNER JOIN Movies m ON s.movie_id = m.movie_id " +
                    "INNER JOIN Rooms r ON s.room_id = r.room_id " +
                    "WHERE s.movie_id = ? AND s.show_date >= CAST(GETDATE() AS DATE) " +
                    "ORDER BY s.show_date, s.show_time";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, movieId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) showtimes.add(extractShowtimeFromResultSet(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return showtimes;
    }

    public List<Showtime> getShowtimesByDate(Date date) {
        List<Showtime> showtimes = new ArrayList<>();
        String sql = "SELECT s.*, m.movie_name, r.room_name, r.total_seats, " +
                    "(SELECT COUNT(*) FROM Tickets t WHERE t.showtime_id = s.showtime_id " +
                    "AND t.payment_status IN ('Paid', 'Pending')) as seats_booked " +
                    "FROM Showtimes s " +
                    "INNER JOIN Movies m ON s.movie_id = m.movie_id " +
                    "INNER JOIN Rooms r ON s.room_id = r.room_id " +
                    "WHERE s.show_date = ? " +
                    "ORDER BY s.show_time";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, date);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) showtimes.add(extractShowtimeFromResultSet(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return showtimes;
    }

    public boolean addShowtime(Showtime showtime) {
        if (!isRoomAvailable(showtime.getRoomId(), showtime.getShowDate(),
                            showtime.getShowTime(), showtime.getMovieId())) {
            System.out.println("Phòng đã có suất chiếu khác vào thời gian này!");
            return false;
        }
        String sql = "INSERT INTO Showtimes (movie_id, room_id, show_date, show_time, " +
                    "ticket_price, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, showtime.getMovieId());
            ps.setInt(2, showtime.getRoomId());
            ps.setDate(3, showtime.getShowDate());
            ps.setTime(4, showtime.getShowTime());
            ps.setDouble(5, showtime.getTicketPrice());
            ps.setString(6, showtime.getStatus());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    private boolean isRoomAvailable(int roomId, Date showDate, Time showTime, int movieId) {
        String durationSql = "SELECT duration FROM Movies WHERE movie_id = ?";
        int duration = 0;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(durationSql)) {
            ps.setInt(1, movieId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) duration = rs.getInt("duration");
        } catch (SQLException e) { e.printStackTrace(); return false; }

        String sql = "SELECT s.*, m.duration FROM Showtimes s " +
                    "INNER JOIN Movies m ON s.movie_id = m.movie_id " +
                    "WHERE s.room_id = ? AND s.show_date = ? AND s.status = 'Scheduled'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            ps.setDate(2, showDate);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Time existingTime = rs.getTime("show_time");
                int existingDuration = rs.getInt("duration");
                long newStartMillis = showTime.getTime();
                long newEndMillis = newStartMillis + (duration + 30) * 60 * 1000L;
                long existingStartMillis = existingTime.getTime();
                long existingEndMillis = existingStartMillis + (existingDuration + 30) * 60 * 1000L;
                if ((newStartMillis >= existingStartMillis && newStartMillis < existingEndMillis) ||
                    (newEndMillis > existingStartMillis && newEndMillis <= existingEndMillis) ||
                    (newStartMillis <= existingStartMillis && newEndMillis >= existingEndMillis)) {
                    return false;
                }
            }
        } catch (SQLException e) { e.printStackTrace(); return false; }
        return true;
    }

    // ── Kết quả hủy suất chiếu ───────────────────────────────────
    public static class CancelShowtimeResult {
        public final boolean success;
        public final int ticketsCancelled;    // số vé bị hủy
        public final int ticketsRefunded;     // số vé được hoàn tiền
        public final double totalRefundAmount; // tổng tiền hoàn

        public CancelShowtimeResult(boolean success, int ticketsCancelled,
                                    int ticketsRefunded, double totalRefundAmount) {
            this.success = success;
            this.ticketsCancelled = ticketsCancelled;
            this.ticketsRefunded = ticketsRefunded;
            this.totalRefundAmount = totalRefundAmount;
        }
    }

    /**
     * Hủy suất chiếu — tự động hủy tất cả vé Paid trước,
     * hoàn tiền 80% nếu hủy trước 2 giờ chiếu.
     * Sau đó mới đặt status = 'Cancelled'.
     */
    public CancelShowtimeResult cancelShowtime(int showtimeId) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            if (conn == null) return new CancelShowtimeResult(false, 0, 0, 0);
            conn.setAutoCommit(false);

            // 1. Lấy thông tin suất chiếu (ngày giờ chiếu)
            Showtime showtime = getShowtimeByIdWithConn(conn, showtimeId);
            if (showtime == null) {
                conn.rollback();
                return new CancelShowtimeResult(false, 0, 0, 0);
            }

            LocalDateTime showDT = LocalDateTime.of(
                showtime.getShowDate().toLocalDate(),
                showtime.getShowTime().toLocalTime());
            long hoursLeft = ChronoUnit.HOURS.between(LocalDateTime.now(), showDT);
            boolean isRefund = hoursLeft >= 2;

            // 2. Lấy tất cả vé Paid của suất này
            String getTicketsSql =
                "SELECT ticket_id, customer_id, final_price, points_used, points_earned " +
                "FROM Tickets WHERE showtime_id = ? AND payment_status = 'Paid'";
            PreparedStatement getTicketsPs = conn.prepareStatement(getTicketsSql);
            getTicketsPs.setInt(1, showtimeId);
            ResultSet ticketRs = getTicketsPs.executeQuery();

            int ticketsCancelled = 0;
            int ticketsRefunded  = 0;
            double totalRefund   = 0;

            // 3. Xử lý từng vé
            while (ticketRs.next()) {
                int ticketId    = ticketRs.getInt("ticket_id");
                int customerId  = ticketRs.getInt("customer_id");
                boolean hasCust = !ticketRs.wasNull();
                double finalPrice   = ticketRs.getDouble("final_price");
                double pointsUsed   = ticketRs.getDouble("points_used");
                double pointsEarned = ticketRs.getDouble("points_earned");

                // Đặt trạng thái vé
                String newStatus = isRefund ? "Refunded" : "Cancelled";
                PreparedStatement updTicket = conn.prepareStatement(
                    "UPDATE Tickets SET payment_status = ? WHERE ticket_id = ?");
                updTicket.setString(1, newStatus);
                updTicket.setInt(2, ticketId);
                updTicket.executeUpdate();

                ticketsCancelled++;

                if (isRefund) {
                    double refundAmt = Math.round(finalPrice * 0.8);
                    totalRefund += refundAmt;
                    ticketsRefunded++;
                }

                // Xử lý điểm nếu có khách hàng
                if (hasCust && customerId > 0) {
                    double delta = 0;
                    // Trừ lại điểm đã tích từ vé này
                    if (pointsEarned > 0) delta -= pointsEarned;
                    // Hoàn lại điểm đã dùng chỉ khi refund
                    if (isRefund && pointsUsed > 0) delta += pointsUsed;

                    if (delta != 0) {
                        PreparedStatement updPoints = conn.prepareStatement(
                            "UPDATE Customers SET loyalty_points = loyalty_points + ? " +
                            "WHERE customer_id = ?");
                        updPoints.setDouble(1, delta);
                        updPoints.setInt(2, customerId);
                        updPoints.executeUpdate();

                        // Ghi log điểm
                        try {
                            PreparedStatement insPoint = conn.prepareStatement(
                                "INSERT INTO PointTransactions " +
                                "(customer_id, ticket_id, points_change, transaction_type, description) " +
                                "VALUES (?, ?, ?, ?, ?)");
                            insPoint.setInt(1, customerId);
                            insPoint.setInt(2, ticketId);
                            insPoint.setDouble(3, delta);
                            insPoint.setString(4, "Refund");
                            insPoint.setString(5, (isRefund ? "Hoàn vé #" : "Hủy vé #") + ticketId
                                + " do hủy suất chiếu #" + showtimeId);
                            insPoint.executeUpdate();
                        } catch (SQLException e) {
                            // PointTransactions có thể không tồn tại — bỏ qua
                            System.out.println("⚠️ PointTransactions insert lỗi: " + e.getMessage());
                        }
                    }
                }
            }

            // 4. Hủy suất chiếu
            PreparedStatement cancelPs = conn.prepareStatement(
                "UPDATE Showtimes SET status = 'Cancelled' WHERE showtime_id = ?");
            cancelPs.setInt(1, showtimeId);
            cancelPs.executeUpdate();

            conn.commit();
            return new CancelShowtimeResult(true, ticketsCancelled, ticketsRefunded, totalRefund);

        } catch (SQLException e) {
            e.printStackTrace();
            rollback(conn);
            return new CancelShowtimeResult(false, 0, 0, 0);
        } finally {
            restoreAndClose(conn);
        }
    }

    // Helper: lấy showtime trong cùng một connection (dùng trong transaction)
    private Showtime getShowtimeByIdWithConn(Connection conn, int showtimeId) throws SQLException {
        String sql = "SELECT s.*, m.movie_name, r.room_name, r.total_seats, 0 as seats_booked " +
                     "FROM Showtimes s " +
                     "INNER JOIN Movies m ON s.movie_id = m.movie_id " +
                     "INNER JOIN Rooms r ON s.room_id = r.room_id " +
                     "WHERE s.showtime_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, showtimeId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return extractShowtimeFromResultSet(rs);
        }
        return null;
    }

    // Hủy toàn bộ suất Scheduled của một phim (dùng khi ngừng chiếu phim)
    // Đây là hủy hàng loạt từ MovieServlet — cũng cần hủy vé kèm theo
    public int cancelScheduledShowtimesByMovie(int movieId) {
        // Lấy danh sách showtimeId cần hủy
        List<Integer> showtimeIds = new ArrayList<>();
        String getSql = "SELECT showtime_id FROM Showtimes " +
                        "WHERE movie_id = ? AND status = 'Scheduled'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(getSql)) {
            ps.setInt(1, movieId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) showtimeIds.add(rs.getInt("showtime_id"));
        } catch (SQLException e) { e.printStackTrace(); return 0; }

        int count = 0;
        for (int sid : showtimeIds) {
            CancelShowtimeResult result = cancelShowtime(sid);
            if (result.success) count++;
        }
        return count;
    }

    /**
     * Kích hoạt lại suất chiếu đã hủy.
     * Kiểm tra phim có đang Active không trước khi kích hoạt.
     */
    public String activateShowtime(Showtime showtime) {
        // Kiểm tra phim có đang Active không
        String checkMovieSql = "SELECT status FROM Movies WHERE movie_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(checkMovieSql)) {
            ps.setInt(1, showtime.getMovieId());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String movieStatus = rs.getString("status");
                if (!"Active".equals(movieStatus)) {
                    return "ERROR_MOVIE_INACTIVE";
                }
            } else {
                return "ERROR_MOVIE_NOT_FOUND";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "ERROR_DB";
        }

        // Thực hiện update
        boolean ok = updateShowtime(showtime);
        return ok ? "OK" : "ERROR_UPDATE";
    }

    public boolean updateShowtime(Showtime showtime) {
        String sql = "UPDATE Showtimes SET movie_id = ?, room_id = ?, show_date = ?, " +
                    "show_time = ?, ticket_price = ?, status = ? WHERE showtime_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, showtime.getMovieId());
            ps.setInt(2, showtime.getRoomId());
            ps.setDate(3, showtime.getShowDate());
            ps.setTime(4, showtime.getShowTime());
            ps.setDouble(5, showtime.getTicketPrice());
            ps.setString(6, showtime.getStatus());
            ps.setInt(7, showtime.getShowtimeId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public List<String> getBookedSeats(int showtimeId) {
        List<String> bookedSeats = new ArrayList<>();
        String sql = "SELECT seat_number FROM Tickets WHERE showtime_id = ? " +
                    "AND payment_status IN ('Paid', 'Pending')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, showtimeId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) bookedSeats.add(rs.getString("seat_number"));
        } catch (SQLException e) { e.printStackTrace(); }
        return bookedSeats;
    }

    public boolean deleteShowtime(int showtimeId) {
        String checkSql = "SELECT COUNT(*) FROM Tickets WHERE showtime_id = ? AND payment_status = 'Paid'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setInt(1, showtimeId);
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("Không thể xóa: còn vé đã thanh toán!");
                return false;
            }
        } catch (SQLException e) { e.printStackTrace(); return false; }

        String deleteTicketsSql = "DELETE FROM Tickets WHERE showtime_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(deleteTicketsSql)) {
            ps.setInt(1, showtimeId);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }

        String sql = "DELETE FROM Showtimes WHERE showtime_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, showtimeId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    private void rollback(Connection conn) {
        if (conn != null) try { conn.rollback(); } catch (SQLException e) { e.printStackTrace(); }
    }

    private void restoreAndClose(Connection conn) {
        if (conn != null) try { conn.setAutoCommit(true); conn.close(); }
        catch (SQLException e) { e.printStackTrace(); }
    }

    private Showtime extractShowtimeFromResultSet(ResultSet rs) throws SQLException {
        Showtime showtime = new Showtime();
        showtime.setShowtimeId(rs.getInt("showtime_id"));
        showtime.setMovieId(rs.getInt("movie_id"));
        showtime.setRoomId(rs.getInt("room_id"));
        showtime.setShowDate(rs.getDate("show_date"));
        showtime.setShowTime(rs.getTime("show_time"));
        showtime.setTicketPrice(rs.getDouble("ticket_price"));
        showtime.setStatus(rs.getString("status"));
        showtime.setMovieName(rs.getString("movie_name"));
        showtime.setRoomName(rs.getString("room_name"));
        showtime.setTotalSeats(rs.getInt("total_seats"));
        showtime.setSeatsBooked(rs.getInt("seats_booked"));
        showtime.setSeatsAvailable(showtime.getTotalSeats() - showtime.getSeatsBooked());
        return showtime;
    }
}