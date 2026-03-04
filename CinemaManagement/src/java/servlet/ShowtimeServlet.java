package servlet;

import dao.ShowtimeDAO;
import dao.MovieDAO;
import dao.RoomDAO;
import model.Showtime;
import model.Movie;
import model.Room;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.Date;
import java.sql.Time;
import java.util.List;

public class ShowtimeServlet extends HttpServlet {
    private ShowtimeDAO showtimeDAO = new ShowtimeDAO();
    private MovieDAO movieDAO = new MovieDAO();
    private RoomDAO roomDAO = new RoomDAO();
    
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
                listShowtimes(request, response);
                break;
            case "add":
                showAddForm(request, response);
                break;
            case "date":
                showByDate(request, response);
                break;
            case "movie":
                showByMovie(request, response);
                break;
            default:
                listShowtimes(request, response);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        
        String action = request.getParameter("action");
        
        if ("create".equals(action)) {
            createShowtime(request, response);
        } else if ("cancel".equals(action)) {
            cancelShowtime(request, response);
        }
    }
    
    private void listShowtimes(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        List<Showtime> showtimes = showtimeDAO.getAllShowtimes();
        request.setAttribute("showtimes", showtimes);
        request.getRequestDispatcher("/showtimes.jsp").forward(request, response);
    }
    
    private void showAddForm(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        List<Movie> movies = movieDAO.getActiveMovies();
        List<Room> rooms = roomDAO.getAllRooms();
        
        request.setAttribute("movies", movies);
        request.setAttribute("rooms", rooms);
        request.getRequestDispatcher("/add-showtime.jsp").forward(request, response);
    }
    
    private void createShowtime(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        try {
            int movieId = Integer.parseInt(request.getParameter("movieId"));
            int roomId = Integer.parseInt(request.getParameter("roomId"));
            Date showDate = Date.valueOf(request.getParameter("showDate"));
            Time showTime = Time.valueOf(request.getParameter("showTime") + ":00");
            double ticketPrice = Double.parseDouble(request.getParameter("ticketPrice"));
            
            Showtime showtime = new Showtime();
            showtime.setMovieId(movieId);
            showtime.setRoomId(roomId);
            showtime.setShowDate(showDate);
            showtime.setShowTime(showTime);
            showtime.setTicketPrice(ticketPrice);
            showtime.setStatus("Scheduled");
            
            boolean success = showtimeDAO.addShowtime(showtime);
            
            if (success) {
                request.getSession().setAttribute("message", "Thêm suất chiếu thành công!");
                request.getSession().setAttribute("messageType", "success");
            } else {
                request.getSession().setAttribute("message", "Phòng đã có lịch chiếu khác trong thời gian này!");
                request.getSession().setAttribute("messageType", "error");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            request.getSession().setAttribute("message", "Lỗi: " + e.getMessage());
            request.getSession().setAttribute("messageType", "error");
        }
        
        response.sendRedirect(request.getContextPath() + "/showtimes");
    }
    
    private void showByDate(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        String dateStr = request.getParameter("date");
        Date date = Date.valueOf(dateStr);
        
        List<Showtime> showtimes = showtimeDAO.getShowtimesByDate(date);
        request.setAttribute("showtimes", showtimes);
        request.setAttribute("selectedDate", dateStr);
        request.getRequestDispatcher("/showtimes.jsp").forward(request, response);
    }
    
    private void showByMovie(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        int movieId = Integer.parseInt(request.getParameter("movieId"));
        
        List<Showtime> showtimes = showtimeDAO.getShowtimesByMovie(movieId);
        Movie movie = movieDAO.getMovieById(movieId);
        
        request.setAttribute("showtimes", showtimes);
        request.setAttribute("selectedMovie", movie);
        request.getRequestDispatcher("/showtimes.jsp").forward(request, response);
    }
    
    private void cancelShowtime(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        int showtimeId = Integer.parseInt(request.getParameter("showtimeId"));
        
        boolean success = showtimeDAO.cancelShowtime(showtimeId);
        
        if (success) {
            request.getSession().setAttribute("message", "Hủy suất chiếu thành công!");
            request.getSession().setAttribute("messageType", "success");
        } else {
            request.getSession().setAttribute("message", "Không thể hủy suất chiếu!");
            request.getSession().setAttribute("messageType", "error");
        }
        
        response.sendRedirect(request.getContextPath() + "/showtimes");
    }
}