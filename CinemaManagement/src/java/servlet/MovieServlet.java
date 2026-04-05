package servlet;

import dao.MovieDAO;
import dao.ShowtimeDAO;
import model.Movie;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

public class MovieServlet extends HttpServlet {
    private MovieDAO    movieDAO    = new MovieDAO();
    private ShowtimeDAO showtimeDAO = new ShowtimeDAO();

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
            case "list":   listMovies(request, response);           break;
            case "search": searchMovies(request, response);         break;
            case "add":    showAddForm(request, response);          break;
            case "edit":   showEditForm(request, response);         break;
            case "toggle": toggleMovieStatus(request, response);    break;
            case "delete": toggleMovieStatus(request, response);    break; // backward compat
            default:       listMovies(request, response);
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
            createMovie(request, response);
        } else if ("update".equals(action)) {
            updateMovie(request, response);
        }
    }

    private boolean isLoggedIn(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && session.getAttribute("loggedIn") != null;
    }

    private void listMovies(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Movie> movies = movieDAO.getAllMovies();
        request.setAttribute("movies", movies);
        request.getRequestDispatcher("/movies.jsp").forward(request, response);
    }

    private void searchMovies(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String keyword = request.getParameter("keyword");
        List<Movie> movies = (keyword == null || keyword.trim().isEmpty())
            ? movieDAO.getAllMovies()
            : movieDAO.searchMoviesByName(keyword);
        request.setAttribute("movies", movies);
        request.setAttribute("keyword", keyword);
        request.getRequestDispatcher("/movies.jsp").forward(request, response);
    }

    private void showAddForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<String> allGenres = movieDAO.getAllGenres();
        request.setAttribute("allGenres", allGenres);
        request.getRequestDispatcher("/movie-form.jsp").forward(request, response);
    }

    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int movieId = Integer.parseInt(request.getParameter("movieId"));
        Movie movie = movieDAO.getMovieById(movieId);
        List<String> allGenres = movieDAO.getAllGenres();
        request.setAttribute("movie", movie);
        request.setAttribute("allGenres", allGenres);
        request.getRequestDispatcher("/movie-form.jsp").forward(request, response);
    }

    private void createMovie(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Movie movie = extractMovieFromRequest(request);
            String[] genreNames = request.getParameterValues("genres");
            List<Integer> genreIds = new ArrayList<>();
            if (genreNames != null) {
                genreIds = movieDAO.getOrCreateGenreIds(Arrays.asList(genreNames));
            }
            boolean success = movieDAO.addMovie(movie, genreIds);
            if (success) {
                request.getSession().setAttribute("message",
                    "✅ Thêm phim \"" + movie.getMovieName() + "\" thành công!");
                request.getSession().setAttribute("messageType", "success");
            } else {
                request.getSession().setAttribute("message", "❌ Thêm phim thất bại!");
                request.getSession().setAttribute("messageType", "error");
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.getSession().setAttribute("message", "❌ Lỗi: " + e.getMessage());
            request.getSession().setAttribute("messageType", "error");
        }
        response.sendRedirect(request.getContextPath() + "/movies");
    }

    private void updateMovie(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int movieId = Integer.parseInt(request.getParameter("movieId"));
            Movie movie = extractMovieFromRequest(request);
            movie.setMovieId(movieId);
            String[] genreNames = request.getParameterValues("genres");
            List<Integer> genreIds = new ArrayList<>();
            if (genreNames != null) {
                genreIds = movieDAO.getOrCreateGenreIds(Arrays.asList(genreNames));
            }
            boolean success = movieDAO.updateMovieWithGenres(movie, genreIds);
            if (success) {
                request.getSession().setAttribute("message",
                    "✅ Cập nhật phim \"" + movie.getMovieName() + "\" thành công!");
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
        response.sendRedirect(request.getContextPath() + "/movies");
    }

    // Toggle Active <-> Inactive, đồng thời hủy suất chiếu khi ngừng phim
    private void toggleMovieStatus(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int movieId = Integer.parseInt(request.getParameter("movieId"));
            Movie movie = movieDAO.getMovieById(movieId);

            if (movie == null) {
                request.getSession().setAttribute("message", "❌ Không tìm thấy phim!");
                request.getSession().setAttribute("messageType", "error");
                response.sendRedirect(request.getContextPath() + "/movies");
                return;
            }

            String newStatus = "Active".equals(movie.getStatus()) ? "Inactive" : "Active";
            movie.setStatus(newStatus);

            boolean success = movieDAO.updateMovie(movie);
            if (success) {
                String msg;
                if ("Active".equals(newStatus)) {
                    msg = "✅ Đã kích hoạt phim \"" + movie.getMovieName() + "\"!";
                } else {
                    // Tự động hủy tất cả suất Scheduled của phim này
                    int cancelledCount = showtimeDAO.cancelScheduledShowtimesByMovie(movieId);
                    msg = "🚫 Đã ngừng chiếu phim \"" + movie.getMovieName() + "\"!";
                    if (cancelledCount > 0) {
                        msg += " Đã tự động hủy " + cancelledCount
                             + " suất chiếu liên quan.";
                    }
                }
                request.getSession().setAttribute("message", msg);
                request.getSession().setAttribute("messageType", "success");
            } else {
                request.getSession().setAttribute("message",
                    "❌ Không thể thay đổi trạng thái phim!");
                request.getSession().setAttribute("messageType", "error");
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.getSession().setAttribute("message", "❌ Lỗi: " + e.getMessage());
            request.getSession().setAttribute("messageType", "error");
        }
        response.sendRedirect(request.getContextPath() + "/movies");
    }

    private Movie extractMovieFromRequest(HttpServletRequest request) {
        Movie movie = new Movie();
        movie.setMovieName(request.getParameter("movieName").trim());
        movie.setDuration(Integer.parseInt(request.getParameter("duration")));
        movie.setCountry(request.getParameter("country").trim());
        movie.setReleaseYear(Integer.parseInt(request.getParameter("releaseYear")));
        movie.setAgeRestriction(Integer.parseInt(request.getParameter("ageRestriction")));
        movie.setDirector(request.getParameter("director").trim());
        String mainActors = request.getParameter("mainActors");
        movie.setMainActors(mainActors != null ? mainActors.trim() : "");
        movie.setStatus(request.getParameter("status"));
        return movie;
    }
}