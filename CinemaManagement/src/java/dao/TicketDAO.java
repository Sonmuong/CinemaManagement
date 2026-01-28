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
    
    // Lấy tất cả vé
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
            
            while (rs.next()) {
                Ticket ticket = extractTicketFromResultSet(rs);
                tickets.add(ticket);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tickets;
    }
    
    // Lấy vé theo ID
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
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, ticketId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return extractTicketFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // BÁN VÉ (với logic tích điểm 5% và sử dụng điểm)
    public int sellTicket(Ticket ticket, boolean usePoints) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            
            // Kiểm tra ghế đã được đặt chưa
            if (isSeatBooked(ticket.getShowtimeId(), ticket.getSeatNumber())) {
                System.out.println("Ghế đã được đặt!");
                return -1;
            }
            
            double finalPrice = ticket.getTicketPrice();
            double pointsUsed = 0;
            double discountAmount = 0;
            
            // XỬ LÝ SỬ DỤNG ĐIỂM (100 điểm = 10,000 VNĐ)
            if (ticket.getCustomerId() != null && usePoints) {
                Customer customer = customerDAO.getCustomerById(ticket.getCustomerId());
                if (customer != null && customer.getLoyaltyPoints() >= 100) {
                    // Tính điểm có thể sử dụng
                    pointsUsed = Math.min(customer.getLoyaltyPoints(), 
                                         Math.floor(customer.getLoyaltyPoints() / 100) * 100);
                    pointsUsed = Math.min(pointsUsed, finalPrice / 100); // Tối đa bằng giá vé / 100
                    
                    discountAmount = (pointsUsed / 100) * 10000; // 100 điểm = 10,000 VNĐ
                    finalPrice -= discountAmount;
                    
                    // Trừ điểm
                    customerDAO.updateLoyaltyPoints(customer.getCustomerId(), -pointsUsed);
                    customerDAO.addPointTransaction(customer.getCustomerId(), null, 
                        -pointsUsed, "Redeem", "Sử dụng điểm mua vé");
                }
            }
            
            // Thêm vé vào database
            String sql = "INSERT INTO Tickets (customer_id, showtime_id, seat_number, " +
                        "ticket_type, ticket_price, discount_amount, final_price, " +
                        "points_used, points_earned, payment_status, payment_date) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            if (ticket.getCustomerId() != null) {
                pstmt.setInt(1, ticket.getCustomerId());
            } else {
                pstmt.setNull(1, Types.INTEGER);
            }
            pstmt.setInt(2, ticket.getShowtimeId());
            pstmt.setString(3, ticket.getSeatNumber());
            pstmt.setString(4, ticket.getTicketType());
            pstmt.setDouble(5, ticket.getTicketPrice());
            pstmt.setDouble(6, discountAmount);
            pstmt.setDouble(7, finalPrice);
            pstmt.setDouble(8, pointsUsed);
            pstmt.setDouble(9, 0); // points_earned sẽ cập nhật sau
            pstmt.setString(10, "Paid");
            pstmt.setTimestamp(11, new Timestamp(System.currentTimeMillis()));
            
            int affectedRows = pstmt.executeUpdate();
            int ticketId = -1;
            
            if (affectedRows > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    ticketId = rs.getInt(1);
                    
                    // TÍCH ĐIỂM 5% GIÁ VÉ CUỐI CÙNG
                    if (ticket.getCustomerId() != null) {
                        double pointsEarned = finalPrice * 0.05;
                        
                        // Cập nhật điểm vào vé
                        String updateTicketSql = "UPDATE Tickets SET points_earned = ? WHERE ticket_id = ?";
                        PreparedStatement updatePstmt = conn.prepareStatement(updateTicketSql);
                        updatePstmt.setDouble(1, pointsEarned);
                        updatePstmt.setInt(2, ticketId);
                        updatePstmt.executeUpdate();
                        
                        // Cộng điểm cho khách hàng
                        customerDAO.updateLoyaltyPoints(ticket.getCustomerId(), pointsEarned);
                        customerDAO.addPointTransaction(ticket.getCustomerId(), ticketId, 
                            pointsEarned, "Earn", "Tích điểm từ mua vé");
                        
                        System.out.println("✓ Đã cộng " + pointsEarned + " điểm tích lũy");
                    }
                }
            }
            
            conn.commit();
            System.out.println("✓ Bán vé thành công!");
            return ticketId;
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return -1;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    // HỦY VÉ (với logic hoàn tiền 80% nếu hủy trước 2 giờ)
    public boolean cancelTicket(int ticketId) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            
            // Lấy thông tin vé
            Ticket ticket = getTicketById(ticketId);
            if (ticket == null) {
                return false;
            }
            
            // Lấy thông tin suất chiếu
            String showtimeSql = "SELECT show_date, show_time FROM Showtimes WHERE showtime_id = ?";
            PreparedStatement showtimePstmt = conn.prepareStatement(showtimeSql);
            showtimePstmt.setInt(1, ticket.getShowtimeId());
            ResultSet rs = showtimePstmt.executeQuery();
            
            if (!rs.next()) {
                return false;
            }
            
            Date showDate = rs.getDate("show_date");
            Time showTime = rs.getTime("show_time");
            
            // Tính thời gian còn lại đến giờ chiếu
            LocalDateTime showDateTime = LocalDateTime.of(
                showDate.toLocalDate(),
                showTime.toLocalTime()
            );
            LocalDateTime now = LocalDateTime.now();
            long hoursUntilShow = ChronoUnit.HOURS.between(now, showDateTime);
            
            double refundAmount = 0;
            String refundStatus = "Cancelled";
            
            // CHÍNH SÁCH HỦY VÉ
            if (hoursUntilShow >= 2) {
                refundAmount = ticket.getFinalPrice() * 0.8; // Hoàn 80%
                refundStatus = "Refunded";
                System.out.println("✓ Hủy trước 2 giờ: Hoàn 80% = " + refundAmount + " VNĐ");
            } else {
                refundAmount = 0; // Không hoàn tiền
                System.out.println("✗ Hủy trong 2 giờ: Không hoàn tiền");
            }
            
            // Cập nhật trạng thái vé
            String updateSql = "UPDATE Tickets SET payment_status = ? WHERE ticket_id = ?";
            PreparedStatement updatePstmt = conn.prepareStatement(updateSql);
            updatePstmt.setString(1, refundStatus);
            updatePstmt.setInt(2, ticketId);
            updatePstmt.executeUpdate();
            
            // XỬ LÝ ĐIỂM TÍCH LŨY
            if (ticket.getCustomerId() != null) {
                // Trừ điểm đã tích (nếu có)
                if (ticket.getPointsEarned() > 0) {
                    customerDAO.updateLoyaltyPoints(ticket.getCustomerId(), -ticket.getPointsEarned());
                    customerDAO.addPointTransaction(ticket.getCustomerId(), ticketId, 
                        -ticket.getPointsEarned(), "Refund", "Trừ điểm do hủy vé");
                    System.out.println("✓ Đã trừ " + ticket.getPointsEarned() + " điểm tích lũy");
                }
                
                // Hoàn điểm đã sử dụng (nếu có hoàn tiền)
                if (refundAmount > 0 && ticket.getPointsUsed() > 0) {
                    customerDAO.updateLoyaltyPoints(ticket.getCustomerId(), ticket.getPointsUsed());
                    customerDAO.addPointTransaction(ticket.getCustomerId(), ticketId, 
                        ticket.getPointsUsed(), "Refund", "Hoàn điểm do hủy vé");
                    System.out.println("✓ Đã hoàn " + ticket.getPointsUsed() + " điểm đã sử dụng");
                }
            }
            
            conn.commit();
            System.out.println("✓ Hủy vé thành công!");
            return true;
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    // Kiểm tra ghế đã được đặt chưa
    public boolean isSeatBooked(int showtimeId, String seatNumber) {
        String sql = "SELECT COUNT(*) as count FROM Tickets " +
                    "WHERE showtime_id = ? AND seat_number = ? " +
                    "AND payment_status IN ('Paid', 'Pending')";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, showtimeId);
            pstmt.setString(2, seatNumber);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("count") > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    // Lấy vé theo khách hàng
    public List<Ticket> getTicketsByCustomer(int customerId) {
        List<Ticket> tickets = new ArrayList<>();
        String sql = "SELECT t.*, c.full_name as customer_name, m.movie_name, " +
                    "r.room_name, s.show_date, CONVERT(VARCHAR(5), s.show_time, 108) as show_time " +
                    "FROM Tickets t " +
                    "INNER JOIN Customers c ON t.customer_id = c.customer_id " +
                    "INNER JOIN Showtimes s ON t.showtime_id = s.showtime_id " +
                    "INNER JOIN Movies m ON s.movie_id = m.movie_id " +
                    "INNER JOIN Rooms r ON s.room_id = r.room_id " +
                    "WHERE t.customer_id = ? " +
                    "ORDER BY t.booking_date DESC";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Ticket ticket = extractTicketFromResultSet(rs);
                tickets.add(ticket);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tickets;
    }
    
    // Helper method
    private Ticket extractTicketFromResultSet(ResultSet rs) throws SQLException {
        Ticket ticket = new Ticket();
        ticket.setTicketId(rs.getInt("ticket_id"));
        
        int customerId = rs.getInt("customer_id");
        if (!rs.wasNull()) {
            ticket.setCustomerId(customerId);
        }
        
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
        if (paymentDate != null) {
            ticket.setPaymentDate(paymentDate);
        }
        
        // Thông tin bổ sung
        ticket.setCustomerName(rs.getString("customer_name"));
        ticket.setMovieName(rs.getString("movie_name"));
        ticket.setRoomName(rs.getString("room_name"));
        ticket.setShowDate(rs.getDate("show_date"));
        ticket.setShowTime(rs.getString("show_time"));
        
        return ticket;
    }
    
    // Test
    public static void main(String[] args) {
        TicketDAO ticketDAO = new TicketDAO();
        CustomerDAO customerDAO = new CustomerDAO();
        
        System.out.println("=== TEST TICKET DAO - BÁN VÉ VÀ TÍCH ĐIỂM ===\n");
        
        // Lấy khách hàng test
        Customer customer = customerDAO.getCustomerByPhone("0901234567");
        System.out.println("Khách hàng: " + customer.getFullName());
        System.out.println("Điểm hiện tại: " + customer.getLoyaltyPoints() + "\n");
        
        // Test bán vé (không dùng điểm)
        System.out.println("--- TEST 1: Bán vé KHÔNG sử dụng điểm ---");
        Ticket ticket1 = new Ticket();
        ticket1.setCustomerId(customer.getCustomerId());
        ticket1.setShowtimeId(1); // ID suất chiếu vừa tạo
        ticket1.setSeatNumber("A1");
        ticket1.setTicketType("Normal");
        ticket1.setTicketPrice(100000);
        
        int ticketId1 = ticketDAO.sellTicket(ticket1, false);
        System.out.println("Ticket ID: " + ticketId1);
        
        // Kiểm tra điểm sau khi mua
        customer = customerDAO.getCustomerById(customer.getCustomerId());
        System.out.println("Điểm sau mua: " + customer.getLoyaltyPoints() + "\n");
        
        // Test bán vé (có dùng điểm)
        System.out.println("--- TEST 2: Bán vé CÓ sử dụng điểm ---");
        Ticket ticket2 = new Ticket();
        ticket2.setCustomerId(customer.getCustomerId());
        ticket2.setShowtimeId(1);
        ticket2.setSeatNumber("A2");
        ticket2.setTicketType("Normal");
        ticket2.setTicketPrice(100000);
        
        int ticketId2 = ticketDAO.sellTicket(ticket2, true);
        System.out.println("Ticket ID: " + ticketId2);
        
        customer = customerDAO.getCustomerById(customer.getCustomerId());
        System.out.println("Điểm sau mua (dùng điểm): " + customer.getLoyaltyPoints() + "\n");
        
        // Test hủy vé
        System.out.println("--- TEST 3: Hủy vé ---");
        ticketDAO.cancelTicket(ticketId1);
        
        customer = customerDAO.getCustomerById(customer.getCustomerId());
        System.out.println("Điểm sau hủy: " + customer.getLoyaltyPoints());
    }
}
