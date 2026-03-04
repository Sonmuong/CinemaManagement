<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Thêm Suất Chiếu - Cinema</title>
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
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 30px;
            text-align: center;
        }
        
        .header h1 {
            font-size: 2em;
            margin-bottom: 10px;
        }
        
        .form-content {
            padding: 40px;
        }
        
        .form-group {
            margin-bottom: 25px;
        }
        
        label {
            display: block;
            margin-bottom: 8px;
            font-weight: bold;
            color: #333;
        }
        
        input, select {
            width: 100%;
            padding: 12px;
            border: 2px solid #dee2e6;
            border-radius: 8px;
            font-size: 16px;
            transition: all 0.3s;
        }
        
        input:focus, select:focus {
            outline: none;
            border-color: #667eea;
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
            font-size: 16px;
            width: 100%;
        }
        
        .btn:hover {
            background: #5568d3;
            transform: translateY(-2px);
        }
        
        .btn-secondary {
            background: #6c757d;
            margin-top: 10px;
        }
        
        .btn-secondary:hover {
            background: #5a6268;
        }
        
        .info-box {
            background: #e7f3ff;
            padding: 15px;
            border-radius: 8px;
            margin-bottom: 20px;
            border-left: 4px solid #667eea;
        }
        
        .info-box h3 {
            color: #667eea;
            margin-bottom: 10px;
        }
        
        .info-box ul {
            margin-left: 20px;
        }
        
        .info-box li {
            margin-bottom: 5px;
            color: #666;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>➕ THÊM SUẤT CHIẾU MỚI</h1>
            <p>Tạo lịch chiếu phim</p>
        </div>
        
        <div class="form-content">
            <div class="info-box">
                <h3>📋 Lưu ý khi thêm suất chiếu:</h3>
                <ul>
                    <li>Phải lên lịch trước ít nhất 1 ngày</li>
                    <li>Hệ thống sẽ tự động kiểm tra trùng lịch phòng</li>
                    <li>Thời gian dọn dẹp giữa các suất: 30 phút</li>
                </ul>
            </div>
            
            <form action="${pageContext.request.contextPath}/showtimes" method="post">
                <input type="hidden" name="action" value="create">
                
                <div class="form-group">
                    <label for="movieId">🎬 Chọn Phim *</label>
                    <select name="movieId" id="movieId" required>
                        <option value="">-- Chọn phim --</option>
                        <c:forEach var="movie" items="${movies}">
                            <option value="${movie.movieId}">
                                ${movie.movieName} (${movie.duration} phút)
                            </option>
                        </c:forEach>
                    </select>
                </div>
                
                <div class="form-group">
                    <label for="roomId">🏛️ Chọn Phòng *</label>
                    <select name="roomId" id="roomId" required>
                        <option value="">-- Chọn phòng --</option>
                        <c:forEach var="room" items="${rooms}">
                            <option value="${room.roomId}">
                                ${room.roomName} (${room.totalSeats} ghế)
                            </option>
                        </c:forEach>
                    </select>
                </div>
                
                <div class="form-group">
                    <label for="showDate">📅 Ngày Chiếu *</label>
                    <input type="date" name="showDate" id="showDate" required>
                </div>
                
                <div class="form-group">
                    <label for="showTime">⏰ Giờ Chiếu *</label>
                    <input type="time" name="showTime" id="showTime" required>
                </div>
                
                <div class="form-group">
                    <label for="ticketPrice">💰 Giá Vé (VNĐ) *</label>
                    <input type="number" name="ticketPrice" id="ticketPrice" 
                           min="50000" step="10000" value="100000" required>
                </div>
                
                <button type="submit" class="btn">✅ Tạo Suất Chiếu</button>
                <a href="${pageContext.request.contextPath}/showtimes">
                    <button type="button" class="btn btn-secondary">❌ Hủy</button>
                </a>
            </form>
        </div>
    </div>
    
    <script>
        // Set min date to tomorrow
        const tomorrow = new Date();
        tomorrow.setDate(tomorrow.getDate() + 1);
        document.getElementById('showDate').min = tomorrow.toISOString().split('T')[0];
    </script>
</body>
</html>