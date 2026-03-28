<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
    // Kiểm tra đăng nhập — nếu chưa login thì chuyển sang trang login
    if (session == null || session.getAttribute("loggedIn") == null) {
        response.sendRedirect(request.getContextPath() + "/login");
        return;
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Cinema Management System</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }

        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 20px;
        }

        .container {
            max-width: 1200px;
            width: 100%;
            background: white;
            border-radius: 20px;
            box-shadow: 0 20px 60px rgba(0,0,0,0.3);
            overflow: hidden;
        }

        .header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 50px 40px 40px;
            text-align: center;
            position: relative;
        }

        .header h1 {
            font-size: 2.8em;
            margin-bottom: 8px;
            text-shadow: 2px 2px 4px rgba(0,0,0,0.2);
        }

        .header p { font-size: 1.1em; opacity: 0.9; }

        .logout-btn {
            position: absolute;
            right: 25px;
            top: 25px;
            background: rgba(255,255,255,0.2);
            color: white;
            text-decoration: none;
            padding: 8px 18px;
            border-radius: 8px;
            font-size: 0.85em;
            font-weight: bold;
            transition: background 0.2s;
        }
        .logout-btn:hover { background: rgba(255,255,255,0.35); }

        .user-info {
            position: absolute;
            left: 25px;
            top: 25px;
            background: rgba(255,255,255,0.15);
            color: white;
            padding: 8px 16px;
            border-radius: 8px;
            font-size: 0.85em;
        }

        .menu-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
            gap: 25px;
            padding: 40px;
        }

        .menu-card {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 35px 30px;
            border-radius: 15px;
            text-decoration: none;
            transition: all 0.3s;
            box-shadow: 0 5px 15px rgba(0,0,0,0.1);
        }

        .menu-card:hover {
            transform: translateY(-8px);
            box-shadow: 0 15px 35px rgba(0,0,0,0.2);
        }

        .menu-icon { font-size: 3.5em; margin-bottom: 15px; display: block; }
        .menu-card h2 { font-size: 1.5em; margin-bottom: 10px; }
        .menu-card p { opacity: 0.88; line-height: 1.6; font-size: 0.92em; }

        .features {
            background: #f8f9fa;
            padding: 35px 40px;
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
            gap: 20px;
        }

        .feature {
            text-align: center;
            padding: 18px;
        }

        .feature-icon { font-size: 2.2em; margin-bottom: 10px; }
        .feature h3 { color: #667eea; margin-bottom: 8px; font-size: 0.95em; }
        .feature p { color: #666; font-size: 0.88em; line-height: 1.5; }

        .footer {
            background: #2d3436;
            color: #ccc;
            text-align: center;
            padding: 25px;
            font-size: 0.88em;
            line-height: 1.8;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <span class="user-info">👤 Xin chào, <strong>${sessionScope.username}</strong></span>
            <h1>🎬 CINEMA MANAGEMENT</h1>
            <p>Hệ thống quản lý rạp chiếu phim chuyên nghiệp</p>
            <a href="${pageContext.request.contextPath}/login?action=logout" class="logout-btn">🚪 Đăng xuất</a>
        </div>

        <div class="menu-grid">
            <a href="${pageContext.request.contextPath}/movies" class="menu-card">
                <span class="menu-icon">🎥</span>
                <h2>Quản Lý Phim</h2>
                <p>Quản lý danh sách phim, thể loại, thêm/sửa/kích hoạt phim</p>
            </a>

            <a href="${pageContext.request.contextPath}/showtimes" class="menu-card">
                <span class="menu-icon">🎫</span>
                <h2>Lịch Chiếu</h2>
                <p>Quản lý suất chiếu, phòng chiếu, lịch chiếu phim theo ngày</p>
            </a>

            <%-- FIX: Menu "Bán vé" link sang showtimes để chọn suất trước --%>
            <a href="${pageContext.request.contextPath}/showtimes" class="menu-card">
                <span class="menu-icon">🎟️</span>
                <h2>Bán Vé</h2>
                <p>Chọn suất chiếu → bán vé, chọn ghế, áp dụng điểm tích lũy</p>
            </a>

            <a href="${pageContext.request.contextPath}/customers" class="menu-card">
                <span class="menu-icon">👥</span>
                <h2>Khách Hàng</h2>
                <p>Quản lý thông tin khách hàng, điểm tích lũy, thành viên VIP</p>
            </a>

            <a href="${pageContext.request.contextPath}/tickets" class="menu-card">
                <span class="menu-icon">📋</span>
                <h2>Quản Lý Vé</h2>
                <p>Xem danh sách vé đã bán, hủy vé, thống kê doanh thu nhanh</p>
            </a>

            <a href="${pageContext.request.contextPath}/reports" class="menu-card">
                <span class="menu-icon">📊</span>
                <h2>Báo Cáo</h2>
                <p>Thống kê doanh thu, tỷ lệ lấp đầy, top phim, khách VIP</p>
            </a>
        </div>

        <div class="features">
            <div class="feature">
                <div class="feature-icon">⭐</div>
                <h3>Tích Điểm Thông Minh</h3>
                <p>Cộng 5% giá vé vào điểm tích lũy sau mỗi giao dịch</p>
            </div>
            <div class="feature">
                <div class="feature-icon">💰</div>
                <h3>Giảm Giá Linh Hoạt</h3>
                <p>100 điểm = 10,000 VNĐ giảm giá khi mua vé</p>
            </div>
            <div class="feature">
                <div class="feature-icon">🔄</div>
                <h3>Hủy Vé Dễ Dàng</h3>
                <p>Hoàn 80% nếu hủy trước 2 giờ chiếu</p>
            </div>
            <div class="feature">
                <div class="feature-icon">🏆</div>
                <h3>Khách Hàng VIP</h3>
                <p>Tự động nâng hạng VIP khi đạt 1,000 điểm</p>
            </div>
        </div>

        <div class="footer">
            <p>🎬 Cinema Management System &copy; 2024</p>
            <p>Developed with ❤️ using Jakarta EE &amp; Microsoft SQL Server</p>
        </div>
    </div>
</body>
</html>
