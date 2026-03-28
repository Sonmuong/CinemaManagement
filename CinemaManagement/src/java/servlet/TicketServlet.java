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

        String action = request.getParameter("action");
        if ("create".equals(action)) {
            sellTickets(request, response);          // đổi tên → xử lý nhiều ghế
        } else if ("cancel".equals(action)) {
            cancelTicket(request, response);
        } else if ("findCustomer".equals(action)) {
            findCustomer(request, response);
        }
    }

    // ── Danh sách vé ────────────────────────────────────────────
    private void listTickets(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Ticket> tickets = ticketDAO.getAllTickets();
        request.setAttribute("tickets", tickets);
        request.getRequestDispatcher("/tickets.jsp").forward(request, response);
    }

    // ── Form bán vé ──────────────────────────────────────────────
    private void showSellForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int showtimeId = Integer.parseInt(request.getParameter("showtimeId"));
        Showtime showtime = showtimeDAO.getShowtimeById(showtimeId);
        List<String> bookedSeats = showtimeDAO.getBookedSeats(showtimeId);

        request.setAttribute("showtime", showtime);
        request.setAttribute("bookedSeats", bookedSeats);
        request.getRequestDispatcher("/sell-ticket.jsp").forward(request, response);
    }

    // ── Bán vé (hỗ trợ NHIỀU ghế) ───────────────────────────────
    private void sellTickets(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            // ══ DEBUG: in tất cả params nhận được ══
            System.out.println("════ DEBUG sellTickets ════");
            java.util.Enumeration<String> paramNames = request.getParameterNames();
            while (paramNames.hasMoreElements()) {
                String pName = paramNames.nextElement();
                String[] pVals = request.getParameterValues(pName);
                System.out.println("  PARAM [" + pName + "] = " + java.util.Arrays.toString(pVals));
            }
            System.out.println("═══════════════════════════");

            int showtimeId       = Integer.parseInt(request.getParameter("showtimeId"));
            String[] seatNumbers = request.getParameterValues("seatNumber");
            String ticketType    = request.getParameter("ticketType");
            double ticketPrice   = Double.parseDouble(request.getParameter("ticketPrice"));
            boolean usePoints    = "true".equals(request.getParameter("usePoints"));

            System.out.println("  showtimeId=" + showtimeId + " | seatNumbers=" + java.util.Arrays.toString(seatNumbers)
                + " | ticketPrice=" + ticketPrice + " | usePoints=" + usePoints);

            if (seatNumbers == null || seatNumbers.length == 0) {
                System.out.println("  ❌ seatNumbers null hoặc rỗng — form không gửi ghế!");
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

            int successCount = 0;
            int lastTicketId = -1;

            for (int i = 0; i < seatNumbers.length; i++) {
                String seatNumber = seatNumbers[i].trim();
                if (seatNumber.isEmpty()) continue;

                Ticket ticket = new Ticket();
                ticket.setShowtimeId(showtimeId);
                ticket.setSeatNumber(seatNumber);
                ticket.setTicketType(ticketType);
                ticket.setTicketPrice(ticketPrice);
                ticket.setCustomerId(customerId);

                // Chỉ áp dụng usePoints cho ghế đầu tiên để không trừ điểm nhiều lần
                boolean applyPoints = usePoints && (i == 0);

                int ticketId = ticketDAO.sellTicket(ticket, applyPoints);
                if (ticketId > 0) {
                    successCount++;
                    lastTicketId = ticketId;
                }
            }

            if (successCount > 0) {
                request.getSession().setAttribute("message",
                    "Bán vé thành công! Đã bán " + successCount + "/" + seatNumbers.length + " ghế.");
                request.getSession().setAttribute("messageType", "success");

                if (lastTicketId > 0) {
                    Ticket soldTicket = ticketDAO.getTicketById(lastTicketId);
                    request.getSession().setAttribute("soldTicket", soldTicket);
                }
            } else {
                request.getSession().setAttribute("message",
                    "Bán vé thất bại! Các ghế đã chọn có thể đã được đặt.");
                request.getSession().setAttribute("messageType", "error");
            }

        } catch (Exception e) {
            e.printStackTrace();
            request.getSession().setAttribute("message", "Lỗi: " + e.getMessage());
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
                success ? "Hủy vé thành công!" : "Không thể hủy vé!");
            request.getSession().setAttribute("messageType", success ? "success" : "error");
        } catch (Exception e) {
            e.printStackTrace();
            request.getSession().setAttribute("message", "Lỗi: " + e.getMessage());
            request.getSession().setAttribute("messageType", "error");
        }
        response.sendRedirect(request.getContextPath() + "/tickets");
    }

    // ── Tìm khách hàng ───────────────────────────────────────────
    private void findCustomer(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String phone    = request.getParameter("phone");
        int showtimeId  = Integer.parseInt(request.getParameter("showtimeId"));

        Customer customer        = customerDAO.getCustomerByPhone(phone);
        Showtime showtime        = showtimeDAO.getShowtimeById(showtimeId);
        List<String> bookedSeats = showtimeDAO.getBookedSeats(showtimeId);

        request.setAttribute("showtime", showtime);
        request.setAttribute("bookedSeats", bookedSeats);
        request.setAttribute("customer", customer);
        request.setAttribute("searchedPhone", phone);
        if (customer == null) {
            request.setAttribute("customerMessage", "Không tìm thấy khách hàng. Bán vé lẻ?");
        }
        request.getRequestDispatcher("/sell-ticket.jsp").forward(request, response);
    }
}