package servlet;

import dao.CustomerDAO;
import model.Customer;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
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
            case "list":   listCustomers(request, response);   break;
            case "search": searchCustomers(request, response); break;
            case "vip":    listVIPCustomers(request, response);break;
            case "add":    showAddForm(request, response);     break;
            case "edit":   showEditForm(request, response);    break;
            case "delete": deleteCustomer(request, response);  break;
            default:       listCustomers(request, response);
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
        switch (action != null ? action : "") {
            case "addCustomer":    addCustomer(request, response);    break;
            case "updateCustomer": updateCustomer(request, response); break;
            default:               listCustomers(request, response);
        }
    }

    private boolean isLoggedIn(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && session.getAttribute("loggedIn") != null;
    }

    // ── Sort helper ──────────────────────────────────────────────
    private List<Customer> applySorting(List<Customer> customers, HttpServletRequest request) {
        String sortBy  = request.getParameter("sortBy");
        String sortDir = request.getParameter("sortDir");
        if (sortBy == null || sortBy.isEmpty()) return customers;

        boolean desc = "desc".equalsIgnoreCase(sortDir);
        List<Customer> sorted = new ArrayList<>(customers);
        Comparator<Customer> comparator;

        switch (sortBy) {
            case "id":
                comparator = Comparator.comparingInt(Customer::getCustomerId);
                break;
            case "name":
                comparator = Comparator.comparing(
                    c -> c.getFullName() != null ? c.getFullName() : "",
                    String.CASE_INSENSITIVE_ORDER);
                break;
            case "points":
                comparator = Comparator.comparingDouble(Customer::getLoyaltyPoints);
                break;
            case "rank":
                comparator = Comparator.<Customer, Boolean>comparing(c -> !c.isVIP())
                    .thenComparingDouble(c -> -c.getLoyaltyPoints());
                break;
            default:
                return customers;
        }

        if (desc) comparator = comparator.reversed();
        sorted.sort(comparator);
        request.setAttribute("sortBy",  sortBy);
        request.setAttribute("sortDir", desc ? "desc" : "asc");
        return sorted;
    }

    // ── List / Search / VIP ──────────────────────────────────────
    private void listCustomers(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Customer> customers = customerDAO.getAllCustomers();
        customers = applySorting(customers, request);
        request.setAttribute("customers", customers);
        request.getRequestDispatcher("/customers.jsp").forward(request, response);
    }

    private void searchCustomers(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String keyword = request.getParameter("keyword");
        List<Customer> customers = (keyword == null || keyword.trim().isEmpty())
            ? customerDAO.getAllCustomers()
            : customerDAO.searchCustomers(keyword);
        customers = applySorting(customers, request);
        request.setAttribute("customers", customers);
        request.setAttribute("keyword", keyword);
        request.getRequestDispatcher("/customers.jsp").forward(request, response);
    }

    private void listVIPCustomers(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Customer> customers = customerDAO.getVIPCustomers();
        customers = applySorting(customers, request);
        request.setAttribute("customers", customers);
        request.setAttribute("isVIPOnly", true);
        request.getRequestDispatcher("/customers.jsp").forward(request, response);
    }

    private void showAddForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/add-customer.jsp").forward(request, response);
    }

    // ── Thêm khách hàng ─────────────────────────────────────────
    private void addCustomer(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String fullName  = request.getParameter("fullName");
            String phone     = request.getParameter("phone");
            String email     = request.getParameter("email");
            String pointsStr = request.getParameter("loyaltyPoints");

            if (fullName == null || fullName.trim().isEmpty()
                    || phone == null || phone.trim().isEmpty()) {
                request.getSession().setAttribute("message", "❌ Họ tên và số điện thoại là bắt buộc!");
                request.getSession().setAttribute("messageType", "error");
                response.sendRedirect(request.getContextPath() + "/customers?action=add");
                return;
            }

            Customer existing = customerDAO.getCustomerByPhone(phone.trim());
            if (existing != null) {
                request.getSession().setAttribute("message",
                    "❌ Số điện thoại " + phone + " đã tồn tại trong hệ thống!");
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
                try { initPoints = Double.parseDouble(pointsStr.trim()); }
                catch (NumberFormatException ignored) {}
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

    // ── Hiện form sửa ────────────────────────────────────────────
    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("customerId");
        if (idStr == null) { listCustomers(request, response); return; }
        try {
            Customer customer = customerDAO.getCustomerById(Integer.parseInt(idStr));
            if (customer == null) {
                request.getSession().setAttribute("message", "❌ Không tìm thấy khách hàng!");
                request.getSession().setAttribute("messageType", "error");
                response.sendRedirect(request.getContextPath() + "/customers");
                return;
            }
            request.setAttribute("customer", customer);
            request.getRequestDispatcher("/edit-customer.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/customers");
        }
    }

    // ── Cập nhật khách hàng ──────────────────────────────────────
    private void updateCustomer(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int customerId   = Integer.parseInt(request.getParameter("customerId"));
            String fullName  = request.getParameter("fullName");
            String phone     = request.getParameter("phone");
            String email     = request.getParameter("email");
            String pointsStr = request.getParameter("loyaltyPoints");

            if (fullName == null || fullName.trim().isEmpty()
                    || phone == null || phone.trim().isEmpty()) {
                request.getSession().setAttribute("message", "❌ Họ tên và số điện thoại là bắt buộc!");
                request.getSession().setAttribute("messageType", "error");
                response.sendRedirect(request.getContextPath() + "/customers?action=edit&customerId=" + customerId);
                return;
            }

            // Kiểm tra SĐT trùng với người khác
            Customer existing = customerDAO.getCustomerByPhone(phone.trim());
            if (existing != null && existing.getCustomerId() != customerId) {
                request.getSession().setAttribute("message",
                    "❌ Số điện thoại \"" + phone + "\" đã được dùng bởi khách hàng khác!");
                request.getSession().setAttribute("messageType", "error");
                response.sendRedirect(request.getContextPath() + "/customers?action=edit&customerId=" + customerId);
                return;
            }

            Customer customer = new Customer();
            customer.setCustomerId(customerId);
            customer.setFullName(fullName.trim());
            customer.setPhone(phone.trim());
            customer.setEmail(email != null ? email.trim() : "");
            double points = 0;
            if (pointsStr != null && !pointsStr.trim().isEmpty()) {
                try { points = Double.parseDouble(pointsStr.trim()); }
                catch (NumberFormatException ignored) {}
            }
            customer.setLoyaltyPoints(points);

            boolean success = customerDAO.updateCustomer(customer);
            request.getSession().setAttribute("message",
                success ? "✅ Cập nhật khách hàng \"" + fullName.trim() + "\" thành công!"
                        : "❌ Cập nhật thất bại!");
            request.getSession().setAttribute("messageType", success ? "success" : "error");
        } catch (Exception e) {
            e.printStackTrace();
            request.getSession().setAttribute("message", "❌ Lỗi: " + e.getMessage());
            request.getSession().setAttribute("messageType", "error");
        }
        response.sendRedirect(request.getContextPath() + "/customers");
    }

    // ── Xóa khách hàng ───────────────────────────────────────────
    private void deleteCustomer(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int customerId = Integer.parseInt(request.getParameter("customerId"));
            boolean success = customerDAO.deleteCustomer(customerId);
            request.getSession().setAttribute("message",
                success ? "🗑️ Đã xóa khách hàng thành công!"
                        : "❌ Không thể xóa! Khách hàng còn vé đã thanh toán.");
            request.getSession().setAttribute("messageType", success ? "success" : "error");
        } catch (Exception e) {
            e.printStackTrace();
            request.getSession().setAttribute("message", "❌ Lỗi: " + e.getMessage());
            request.getSession().setAttribute("messageType", "error");
        }
        response.sendRedirect(request.getContextPath() + "/customers");
    }
}