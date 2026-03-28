package servlet;

import dao.MovieDAO;
import model.Movie;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

public class MovieServlet extends HttpServlet {
    private MovieDAO movieDAO = new MovieDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");
        if (action == null) action = "list";

        switch (action) {
            case "list":   listMovies(request, response);   break;
            case "search": searchMovies(request, response); break;
            case "add":    showAddForm(request, response);  break;
            case "edit":   showEditForm(request, response); break;
            case "delete": deleteMovie(request, response);  break;
            default:       listMovies(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");
        if ("create".equals(action)) {
            createMovie(request, response);
        } else if ("update".equals(action)) {
            updateMovie(request, response);
        }
    }

    // ── Danh sách phim ───────────────────────────────────────────
    private void listMovies(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Movie> movies = movieDAO.getAllMovies();
        request.setAttribute("movies", movies);
        request.getRequestDispatcher("/movies.jsp").forward(request, response);
    }

    // ── Tìm kiếm ─────────────────────────────────────────────────
    private void searchMovies(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String keyword = request.getParameter("keyword");
        List<Movie> movies = movieDAO.searchMoviesByName(keyword);
        request.setAttribute("movies", movies);
        request.setAttribute("keyword", keyword);
        request.getRequestDispatcher("/movies.jsp").forward(request, response);
    }

    // ── Form thêm phim ────────────────────────────────────────────
    private void showAddForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<String> allGenres = movieDAO.getAllGenres();
        request.setAttribute("allGenres", allGenres);
        request.getRequestDispatcher("/movie-form.jsp").forward(request, response);
    }

    // ── Form sửa phim ─────────────────────────────────────────────
    private void showEditForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int movieId = Integer.parseInt(request.getParameter("movieId"));
        Movie movie = movieDAO.getMovieById(movieId);
        List<String> allGenres = movieDAO.getAllGenres();
        request.setAttribute("movie", movie);
        request.setAttribute("allGenres", allGenres);
        request.getRequestDispatcher("/movie-form.jsp").forward(request, response);
    }

    // ── Tạo phim mới ─────────────────────────────────────────────
    private void createMovie(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Movie movie = extractMovieFromRequest(request);

            // Xử lý thể loại
            String[] genreNames = request.getParameterValues("genres");
            List<Integer> genreIds = new ArrayList<>();
            if (genreNames != null) {
                genreIds = movieDAO.getOrCreateGenreIds(Arrays.asList(genreNames));
            }

            boolean success = movieDAO.addMovie(movie, genreIds);
            if (success) {
                request.getSession().setAttribute("message", "✅ Thêm phim \"" + movie.getMovieName() + "\" thành công!");
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

    // ── Cập nhật phim ────────────────────────────────────────────
    private void updateMovie(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int movieId = Integer.parseInt(request.getParameter("movieId"));
            Movie movie = extractMovieFromRequest(request);
            movie.setMovieId(movieId);

            // Xử lý thể loại
            String[] genreNames = request.getParameterValues("genres");
            List<Integer> genreIds = new ArrayList<>();
            if (genreNames != null) {
                genreIds = movieDAO.getOrCreateGenreIds(Arrays.asList(genreNames));
            }

            boolean success = movieDAO.updateMovieWithGenres(movie, genreIds);
            if (success) {
                request.getSession().setAttribute("message", "✅ Cập nhật phim \"" + movie.getMovieName() + "\" thành công!");
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

    // ── Xóa phim (chuyển sang Inactive) ──────────────────────────
    private void deleteMovie(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int movieId = Integer.parseInt(request.getParameter("movieId"));
            boolean success = movieDAO.deleteMovie(movieId);
            request.getSession().setAttribute("message",
                success ? "✅ Đã ngừng chiếu phim!" : "❌ Không thể ngừng chiếu!");
            request.getSession().setAttribute("messageType", success ? "success" : "error");
        } catch (Exception e) {
            e.printStackTrace();
            request.getSession().setAttribute("message", "❌ Lỗi: " + e.getMessage());
            request.getSession().setAttribute("messageType", "error");
        }
        response.sendRedirect(request.getContextPath() + "/movies");
    }

    // ── Helper: đọc dữ liệu phim từ form ─────────────────────────
    private Movie extractMovieFromRequest(HttpServletRequest request) {
        Movie movie = new Movie();
        movie.setMovieName(request.getParameter("movieName").trim());
        movie.setDuration(Integer.parseInt(request.getParameter("duration")));
        movie.setCountry(request.getParameter("country").trim());
        movie.setReleaseYear(Integer.parseInt(request.getParameter("releaseYear")));
        movie.setAgeRestriction(Integer.parseInt(request.getParameter("ageRestriction")));
        movie.setDirector(request.getParameter("director").trim());
        movie.setMainActors(request.getParameter("mainActors").trim());
        movie.setStatus(request.getParameter("status"));
        return movie;
    }
}