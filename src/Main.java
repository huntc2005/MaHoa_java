import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import org.mindrot.jbcrypt.BCrypt;

public class Main {
    // Thay đổi các thông số kết nối dưới đây
    static final String DB_URL = "jdbc:mysql://localhost:3306/login";
    static final String USER = "root";
    static final String PASS = "123";

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS)) {
            createTable(conn); // Tạo bảng users nếu chưa tồn tại
            Scanner scanner = new Scanner(System.in);
            System.out.println("1. Đăng ký\n2. Đăng nhập");
            int choice = scanner.nextInt();
            if (choice == 1) {
                // Đăng ký
                System.out.println("Nhập tên đăng nhập:");
                String username = scanner.next();
                System.out.println("Nhập mật khẩu:");
                String password = scanner.next();
                String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
                register(conn, username, hashedPassword);
                System.out.println("Đăng ký thành công!");
            } else if (choice == 2) {
                // Đăng nhập
                System.out.println("Nhập tên đăng nhập:");
                String username = scanner.next();
                System.out.println("Nhập mật khẩu:");
                String password = scanner.next();
                if (login(conn, username, password)) {
                    System.out.println("Đăng nhập thành công! Chào mừng bạn.");
                } else {
                    System.out.println("Tên đăng nhập hoặc mật khẩu không đúng.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Tạo bảng users nếu chưa tồn tại
    static void createTable(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS users (id INT AUTO_INCREMENT PRIMARY KEY, username VARCHAR(50) UNIQUE, password VARCHAR(255))";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        }
    }

    // Đăng ký
    static void register(Connection conn, String username, String password) throws SQLException {
        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.executeUpdate();
        }
    }

    // Đăng nhập
    static boolean login(Connection conn, String username, String password) throws SQLException {
        String sql = "SELECT password FROM users WHERE username = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String hashedPassword = rs.getString("password");
                    return BCrypt.checkpw(password, hashedPassword);
                }
            }
        }
        return false;
    }
}