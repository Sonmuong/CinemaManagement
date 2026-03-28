<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Quản Lý Phim - Cinema</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }

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
        .header h1 { font-size: 2em; margin-bottom: 10px; }

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
        .nav a:hover { background: #667eea; color: white; }

        .content { padding: 30px; }

        .message {
            padding: 15px;
            border-radius: 8px;
            margin-bottom: 20px;
            font-weight: bold;
        }
        .message.success { background: #d4edda; color: #155724; }
        .message.error   { background: #f8d7da; color: #721c24; }

        .toolbar {
            display: flex;
            gap: 15px;
            margin-bottom: 30px;
            flex-wrap: wrap;
            align-items: center;
        }
        .search-box { flex: 1; display: flex; gap: 10px; }
        .search-box input {
            flex: 1;
            padding: 12px;
            border: 2px solid #dee2e6;
            border-radius: 8px;
            font-size: 16px;
            outline: none;
        }
        .search-box input:focus { border-color: #667eea; }

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
            white-space: nowrap;
        }
        .btn:hover { background: #5568d3; transform: translateY(-2px); }
        .btn-success { background: #28a745; }
        .btn-success:hover { background: #218838; }
        .btn-warning { background: #ffc107; color: #333; }
        .btn-warning:hover { background: #e0a800; }
        .btn-danger  { background: #dc3545; }
        .btn-danger:hover { background: #c82333; }
        .btn-sm { padding: 6px 12px; font-size: 0.82em; }

        /* Grid phim */
        .movies-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
            gap: 25px;
        }

        .movie-card {
            background: white;
            border-radius: 12px;
            overflow: hidden;
            box-shadow: 0 4px 15px rgba(0,0,0,0.1);
            transition: all 0.3s;
            border: 1px solid #e9ecef;
        }
        .movie-card:hover {
            transform: translateY(-5px);
            box-shadow: 0 8px 28px rgba(0,0,0,0.15);
        }

        .movie-header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 18px 20px;
        }
        .movie-title { font-size: 1.15em; font-weight: bold; margin-bottom: 10px; }
        .movie-genres { display: flex; flex-wrap: wrap; gap: 6px; }
        .genre-tag {
            background: rgba(255,255,255,0.22);
            padding: 3px 10px;
            border-radius: 12px;
            font-size: 0.82em;
        }

        .movie-body { padding: 18px 20px; }

        .info-row {
            display: flex;
            justify-content: space-between;
            margin-bottom: 7px;
            padding-bottom: 7px;
            border-bottom: 1px solid #f0f0f0;
            font-size: 0.92em;
        }
        .info-row:last-of-type { border-bottom: none; }
        .info-label { color: #666; font-weight: 600; }

        .status-badge {
            padding: 4px 12px;
            border-radius: 12px;
            font-weight: bold;
            font-size: 0.82em;
        }
        .status-active   { background: #28a745; color: white; }
        .status-inactive { background: #dc3545; color: white; }

        /* Action buttons inside card */
        .movie-actions {
            display: flex;
            gap: 7px;
            margin-top: 14px;
            flex-wrap: wrap;
        }
        .movie-actions .btn { flex: 1; text-align: center; }

        .no-results { text-align: center; padding: 50px; color: #666; font-size: 1.2em; }
    </style>
</head>
<body>
<div class="container">
    <div class="header">
        <h1>🎬 QUẢN LÝ PHIM</h1>
        <p>Danh sách phim và quản lý thông tin</p>
    </div>

    <div class="nav">
        <a href="${pageContext.request.contextPath}/">🏠 Trang chủ</a>
        <a href="${pageContext.request.contextPath}/movies">🎥 Phim</a>
        <a href="${pageContext.request.contextPath}/showtimes">🎫 Suất chiếu</a>
        <a href="${pageContext.request.contextPath}/tickets">🎟️ Bán vé</a>
        <a href="${pageContext.request.contextPath}/customers">👥 Khách hàng</a>
    </div>

    <div class="content">

        <%-- Thông báo --%>
        <c:if test="${sessionScope.message != null}">
            <div class="message ${sessionScope.messageType}">
                ${sessionScope.message}
            </div>
            <c:remove var="message"     scope="session"/>
            <c:remove var="messageType" scope="session"/>
        </c:if>

        <%-- Toolbar --%>
        <div class="toolbar">
            <div class="search-box">
                <form action="movies" method="get" style="display:flex; gap:10px; width:100%;">
                    <input type="hidden" name="action" value="search">
                    <input type="text" name="keyword" placeholder="Tìm kiếm phim..."
                           value="${keyword}">
                    <button type="submit" class="btn">🔍 Tìm kiếm</button>
                </form>
            </div>
            <a href="movies" class="btn" style="background:#6c757d;">📋 Tất cả</a>
            <a href="movies?action=add" class="btn btn-success">➕ Thêm phim mới</a>
        </div>

        <c:if test="${keyword != null}">
            <p style="margin-bottom:20px; color:#666;">
                Kết quả tìm kiếm: <strong>"${keyword}"</strong>
                (${movies.size()} kết quả)
            </p>
        </c:if>

        <%-- Danh sách phim --%>
        <c:choose>
            <c:when test="${not empty movies}">
                <div class="movies-grid">
                    <c:forEach var="movie" items="${movies}">
                        <div class="movie-card">
                            <div class="movie-header">
                                <div class="movie-title">${movie.movieName}</div>
                                <div class="movie-genres">
                                    <c:forEach var="genre" items="${movie.genres}">
                                        <span class="genre-tag">${genre}</span>
                                    </c:forEach>
                                    <c:if test="${empty movie.genres}">
                                        <span class="genre-tag" style="opacity:0.6;">Chưa phân loại</span>
                                    </c:if>
                                </div>
                            </div>

                            <div class="movie-body">
                                <div class="info-row">
                                    <span class="info-label">⏱️ Thời lượng</span>
                                    <span>${movie.duration} phút</span>
                                </div>
                                <div class="info-row">
                                    <span class="info-label">🌍 Quốc gia</span>
                                    <span>${movie.country}</span>
                                </div>
                                <div class="info-row">
                                    <span class="info-label">📅 Năm</span>
                                    <span>${movie.releaseYear}</span>
                                </div>
                                <div class="info-row">
                                    <span class="info-label">🔞 Độ tuổi</span>
                                    <span>${movie.ageRestriction == 0 ? 'Mọi lứa tuổi' : movie.ageRestriction += '+'}</span>
                                </div>
                                <div class="info-row">
                                    <span class="info-label">🎬 Đạo diễn</span>
                                    <span>${movie.director}</span>
                                </div>
                                <div class="info-row">
                                    <span class="info-label">📊 Trạng thái</span>
                                    <c:choose>
                                        <c:when test="${movie.status == 'Active'}">
                                            <span class="status-badge status-active">Đang chiếu</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="status-badge status-inactive">Ngừng chiếu</span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>

                                <%-- Hành động --%>
                                <div class="movie-actions">
                                    <%-- Xem lịch chiếu --%>
                                    <a href="showtimes?action=movie&movieId=${movie.movieId}"
                                       class="btn btn-sm">📅 Lịch chiếu</a>

                                    <%-- Sửa phim --%>
                                    <a href="movies?action=edit&movieId=${movie.movieId}"
                                       class="btn btn-sm btn-warning">✏️ Sửa</a>

                                    <%-- Lên lịch (chỉ khi đang chiếu) --%>
                                    <c:if test="${movie.status == 'Active'}">
                                        <a href="showtimes?action=add&movieId=${movie.movieId}"
                                           class="btn btn-sm btn-success">➕ Lên lịch</a>
                                    </c:if>

                                    <%-- Ngừng chiếu / Kích hoạt --%>
                                    <form action="movies" method="get" style="flex:1;"
                                          onsubmit="return confirm('${movie.status == 'Active' ? 'Ngừng chiếu phim này?' : 'Kích hoạt lại phim này?'}')">
                                        <input type="hidden" name="action" value="delete">
                                        <input type="hidden" name="movieId" value="${movie.movieId}">
                                        <button type="submit"
                                                class="btn btn-sm ${movie.status == 'Active' ? 'btn-danger' : 'btn-success'}"
                                                style="width:100%;">
                                            ${movie.status == 'Active' ? '🚫 Ngừng chiếu' : '✅ Kích hoạt'}
                                        </button>
                                    </form>
                                </div>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </c:when>
            <c:otherwise>
                <div class="no-results">😕 Không tìm thấy phim nào</div>
            </c:otherwise>
        </c:choose>

    </div>
</div>
</body>
</html>
