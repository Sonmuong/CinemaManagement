package servlet;

import dao.ShowtimeDAO;
import dao.MovieDAO;
import dao.RoomDAO;
import model.Showtime;
import model.Movie;
import model.Room;
import jakarta.servlet.ServletException;
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

        if (!isLoggedIn(request)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String action = request.getParameter("action");
        if (action == null) action = "list";

        switch (action) {
            case "list":   listShowtimes(request, response);  break;
            case "add":    showAddForm(request, response);    break;
            case "edit":   showEditForm(request, response);   break;
            case "date":   showByDate(request, response);     break;
            case "movie":  showByMovie(request, response);    break;
            default:       listShowtimes(request, response);
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
            case "create": createShowtime(request, response);  break;
            case "cancel": cancelShowtime(request, response);  break;
            case "delete": deleteShowtime(request, response);  break;
            case "update": updateShowtime(request, response);  break;
            default:       listShowtimes(request, response);
        }
    }

    private boolean isLoggedIn(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && session.getAttribute("loggedIn") != null;
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

    // FIX: Thêm action edit để sửa suất chiếu đã hủy
    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String showtimeIdStr = request.getParameter("showtimeId");
        if (showtimeIdStr == null) {
            response.sendRedirect(request.getContextPath() + "/showtimes");
            return;
        }
        int showtimeId = Integer.parseInt(showtimeIdStr);
        Showtime showtime = showtimeDAO.getShowtimeById(showtimeId);
        List<Movie> movies = movieDAO.getAllMovies();
        List<Room> rooms = roomDAO.getAllRooms();
        request.setAttribute("showtime", showtime);
        request.setAttribute("movies", movies);
        request.setAttribute("rooms", rooms);
        request.getRequestDispatcher("/edit-showtime.jsp").forward(request, response);
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
                request.getSession().setAttribute("message", "✅ Thêm suất chiếu thành công!");
                request.getSession().setAttribute("messageType", "success");
            } else {
                request.getSession().setAttribute("message", "❌ Phòng đã có lịch chiếu khác trong thời gian này! (Cần 30 phút dọn dẹp giữa các suất)");
                request.getSession().setAttribute("messageType", "error");
            }

        } catch (Exception e) {
            e.printStackTrace();
            request.getSession().setAttribute("message", "❌ Lỗi: " + e.getMessage());
            request.getSession().setAttribute("messageType", "error");
        }

        response.sendRedirect(request.getContextPath() + "/showtimes");
    }

   private void showByDate(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
    String dateStr = request.getParameter("date");
    if (dateStr == null || dateStr.trim().isEmpty()) {
        listShowtimes(request, response);
        return;
    }
    try {
        Date date = Date.valueOf(dateStr.trim());
        List<Showtime> showtimes = showtimeDAO.getShowtimesByDate(date);
        request.setAttribute("showtimes", showtimes);
        request.setAttribute("selectedDate", dateStr.trim());
        request.getRequestDispatcher("/showtimes.jsp").forward(request, response);
    } catch (IllegalArgumentException e) {
        // Ngày không hợp lệ → hiện tất cả
        request.getSession().setAttribute("message", "❌ Định dạng ngày không hợp lệ!");
        request.getSession().setAttribute("messageType", "error");
        response.sendRedirect(request.getContextPath() + "/showtimes");
    }
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
            request.getSession().setAttribute("message", "✅ Hủy suất chiếu thành công!");
            request.getSession().setAttribute("messageType", "success");
        } else {
            request.getSession().setAttribute("message", "❌ Không thể hủy suất chiếu!");
            request.getSession().setAttribute("messageType", "error");
        }

        response.sendRedirect(request.getContextPath() + "/showtimes");
    }

    // FIX: Thêm action xóa vĩnh viễn suất chiếu đã hủy
    private void deleteShowtime(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int showtimeId = Integer.parseInt(request.getParameter("showtimeId"));
        boolean success = showtimeDAO.deleteShowtime(showtimeId);

        if (success) {
            request.getSession().setAttribute("message", "🗑️ Đã xóa suất chiếu #" + showtimeId + "!");
            request.getSession().setAttribute("messageType", "success");
        } else {
            request.getSession().setAttribute("message", "❌ Không thể xóa suất chiếu (có thể còn vé liên quan)!");
            request.getSession().setAttribute("messageType", "error");
        }

        response.sendRedirect(request.getContextPath() + "/showtimes");
    }

    // FIX: Thêm action cập nhật suất chiếu
    private void updateShowtime(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int showtimeId = Integer.parseInt(request.getParameter("showtimeId"));
            int movieId = Integer.parseInt(request.getParameter("movieId"));
            int roomId = Integer.parseInt(request.getParameter("roomId"));
            Date showDate = Date.valueOf(request.getParameter("showDate"));
            Time showTime = Time.valueOf(request.getParameter("showTime") + ":00");
            double ticketPrice = Double.parseDouble(request.getParameter("ticketPrice"));
            String status = request.getParameter("status");

            Showtime showtime = new Showtime();
            showtime.setShowtimeId(showtimeId);
            showtime.setMovieId(movieId);
            showtime.setRoomId(roomId);
            showtime.setShowDate(showDate);
            showtime.setShowTime(showTime);
            showtime.setTicketPrice(ticketPrice);
            showtime.setStatus(status != null ? status : "Scheduled");

            boolean success = showtimeDAO.updateShowtime(showtime);
            if (success) {
                request.getSession().setAttribute("message", "✅ Cập nhật suất chiếu thành công!");
                request.getSession().setAttribute("messageType", "success");
            } else {
                request.getSession().setAttribute("message", "❌ Cập nhật thất bại!");
                request.getSession().setAttribute("messageType", "error");
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.getSession().setAttribute("message", "❌ Lỗi: " + e.getMessage());
            request.getSession().setAttribute("messageType", "error");
        }
        response.sendRedirect(request.getContextPath() + "/showtimes");
    }
}