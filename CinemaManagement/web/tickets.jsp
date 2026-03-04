<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Quản Lý Vé - Cinema</title>
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
        
        .message {
            padding: 15px;
            border-radius: 8px;
            margin-bottom: 20px;
            font-weight: bold;
        }
        
        .message.success {
            background: #d4edda;
            color: #155724;
        }
        
        .message.error {
            background: #f8d7da;
            color: #721c24;
        }
        
        .ticket-card {
            background: white;
            border: 2px solid #dee2e6;
            border-radius: 10px;
            padding: 20px;
            margin-bottom: 20px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
        }
        
        .ticket-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 15px;
            padding-bottom: 15px;
            border-bottom: 2px dashed #dee2e6;
        }
        
        .ticket-id {
            font-size: 1.2em;
            font-weight: bold;
            color: #667eea;
        }
        
        .ticket-body {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 15px;
            margin-bottom: 15px;
        }
        
        .info-item {
            display: flex;
            flex-direction: column;
            gap: 5px;
        }
        
        .info-label {
            font-weight: bold;
            color: #666;
            font-size: 0.9em;
        }
        
        .info-value {
            color: #333;
            font-size: 1.1em;
        }
        
        .status-badge {
            padding: 5px 15px;
            border-radius: 15px;
            font-weight: bold;
            font-size: 0.85em;
            display: inline-block;
        }
        
        .status-paid {
            background: #28a745;
            color: white;
        }
        
        .status-cancelled, .status-refunded {
            background: #dc3545;
            color: white;
        }
        
        .price-highlight {
            color: #667eea;
            font-weight: bold;
            font-size: 1.3em;
        }
        
        .btn {
            padding: 8px 20px;
            background: #667eea;
            color: white;
            border: none;
            border-radius: 5px;
            font-weight: bold;
            cursor: pointer;
            transition: all 0.3s;
            text-decoration: none;
            display: inline-block;
        }
        
        .btn:hover {
            background: #5568d3;
        }
        
        .btn-danger {
            background: #dc3545;
        }
        
        .btn-danger:hover {
            background: #c82333;
        }
        
        .stats-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 20px;
            margin-bottom: 30px;
        }
        
        .stat-card {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 20px;
            border-radius: 10px;
            text-align: center;
        }
        
        .stat-number {
            font-size: 2.5em;
            font-weight: bold;
        }
        
        .stat-label {
            margin-top: 5px;
            opacity: 0.9;
        }
        
        .sold-ticket-info {
            background: linear-gradient(135deg, #28a745 0%, #20c997 100%);
            color: white;
            padding: 30px;
            border-radius: 10px;
            margin-bottom: 30px;
        }
        
        .sold-ticket-info h2 {
            margin-bottom: 20px;
            text-align: center;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>🎟️ QUẢN LÝ VÉ</h1>
            <p>Danh sách vé đã bán và thống kê</p>
        </div>
        
        <div class="nav">
            <a href="${pageContext.request.contextPath}/">🏠 Trang chủ</a>
            <a href="${pageContext.request.contextPath}/movies">🎥 Phim</a>
            <a href="${pageContext.request.contextPath}/showtimes">🎫 Suất chiếu</a>
            <a href="${pageContext.request.contextPath}/tickets">🎟️ Vé</a>
            <a href="${pageContext.request.contextPath}/customers">👥 Khách hàng</a>
        </div>
        
        <div class="content">
            <c:if test="${sessionScope.message != null}">
                <div class="message ${sessionScope.messageType}">
                    ${sessionScope.message}
                </div>
                <c:remove var="message" scope="session"/>
                <c:remove var="messageType" scope="session"/>
            </c:if>
            
            <c:if test="${sessionScope.soldTicket != null}">
                <div class="sold-ticket-info">
                    <h2>✅ VÉ VỪA BÁN THÀNH CÔNG!</h2>
                    <div style="display: grid; grid-template-columns: repeat(2, 1fr); gap: 20px;">
                        <div>
                            <p><strong>Mã vé:</strong> #${sessionScope.soldTicket.ticketId}</p>
                            <p><strong>Phim:</strong> ${sessionScope.soldTicket.movieName}</p>
                            <p><strong>Phòng:</strong> ${sessionScope.soldTicket.roomName}</p>
                            <p><strong>Ghế:</strong> ${sessionScope.soldTicket.seatNumber}</p>
                        </div>
                        <div>
                            <p><strong>Ngày:</strong> 
                                <fmt:formatDate value="${sessionScope.soldTicket.showDate}" pattern="dd/MM/yyyy"/>
                            </p>
                            <p><strong>Giờ:</strong> ${sessionScope.soldTicket.showTime}</p>
                            <p><strong>Giá vé:</strong> 
                                <fmt:formatNumber value="${sessionScope.soldTicket.finalPrice}" pattern="#,##0"/> đ
                            </p>
                            <c:if test="${sessionScope.soldTicket.pointsEarned > 0}">
                                <p><strong>Điểm tích lũy:</strong> +${sessionScope.soldTicket.pointsEarned}</p>
                            </c:if>
                        </div>
                    </div>
                </div>
                <c:remove var="soldTicket" scope="session"/>
            </c:if>
            
            <div class="stats-grid">
                <div class="stat-card">
                    <div class="stat-number">${tickets.size()}</div>
                    <div class="stat-label">Tổng số vé</div>
                </div>
                <div class="stat-card">
                    <div class="stat-number">
                        <c:set var="totalRevenue" value="0"/>
                        <c:forEach var="t" items="${tickets}">
                            <c:if test="${t.paymentStatus == 'Paid'}">
                                <c:set var="totalRevenue" value="${totalRevenue + t.finalPrice}"/>
                            </c:if>
                        </c:forEach>
                        <fmt:formatNumber value="${totalRevenue / 1000000}" pattern="#,##0"/>M
                    </div>
                    <div class="stat-label">Doanh thu</div>
                </div>
                <div class="stat-card">
                    <div class="stat-number">
                        <c:set var="paidCount" value="0"/>
                        <c:forEach var="t" items="${tickets}">
                            <c:if test="${t.paymentStatus == 'Paid'}">
                                <c:set var="paidCount" value="${paidCount + 1}"/>
                            </c:if>
                        </c:forEach>
                        ${paidCount}
                    </div>
                    <div class="stat-label">Vé đã thanh toán</div>
                </div>
            </div>
            
            <h2 style="margin-bottom: 20px;">📋 Danh Sách Vé</h2>
            
            <c:forEach var="ticket" items="${tickets}">
                <div class="ticket-card">
                    <div class="ticket-header">
                        <span class="ticket-id">🎟️ Vé #${ticket.ticketId}</span>
                        <c:choose>
                            <c:when test="${ticket.paymentStatus == 'Paid'}">
                                <span class="status-badge status-paid">✅ Đã thanh toán</span>
                            </c:when>
                            <c:when test="${ticket.paymentStatus == 'Cancelled'}">
                                <span class="status-badge status-cancelled">❌ Đã hủy</span>
                            </c:when>
                            <c:when test="${ticket.paymentStatus == 'Refunded'}">
                                <span class="status-badge status-refunded">💰 Đã hoàn tiền</span>
                            </c:when>
                        </c:choose>
                    </div>
                    
                    <div class="ticket-body">
                        <div class="info-item">
                            <span class="info-label">🎬 Phim</span>
                            <span class="info-value">${ticket.movieName}</span>
                        </div>
                        <div class="info-item">
                            <span class="info-label">🏛️ Phòng - Ghế</span>
                            <span class="info-value">${ticket.roomName} - ${ticket.seatNumber}</span>
                        </div>
                        <div class="info-item">
                            <span class="info-label">📅 Ngày chiếu</span>
                            <span class="info-value">
                                <fmt:formatDate value="${ticket.showDate}" pattern="dd/MM/yyyy"/>
                            </span>
                        </div>
                        <div class="info-item">
                            <span class="info-label">⏰ Giờ chiếu</span>
                            <span class="info-value">${ticket.showTime}</span>
                        </div>
                        <div class="info-item">
                            <span class="info-label">👤 Khách hàng</span>
                            <span class="info-value">
                                <c:choose>
                                    <c:when test="${ticket.customerName != null}">
                                        ${ticket.customerName}
                                    </c:when>
                                    <c:otherwise>
                                        <em>Vé lẻ</em>
                                    </c:otherwise>
                                </c:choose>
                            </span>
                        </div>
                        <div class="info-item">
                            <span class="info-label">💰 Thành tiền</span>
                            <span class="price-highlight">
                                <fmt:formatNumber value="${ticket.finalPrice}" pattern="#,##0"/> đ
                            </span>
                        </div>
                        <c:if test="${ticket.pointsUsed > 0}">
                            <div class="info-item">
                                <span class="info-label">💎 Điểm đã dùng</span>
                                <span class="info-value" style="color: #dc3545;">
                                    -${ticket.pointsUsed} điểm
                                </span>
                            </div>
                        </c:if>
                        <c:if test="${ticket.pointsEarned > 0}">
                            <div class="info-item">
                                <span class="info-label">⭐ Điểm tích lũy</span>
                                <span class="info-value" style="color: #28a745;">
                                    +${ticket.pointsEarned} điểm
                                </span>
                            </div>
                        </c:if>
                    </div>
                    
                    <c:if test="${ticket.paymentStatus == 'Paid'}">
                        <form action="tickets" method="post" style="text-align: right;"
                              onsubmit="return confirm('Bạn có chắc muốn hủy vé này?')">
                            <input type="hidden" name="action" value="cancel">
                            <input type="hidden" name="ticketId" value="${ticket.ticketId}">
                            <button type="submit" class="btn btn-danger">
                                ❌ Hủy vé
                            </button>
                        </form>
                    </c:if>
                </div>
            </c:forEach>
            
            <c:if test="${tickets.size() == 0}">
                <div style="text-align: center; padding: 50px; color: #666;">
                    <p style="font-size: 1.2em;">😕 Chưa có vé nào được bán</p>
                </div>
            </c:if>
        </div>
    </div>
</body>
</html>