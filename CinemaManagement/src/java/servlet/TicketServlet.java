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

    private String buildBookedSeatsJson(List<String> bookedSeats) {
        if (bookedSeats == null || bookedSeats.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (String seat : bookedSeats) {
            if (seat == null) continue;
            if (!first) sb.append(",");
            sb.append("\"").append(seat.trim().replace("\"", "\\\"")).append("\"");
            first = false;
        }
        sb.append("]");
        return sb.toString();
    }

    private void listTickets(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Ticket> tickets = ticketDAO.getAllTickets();
        request.setAttribute("tickets", tickets);
        request.getRequestDispatcher("/tickets.jsp").forward(request, response);
    }

    private void showSellForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String showtimeIdStr = request.getParameter("showtimeId");
        if (showtimeIdStr == null || showtimeIdStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/showtimes");
            return;
        }

        int showtimeId;
        try {
            showtimeId = Integer.parseInt(showtimeIdStr.trim());
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/showtimes");
            return;
        }

        Showtime showtime = showtimeDAO.getShowtimeById(showtimeId);
        if (showtime == null) {
            request.getSession().setAttribute("message", "❌ Không tìm thấy suất chiếu #" + showtimeId);
            request.getSession().setAttribute("messageType", "error");
            response.sendRedirect(request.getContextPath() + "/showtimes");
            return;
        }

        List<String> bookedSeats = showtimeDAO.getBookedSeats(showtimeId);
        String bookedSeatsJson = buildBookedSeatsJson(bookedSeats);

        HttpSession session = request.getSession(false);
        if (session != null) {
            Customer savedCustomer   = (Customer) session.getAttribute("currentCustomer");
            Integer  savedShowtimeId = (Integer)  session.getAttribute("currentShowtimeId");
            if (savedCustomer != null && savedShowtimeId != null
                    && savedShowtimeId.equals(showtimeId)) {
                request.setAttribute("customer", savedCustomer);
            }
            session.removeAttribute("currentCustomer");
            session.removeAttribute("currentShowtimeId");
        }

        request.setAttribute("showtime",        showtime);
        request.setAttribute("bookedSeats",     bookedSeats);
        request.setAttribute("bookedSeatsJson", bookedSeatsJson);
        request.getRequestDispatcher("/sell-ticket.jsp").forward(request, response);
    }

    private void sellTickets(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        System.out.println("\n======= sellTickets START =======");

        try {
            String showtimeIdStr  = request.getParameter("showtimeId");
            String ticketPriceStr = request.getParameter("ticketPrice");
            String[] seatNumbers  = request.getParameterValues("seatNumber");
            String ticketType     = request.getParameter("ticketType");
            String customerIdStr  = request.getParameter("customerId");
            String usePointsStr   = request.getParameter("usePoints");

            System.out.println("  showtimeId   = " + showtimeIdStr);
            System.out.println("  ticketPrice  = " + ticketPriceStr);
            System.out.println("  seatNumbers  = " + java.util.Arrays.toString(seatNumbers));
            System.out.println("  ticketType   = " + ticketType);
            System.out.println("  customerId   = " + customerIdStr);
            System.out.println("  usePoints    = " + usePointsStr);

            // ── Validate đầu vào ──────────────────────────────
            if (showtimeIdStr == null || showtimeIdStr.trim().isEmpty()) {
                setError(request, "❌ Thiếu showtimeId!");
                response.sendRedirect(request.getContextPath() + "/showtimes");
                return;
            }
            if (seatNumbers == null || seatNumbers.length == 0) {
                setError(request, "❌ Vui lòng chọn ít nhất 1 ghế!");
                response.sendRedirect(request.getContextPath() + "/showtimes");
                return;
            }
            if (ticketPriceStr == null || ticketPriceStr.trim().isEmpty()) {
                setError(request, "❌ Thiếu giá vé!");
                response.sendRedirect(request.getContextPath() + "/showtimes");
                return;
            }

            int showtimeId     = Integer.parseInt(showtimeIdStr.trim());
            double ticketPrice = Double.parseDouble(ticketPriceStr.trim());
            boolean usePoints  = "true".equalsIgnoreCase(usePointsStr);

            Integer customerId = null;
            if (customerIdStr != null && !customerIdStr.trim().isEmpty()) {
                try {
                    customerId = Integer.parseInt(customerIdStr.trim());
                } catch (NumberFormatException e) {
                    System.out.println("  ⚠️ customerId parse lỗi: " + customerIdStr);
                }
            }

            // ── Bán từng ghế ─────────────────────────────────
            int successCount = 0;
            int failBooked   = 0;
            int failOther    = 0;
            int lastTicketId = -1;

            for (int i = 0; i < seatNumbers.length; i++) {
                if (seatNumbers[i] == null || seatNumbers[i].trim().isEmpty()) continue;
                String seat = seatNumbers[i].trim();

                Ticket ticket = new Ticket();
                ticket.setShowtimeId(showtimeId);
                ticket.setSeatNumber(seat);
                ticket.setTicketType(ticketType != null && !ticketType.isEmpty()
                    ? ticketType : "Normal");
                ticket.setTicketPrice(ticketPrice);
                ticket.setCustomerId(customerId);

                boolean applyPoints = usePoints && (i == 0);
                System.out.println("  → Bán ghế [" + seat + "] applyPoints=" + applyPoints);

                int result = ticketDAO.sellTicket(ticket, applyPoints);
                System.out.println("  → Result = " + result);

                if (result > 0) {
                    successCount++;
                    lastTicketId = result;
                } else if (result == -2) {
                    failBooked++;
                } else {
                    failOther++;
                }
            }

            System.out.println("  Tổng: ok=" + successCount
                + " booked=" + failBooked + " error=" + failOther);

            // ── Set message ───────────────────────────────────
            if (successCount > 0) {
                StringBuilder msg = new StringBuilder("✅ Bán vé thành công! Đã bán ")
                    .append(successCount).append(" ghế.");
                if (failBooked > 0)
                    msg.append(" (").append(failBooked).append(" ghế đã có người đặt, bỏ qua.)");
                if (failOther > 0)
                    msg.append(" (").append(failOther).append(" ghế gặp lỗi DB.)");

                request.getSession().setAttribute("message", msg.toString());
                request.getSession().setAttribute("messageType", "success");

                if (lastTicketId > 0) {
                    Ticket sold = ticketDAO.getTicketById(lastTicketId);
                    if (sold != null) request.getSession().setAttribute("soldTicket", sold);
                }

            } else if (failBooked > 0) {
                setError(request, "❌ Tất cả " + failBooked
                    + " ghế đã được đặt trước! Vui lòng chọn ghế khác.");
            } else {
                // failOther > 0 — lỗi DB/kết nối
                setError(request, "❌ Bán vé thất bại! Kiểm tra kết nối database và Tomcat log. "
                    + "(failOther=" + failOther + ")");
            }

        } catch (NumberFormatException e) {
            System.out.println("❌ NumberFormatException: " + e.getMessage());
            e.printStackTrace();
            setError(request, "❌ Dữ liệu không hợp lệ: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("❌ Exception khi bán vé: " + e.getMessage());
            e.printStackTrace();
            setError(request, "❌ Lỗi hệ thống: " + e.getClass().getSimpleName()
                + " - " + e.getMessage());
        }

        System.out.println("======= sellTickets END =======\n");
        response.sendRedirect(request.getContextPath() + "/tickets");
    }

    private void setError(HttpServletRequest request, String msg) {
        request.getSession().setAttribute("message", msg);
        request.getSession().setAttribute("messageType", "error");
    }

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
            setError(request, "❌ Lỗi: " + e.getMessage());
        }
        response.sendRedirect(request.getContextPath() + "/tickets");
    }

    private void findCustomer(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String phone         = request.getParameter("phone");
        String showtimeIdStr = request.getParameter("showtimeId");

        if (showtimeIdStr == null || showtimeIdStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/showtimes");
            return;
        }

        int showtimeId;
        try {
            showtimeId = Integer.parseInt(showtimeIdStr.trim());
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/showtimes");
            return;
        }

        Customer customer        = (phone != null && !phone.trim().isEmpty())
                                   ? customerDAO.getCustomerByPhone(phone.trim()) : null;
        Showtime showtime        = showtimeDAO.getShowtimeById(showtimeId);
        List<String> bookedSeats = showtimeDAO.getBookedSeats(showtimeId);
        String bookedSeatsJson   = buildBookedSeatsJson(bookedSeats);

        HttpSession session = request.getSession();
        if (customer != null) {
            session.setAttribute("currentCustomer",   customer);
            session.setAttribute("currentShowtimeId", showtimeId);
        } else {
            session.removeAttribute("currentCustomer");
            session.removeAttribute("currentShowtimeId");
        }

        request.setAttribute("showtime",        showtime);
        request.setAttribute("bookedSeats",     bookedSeats);
        request.setAttribute("bookedSeatsJson", bookedSeatsJson);
        request.setAttribute("customer",        customer);
        request.setAttribute("searchedPhone",   phone);

        if (customer == null && phone != null && !phone.trim().isEmpty()) {
            request.setAttribute("customerMessage",
                "⚠️ Không tìm thấy khách hàng với SĐT: " + phone.trim()
                + ". Sẽ bán vé lẻ (không tích điểm).");
        }
        request.getRequestDispatcher("/sell-ticket.jsp").forward(request, response);
    }
}