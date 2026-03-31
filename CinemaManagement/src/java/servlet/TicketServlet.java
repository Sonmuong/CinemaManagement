package servlet;

import dao.TicketDAO;
import dao.ShowtimeDAO;
import dao.CustomerDAO;
import model.Ticket;
import model.Showtime;
import model.Customer;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

public class TicketServlet extends HttpServlet {
    private TicketDAO     ticketDAO   = new TicketDAO();
    private ShowtimeDAO   showtimeDAO = new ShowtimeDAO();
    private CustomerDAO   customerDAO = new CustomerDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        if (!isLoggedIn(request)) { response.sendRedirect(request.getContextPath() + "/login"); return; }

        String action = request.getParameter("action");
        if (action == null) action = "list";
        switch (action) {
            case "list":   listTickets(request, response);   break;
            case "sell":   showSellForm(request, response);  break;
            case "result": showTicketResult(request, response); break;
            default:       listTickets(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        if (!isLoggedIn(request)) { response.sendRedirect(request.getContextPath() + "/login"); return; }

        String action = request.getParameter("action");
        switch (action != null ? action : "") {
            case "create":       sellTickets(request, response);   break;
            case "cancel":       cancelTicket(request, response);  break;
            case "findCustomer": findCustomer(request, response);  break;
            default:             listTickets(request, response);
        }
    }

    private boolean isLoggedIn(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        return s != null && s.getAttribute("loggedIn") != null;
    }

    // ── Danh sách vé ────────────────────────────────────────────
    private void listTickets(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("tickets", ticketDAO.getAllTickets());
        request.getRequestDispatcher("/tickets.jsp").forward(request, response);
    }

    // ── Hiện form bán vé ────────────────────────────────────────
    private void showSellForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("showtimeId");
        if (idStr == null || idStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/showtimes"); return;
        }
        int showtimeId  = Integer.parseInt(idStr);
        Showtime showtime = showtimeDAO.getShowtimeById(showtimeId);
        if (showtime == null) {
            response.sendRedirect(request.getContextPath() + "/showtimes"); return;
        }

        // Restore customer từ session nếu vừa tìm xong
        HttpSession session = request.getSession(false);
        if (session != null) {
            Customer c   = (Customer) session.getAttribute("currentCustomer");
            Integer  sid = (Integer)  session.getAttribute("currentShowtimeId");
            if (c != null && sid != null && sid == showtimeId) {
                request.setAttribute("customer", c);
            }
            session.removeAttribute("currentCustomer");
            session.removeAttribute("currentShowtimeId");
        }

        request.setAttribute("showtime", showtime);
        request.setAttribute("bookedSeats", showtimeDAO.getBookedSeats(showtimeId));
        request.getRequestDispatcher("/sell-ticket.jsp").forward(request, response);
    }

    // ── Bán vé ──────────────────────────────────────────────────
    private void sellTickets(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int      showtimeId  = Integer.parseInt(request.getParameter("showtimeId"));
            String[] seatNumbers = request.getParameterValues("seatNumber");
            String   ticketType  = request.getParameter("ticketType");
            double   ticketPrice = Double.parseDouble(request.getParameter("ticketPrice"));
            boolean  usePoints   = "true".equals(request.getParameter("usePoints"));

            System.out.println("=== sellTickets() === seats=" +
                (seatNumbers != null ? java.util.Arrays.toString(seatNumbers) : "NULL"));

            if (seatNumbers == null || seatNumbers.length == 0) {
                request.getSession().setAttribute("message", "❌ Chưa chọn ghế, vui lòng thử lại.");
                request.getSession().setAttribute("messageType", "error");
                response.sendRedirect(request.getContextPath() + "/showtimes");
                return;
            }

            Integer customerId = null;
            String cidStr = request.getParameter("customerId");
            if (cidStr != null && !cidStr.isEmpty()) customerId = Integer.parseInt(cidStr);

            int successCount = 0;
            int lastTicketId = -1;

            for (int i = 0; i < seatNumbers.length; i++) {
                String seat = seatNumbers[i].trim();
                if (seat.isEmpty()) continue;

                Ticket ticket = new Ticket();
                ticket.setShowtimeId(showtimeId);
                ticket.setSeatNumber(seat);
                ticket.setTicketType(ticketType);
                ticket.setTicketPrice(ticketPrice);
                ticket.setCustomerId(customerId);

                int tid = ticketDAO.sellTicket(ticket, usePoints && i == 0);
                if (tid > 0) { successCount++; lastTicketId = tid; }
            }

            if (successCount > 0 && lastTicketId > 0) {
                // Lưu vé vừa bán vào session để hiển thị trang kết quả
                Ticket soldTicket = ticketDAO.getTicketById(lastTicketId);
                request.getSession().setAttribute("soldTicket", soldTicket);

                // Nếu bán nhiều ghế thì lưu thêm count
                if (seatNumbers.length > 1) {
                    request.getSession().setAttribute("soldCount", successCount);
                    request.getSession().setAttribute("totalCount", seatNumbers.length);
                }

                // FIX: Redirect sang trang vé đẹp
                response.sendRedirect(request.getContextPath() + "/tickets?action=result");
            } else {
                request.getSession().setAttribute("message",
                    "❌ Bán vé thất bại! Ghế có thể đã được đặt, vui lòng kiểm tra lại.");
                request.getSession().setAttribute("messageType", "error");
                response.sendRedirect(request.getContextPath() + "/tickets");
            }

        } catch (Exception e) {
            e.printStackTrace();
            request.getSession().setAttribute("message", "❌ Lỗi hệ thống: " + e.getMessage());
            request.getSession().setAttribute("messageType", "error");
            response.sendRedirect(request.getContextPath() + "/tickets");
        }
    }

    // ── Hiện trang kết quả vé ───────────────────────────────────
    private void showTicketResult(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("soldTicket") == null) {
            response.sendRedirect(request.getContextPath() + "/tickets");
            return;
        }
        request.setAttribute("soldTicket", session.getAttribute("soldTicket"));
        request.setAttribute("soldCount",  session.getAttribute("soldCount"));
        request.setAttribute("totalCount", session.getAttribute("totalCount"));
        session.removeAttribute("soldTicket");
        session.removeAttribute("soldCount");
        session.removeAttribute("totalCount");
        request.getRequestDispatcher("/ticket-result.jsp").forward(request, response);
    }

    // ── Hủy vé ──────────────────────────────────────────────────
    private void cancelTicket(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int ticketId = Integer.parseInt(request.getParameter("ticketId"));
            boolean ok   = ticketDAO.cancelTicket(ticketId);
            request.getSession().setAttribute("message", ok ? "✅ Hủy vé thành công!" : "❌ Không thể hủy vé!");
            request.getSession().setAttribute("messageType", ok ? "success" : "error");
        } catch (Exception e) {
            e.printStackTrace();
            request.getSession().setAttribute("message", "❌ Lỗi: " + e.getMessage());
            request.getSession().setAttribute("messageType", "error");
        }
        response.sendRedirect(request.getContextPath() + "/tickets");
    }

    // ── Tìm khách hàng ───────────────────────────────────────────
    private void findCustomer(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String phone       = request.getParameter("phone");
        String showtimeIdStr = request.getParameter("showtimeId");

        if (showtimeIdStr == null || showtimeIdStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/showtimes"); return;
        }

        int showtimeId           = Integer.parseInt(showtimeIdStr);
        Showtime showtime        = showtimeDAO.getShowtimeById(showtimeId);
        List<String> bookedSeats = showtimeDAO.getBookedSeats(showtimeId);

        Customer customer = null;
        if (phone != null && !phone.trim().isEmpty()) {
            customer = customerDAO.getCustomerByPhone(phone.trim());
        }

        HttpSession session = request.getSession();
        if (customer != null) {
            session.setAttribute("currentCustomer",  customer);
            session.setAttribute("currentShowtimeId", showtimeId);
        }

        request.setAttribute("showtime",    showtime);
        request.setAttribute("bookedSeats", bookedSeats);
        request.setAttribute("customer",    customer);
        request.setAttribute("searchedPhone", phone);

        // FIX: Set flag rõ ràng để JSP phân biệt "chưa tìm" vs "tìm không thấy"
        if (phone != null && !phone.trim().isEmpty() && customer == null) {
            request.setAttribute("customerNotFound", true);
        }

        request.getRequestDispatcher("/sell-ticket.jsp").forward(request, response);
    }
}