package dao;

import model.Customer;
import util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {
    
    // Lấy tất cả khách hàng
    public List<Customer> getAllCustomers() {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM Customers ORDER BY full_name";
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Customer customer = extractCustomerFromResultSet(rs);
                customers.add(customer);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return customers;
    }
    
    // Lấy khách hàng theo ID
    public Customer getCustomerById(int customerId) {
        String sql = "SELECT * FROM Customers WHERE customer_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return extractCustomerFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // Tìm khách hàng theo số điện thoại
    public Customer getCustomerByPhone(String phone) {
        String sql = "SELECT * FROM Customers WHERE phone = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, phone);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return extractCustomerFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // Thêm khách hàng mới
    public int addCustomer(Customer customer) {
        String sql = "INSERT INTO Customers (full_name, phone, email, loyalty_points) " +
                    "VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, customer.getFullName());
            pstmt.setString(2, customer.getPhone());
            pstmt.setString(3, customer.getEmail());
            pstmt.setDouble(4, customer.getLoyaltyPoints());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
    
    // Cập nhật thông tin khách hàng
    public boolean updateCustomer(Customer customer) {
        String sql = "UPDATE Customers SET full_name = ?, phone = ?, email = ?, " +
                    "loyalty_points = ? WHERE customer_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, customer.getFullName());
            pstmt.setString(2, customer.getPhone());
            pstmt.setString(3, customer.getEmail());
            pstmt.setDouble(4, customer.getLoyaltyPoints());
            pstmt.setInt(5, customer.getCustomerId());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Cập nhật điểm tích lũy
    public boolean updateLoyaltyPoints(int customerId, double points) {
        String sql = "UPDATE Customers SET loyalty_points = loyalty_points + ? WHERE customer_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDouble(1, points);
            pstmt.setInt(2, customerId);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Tìm kiếm khách hàng
    public List<Customer> searchCustomers(String keyword) {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM Customers WHERE full_name LIKE ? OR phone LIKE ? " +
                    "OR email LIKE ? ORDER BY full_name";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            pstmt.setString(3, searchPattern);
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Customer customer = extractCustomerFromResultSet(rs);
                customers.add(customer);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return customers;
    }
    
    // Lấy danh sách khách hàng VIP
    public List<Customer> getVIPCustomers() {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM Customers WHERE loyalty_points >= 1000 " +
                    "ORDER BY loyalty_points DESC";
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Customer customer = extractCustomerFromResultSet(rs);
                customers.add(customer);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return customers;
    }
    
    // Lấy top khách hàng có điểm cao nhất
    public List<Customer> getTopCustomers(int limit) {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT TOP (?) * FROM Customers ORDER BY loyalty_points DESC";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Customer customer = extractCustomerFromResultSet(rs);
                customers.add(customer);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return customers;
    }
    
    // Thêm giao dịch điểm
    public boolean addPointTransaction(int customerId, Integer ticketId, double pointsChange, 
                                      String transactionType, String description) {
        String sql = "INSERT INTO PointTransactions (customer_id, ticket_id, points_change, " +
                    "transaction_type, description) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, customerId);
            if (ticketId != null) {
                pstmt.setInt(2, ticketId);
            } else {
                pstmt.setNull(2, Types.INTEGER);
            }
            pstmt.setDouble(3, pointsChange);
            pstmt.setString(4, transactionType);
            pstmt.setString(5, description);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Helper method
    private Customer extractCustomerFromResultSet(ResultSet rs) throws SQLException {
        Customer customer = new Customer();
        customer.setCustomerId(rs.getInt("customer_id"));
        customer.setFullName(rs.getString("full_name"));
        customer.setPhone(rs.getString("phone"));
        customer.setEmail(rs.getString("email"));
        customer.setLoyaltyPoints(rs.getDouble("loyalty_points"));
        customer.setCreatedDate(rs.getTimestamp("created_date"));
        return customer;
    }
    
    // Test
    public static void main(String[] args) {
        CustomerDAO dao = new CustomerDAO();
        
        System.out.println("=== TEST CUSTOMER DAO ===\n");
        
        // Test lấy tất cả khách hàng
        List<Customer> customers = dao.getAllCustomers();
        System.out.println("Tổng số khách hàng: " + customers.size());
        
        for (Customer c : customers) {
            System.out.println("- " + c.getFullName() + " | " + c.getPhone());
            System.out.println("  Điểm tích lũy: " + c.getLoyaltyPoints());
            System.out.println("  VIP: " + (c.isVIP() ? "✓" : "✗"));
        }
        
        // Test khách hàng VIP
        System.out.println("\n=== KHÁCH HÀNG VIP ===");
        List<Customer> vips = dao.getVIPCustomers();
        System.out.println("Số lượng VIP: " + vips.size());
        for (Customer v : vips) {
            System.out.println("- " + v.getFullName() + ": " + v.getLoyaltyPoints() + " điểm");
        }
    }
    public boolean deleteCustomer(int customerId) {
    // Không xóa nếu còn vé Paid
    String checkSql = "SELECT COUNT(*) FROM Tickets WHERE customer_id = ? AND payment_status = 'Paid'";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(checkSql)) {
        ps.setInt(1, customerId);
        ResultSet rs = ps.executeQuery();
        if (rs.next() && rs.getInt(1) > 0) return false;
    } catch (SQLException e) { e.printStackTrace(); return false; }

    String sql = "DELETE FROM Customers WHERE customer_id = ?";
    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setInt(1, customerId);
        return ps.executeUpdate() > 0;
    } catch (SQLException e) { e.printStackTrace(); return false; }
}
}
