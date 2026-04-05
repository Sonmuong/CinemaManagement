package dao;

import model.Showtime;
import util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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

    public boolean cancelShowtime(int showtimeId) {
        String sql = "UPDATE Showtimes SET status = 'Cancelled' WHERE showtime_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, showtimeId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // Hủy toàn bộ suất Scheduled của một phim (dùng khi ngừng chiếu phim)
    public int cancelScheduledShowtimesByMovie(int movieId) {
        String sql = "UPDATE Showtimes SET status = 'Cancelled' " +
                     "WHERE movie_id = ? AND status = 'Scheduled'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, movieId);
            return ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); return 0; }
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