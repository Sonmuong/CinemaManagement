package dao;

import model.Movie;
import util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MovieDAO {

    // ── Lấy tất cả phim ──────────────────────────────────────────
    public List<Movie> getAllMovies() {
        List<Movie> movies = new ArrayList<>();
        String sql = "SELECT * FROM Movies ORDER BY created_date DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) movies.add(extractMovieFromResultSet(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return movies;
    }

    // ── Lấy phim đang chiếu ──────────────────────────────────────
    public List<Movie> getActiveMovies() {
        List<Movie> movies = new ArrayList<>();
        String sql = "SELECT * FROM Movies WHERE status = 'Active' ORDER BY movie_name";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) movies.add(extractMovieFromResultSet(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return movies;
    }

    // ── Lấy phim theo ID ─────────────────────────────────────────
    public Movie getMovieById(int movieId) {
        String sql = "SELECT * FROM Movies WHERE movie_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, movieId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return extractMovieFromResultSet(rs);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // ── Thêm phim mới ─────────────────────────────────────────────
    public boolean addMovie(Movie movie, List<Integer> genreIds) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            String sql = "INSERT INTO Movies (movie_name, duration, country, release_year, " +
                         "age_restriction, director, main_actors, status) VALUES (?,?,?,?,?,?,?,?)";
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, movie.getMovieName());
            ps.setInt(2, movie.getDuration());
            ps.setString(3, movie.getCountry());
            ps.setInt(4, movie.getReleaseYear());
            ps.setInt(5, movie.getAgeRestriction());
            ps.setString(6, movie.getDirector());
            ps.setString(7, movie.getMainActors());
            ps.setString(8, movie.getStatus());
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                int newId = keys.getInt(1);
                insertMovieGenres(conn, newId, genreIds);
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            rollback(conn);
            e.printStackTrace();
            return false;
        } finally { restoreAutoCommit(conn); }
    }

    // ── Cập nhật phim ────────────────────────────────────────────
    public boolean updateMovie(Movie movie) {
        String sql = "UPDATE Movies SET movie_name=?, duration=?, country=?, release_year=?, " +
                     "age_restriction=?, director=?, main_actors=?, status=? WHERE movie_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, movie.getMovieName());
            ps.setInt(2, movie.getDuration());
            ps.setString(3, movie.getCountry());
            ps.setInt(4, movie.getReleaseYear());
            ps.setInt(5, movie.getAgeRestriction());
            ps.setString(6, movie.getDirector());
            ps.setString(7, movie.getMainActors());
            ps.setString(8, movie.getStatus());
            ps.setInt(9, movie.getMovieId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ── Cập nhật phim + thể loại ─────────────────────────────────
    public boolean updateMovieWithGenres(Movie movie, List<Integer> genreIds) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            String sql = "UPDATE Movies SET movie_name=?, duration=?, country=?, release_year=?, " +
                         "age_restriction=?, director=?, main_actors=?, status=? WHERE movie_id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, movie.getMovieName());
            ps.setInt(2, movie.getDuration());
            ps.setString(3, movie.getCountry());
            ps.setInt(4, movie.getReleaseYear());
            ps.setInt(5, movie.getAgeRestriction());
            ps.setString(6, movie.getDirector());
            ps.setString(7, movie.getMainActors());
            ps.setString(8, movie.getStatus());
            ps.setInt(9, movie.getMovieId());
            ps.executeUpdate();

            // Xóa thể loại cũ rồi thêm mới
            PreparedStatement del = conn.prepareStatement(
                "DELETE FROM MovieGenres WHERE movie_id = ?");
            del.setInt(1, movie.getMovieId());
            del.executeUpdate();

            insertMovieGenres(conn, movie.getMovieId(), genreIds);

            conn.commit();
            return true;
        } catch (SQLException e) {
            rollback(conn);
            e.printStackTrace();
            return false;
        } finally { restoreAutoCommit(conn); }
    }

    // ── Ngừng chiếu phim ─────────────────────────────────────────
    public boolean deleteMovie(int movieId) {
        String sql = "UPDATE Movies SET status = 'Inactive' WHERE movie_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, movieId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ── Tìm kiếm phim ────────────────────────────────────────────
    public List<Movie> searchMoviesByName(String keyword) {
        List<Movie> movies = new ArrayList<>();
        String sql = "SELECT * FROM Movies WHERE movie_name LIKE ? ORDER BY movie_name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) movies.add(extractMovieFromResultSet(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return movies;
    }

    // ── Lấy tất cả thể loại ──────────────────────────────────────
    public List<String> getAllGenres() {
        List<String> genres = new ArrayList<>();
        String sql = "SELECT genre_name FROM Genres ORDER BY genre_name";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) genres.add(rs.getString("genre_name"));
        } catch (SQLException e) { e.printStackTrace(); }
        return genres;
    }

    // ── Lấy thể loại của một phim ────────────────────────────────
    public List<String> getMovieGenres(int movieId) {
        List<String> genres = new ArrayList<>();
        String sql = "SELECT g.genre_name FROM Genres g " +
                     "INNER JOIN MovieGenres mg ON g.genre_id = mg.genre_id " +
                     "WHERE mg.movie_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, movieId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) genres.add(rs.getString("genre_name"));
        } catch (SQLException e) { e.printStackTrace(); }
        return genres;
    }

    /**
     * Lấy hoặc tạo genre_id theo tên thể loại.
     * Dùng một connection duy nhất để tránh race condition.
     * Dùng MERGE (SQL Server) để INSERT IF NOT EXISTS an toàn.
     */
    public List<Integer> getOrCreateGenreIds(List<String> genreNames) {
        List<Integer> ids = new ArrayList<>();
        if (genreNames == null || genreNames.isEmpty()) return ids;

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            for (String rawName : genreNames) {
                String name = rawName == null ? "" : rawName.trim();
                if (name.isEmpty()) continue;

                // MERGE: INSERT nếu chưa có, không làm gì nếu đã có
                // Sau đó SELECT để lấy ID
                String mergeSql =
                    "MERGE INTO Genres AS target " +
                    "USING (SELECT ? AS genre_name) AS src ON target.genre_name = src.genre_name " +
                    "WHEN NOT MATCHED THEN INSERT (genre_name) VALUES (src.genre_name);";

                try (PreparedStatement mergePs = conn.prepareStatement(mergeSql)) {
                    mergePs.setString(1, name);
                    mergePs.executeUpdate();
                }

                // Lấy ID (đã chắc chắn tồn tại sau MERGE)
                String selectSql = "SELECT genre_id FROM Genres WHERE genre_name = ?";
                try (PreparedStatement selectPs = conn.prepareStatement(selectSql)) {
                    selectPs.setString(1, name);
                    ResultSet rs = selectPs.executeQuery();
                    if (rs.next()) {
                        ids.add(rs.getInt("genre_id"));
                    }
                }
            }

            conn.commit();
        } catch (SQLException e) {
            rollback(conn);
            e.printStackTrace();
        } finally {
            restoreAutoCommit(conn);
        }
        return ids;
    }

    // ── Helper: insert vào MovieGenres ────────────────────────────
    private void insertMovieGenres(Connection conn, int movieId, List<Integer> genreIds)
            throws SQLException {
        if (genreIds == null || genreIds.isEmpty()) return;
        String sql = "INSERT INTO MovieGenres (movie_id, genre_id) VALUES (?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql);
        for (int gid : genreIds) {
            ps.setInt(1, movieId);
            ps.setInt(2, gid);
            ps.addBatch();
        }
        ps.executeBatch();
    }

    // ── Helper: extract Movie từ ResultSet ────────────────────────
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
        movie.setGenres(getMovieGenres(movie.getMovieId()));
        return movie;
    }

    // ── Helper: rollback & restore ───────────────────────────────
    private void rollback(Connection conn) {
        if (conn != null) try { conn.rollback(); } catch (SQLException e) { e.printStackTrace(); }
    }
    private void restoreAutoCommit(Connection conn) {
        if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
    }
}