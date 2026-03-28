// =================== CustomerServlet.java ===================
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
            case "list":   listCustomers(request, response);   break;
            case "search": searchCustomers(request, response); break;
            case "vip":    listVIPCustomers(request, response);break;
            default:       listCustomers(request, response);
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
}