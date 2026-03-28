package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import java.io.IOException;

public class LoginServlet extends HttpServlet {

    // Tài khoản admin mặc định (có thể thay bằng DB sau)
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        // Nếu đã đăng nhập rồi thì chuyển thẳng vào trang chủ
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("loggedIn") != null) {
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }

        String action = request.getParameter("action");
        if ("logout".equals(action)) {
            if (session != null) session.invalidate();
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        request.getRequestDispatcher("/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (ADMIN_USERNAME.equals(username) && ADMIN_PASSWORD.equals(password)) {
            HttpSession session = request.getSession();
            session.setAttribute("loggedIn", true);
            session.setAttribute("username", username);
            session.setMaxInactiveInterval(60 * 60); // 1 giờ
            response.sendRedirect(request.getContextPath() + "/");
        } else {
            request.setAttribute("error", "❌ Tên đăng nhập hoặc mật khẩu không đúng!");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
        }
    }
}