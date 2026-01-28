package dao;

import model.Movie;
import util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MovieDAO {
    
    // Lấy tất cả phim
    public List<Movie> getAllMovies() {
        List<Movie> movies = new ArrayList<>();
        String sql = "SELECT * FROM Movies ORDER BY created_date DESC";
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Movie movie = extractMovieFromResultSet(rs);
                movies.add(movie);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return movies;
    }
    
    // Lấy phim đang chiếu
    public List<Movie> getActiveMovies() {
        List<Movie> movies = new ArrayList<>();
        String sql = "SELECT * FROM Movies WHERE status = 'Active' ORDER BY movie_name";
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Movie movie = extractMovieFromResultSet(rs);
                movies.add(movie);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return movies;
    }
    
    // Lấy phim theo ID
    public Movie getMovieById(int movieId) {
        String sql = "SELECT * FROM Movies WHERE movie_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, movieId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return extractMovieFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // Thêm phim mới
    public boolean addMovie(Movie movie, List<Integer> genreIds) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            
            // Thêm phim
            String sql = "INSERT INTO Movies (movie_name, duration, country, release_year, " +
                        "age_restriction, director, main_actors, status) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, movie.getMovieName());
            pstmt.setInt(2, movie.getDuration());
            pstmt.setString(3, movie.getCountry());
            pstmt.setInt(4, movie.getReleaseYear());
            pstmt.setInt(5, movie.getAgeRestriction());
            pstmt.setString(6, movie.getDirector());
            pstmt.setString(7, movie.getMainActors());
            pstmt.setString(8, movie.getStatus());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    int movieId = rs.getInt(1);
                    
                    // Thêm thể loại cho phim
                    if (genreIds != null && !genreIds.isEmpty()) {
                        String genreSql = "INSERT INTO MovieGenres (movie_id, genre_id) VALUES (?, ?)";
                        PreparedStatement genrePstmt = conn.prepareStatement(genreSql);
                        
                        for (Integer genreId : genreIds) {
                            genrePstmt.setInt(1, movieId);
                            genrePstmt.setInt(2, genreId);
                            genrePstmt.addBatch();
                        }
                        genrePstmt.executeBatch();
                    }
                }
            }
            
            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    // Cập nhật phim
    public boolean updateMovie(Movie movie) {
        String sql = "UPDATE Movies SET movie_name = ?, duration = ?, country = ?, " +
                    "release_year = ?, age_restriction = ?, director = ?, main_actors = ?, " +
                    "status = ? WHERE movie_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, movie.getMovieName());
            pstmt.setInt(2, movie.getDuration());
            pstmt.setString(3, movie.getCountry());
            pstmt.setInt(4, movie.getReleaseYear());
            pstmt.setInt(5, movie.getAgeRestriction());
            pstmt.setString(6, movie.getDirector());
            pstmt.setString(7, movie.getMainActors());
            pstmt.setString(8, movie.getStatus());
            pstmt.setInt(9, movie.getMovieId());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Xóa phim (chuyển sang Inactive)
    public boolean deleteMovie(int movieId) {
        String sql = "UPDATE Movies SET status = 'Inactive' WHERE movie_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, movieId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Tìm kiếm phim theo tên
    public List<Movie> searchMoviesByName(String keyword) {
        List<Movie> movies = new ArrayList<>();
        String sql = "SELECT * FROM Movies WHERE movie_name LIKE ? ORDER BY movie_name";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, "%" + keyword + "%");
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Movie movie = extractMovieFromResultSet(rs);
                movies.add(movie);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return movies;
    }
    
    // Lấy thể loại của phim
    public List<String> getMovieGenres(int movieId) {
        List<String> genres = new ArrayList<>();
        String sql = "SELECT g.genre_name FROM Genres g " +
                    "INNER JOIN MovieGenres mg ON g.genre_id = mg.genre_id " +
                    "WHERE mg.movie_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, movieId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                genres.add(rs.getString("genre_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return genres;
    }
    
    // Helper method
    private Movie extractMovieFromResultSet(ResultSet rs) throws SQLException {
        Movie movie = new Movie();
        movie.setMovieId(rs.getInt("movie_id"));
        movie.setMovieName(rs.getString("movie_name"));
        movie.setDuration(rs.getInt("duration"));
        movie.setCountry(rs.getString("country"));
        movie.setReleaseYear(rs.getInt("release_year"));
        movie.setAgeRestriction(rs.getInt("age_restriction"));
        movie.setDirector(rs.getString("director"));
        movie.setMainActors(rs.getString("main_actors"));
        movie.setStatus(rs.getString("status"));
        movie.setCreatedDate(rs.getTimestamp("created_date"));
        
        // Lấy thể loại
        movie.setGenres(getMovieGenres(movie.getMovieId()));
        
        return movie;
    }
    
    // Test
    public static void main(String[] args) {
        MovieDAO dao = new MovieDAO();
        
        System.out.println("=== TEST MOVIE DAO ===");
        
        // Test lấy tất cả phim
        List<Movie> movies = dao.getAllMovies();
        System.out.println("Tổng số phim: " + movies.size());
        
        // Hiển thị thông tin phim
        for (Movie m : movies) {
            System.out.println("- " + m.getMovieName() + " (" + m.getDuration() + " phút)");
            System.out.println("  Thể loại: " + String.join(", ", m.getGenres()));
        }
    }
}
