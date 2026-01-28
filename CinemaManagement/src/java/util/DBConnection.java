package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=CinemaManagement;encrypt=true;trustServerCertificate=true";
    private static final String USER = "sa"; 
    private static final String PASS = "123"; // thay bằng pass của các em nhé
    
    public static Connection getConnection() {
        Connection conn = null;
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            System.out.println("Kết nối database thành công!");
        } catch (ClassNotFoundException e) {
            System.err.println("Không tìm thấy driver JDBC!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Lỗi kết nối database!");
            e.printStackTrace();
        }
        return conn;
    }
    
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
                System.out.println("Đã đóng kết nối database!");
            } catch (SQLException e) {
                System.err.println("Lỗi khi đóng kết nối!");
                e.printStackTrace();
            }
        }
    }
    
    // Test connection
    public static void main(String[] args) {
        Connection conn = getConnection();
        if (conn != null) {
            System.out.println("Done: Test kết nối thành công!");
            closeConnection(conn);
        } else {
            System.out.println("Failed: Test kết nối thất bại!");
        }
    }
}