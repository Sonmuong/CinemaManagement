package servlet;

import dao.CustomerDAO;
import model.Customer;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

public class CustomerServlet extends HttpServlet {
    private CustomerDAO customerDAO = new CustomerDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        if (!isLoggedIn(request)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String action = request.getParameter("action");
        if (action == null) action = "list";

        switch (action) {
            case "list":      listCustomers(request, response);   break;
            case "search":    searchCustomers(request, response); break;
            case "vip":       listVIPCustomers(request, response);break;
            case "add":       showAddForm(request, response);     break;
            default:          listCustomers(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        if (!isLoggedIn(request)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String action = request.getParameter("action");
        if ("addCustomer".equals(action)) {
            addCustomer(request, response);
        } else {
            listCustomers(request, response);
        }
    }

    private boolean isLoggedIn(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && session.getAttribute("loggedIn") != null;
    }

    private void listCustomers(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Customer> customers = customerDAO.getAllCustomers();
        request.setAttribute("customers", customers);
        request.getRequestDispatcher("/customers.jsp").forward(request, response);
    }

    private void searchCustomers(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String keyword = request.getParameter("keyword");
        List<Customer> customers = (keyword == null || keyword.trim().isEmpty())
            ? customerDAO.getAllCustomers()
            : customerDAO.searchCustomers(keyword);
        request.setAttribute("customers", customers);
        request.setAttribute("keyword", keyword);
        request.getRequestDispatcher("/customers.jsp").forward(request, response);
    }

    private void listVIPCustomers(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Customer> customers = customerDAO.getVIPCustomers();
        request.setAttribute("customers", customers);
        request.setAttribute("isVIPOnly", true);
        request.getRequestDispatcher("/customers.jsp").forward(request, response);
    }

    private void showAddForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/add-customer.jsp").forward(request, response);
    }

    private void addCustomer(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String fullName = request.getParameter("fullName");
            String phone    = request.getParameter("phone");
            String email    = request.getParameter("email");
            String pointsStr = request.getParameter("loyaltyPoints");

            // Kiểm tra dữ liệu cơ bản
            if (fullName == null || fullName.trim().isEmpty()
                    || phone == null || phone.trim().isEmpty()) {
                request.getSession().setAttribute("message", "❌ Họ tên và số điện thoại là bắt buộc!");
                request.getSession().setAttribute("messageType", "error");
                response.sendRedirect(request.getContextPath() + "/customers?action=add");
                return;
            }

            // Kiểm tra trùng số điện thoại
            Customer existing = customerDAO.getCustomerByPhone(phone.trim());
            if (existing != null) {
                request.getSession().setAttribute("message", "❌ Số điện thoại " + phone + " đã tồn tại trong hệ thống!");
                request.getSession().setAttribute("messageType", "error");
                response.sendRedirect(request.getContextPath() + "/customers?action=add");
                return;
            }

            Customer customer = new Customer();
            customer.setFullName(fullName.trim());
            customer.setPhone(phone.trim());
            customer.setEmail(email != null ? email.trim() : "");
            double initPoints = 0;
            if (pointsStr != null && !pointsStr.trim().isEmpty()) {
                try { initPoints = Double.parseDouble(pointsStr.trim()); } catch (NumberFormatException ignored) {}
            }
            customer.setLoyaltyPoints(initPoints);

            int newId = customerDAO.addCustomer(customer);
            if (newId > 0) {
                request.getSession().setAttribute("message",
                    "✅ Thêm khách hàng \"" + fullName.trim() + "\" thành công! (ID: " + newId + ")");
                request.getSession().setAttribute("messageType", "success");
                response.sendRedirect(request.getContextPath() + "/customers");
            } else {
                request.getSession().setAttribute("message", "❌ Thêm khách hàng thất bại!");
                request.getSession().setAttribute("messageType", "error");
                response.sendRedirect(request.getContextPath() + "/customers?action=add");
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.getSession().setAttribute("message", "❌ Lỗi: " + e.getMessage());
            request.getSession().setAttribute("messageType", "error");
            response.sendRedirect(request.getContextPath() + "/customers?action=add");
        }
    }
}