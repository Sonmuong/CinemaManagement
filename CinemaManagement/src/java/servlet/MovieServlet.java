package servlet;

import dao.MovieDAO;
import model.Movie;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

@WebServlet("/movies")
public class MovieServlet extends HttpServlet {
    private MovieDAO movieDAO = new MovieDAO();
    
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
                listMovies(request, response);
                break;
            case "search":
                searchMovies(request, response);
                break;
            default:
                listMovies(request, response);
        }
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
        List<Movie> movies = movieDAO.searchMoviesByName(keyword);
        request.setAttribute("movies", movies);
        request.setAttribute("keyword", keyword);
        request.getRequestDispatcher("/movies.jsp").forward(request, response);
    }
}