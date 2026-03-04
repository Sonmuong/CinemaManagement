package servlet;

import dao.TicketDAO;
import dao.ShowtimeDAO;
import dao.CustomerDAO;
import model.Ticket;
import model.Showtime;
import model.Customer;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;


public class TicketServlet extends HttpServlet {
    private TicketDAO ticketDAO = new TicketDAO();
    private ShowtimeDAO showtimeDAO = new ShowtimeDAO();
    private CustomerDAO customerDAO = new CustomerDAO();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        
        String action = request.getParameter("action");
        
        if (action == null) {
            action = "list";
        }
        
        switch (action) {
            case "list":
                listTickets(request, response);
                break;
            case "sell":
                showSellForm(request, response);
                break;
            default:
                listTickets(request, response);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        
        String action = request.getParameter("action");
        
        if ("create".equals(action)) {
            sellTicket(request, response);
        } else if ("cancel".equals(action)) {
            cancelTicket(request, response);
        } else if ("findCustomer".equals(action)) {
            findCustomer(request, response);
        }
    }
    
    private void listTickets(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        List<Ticket> tickets = ticketDAO.getAllTickets();
        request.setAttribute("tickets", tickets);
        request.getRequestDispatcher("/tickets.jsp").forward(request, response);
    }
    
    private void showSellForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        int showtimeId = Integer.parseInt(request.getParameter("showtimeId"));
        
        Showtime showtime = showtimeDAO.getShowtimeById(showtimeId);
        List<String> bookedSeats = showtimeDAO.getBookedSeats(showtimeId);
        
        request.setAttribute("showtime", showtime);
        request.setAttribute("bookedSeats", bookedSeats);
        request.getRequestDispatcher("/sell-ticket.jsp").forward(request, response);
    }
    
    private void sellTicket(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            int showtimeId = Integer.parseInt(request.getParameter("showtimeId"));
            String seatNumber = request.getParameter("seatNumber");
            String ticketType = request.getParameter("ticketType");
            double ticketPrice = Double.parseDouble(request.getParameter("ticketPrice"));
            
            Integer customerId = null;
            String customerIdStr = request.getParameter("customerId");
            if (customerIdStr != null && !customerIdStr.isEmpty()) {
                customerId = Integer.parseInt(customerIdStr);
            }
            
            boolean usePoints = "true".equals(request.getParameter("usePoints"));
            
            Ticket ticket = new Ticket();
            ticket.setShowtimeId(showtimeId);
            ticket.setSeatNumber(seatNumber);
            ticket.setTicketType(ticketType);
            ticket.setTicketPrice(ticketPrice);
            ticket.setCustomerId(customerId);
            
            int ticketId = ticketDAO.sellTicket(ticket, usePoints);
            
            if (ticketId > 0) {
                request.getSession().setAttribute("message", "Bán vé thành công! Mã vé: " + ticketId);
                request.getSession().setAttribute("messageType", "success");
                
                Ticket soldTicket = ticketDAO.getTicketById(ticketId);
                request.getSession().setAttribute("soldTicket", soldTicket);
            } else {
                request.getSession().setAttribute("message", "Bán vé thất bại! Ghế có thể đã được đặt.");
                request.getSession().setAttribute("messageType", "error");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            request.getSession().setAttribute("message", "Lỗi: " + e.getMessage());
            request.getSession().setAttribute("messageType", "error");
        }
        
        response.sendRedirect(request.getContextPath() + "/tickets");
    }
    
    private void cancelTicket(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            int ticketId = Integer.parseInt(request.getParameter("ticketId"));
            
            boolean success = ticketDAO.cancelTicket(ticketId);
            
            if (success) {
                request.getSession().setAttribute("message", "Hủy vé thành công!");
                request.getSession().setAttribute("messageType", "success");
            } else {
                request.getSession().setAttribute("message", "Không thể hủy vé!");
                request.getSession().setAttribute("messageType", "error");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            request.getSession().setAttribute("message", "Lỗi: " + e.getMessage());
            request.getSession().setAttribute("messageType", "error");
        }
        
        response.sendRedirect(request.getContextPath() + "/tickets");
    }
    
    private void findCustomer(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String phone = request.getParameter("phone");
        int showtimeId = Integer.parseInt(request.getParameter("showtimeId"));
        
        Customer customer = customerDAO.getCustomerByPhone(phone);
        Showtime showtime = showtimeDAO.getShowtimeById(showtimeId);
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