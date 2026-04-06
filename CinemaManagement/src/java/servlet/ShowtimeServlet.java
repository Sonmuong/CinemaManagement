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
    private MovieDAO    movieDAO    = new MovieDAO();
    private RoomDAO     roomDAO     = new RoomDAO();

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
            case "list":  listShowtimes(request, response); break;
            case "add":   showAddForm(request, response);   break;
            case "edit":  showEditForm(request, response);  break;
            case "date":  showByDate(request, response);    break;
            case "movie": showByMovie(request, response);   break;
            default:      listShowtimes(request, response);
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
            case "create": createShowtime(request, response); break;
            case "cancel": cancelShowtime(request, response); break;
            case "delete": deleteShowtime(request, response); break;
            case "update": updateShowtime(request, response); break;
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
        List<Room>  rooms  = roomDAO.getAllRooms();
        request.setAttribute("movies", movies);
        request.setAttribute("rooms",  rooms);
        request.getRequestDispatcher("/add-showtime.jsp").forward(request, response);
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String showtimeIdStr = request.getParameter("showtimeId");
        if (showtimeIdStr == null) {
            response.sendRedirect(request.getContextPath() + "/showtimes"); return;
        }
        int showtimeId    = Integer.parseInt(showtimeIdStr);
        Showtime showtime = showtimeDAO.getShowtimeById(showtimeId);

        if (showtime == null) {
            request.getSession().setAttribute("message", "❌ Không tìm thấy suất chiếu!");
            request.getSession().setAttribute("messageType", "error");
            response.sendRedirect(request.getContextPath() + "/showtimes");
            return;
        }

        List<Movie> movies = movieDAO.getAllMovies();
        List<Room>  rooms  = roomDAO.getAllRooms();
        request.setAttribute("showtime", showtime);
        request.setAttribute("movies",   movies);
        request.setAttribute("rooms",    rooms);
        request.getRequestDispatcher("/edit-showtime.jsp").forward(request, response);
    }

    private void createShowtime(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int    movieId    = Integer.parseInt(request.getParameter("movieId"));
            int    roomId     = Integer.parseInt(request.getParameter("roomId"));
            Date   showDate   = Date.valueOf(request.getParameter("showDate"));
            Time   showTime   = Time.valueOf(request.getParameter("showTime") + ":00");
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
                request.getSession().setAttribute("message",
                    "❌ Phòng đã có lịch chiếu khác trong thời gian này! (Cần 30 phút dọn dẹp giữa các suất)");
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
            request.setAttribute("showtimes",    showtimes);
            request.setAttribute("selectedDate", dateStr.trim());
            request.getRequestDispatcher("/showtimes.jsp").forward(request, response);
        } catch (IllegalArgumentException e) {
            request.getSession().setAttribute("message", "❌ Ngày không hợp lệ: " + dateStr);
            request.getSession().setAttribute("messageType", "error");
            response.sendRedirect(request.getContextPath() + "/showtimes");
        }
    }

    private void showByMovie(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int movieId = Integer.parseInt(request.getParameter("movieId"));
        List<Showtime> showtimes = showtimeDAO.getShowtimesByMovie(movieId);
        Movie movie = movieDAO.getMovieById(movieId);
        request.setAttribute("showtimes",     showtimes);
        request.setAttribute("selectedMovie", movie);
        request.getRequestDispatcher("/showtimes.jsp").forward(request, response);
    }

    /**
     * Hủy suất chiếu — tự động hủy vé trước, sau đó hủy suất.
     * Thông báo rõ số vé bị hủy và số tiền hoàn (nếu có).
     */
    private void cancelShowtime(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int showtimeId = Integer.parseInt(request.getParameter("showtimeId"));
            ShowtimeDAO.CancelShowtimeResult result = showtimeDAO.cancelShowtime(showtimeId);

            if (result.success) {
                StringBuilder msg = new StringBuilder("✅ Hủy suất chiếu #" + showtimeId + " thành công!");
                if (result.ticketsCancelled > 0) {
                    msg.append(" Đã hủy ").append(result.ticketsCancelled).append(" vé liên quan.");
                    if (result.ticketsRefunded > 0) {
                        String refundStr = String.format("%,.0f", result.totalRefundAmount).replace(",", ".");
                        msg.append(" Cần hoàn tiền cho ")
                           .append(result.ticketsRefunded)
                           .append(" vé: ").append(refundStr).append(" đ (80% giá vé).");
                    } else {
                        msg.append(" Không hoàn tiền (chiếu trong vòng 2 giờ).");
                    }
                }
                request.getSession().setAttribute("message", msg.toString());
                request.getSession().setAttribute("messageType", "success");
            } else {
                request.getSession().setAttribute("message", "❌ Không thể hủy suất chiếu! Vui lòng thử lại.");
                request.getSession().setAttribute("messageType", "error");
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.getSession().setAttribute("message", "❌ Lỗi: " + e.getMessage());
            request.getSession().setAttribute("messageType", "error");
        }
        response.sendRedirect(request.getContextPath() + "/showtimes");
    }

    private void deleteShowtime(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int showtimeId = Integer.parseInt(request.getParameter("showtimeId"));
            boolean success = showtimeDAO.deleteShowtime(showtimeId);
            request.getSession().setAttribute("message",
                success ? "🗑️ Đã xóa suất chiếu #" + showtimeId + "!"
                        : "❌ Không thể xóa suất chiếu (còn vé đã thanh toán chưa được hủy)!");
            request.getSession().setAttribute("messageType", success ? "success" : "error");
        } catch (Exception e) {
            e.printStackTrace();
            request.getSession().setAttribute("message", "❌ Lỗi: " + e.getMessage());
            request.getSession().setAttribute("messageType", "error");
        }
        response.sendRedirect(request.getContextPath() + "/showtimes");
    }

    /**
     * Kích hoạt lại suất chiếu đã hủy.
     * Kiểm tra phim có đang Active không trước khi cho kích hoạt.
     */
    private void updateShowtime(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int    showtimeId  = Integer.parseInt(request.getParameter("showtimeId"));
            int    movieId     = Integer.parseInt(request.getParameter("movieId"));
            int    roomId      = Integer.parseInt(request.getParameter("roomId"));
            Date   showDate    = Date.valueOf(request.getParameter("showDate"));
            Time   showTime    = Time.valueOf(request.getParameter("showTime") + ":00");
            double ticketPrice = Double.parseDouble(request.getParameter("ticketPrice"));
            String status      = request.getParameter("status");

            Showtime showtime = new Showtime();
            showtime.setShowtimeId(showtimeId);
            showtime.setMovieId(movieId);
            showtime.setRoomId(roomId);
            showtime.setShowDate(showDate);
            showtime.setShowTime(showTime);
            showtime.setTicketPrice(ticketPrice);
            showtime.setStatus(status != null ? status : "Scheduled");

            // Nếu đang kích hoạt lại (status = Scheduled), kiểm tra phim
            if ("Scheduled".equals(showtime.getStatus())) {
                String activateResult = showtimeDAO.activateShowtime(showtime);
                switch (activateResult) {
                    case "OK":
                        request.getSession().setAttribute("message",
                            "✅ Kích hoạt suất chiếu #" + showtimeId + " thành công!");
                        request.getSession().setAttribute("messageType", "success");
                        break;
                    case "ERROR_MOVIE_INACTIVE":
                        // Lấy tên phim để thông báo rõ hơn
                        Movie movie = movieDAO.getMovieById(movieId);
                        String movieName = movie != null ? movie.getMovieName() : "ID " + movieId;
                        request.getSession().setAttribute("message",
                            "❌ Không thể kích hoạt! Phim \"" + movieName + "\" hiện đang NGỪNG CHIẾU. " +
                            "Vui lòng kích hoạt lại phim trước, sau đó mới kích hoạt suất chiếu này.");
                        request.getSession().setAttribute("messageType", "error");
                        break;
                    case "ERROR_MOVIE_NOT_FOUND":
                        request.getSession().setAttribute("message",
                            "❌ Không tìm thấy phim! Vui lòng kiểm tra lại.");
                        request.getSession().setAttribute("messageType", "error");
                        break;
                    default:
                        request.getSession().setAttribute("message",
                            "❌ Kích hoạt thất bại! Vui lòng thử lại.");
                        request.getSession().setAttribute("messageType", "error");
                }
            } else {
                // Update thông thường (không phải kích hoạt)
                boolean success = showtimeDAO.updateShowtime(showtime);
                request.getSession().setAttribute("message",
                    success ? "✅ Cập nhật suất chiếu thành công!" : "❌ Cập nhật thất bại!");
                request.getSession().setAttribute("messageType", success ? "success" : "error");
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.getSession().setAttribute("message", "❌ Lỗi: " + e.getMessage());
            request.getSession().setAttribute("messageType", "error");
        }
        response.sendRedirect(request.getContextPath() + "/showtimes");
    }
}