<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Cinema Management System</title>
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
            padding: 60px 40px;
            text-align: center;
        }
        
        .header h1 {
            font-size: 3em;
            margin-bottom: 10px;
            text-shadow: 2px 2px 4px rgba(0,0,0,0.2);
        }
        
        .header p {
            font-size: 1.2em;
            opacity: 0.9;
        }
        
        .menu-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
            gap: 30px;
            padding: 50px 40px;
        }
        
        .menu-card {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 40px;
            border-radius: 15px;
            text-decoration: none;
            transition: all 0.3s;
            box-shadow: 0 5px 15px rgba(0,0,0,0.1);
            cursor: pointer;
        }
        
        .menu-card:hover {
            transform: translateY(-10px);
            box-shadow: 0 15px 35px rgba(0,0,0,0.2);
        }
        
        .menu-icon {
            font-size: 4em;
            margin-bottom: 20px;
            display: block;
        }
        
        .menu-card h2 {
            font-size: 1.8em;
            margin-bottom: 15px;
        }
        
        .menu-card p {
            opacity: 0.9;
            line-height: 1.6;
        }
        
        .features {
            background: #f8f9fa;
            padding: 40px;
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
            gap: 20px;
        }
        
        .feature {
            text-align: center;
            padding: 20px;
        }
        
        .feature-icon {
            font-size: 2.5em;
            margin-bottom: 10px;
        }
        
        .feature h3 {
            color: #667eea;
            margin-bottom: 10px;
        }
        
        .footer {
            background: #2d3436;
            color: white;
            text-align: center;
            padding: 30px;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>🎬 CINEMA MANAGEMENT</h1>
            <p>Hệ thống quản lý rạp chiếu phim chuyên nghiệp</p>
        </div>
        
        <div class="menu-grid">
            <a href="${pageContext.request.contextPath}/movies" class="menu-card">
                <span class="menu-icon">🎥</span>
                <h2>Quản Lý Phim</h2>
                <p>Quản lý danh sách phim, thể loại, thông tin chi tiết phim đang chiếu</p>
            </a>
            
            <a href="${pageContext.request.contextPath}/showtimes" class="menu-card">
                <span class="menu-icon">🎫</span>
                <h2>Lịch Chiếu</h2>
                <p>Quản lý suất chiếu, phòng chiếu, lịch chiếu phim theo ngày</p>
            </a>
            
            <a href="${pageContext.request.contextPath}/tickets" class="menu-card">
                <span class="menu-icon">🎟️</span>
                <h2>Bán Vé</h2>
                <p>Bán vé, đặt chỗ, quản lý vé đã bán, hủy vé</p>
            </a>
            
            <a href="${pageContext.request.contextPath}/customers" class="menu-card">
                <span class="menu-icon">👥</span>
                <h2>Khách Hàng</h2>
                <p>Quản lý thông tin khách hàng, điểm tích lũy, thành viên VIP</p>
            </a>
        </div>
        
        <div class="features">
            <div class="feature">
                <div class="feature-icon">⭐</div>
                <h3>Tích Điểm Thông Minh</h3>
                <p>Cộng 5% giá vé vào điểm tích lũy</p>
            </div>
            
            <div class="feature">
                <div class="feature-icon">💰</div>
                <h3>Giảm Giá Linh Hoạt</h3>
                <p>100 điểm = 10,000 VNĐ giảm giá</p>
            </div>
            
            <div class="feature">
                <div class="feature-icon">🔄</div>
                <h3>Hủy Vé Dễ Dàng</h3>
                <p>Hoàn 80% nếu hủy trước 2 giờ</p>
            </div>
            
            <div class="feature">
                <div class="feature-icon">📈</div>
                <h3>Báo Cáo Chi Tiết</h3>
                <p>Thống kê doanh thu theo nhiều tiêu chí</p>
            </div>
        </div>
        
        <div class="footer">
            <p>&copy; 2024 Cinema Management System. All rights reserved.</p>
            <p>Developed with ❤️ using Jakarta EE</p>
        </div>
    </div>
</body>
</html>