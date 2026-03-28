package dao;

import util.DBConnection;
import java.sql.*;
import java.util.*;

public class ReportDAO {

    // ==================== DOANH THU ====================

    /**
     * Thống kê doanh thu theo khoảng thời gian
     * period: 'day', 'week', 'month', 'quarter', 'year'
     */
    public List<Map<String, Object>> getRevenueByPeriod(String period, int year, Integer month) {
        List<Map<String, Object>> result = new ArrayList<>();

        String groupExpr, labelExpr, orderExpr;
        switch (period) {
            case "day":
                groupExpr = "CAST(t.payment_date AS DATE)";
                labelExpr = "CONVERT(VARCHAR(10), CAST(t.payment_date AS DATE), 103)";
                orderExpr = "CAST(t.payment_date AS DATE)";
                break;
            case "week":
                groupExpr = "DATEPART(WEEK, t.payment_date), YEAR(t.payment_date)";
                labelExpr = "'Tuần ' + CAST(DATEPART(WEEK, t.payment_date) AS VARCHAR) + '/' + CAST(YEAR(t.payment_date) AS VARCHAR)";
                orderExpr = "YEAR(t.payment_date), DATEPART(WEEK, t.payment_date)";
                break;
            case "month":
                groupExpr = "MONTH(t.payment_date), YEAR(t.payment_date)";
                labelExpr = "'Tháng ' + CAST(MONTH(t.payment_date) AS VARCHAR) + '/' + CAST(YEAR(t.payment_date) AS VARCHAR)";
                orderExpr = "YEAR(t.payment_date), MONTH(t.payment_date)";
                break;
            case "quarter":
                groupExpr = "DATEPART(QUARTER, t.payment_date), YEAR(t.payment_date)";
                labelExpr = "'Q' + CAST(DATEPART(QUARTER, t.payment_date) AS VARCHAR) + '/' + CAST(YEAR(t.payment_date) AS VARCHAR)";
                orderExpr = "YEAR(t.payment_date), DATEPART(QUARTER, t.payment_date)";
                break;
            default: // year
                groupExpr = "YEAR(t.payment_date)";
                labelExpr = "CAST(YEAR(t.payment_date) AS VARCHAR)";
                orderExpr = "YEAR(t.payment_date)";
        }

        StringBuilder sql = new StringBuilder(
            "SELECT " + labelExpr + " AS period_label, " +
            "COUNT(*) AS ticket_count, " +
            "SUM(t.final_price) AS total_revenue " +
            "FROM Tickets t " +
            "WHERE t.payment_status = 'Paid' " +
            "AND YEAR(t.payment_date) = ? "
        );
        if (month != null && (period.equals("day") || period.equals("week"))) {
            sql.append("AND MONTH(t.payment_date) = ? ");
        }
        sql.append("GROUP BY ").append(groupExpr).append(" ORDER BY ").append(orderExpr);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            pstmt.setInt(1, year);
            if (month != null && (period.equals("day") || period.equals("week"))) {
                pstmt.setInt(2, month);
            }
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("period_label", rs.getString("period_label"));
                row.put("ticket_count", rs.getInt("ticket_count"));
                row.put("total_revenue", rs.getDouble("total_revenue"));
                result.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    // ==================== VÉ THEO PHIM / SUẤT ====================

    /**
     * Thống kê số vé bán theo từng phim
     */
    public List<Map<String, Object>> getTicketsByMovie() {
        List<Map<String, Object>> result = new ArrayList<>();
        String sql =
            "SELECT m.movie_id, m.movie_name, " +
            "COUNT(t.ticket_id) AS ticket_count, " +
            "SUM(t.final_price) AS total_revenue, " +
            "AVG(t.final_price) AS avg_price " +
            "FROM Movies m " +
            "LEFT JOIN Showtimes s ON m.movie_id = s.movie_id " +
            "LEFT JOIN Tickets t ON s.showtime_id = t.showtime_id AND t.payment_status = 'Paid' " +
            "GROUP BY m.movie_id, m.movie_name " +
            "ORDER BY ticket_count DESC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("movie_id", rs.getInt("movie_id"));
                row.put("movie_name", rs.getString("movie_name"));
                row.put("ticket_count", rs.getInt("ticket_count"));
                row.put("total_revenue", rs.getDouble("total_revenue"));
                row.put("avg_price", rs.getDouble("avg_price"));
                result.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Thống kê số vé bán theo từng suất chiếu
     */
    public List<Map<String, Object>> getTicketsByShowtime() {
        List<Map<String, Object>> result = new ArrayList<>();
        String sql =
            "SELECT s.showtime_id, m.movie_name, r.room_name, " +
            "s.show_date, CONVERT(VARCHAR(5), s.show_time, 108) AS show_time, " +
            "r.total_seats, " +
            "COUNT(t.ticket_id) AS tickets_sold, " +
            "SUM(t.final_price) AS total_revenue, " +
            "CAST(COUNT(t.ticket_id) * 100.0 / r.total_seats AS DECIMAL(5,1)) AS occupancy_rate " +
            "FROM Showtimes s " +
            "INNER JOIN Movies m ON s.movie_id = m.movie_id " +
            "INNER JOIN Rooms r ON s.room_id = r.room_id " +
            "LEFT JOIN Tickets t ON s.showtime_id = t.showtime_id AND t.payment_status = 'Paid' " +
            "GROUP BY s.showtime_id, m.movie_name, r.room_name, s.show_date, s.show_time, r.total_seats " +
            "ORDER BY s.show_date DESC, s.show_time DESC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("showtime_id", rs.getInt("showtime_id"));
                row.put("movie_name", rs.getString("movie_name"));
                row.put("room_name", rs.getString("room_name"));
                row.put("show_date", rs.getDate("show_date"));
                row.put("show_time", rs.getString("show_time"));
                row.put("total_seats", rs.getInt("total_seats"));
                row.put("tickets_sold", rs.getInt("tickets_sold"));
                row.put("total_revenue", rs.getDouble("total_revenue"));
                row.put("occupancy_rate", rs.getDouble("occupancy_rate"));
                result.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    // ==================== TỶ LỆ LẤP ĐẦY ====================

    /**
     * Thống kê tỷ lệ lấp đầy theo phòng chiếu
     */
    public List<Map<String, Object>> getOccupancyByRoom() {
        List<Map<String, Object>> result = new ArrayList<>();
        String sql =
            "SELECT r.room_id, r.room_name, r.total_seats, " +
            "COUNT(DISTINCT s.showtime_id) AS total_showtimes, " +
            "COUNT(t.ticket_id) AS total_tickets_sold, " +
            "COUNT(DISTINCT s.showtime_id) * r.total_seats AS total_capacity, " +
            "CASE WHEN COUNT(DISTINCT s.showtime_id) = 0 THEN 0 " +
            "ELSE CAST(COUNT(t.ticket_id) * 100.0 / (COUNT(DISTINCT s.showtime_id) * r.total_seats) AS DECIMAL(5,1)) " +
            "END AS avg_occupancy_rate " +
            "FROM Rooms r " +
            "LEFT JOIN Showtimes s ON r.room_id = s.room_id AND s.status = 'Scheduled' " +
            "LEFT JOIN Tickets t ON s.showtime_id = t.showtime_id AND t.payment_status = 'Paid' " +
            "GROUP BY r.room_id, r.room_name, r.total_seats " +
            "ORDER BY avg_occupancy_rate DESC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("room_id", rs.getInt("room_id"));
                row.put("room_name", rs.getString("room_name"));
                row.put("total_seats", rs.getInt("total_seats"));
                row.put("total_showtimes", rs.getInt("total_showtimes"));
                row.put("total_tickets_sold", rs.getInt("total_tickets_sold"));
                row.put("avg_occupancy_rate", rs.getDouble("avg_occupancy_rate"));
                result.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    // ==================== TOP 10 PHIM ====================

    /**
     * Top 10 phim doanh thu cao nhất trong tháng
     */
    public List<Map<String, Object>> getTop10MoviesByRevenue(int year, int month) {
        List<Map<String, Object>> result = new ArrayList<>();
        String sql =
            "SELECT TOP 10 m.movie_id, m.movie_name, " +
            "COUNT(t.ticket_id) AS ticket_count, " +
            "SUM(t.final_price) AS total_revenue " +
            "FROM Movies m " +
            "INNER JOIN Showtimes s ON m.movie_id = s.movie_id " +
            "INNER JOIN Tickets t ON s.showtime_id = t.showtime_id " +
            "WHERE t.payment_status = 'Paid' " +
            "AND YEAR(t.payment_date) = ? AND MONTH(t.payment_date) = ? " +
            "GROUP BY m.movie_id, m.movie_name " +
            "ORDER BY total_revenue DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, year);
            pstmt.setInt(2, month);
            ResultSet rs = pstmt.executeQuery();
            int rank = 1;
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("rank", rank++);
                row.put("movie_id", rs.getInt("movie_id"));
                row.put("movie_name", rs.getString("movie_name"));
                row.put("ticket_count", rs.getInt("ticket_count"));
                row.put("total_revenue", rs.getDouble("total_revenue"));
                result.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    // ==================== KHÁCH HÀNG ĐIỂM CAO ====================

    /**
     * Danh sách khách hàng có điểm tích lũy cao nhất (VIP >= 1000 điểm)
     */
    public List<Map<String, Object>> getTopCustomersByPoints(int limit) {
        List<Map<String, Object>> result = new ArrayList<>();
        String sql =
            "SELECT TOP (?) c.customer_id, c.full_name, c.phone, c.email, " +
            "c.loyalty_points, " +
            "COUNT(t.ticket_id) AS total_tickets, " +
            "SUM(t.final_price) AS total_spent " +
            "FROM Customers c " +
            "LEFT JOIN Tickets t ON c.customer_id = t.customer_id AND t.payment_status = 'Paid' " +
            "GROUP BY c.customer_id, c.full_name, c.phone, c.email, c.loyalty_points " +
            "ORDER BY c.loyalty_points DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();
            int rank = 1;
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("rank", rank++);
                row.put("customer_id", rs.getInt("customer_id"));
                row.put("full_name", rs.getString("full_name"));
                row.put("phone", rs.getString("phone"));
                row.put("email", rs.getString("email"));
                row.put("loyalty_points", rs.getDouble("loyalty_points"));
                row.put("total_tickets", rs.getInt("total_tickets"));
                row.put("total_spent", rs.getDouble("total_spent"));
                row.put("is_vip", rs.getDouble("loyalty_points") >= 1000);
                result.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    // ==================== TỔNG KẾT 6 THÁNG ====================

    /**
     * Tổng kết kinh doanh trong N tháng gần nhất
     */
    public Map<String, Object> getSummaryReport(int months) {
        Map<String, Object> summary = new LinkedHashMap<>();

        String sql =
            "SELECT " +
            "COUNT(t.ticket_id) AS total_tickets, " +
            "SUM(t.final_price) AS total_revenue, " +
            "COUNT(DISTINCT s.showtime_id) AS total_showtimes, " +
            "COUNT(DISTINCT s.movie_id) AS total_movies " +
            "FROM Tickets t " +
            "INNER JOIN Showtimes s ON t.showtime_id = s.showtime_id " +
            "WHERE t.payment_status = 'Paid' " +
            "AND t.payment_date >= DATEADD(MONTH, -?, GETDATE())";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, months);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                summary.put("total_tickets", rs.getInt("total_tickets"));
                summary.put("total_revenue", rs.getDouble("total_revenue"));
                summary.put("total_showtimes", rs.getInt("total_showtimes"));
                summary.put("total_movies", rs.getInt("total_movies"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Tỷ lệ lấp đầy trung bình
        String occupancySql =
            "SELECT AVG(occ.rate) AS avg_occupancy FROM (" +
            "SELECT CAST(COUNT(t.ticket_id) * 100.0 / r.total_seats AS DECIMAL(5,1)) AS rate " +
            "FROM Showtimes s " +
            "INNER JOIN Rooms r ON s.room_id = r.room_id " +
            "LEFT JOIN Tickets t ON s.showtime_id = t.showtime_id AND t.payment_status = 'Paid' " +
            "WHERE s.show_date >= DATEADD(MONTH, -?, CAST(GETDATE() AS DATE)) " +
            "GROUP BY s.showtime_id, r.total_seats) occ";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(occupancySql)) {
            pstmt.setInt(1, months);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                summary.put("avg_occupancy", rs.getDouble("avg_occupancy"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Phim doanh thu cao nhất
        String bestMovieSql =
            "SELECT TOP 1 m.movie_name, SUM(t.final_price) AS revenue " +
            "FROM Movies m INNER JOIN Showtimes s ON m.movie_id = s.movie_id " +
            "INNER JOIN Tickets t ON s.showtime_id = t.showtime_id " +
            "WHERE t.payment_status = 'Paid' AND t.payment_date >= DATEADD(MONTH, -?, GETDATE()) " +
            "GROUP BY m.movie_name ORDER BY revenue DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(bestMovieSql)) {
            pstmt.setInt(1, months);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                summary.put("best_movie", rs.getString("movie_name"));
                summary.put("best_movie_revenue", rs.getDouble("revenue"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Phim doanh thu thấp nhất (có bán vé)
        String worstMovieSql =
            "SELECT TOP 1 m.movie_name, SUM(t.final_price) AS revenue " +
            "FROM Movies m INNER JOIN Showtimes s ON m.movie_id = s.movie_id " +
            "INNER JOIN Tickets t ON s.showtime_id = t.showtime_id " +
            "WHERE t.payment_status = 'Paid' AND t.payment_date >= DATEADD(MONTH, -?, GETDATE()) " +
            "GROUP BY m.movie_name ORDER BY revenue ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(worstMovieSql)) {
            pstmt.setInt(1, months);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                summary.put("worst_movie", rs.getString("movie_name"));
                summary.put("worst_movie_revenue", rs.getDouble("revenue"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Số khách hàng VIP (>= 1000 điểm)
        String vipSql = "SELECT COUNT(*) AS vip_count FROM Customers WHERE loyalty_points >= 1000";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(vipSql)) {
            if (rs.next()) {
                summary.put("vip_customers", rs.getInt("vip_count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return summary;
    }
}