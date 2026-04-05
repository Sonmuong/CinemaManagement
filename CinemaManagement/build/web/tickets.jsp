<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Quản Lý Vé - Cinema</title>
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
        .header h1 { font-size: 2em; margin-bottom: 8px; }

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
        .message.success { background: #d4edda; color: #155724; border: 1px solid #c3e6cb; }
        .message.error   { background: #f8d7da; color: #721c24; border: 1px solid #f5c6cb; }

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
        .stat-number { font-size: 2.5em; font-weight: bold; }
        .stat-label  { margin-top: 5px; opacity: 0.9; }

        .ticket-card {
            background: white;
            border: 2px solid #dee2e6;
            border-radius: 12px;
            padding: 20px;
            margin-bottom: 20px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.08);
            transition: box-shadow 0.2s;
        }
        .ticket-card:hover { box-shadow: 0 4px 16px rgba(0,0,0,0.12); }
        .ticket-card.cancelled { opacity: 0.7; border-color: #f5c6cb; }
        .ticket-card.refunded  { border-color: #bee5eb; }

        .ticket-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 15px;
            padding-bottom: 15px;
            border-bottom: 2px dashed #dee2e6;
            flex-wrap: wrap;
            gap: 10px;
        }

        .ticket-id { font-size: 1.2em; font-weight: bold; color: #667eea; }

        .ticket-body {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 15px;
            margin-bottom: 15px;
        }

        .info-item { display: flex; flex-direction: column; gap: 4px; }
        .info-label { font-weight: bold; color: #666; font-size: 0.88em; }
        .info-value { color: #333; font-size: 1em; }

        .status-badge {
            padding: 5px 15px;
            border-radius: 15px;
            font-weight: bold;
            font-size: 0.85em;
            display: inline-block;
        }
        .status-paid     { background: #28a745; color: white; }
        .status-cancelled { background: #dc3545; color: white; }
        .status-refunded  { background: #17a2b8; color: white; }

        .price-highlight { color: #667eea; font-weight: bold; font-size: 1.2em; }

        .refund-info {
            background: #e8f4f8;
            border: 1px solid #bee5eb;
            border-radius: 8px;
            padding: 10px 14px;
            margin-bottom: 12px;
            font-size: 0.9em;
            color: #0c5460;
        }

        .btn {
            padding: 8px 20px;
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
        .btn:hover { background: #5568d3; transform: translateY(-1px); }
        .btn-danger { background: #dc3545; }
        .btn-danger:hover { background: #c82333; }

        .section-title {
            font-size: 1.2em;
            font-weight: bold;
            color: #333;
            margin-bottom: 20px;
            padding-bottom: 10px;
            border-bottom: 2px solid #dee2e6;
        }

        .empty-state {
            text-align: center;
            padding: 60px 20px;
            color: #888;
        }
        .empty-state p { font-size: 1.2em; margin-bottom: 10px; }
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
        <a href="${pageContext.request.contextPath}/reports">📊 Báo cáo</a>
    </div>

    <div class="content">

        <c:if test="${sessionScope.message != null}">
            <div class="message ${sessionScope.messageType}">
                ${sessionScope.message}
            </div>
            <c:remove var="message" scope="session"/>
            <c:remove var="messageType" scope="session"/>
        </c:if>

        <%-- Thống kê --%>
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
                    <fmt:formatNumber value="${totalRevenue / 1000000}" pattern="#,##0.0"/>M
                </div>
                <div class="stat-label">Doanh thu (VNĐ)</div>
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
            <div class="stat-card">
                <div class="stat-number">
                    <c:set var="cancelCount" value="0"/>
                    <c:forEach var="t" items="${tickets}">
                        <c:if test="${t.paymentStatus == 'Cancelled' || t.paymentStatus == 'Refunded'}">
                            <c:set var="cancelCount" value="${cancelCount + 1}"/>
                        </c:if>
                    </c:forEach>
                    ${cancelCount}
                </div>
                <div class="stat-label">Vé đã hủy / hoàn</div>
            </div>
        </div>

        <div class="section-title">📋 Danh Sách Vé</div>

        <c:choose>
            <c:when test="${not empty tickets}">
                <c:forEach var="ticket" items="${tickets}">
                    <div class="ticket-card ${ticket.paymentStatus == 'Cancelled' ? 'cancelled' : ticket.paymentStatus == 'Refunded' ? 'refunded' : ''}">
                        <div class="ticket-header">
                            <span class="ticket-id">🎟️ Vé #${ticket.ticketId}</span>
                            <c:choose>
                                <c:when test="${ticket.paymentStatus == 'Paid'}">
                                    <span class="status-badge status-paid">✅ Đã thanh toán</span>
                                </c:when>
                                <c:when test="${ticket.paymentStatus == 'Refunded'}">
                                    <span class="status-badge status-refunded">💰 Đã hoàn tiền (80%)</span>
                                </c:when>
                                <c:when test="${ticket.paymentStatus == 'Cancelled'}">
                                    <span class="status-badge status-cancelled">❌ Đã hủy (không hoàn)</span>
                                </c:when>
                            </c:choose>
                        </div>

                        <div class="ticket-body">
                            <div class="info-item">
                                <span class="info-label">🎬 Phim</span>
                                <span class="info-value"><strong>${ticket.movieName}</strong></span>
                            </div>
                            <div class="info-item">
                                <span class="info-label">🏛️ Phòng - Ghế</span>
                                <span class="info-value">${ticket.roomName} — <strong>${ticket.seatNumber}</strong></span>
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
                                <span class="info-label">🎫 Loại vé</span>
                                <span class="info-value">${ticket.ticketType}</span>
                            </div>
                            <div class="info-item">
                                <span class="info-label">👤 Khách hàng</span>
                                <span class="info-value">
                                    <c:choose>
                                        <c:when test="${ticket.customerName != null}">${ticket.customerName}</c:when>
                                        <c:otherwise><em style="color:#999">Vé lẻ</em></c:otherwise>
                                    </c:choose>
                                </span>
                            </div>
                            <div class="info-item">
                                <span class="info-label">💰 Thành tiền</span>
                                <span class="price-highlight">
                                    <fmt:formatNumber value="${ticket.finalPrice}" pattern="#,##0"/> đ
                                </span>
                            </div>
                            <c:if test="${ticket.discountAmount > 0}">
                                <div class="info-item">
                                    <span class="info-label">🏷️ Giảm giá (điểm)</span>
                                    <span class="info-value" style="color:#dc3545;">
                                        -<fmt:formatNumber value="${ticket.discountAmount}" pattern="#,##0"/> đ
                                    </span>
                                </div>
                            </c:if>
                            <c:if test="${ticket.pointsUsed > 0}">
                                <div class="info-item">
                                    <span class="info-label">💎 Điểm đã dùng</span>
                                    <span class="info-value" style="color:#dc3545;">
                                        -<fmt:formatNumber value="${ticket.pointsUsed}" pattern="#,##0"/> điểm
                                    </span>
                                </div>
                            </c:if>
                            <c:if test="${ticket.pointsEarned > 0}">
                                <div class="info-item">
                                    <span class="info-label">⭐ Điểm tích lũy</span>
                                    <span class="info-value" style="color:#28a745;">
                                        +<fmt:formatNumber value="${ticket.pointsEarned}" pattern="#,##0"/> điểm
                                    </span>
                                </div>
                            </c:if>
                        </div>

                        <%-- Thông tin hoàn tiền khi vé đã Refunded --%>
                        <c:if test="${ticket.paymentStatus == 'Refunded'}">
                            <div class="refund-info">
                                💰 Vé này đã được hoàn tiền 80%:
                                <strong>
                                    <fmt:formatNumber value="${ticket.finalPrice * 0.8}" pattern="#,##0"/> đ
                                </strong>
                                (80% của <fmt:formatNumber value="${ticket.finalPrice}" pattern="#,##0"/> đ)
                            </div>
                        </c:if>

                        <%-- Nút hủy vé (chỉ hiện khi vé Paid) --%>
                        <c:if test="${ticket.paymentStatus == 'Paid'}">
                            <form action="tickets" method="post" style="text-align:right;"
                                  onsubmit="return confirm('Hủy vé #${ticket.ticketId}?\n• Hủy trước 2 giờ chiếu: hoàn 80% tiền\n• Hủy trễ hơn: không hoàn tiền')">
                                <input type="hidden" name="action"   value="cancel">
                                <input type="hidden" name="ticketId" value="${ticket.ticketId}">
                                <button type="submit" class="btn btn-danger">❌ Hủy vé</button>
                            </form>
                        </c:if>
                    </div>
                </c:forEach>
            </c:when>
            <c:otherwise>
                <div class="empty-state">
                    <p>😕 Chưa có vé nào được bán</p>
                    <a href="${pageContext.request.contextPath}/showtimes" class="btn">
                        🎫 Đi bán vé ngay
                    </a>
                </div>
            </c:otherwise>
        </c:choose>

    </div>
</div>
</body>
</html>
