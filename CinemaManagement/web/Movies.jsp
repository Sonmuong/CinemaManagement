<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Quản Lý Phim - Cinema</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            padding: 20px;
        }
        
        .container {
            max-width: 1400px;
            margin: 0 auto;
            background: white;
            border-radius: 15px;
            box-shadow: 0 10px 40px rgba(0,0,0,0.2);
            overflow: hidden;
        }
        
        .header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 30px;
            text-align: center;
        }
        
        .header h1 {
            font-size: 2em;
            margin-bottom: 10px;
        }
        
        .nav {
            background: #f8f9fa;
            padding: 15px 30px;
            display: flex;
            gap: 15px;
            border-bottom: 2px solid #dee2e6;
            flex-wrap: wrap;
        }
        
        .nav a {
            text-decoration: none;
            color: #667eea;
            padding: 10px 20px;
            border-radius: 5px;
            font-weight: bold;
            transition: all 0.3s;
        }
        
        .nav a:hover {
            background: #667eea;
            color: white;
        }
        
        .content {
            padding: 30px;
        }
        
        .toolbar {
            display: flex;
            gap: 15px;
            margin-bottom: 30px;
            flex-wrap: wrap;
        }
        
        .search-box {
            flex: 1;
            display: flex;
            gap: 10px;
        }
        
        .search-box input {
            flex: 1;
            padding: 12px;
            border: 2px solid #dee2e6;
            border-radius: 8px;
            font-size: 16px;
        }
        
        .btn {
            padding: 12px 25px;
            background: #667eea;
            color: white;
            border: none;
            border-radius: 8px;
            font-weight: bold;
            cursor: pointer;
            transition: all 0.3s;
            text-decoration: none;
            display: inline-block;
        }
        
        .btn:hover {
            background: #5568d3;
            transform: translateY(-2px);
        }
        
        .movies-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
            gap: 25px;
        }
        
        .movie-card {
            background: white;
            border-radius: 10px;
            overflow: hidden;
            box-shadow: 0 4px 15px rgba(0,0,0,0.1);
            transition: all 0.3s;
        }
        
        .movie-card:hover {
            transform: translateY(-5px);
            box-shadow: 0 8px 25px rgba(0,0,0,0.15);
        }
        
        .movie-header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 20px;
        }
        
        .movie-title {
            font-size: 1.3em;
            font-weight: bold;
            margin-bottom: 10px;
        }
        
        .movie-genres {
            display: flex;
            flex-wrap: wrap;
            gap: 8px;
        }
        
        .genre-tag {
            background: rgba(255,255,255,0.2);
            padding: 4px 12px;
            border-radius: 12px;
            font-size: 0.85em;
        }
        
        .movie-body {
            padding: 20px;
        }
        
        .movie-info {
            margin-bottom: 15px;
        }
        
        .info-row {
            display: flex;
            justify-content: space-between;
            margin-bottom: 8px;
            padding-bottom: 8px;
            border-bottom: 1px solid #eee;
        }
        
        .info-label {
            color: #666;
            font-weight: 600;
        }
        
        .info-value {
            color: #333;
        }
        
        .movie-actions {
            display: flex;
            gap: 10px;
            margin-top: 15px;
        }
        
        .btn-small {
            flex: 1;
            padding: 8px 15px;
            font-size: 0.9em;
        }
        
        .btn-success {
            background: #28a745;
        }
        
        .btn-success:hover {
            background: #218838;
        }
        
        .status-badge {
            padding: 5px 15px;
            border-radius: 15px;
            font-weight: bold;
            font-size: 0.85em;
            display: inline-block;
        }
        
        .status-active {
            background: #28a745;
            color: white;
        }
        
        .status-inactive {
            background: #dc3545;
            color: white;
        }
        
        .no-results {
            text-align: center;
            padding: 50px;
            color: #666;
            font-size: 1.2em;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>🎬 QUẢN LÝ PHIM</h1>
            <p>Danh sách phim đang chiếu và quản lý thông tin phim</p>
        </div>
        
        <div class="nav">
            <a href="${pageContext.request.contextPath}/">🏠 Trang chủ</a>
            <a href="${pageContext.request.contextPath}/movies">🎥 Phim</a>
            <a href="${pageContext.request.contextPath}/showtimes">🎫 Suất chiếu</a>
            <a href="${pageContext.request.contextPath}/tickets">🎟️ Bán vé</a>
            <a href="${pageContext.request.contextPath}/customers">👥 Khách hàng</a>
        </div>
        
        <div class="content">
            <div class="toolbar">
                <div class="search-box">
                    <form action="movies" method="get" style="display: flex; gap: 10px; width: 100%;">
                        <input type="hidden" name="action" value="search">
                        <input type="text" name="keyword" placeholder="Tìm kiếm phim..." 
                               value="${keyword}" required>
                        <button type="submit" class="btn">🔍 Tìm kiếm</button>
                    </form>
                </div>
                <a href="movies" class="btn" style="background: #6c757d;">📋 Tất cả</a>
            </div>
            
            <c:if test="${keyword != null}">
                <p style="margin-bottom: 20px; color: #666;">
                    Kết quả tìm kiếm cho: <strong>"${keyword}"</strong> 
                    (${movies.size()} kết quả)
                </p>
            </c:if>
            
            <c:choose>
                <c:when test="${movies.size() > 0}">
                    <div class="movies-grid">
                        <c:forEach var="movie" items="${movies}">
                            <div class="movie-card">
                                <div class="movie-header">
                                    <div class="movie-title">${movie.movieName}</div>
                                    <div class="movie-genres">
                                        <c:forEach var="genre" items="${movie.genres}">
                                            <span class="genre-tag">${genre}</span>
                                        </c:forEach>
                                    </div>
                                </div>
                                
                                <div class="movie-body">
                                    <div class="movie-info">
                                        <div class="info-row">
                                            <span class="info-label">⏱️ Thời lượng:</span>
                                            <span class="info-value">${movie.duration} phút</span>
                                        </div>
                                        <div class="info-row">
                                            <span class="info-label">🌍 Quốc gia:</span>
                                            <span class="info-value">${movie.country}</span>
                                        </div>
                                        <div class="info-row">
                                            <span class="info-label">📅 Năm:</span>
                                            <span class="info-value">${movie.releaseYear}</span>
                                        </div>
                                        <div class="info-row">
                                            <span class="info-label">🔞 Độ tuổi:</span>
                                            <span class="info-value">${movie.ageRestriction}+</span>
                                        </div>
                                        <div class="info-row">
                                            <span class="info-label">🎬 Đạo diễn:</span>
                                            <span class="info-value">${movie.director}</span>
                                        </div>
                                        <div class="info-row">
                                            <span class="info-label">📊 Trạng thái:</span>
                                            <span>
                                                <c:choose>
                                                    <c:when test="${movie.status == 'Active'}">
                                                        <span class="status-badge status-active">Đang chiếu</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="status-badge status-inactive">Ngừng chiếu</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </span>
                                        </div>
                                    </div>
                                    
                                    <div class="movie-actions">
                                        <a href="showtimes?action=movie&movieId=${movie.movieId}" 
                                           class="btn btn-small">
                                            📅 Xem Lịch
                                        </a>
                                        <c:if test="${movie.status == 'Active'}">
                                            <a href="showtimes?action=add&movieId=${movie.movieId}" 
                                               class="btn btn-small btn-success">
                                                ➕ Lên Lịch
                                            </a>
                                        </c:if>
                                    </div>
                                </div>
                            </div>
                        </c:forEach>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="no-results">
                        <p>😕 Không tìm thấy phim nào</p>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</body>
</html>