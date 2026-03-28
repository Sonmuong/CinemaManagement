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
    private TicketDAO ticketDAO       = new TicketDAO();
    private ShowtimeDAO showtimeDAO   = new ShowtimeDAO();
    private CustomerDAO customerDAO   = new CustomerDAO();

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
            case "list":  listTickets(request, response);  break;
            case "sell":  showSellForm(request, response); break;
            default:      listTickets(request, response);
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
        if ("create".equals(action)) {
            sellTickets(request, response);
        } else if ("cancel".equals(action)) {
            cancelTicket(request, response);
        } else if ("findCustomer".equals(action)) {
            findCustomer(request, response);
        }
    }

    private boolean isLoggedIn(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && session.getAttribute("loggedIn") != null;
    }

    // ── Danh sách vé ────────────────────────────────────────────
    private void listTickets(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Ticket> tickets = ticketDAO.getAllTickets();
        request.setAttribute("tickets", tickets);
        request.getRequestDispatcher("/tickets.jsp").forward(request, response);
    }

    // ── Helper: build JSON array of booked seats (safe, no JSP rendering issues) ──
    private String buildBookedSeatsJson(List<String> bookedSeats) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < bookedSeats.size(); i++) {
            sb.append("\"").append(bookedSeats.get(i).trim()).append("\"");
            if (i < bookedSeats.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    // ── Form bán vé ──────────────────────────────────────────────
    private void showSellForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String showtimeIdStr = request.getParameter("showtimeId");
        if (showtimeIdStr == null || showtimeIdStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/showtimes");
            return;
        }
        int showtimeId = Integer.parseInt(showtimeIdStr);
        Showtime showtime = showtimeDAO.getShowtimeById(showtimeId);
        if (showtime == null) {
            response.sendRedirect(request.getContextPath() + "/showtimes");
            return;
        }
        List<String> bookedSeats = showtimeDAO.getBookedSeats(showtimeId);

        // FIX: Build JSON string để tránh lỗi render JSP forEach với whitespace
        String bookedSeatsJson = buildBookedSeatsJson(bookedSeats);

        // Restore customer từ session nếu có
        HttpSession session = request.getSession(false);
        if (session != null) {
            Customer savedCustomer   = (Customer) session.getAttribute("currentCustomer");
            Integer  savedShowtimeId = (Integer)  session.getAttribute("currentShowtimeId");
            if (savedCustomer != null && savedShowtimeId != null && savedShowtimeId == showtimeId) {
                request.setAttribute("customer", savedCustomer);
            }
            session.removeAttribute("currentCustomer");
            session.removeAttribute("currentShowtimeId");
        }

        request.setAttribute("showtime", showtime);
        request.setAttribute("bookedSeats", bookedSeats);
        request.setAttribute("bookedSeatsJson", bookedSeatsJson); // FIX: thêm JSON
        request.getRequestDispatcher("/sell-ticket.jsp").forward(request, response);
    }

    // ── Bán vé (hỗ trợ NHIỀU ghế) ───────────────────────────────
    private void sellTickets(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int showtimeId       = Integer.parseInt(request.getParameter("showtimeId"));
            String[] seatNumbers = request.getParameterValues("seatNumber");
            String ticketType    = request.getParameter("ticketType");
            double ticketPrice   = Double.parseDouble(request.getParameter("ticketPrice"));
            boolean usePoints    = "true".equals(request.getParameter("usePoints"));

            if (seatNumbers == null || seatNumbers.length == 0) {
                request.getSession().setAttribute("message", "Vui lòng chọn ít nhất 1 ghế!");
                request.getSession().setAttribute("messageType", "error");
                response.sendRedirect(request.getContextPath() + "/tickets");
                return;
            }

            Integer customerId = null;
            String customerIdStr = request.getParameter("customerId");
            if (customerIdStr != null && !customerIdStr.isEmpty()) {
                customerId = Integer.parseInt(customerIdStr);
            }

            int successCount  = 0;
            int failBooked    = 0;
            int failOther     = 0;
            int lastTicketId  = -1;

            for (int i = 0; i < seatNumbers.length; i++) {
                String seatNumber = seatNumbers[i].trim();
                if (seatNumber.isEmpty()) continue;

                Ticket ticket = new Ticket();
                ticket.setShowtimeId(showtimeId);
                ticket.setSeatNumber(seatNumber);
                ticket.setTicketType(ticketType);
                ticket.setTicketPrice(ticketPrice);
                ticket.setCustomerId(customerId);

                // Chỉ dùng điểm cho vé đầu tiên
                boolean applyPoints = usePoints && (i == 0);

                int result = ticketDAO.sellTicket(ticket, applyPoints);
                if (result > 0) {
                    successCount++;
                    lastTicketId = result;
                } else if (result == -2) {
                    failBooked++; // Ghế đã được đặt
                } else {
                    failOther++; // Lỗi khác (DB, v.v.)
                }
            }

            if (successCount > 0) {
                String msg = "✅ Bán vé thành công! Đã bán " + successCount + " ghế.";
                if (failBooked > 0) msg += " (" + failBooked + " ghế đã có người đặt trước.)";
                request.getSession().setAttribute("message", msg);
                request.getSession().setAttribute("messageType", "success");

                if (lastTicketId > 0) {
                    Ticket soldTicket = ticketDAO.getTicketById(lastTicketId);
                    request.getSession().setAttribute("soldTicket", soldTicket);
                }
            } else if (failBooked > 0) {
                request.getSession().setAttribute("message",
                    "❌ Tất cả ghế đã chọn đều đã được đặt bởi người khác!");
                request.getSession().setAttribute("messageType", "error");
            } else {
                request.getSession().setAttribute("message",
                    "❌ Bán vé thất bại! Vui lòng kiểm tra kết nối database và thử lại.");
                request.getSession().setAttribute("messageType", "error");
            }

        } catch (Exception e) {
            e.printStackTrace();
            request.getSession().setAttribute("message", "❌ Lỗi: " + e.getMessage());
            request.getSession().setAttribute("messageType", "error");
        }

        response.sendRedirect(request.getContextPath() + "/tickets");
    }

    // ── Hủy vé ──────────────────────────────────────────────────
    private void cancelTicket(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int ticketId = Integer.parseInt(request.getParameter("ticketId"));
            boolean success = ticketDAO.cancelTicket(ticketId);
            request.getSession().setAttribute("message",
                success ? "✅ Hủy vé thành công!" : "❌ Không thể hủy vé!");
            request.getSession().setAttribute("messageType", success ? "success" : "error");
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
        String phone         = request.getParameter("phone");
        String showtimeIdStr = request.getParameter("showtimeId");

        if (showtimeIdStr == null || showtimeIdStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/showtimes");
            return;
        }

        int showtimeId           = Integer.parseInt(showtimeIdStr);
        Customer customer        = customerDAO.getCustomerByPhone(phone);
        Showtime showtime        = showtimeDAO.getShowtimeById(showtimeId);
        List<String> bookedSeats = showtimeDAO.getBookedSeats(showtimeId);

        // FIX: Build JSON
        String bookedSeatsJson = buildBookedSeatsJson(bookedSeats);

        HttpSession session = request.getSession();
        if (customer != null) {
            session.setAttribute("currentCustomer",   customer);
            session.setAttribute("currentShowtimeId", showtimeId);
        }

        request.setAttribute("showtime",        showtime);
        request.setAttribute("bookedSeats",     bookedSeats);
        request.setAttribute("bookedSeatsJson", bookedSeatsJson); // FIX
        request.setAttribute("customer",        customer);
        request.setAttribute("searchedPhone",   phone);
        if (customer == null) {
            request.setAttribute("customerMessage",
                "⚠️ Không tìm thấy khách hàng với SĐT: " + phone + ". Sẽ bán vé lẻ.");
        }
        request.getRequestDispatcher("/sell-ticket.jsp").forward(request, response);
    }
}