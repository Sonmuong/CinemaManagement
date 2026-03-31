<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Kích Hoạt Suất Chiếu - Cinema</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }

        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            padding: 20px;
            display: flex;
            align-items: center;
            justify-content: center;
        }

        .container {
            max-width: 600px;
            width: 100%;
            background: white;
            border-radius: 15px;
            box-shadow: 0 10px 40px rgba(0,0,0,0.2);
            overflow: hidden;
        }

        .header {
            background: linear-gradient(135deg, #28a745 0%, #20c997 100%);
            color: white;
            padding: 30px;
            text-align: center;
        }
        .header h1 { font-size: 1.8em; margin-bottom: 8px; }
        .header p  { opacity: 0.85; font-size: 0.95em; }

        .form-content { padding: 35px 40px; }

        .form-group { margin-bottom: 22px; }

        label {
            display: block;
            margin-bottom: 8px;
            font-weight: bold;
            color: #333;
            font-size: 0.95em;
        }

        input[type=date],
        input[type=time],
        input[type=number],
        select {
            width: 100%;
            padding: 12px 14px;
            border: 2px solid #dee2e6;
            border-radius: 8px;
            font-size: 15px;
            font-family: inherit;
            transition: border-color 0.2s;
            outline: none;
        }
        input:focus, select:focus { border-color: #28a745; }

        .info-box {
            background: #e8f5e9;
            padding: 15px 18px;
            border-radius: 8px;
            margin-bottom: 24px;
            border-left: 4px solid #28a745;
            font-size: 0.9em;
            color: #2e7d32;
            line-height: 1.7;
        }
        .info-box strong { color: #1b5e20; }

        .current-info {
            background: #f8f9fa;
            border: 1px solid #dee2e6;
            border-radius: 8px;
            padding: 14px 18px;
            margin-bottom: 24px;
        }
        .current-info .ci-title {
            font-weight: 700;
            color: #333;
            margin-bottom: 10px;
            font-size: 0.9em;
        }
        .current-info table { width: 100%; border-collapse: collapse; }
        .current-info td { padding: 5px 8px; font-size: 0.88em; color: #555; }
        .current-info td:first-child { font-weight: 600; color: #444; width: 40%; }

        .btn-row { display: flex; gap: 12px; margin-top: 10px; }

        .btn {
            flex: 1;
            padding: 13px;
            border: none;
            border-radius: 8px;
            font-size: 1em;
            font-weight: bold;
            cursor: pointer;
            transition: all 0.2s;
            text-align: center;
            text-decoration: none;
            display: inline-block;
        }
        .btn-success { background: #28a745; color: white; }
        .btn-success:hover { background: #218838; transform: translateY(-2px); }
        .btn-secondary { background: #6c757d; color: white; }
        .btn-secondary:hover { background: #5a6268; }
    </style>
</head>
<body>
<div class="container">
    <div class="header">
        <h1>✅ KÍCH HOẠT SUẤT CHIẾU</h1>
        <p>Kích hoạt lại suất chiếu đã hủy</p>
    </div>

    <div class="form-content">

        <div class="info-box">
            💡 Bạn đang kích hoạt lại suất chiếu <strong>#${showtime.showtimeId}</strong>.
            Trạng thái sẽ được chuyển về <strong>Đang chiếu (Scheduled)</strong>.
            Bạn có thể chỉnh sửa thông tin trước khi xác nhận.
        </div>

        <div class="current-info">
            <div class="ci-title">📋 Thông tin hiện tại:</div>
            <table>
                <tr>
                    <td>🎬 Phim:</td>
                    <td>${showtime.movieName}</td>
                </tr>
                <tr>
                    <td>🏛️ Phòng:</td>
                    <td>${showtime.roomName}</td>
                </tr>
                <tr>
                    <td>📅 Ngày chiếu:</td>
                    <td><fmt:formatDate value="${showtime.showDate}" pattern="dd/MM/yyyy"/></td>
                </tr>
                <tr>
                    <td>⏰ Giờ chiếu:</td>
                    <td><fmt:formatDate value="${showtime.showTime}" pattern="HH:mm"/></td>
                </tr>
                <tr>
                    <td>💰 Giá vé:</td>
                    <td><fmt:formatNumber value="${showtime.ticketPrice}" pattern="#,##0"/> đ</td>
                </tr>
                <tr>
                    <td>📊 Trạng thái:</td>
                    <td style="color:#dc3545; font-weight:bold;">🔴 Đã hủy</td>
                </tr>
            </table>
        </div>

        <form action="${pageContext.request.contextPath}/showtimes" method="post">
            <input type="hidden" name="action"      value="update">
            <input type="hidden" name="showtimeId"  value="${showtime.showtimeId}">
            <%-- Luôn set Scheduled khi kích hoạt lại --%>
            <input type="hidden" name="status"      value="Scheduled">

            <div class="form-group">
                <label>🎬 Phim *</label>
                <select name="movieId" required>
                    <c:forEach var="movie" items="${movies}">
                        <option value="${movie.movieId}"
                            ${movie.movieId == showtime.movieId ? 'selected' : ''}>
                            ${movie.movieName} (${movie.duration} phút)
                        </option>
                    </c:forEach>
                </select>
            </div>

            <div class="form-group">
                <label>🏛️ Phòng chiếu *</label>
                <select name="roomId" required>
                    <c:forEach var="room" items="${rooms}">
                        <option value="${room.roomId}"
                            ${room.roomId == showtime.roomId ? 'selected' : ''}>
                            ${room.roomName} (${room.totalSeats} ghế)
                        </option>
                    </c:forEach>
                </select>
            </div>

            <div class="form-group">
                <label>📅 Ngày chiếu *</label>
                <input type="date" name="showDate"
                       value="<fmt:formatDate value='${showtime.showDate}' pattern='yyyy-MM-dd'/>"
                       required>
            </div>

            <div class="form-group">
                <label>⏰ Giờ chiếu *</label>
                <input type="time" name="showTime"
                       value="<fmt:formatDate value='${showtime.showTime}' pattern='HH:mm'/>"
                       required>
            </div>

            <div class="form-group">
                <label>💰 Giá vé (VNĐ) *</label>
                <input type="number" name="ticketPrice"
                       value="${showtime.ticketPrice}"
                       min="50000" step="10000" required>
            </div>

            <div class="btn-row">
                <button type="submit" class="btn btn-success">✅ Xác nhận kích hoạt</button>
                <a href="${pageContext.request.contextPath}/showtimes" class="btn btn-secondary">❌ Hủy</a>
            </div>
        </form>

    </div>
</div>
</body>
</html>
