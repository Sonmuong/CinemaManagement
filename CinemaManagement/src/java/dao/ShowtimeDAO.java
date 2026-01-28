package dao;

import model.Showtime;
import util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ShowtimeDAO {
    
    // Lấy tất cả suất chiếu
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
            
            while (rs.next()) {
                Showtime showtime = extractShowtimeFromResultSet(rs);
                showtimes.add(showtime);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return showtimes;
    }
    
    // Lấy suất chiếu theo ID
    public Showtime getShowtimeById(int showtimeId) {
        String sql = "SELECT s.*, m.movie_name, r.room_name, r.total_seats, " +
                    "(SELECT COUNT(*) FROM Tickets t WHERE t.showtime_id = s.showtime_id " +
                    "AND t.payment_status IN ('Paid', 'Pending')) as seats_booked " +
                    "FROM Showtimes s " +
                    "INNER JOIN Movies m ON s.movie_id = m.movie_id " +
                    "INNER JOIN Rooms r ON s.room_id = r.room_id " +
                    "WHERE s.showtime_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, showtimeId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return extractShowtimeFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // Lấy suất chiếu theo phim
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
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, movieId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Showtime showtime = extractShowtimeFromResultSet(rs);
                showtimes.add(showtime);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return showtimes;
    }
    
    // Lấy suất chiếu theo ngày
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
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDate(1, date);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Showtime showtime = extractShowtimeFromResultSet(rs);
                showtimes.add(showtime);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return showtimes;
    }
    
    // Thêm suất chiếu mới
    public boolean addShowtime(Showtime showtime) {
        // Kiểm tra xem phòng có trống không
        if (!isRoomAvailable(showtime.getRoomId(), showtime.getShowDate(), 
                            showtime.getShowTime(), showtime.getMovieId())) {
            System.out.println("Phòng đã có suất chiếu khác vào thời gian này!");
            return false;
        }
        
        String sql = "INSERT INTO Showtimes (movie_id, room_id, show_date, show_time, " +
                    "ticket_price, status) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, showtime.getMovieId());
            pstmt.setInt(2, showtime.getRoomId());
            pstmt.setDate(3, showtime.getShowDate());
            pstmt.setTime(4, showtime.getShowTime());
            pstmt.setDouble(5, showtime.getTicketPrice());
            pstmt.setString(6, showtime.getStatus());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Kiểm tra phòng có trống không (tránh trùng lịch)
    private boolean isRoomAvailable(int roomId, Date showDate, Time showTime, int movieId) {
        // Lấy thời lượng phim
        String durationSql = "SELECT duration FROM Movies WHERE movie_id = ?";
        int duration = 0;
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(durationSql)) {
            
            pstmt.setInt(1, movieId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                duration = rs.getInt("duration");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        
        // Kiểm tra xung đột lịch chiếu
        String sql = "SELECT s.*, m.duration FROM Showtimes s " +
                    "INNER JOIN Movies m ON s.movie_id = m.movie_id " +
                    "WHERE s.room_id = ? AND s.show_date = ? " +
                    "AND s.status = 'Scheduled'";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, roomId);
            pstmt.setDate(2, showDate);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Time existingTime = rs.getTime("show_time");
                int existingDuration = rs.getInt("duration");
                
                // Tính thời gian kết thúc (thêm 30 phút dọn dẹp)
                long newStartMillis = showTime.getTime();
                long newEndMillis = newStartMillis + (duration + 30) * 60 * 1000;
                
                long existingStartMillis = existingTime.getTime();
                long existingEndMillis = existingStartMillis + (existingDuration + 30) * 60 * 1000;
                
                // Kiểm tra xung đột
                if ((newStartMillis >= existingStartMillis && newStartMillis < existingEndMillis) ||
                    (newEndMillis > existingStartMillis && newEndMillis <= existingEndMillis) ||
                    (newStartMillis <= existingStartMillis && newEndMillis >= existingEndMillis)) {
                    return false;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        
        return true;
    }
    
    // Cập nhật suất chiếu
    public boolean updateShowtime(Showtime showtime) {
        String sql = "UPDATE Showtimes SET movie_id = ?, room_id = ?, show_date = ?, " +
                    "show_time = ?, ticket_price = ?, status = ? WHERE showtime_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, showtime.getMovieId());
            pstmt.setInt(2, showtime.getRoomId());
            pstmt.setDate(3, showtime.getShowDate());
            pstmt.setTime(4, showtime.getShowTime());
            pstmt.setDouble(5, showtime.getTicketPrice());
            pstmt.setString(6, showtime.getStatus());
            pstmt.setInt(7, showtime.getShowtimeId());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Hủy suất chiếu
    public boolean cancelShowtime(int showtimeId) {
        String sql = "UPDATE Showtimes SET status = 'Cancelled' WHERE showtime_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, showtimeId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Lấy danh sách ghế đã đặt
    public List<String> getBookedSeats(int showtimeId) {
        List<String> bookedSeats = new ArrayList<>();
        String sql = "SELECT seat_number FROM Tickets WHERE showtime_id = ? " +
                    "AND payment_status IN ('Paid', 'Pending')";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, showtimeId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                bookedSeats.add(rs.getString("seat_number"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bookedSeats;
    }
    
    // Helper method
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
    
    // Test
    public static void main(String[] args) {
        ShowtimeDAO dao = new ShowtimeDAO();
        
        System.out.println("=== TEST SHOWTIME DAO ===\n");
        
        // Test thêm suất chiếu mới
        Showtime newShowtime = new Showtime();
        newShowtime.setMovieId(1); // Avengers
        newShowtime.setRoomId(1); // Phòng 1
        newShowtime.setShowDate(Date.valueOf("2024-02-15"));
        newShowtime.setShowTime(Time.valueOf("19:30:00"));
        newShowtime.setTicketPrice(100000);
        newShowtime.setStatus("Scheduled");
        
        boolean added = dao.addShowtime(newShowtime);
        System.out.println("Thêm suất chiếu: " + (added ? "Thành công ✓" : "Thất bại ✗"));
        
        // Lấy tất cả suất chiếu
        List<Showtime> showtimes = dao.getAllShowtimes();
        System.out.println("\nTổng số suất chiếu: " + showtimes.size());
        
        for (Showtime st : showtimes) {
            System.out.println("\n- " + st.getMovieName());
            System.out.println("  Phòng: " + st.getRoomName());
            System.out.println("  Ngày: " + st.getShowDate() + " | Giờ: " + st.getShowTime());
            System.out.println("  Giá vé: " + st.getTicketPrice() + " VNĐ");
            System.out.println("  Ghế trống: " + st.getSeatsAvailable() + "/" + st.getTotalSeats());
        }
    }
}
