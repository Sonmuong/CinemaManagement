package dao;

import model.Customer;
import util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {

    public List<Customer> getAllCustomers() {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM Customers ORDER BY full_name";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) customers.add(extractCustomerFromResultSet(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return customers;
    }

    public Customer getCustomerById(int customerId) {
        String sql = "SELECT * FROM Customers WHERE customer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return extractCustomerFromResultSet(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public Customer getCustomerByPhone(String phone) {
        String sql = "SELECT * FROM Customers WHERE phone = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phone);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return extractCustomerFromResultSet(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public int addCustomer(Customer customer) {
        String sql = "INSERT INTO Customers (full_name, phone, email, loyalty_points) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, customer.getFullName());
            ps.setString(2, customer.getPhone());
            ps.setString(3, customer.getEmail());
            ps.setDouble(4, customer.getLoyaltyPoints());
            int affected = ps.executeUpdate();
            if (affected > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return -1;
    }

    public boolean updateCustomer(Customer customer) {
        String sql = "UPDATE Customers SET full_name = ?, phone = ?, email = ?, " +
                     "loyalty_points = ? WHERE customer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customer.getFullName());
            ps.setString(2, customer.getPhone());
            ps.setString(3, customer.getEmail());
            ps.setDouble(4, customer.getLoyaltyPoints());
            ps.setInt(5, customer.getCustomerId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // Xóa khách hàng — không cho xóa nếu còn vé Paid
    public boolean deleteCustomer(int customerId) {
        String checkSql = "SELECT COUNT(*) FROM Tickets WHERE customer_id = ? AND payment_status = 'Paid'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(checkSql)) {
            ps.setInt(1, customerId);
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) return false;
        } catch (SQLException e) { e.printStackTrace(); return false; }

        // Xóa lịch sử điểm trước
        String delPoints = "DELETE FROM PointTransactions WHERE customer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(delPoints)) {
            ps.setInt(1, customerId);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }

        String sql = "DELETE FROM Customers WHERE customer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean updateLoyaltyPoints(int customerId, double points) {
        String sql = "UPDATE Customers SET loyalty_points = loyalty_points + ? WHERE customer_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, points);
            ps.setInt(2, customerId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public List<Customer> searchCustomers(String keyword) {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM Customers WHERE full_name LIKE ? OR phone LIKE ? " +
                     "OR email LIKE ? ORDER BY full_name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String p = "%" + keyword + "%";
            ps.setString(1, p); ps.setString(2, p); ps.setString(3, p);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) customers.add(extractCustomerFromResultSet(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return customers;
    }

    public List<Customer> getVIPCustomers() {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM Customers WHERE loyalty_points >= 1000 ORDER BY loyalty_points DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) customers.add(extractCustomerFromResultSet(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return customers;
    }

    public List<Customer> getTopCustomers(int limit) {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT TOP (?) * FROM Customers ORDER BY loyalty_points DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) customers.add(extractCustomerFromResultSet(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return customers;
    }

    public boolean addPointTransaction(int customerId, Integer ticketId, double pointsChange,
                                       String transactionType, String description) {
        String sql = "INSERT INTO PointTransactions (customer_id, ticket_id, points_change, " +
                     "transaction_type, description) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            if (ticketId != null) ps.setInt(2, ticketId);
            else ps.setNull(2, Types.INTEGER);
            ps.setDouble(3, pointsChange);
            ps.setString(4, transactionType);
            ps.setString(5, description);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

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
}