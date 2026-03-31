<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Vé Đã Bán - Cinema</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            padding: 30px 20px;
            display: flex; align-items: flex-start; justify-content: center;
        }
        .page-wrap {
            width: 100%; max-width: 500px;
        }

        /* ── Success banner ── */
        .success-banner {
            background: white; border-radius: 14px;
            padding: 22px 28px; margin-bottom: 20px;
            text-align: center;
            box-shadow: 0 8px 30px rgba(0,0,0,0.15);
        }
        .success-icon { font-size: 3em; margin-bottom: 8px; }
        .success-banner h2 { color: #28a745; font-size: 1.4em; margin-bottom: 4px; }
        .success-banner p  { color: #666; font-size: 0.9em; }

        /* ── Ticket card ── */
        .ticket-wrap {
            box-shadow: 0 8px 30px rgba(0,0,0,0.2);
            border-radius: 14px; overflow: hidden;
            margin-bottom: 20px;
        }

        /* Header — thành viên: tím, vé lẻ: xám */
        .ticket-header-member {
            background: linear-gradient(135deg, #534ab7 0%, #764ba2 100%);
            color: white; padding: 22px 24px;
        }
        .ticket-header-guest {
            background: linear-gradient(135deg, #5f5e5a 0%, #444441 100%);
            color: white; padding: 22px 24px;
        }
        .cinema-label { font-size: 0.75em; letter-spacing: 1.5px; opacity: 0.75; margin-bottom: 6px; }
        .ticket-movie  { font-size: 1.5em; font-weight: bold; margin-bottom: 4px; line-height: 1.3; }
        .ticket-badges { display: flex; gap: 8px; margin-top: 10px; flex-wrap: wrap; }
        .badge {
            display: inline-block; padding: 3px 12px;
            border-radius: 20px; font-size: 0.78em; font-weight: bold;
        }
        .badge-type   { background: rgba(255,255,255,0.22); color: white; }
        .badge-vip    { background: #f59e0b; color: #412402; }
        .badge-member { background: #afa9ec; color: #26215c; }
        .badge-guest  { background: #b4b2a9; color: #2c2c2a; }

        /* Đường cắt vé */
        .ticket-cut {
            background: white;
            position: relative; display: flex; align-items: center;
        }
        .ticket-cut::before, .ticket-cut::after {
            content: ''; width: 20px; height: 20px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            border-radius: 50%; flex-shrink: 0;
        }
        .cut-line {
            flex: 1; border-top: 2px dashed #dee2e6; margin: 0 -1px;
        }

        /* Body */
        .ticket-body { background: white; padding: 20px 24px; }
        .info-grid {
            display: grid; grid-template-columns: 1fr 1fr;
            gap: 14px 20px; margin-bottom: 16px;
        }
        .info-item label {
            font-size: 0.75em; color: #999;
            text-transform: uppercase; letter-spacing: 0.5px;
            display: block; margin-bottom: 3px;
        }
        .info-item span  { font-size: 1.05em; font-weight: bold; color: #333; }
        .info-item.seat-big span { font-size: 1.8em; color: #667eea; }

        /* Divider */
        .info-divider { border: none; border-top: 1px solid #eee; margin: 14px 0; }

        /* Member section */
        .member-section {
            background: linear-gradient(135deg, #eeedfe 0%, #e0dcfd 100%);
            border-radius: 10px; padding: 14px 16px; margin-bottom: 16px;
        }
        .member-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px; }
        .member-row:last-child { margin-bottom: 0; }
        .member-row label { font-size: 0.82em; color: #534ab7; }
        .member-row span  { font-size: 0.92em; font-weight: bold; color: #3c3489; }
        .points-used   { color: #dc3545 !important; }
        .points-earned { color: #28a745 !important; }

        /* Guest section */
        .guest-section {
            background: #f8f9fa; border-radius: 10px;
            padding: 12px 16px; margin-bottom: 16px;
            display: flex; align-items: center; gap: 10px;
        }
        .guest-icon { font-size: 1.5em; }
        .guest-text label { font-size: 0.8em; color: #999; display: block; }
        .guest-text span  { font-size: 0.9em; color: #666; }

        /* Footer */
        .ticket-footer {
            background: white; padding: 16px 24px;
            display: flex; align-items: center; justify-content: space-between;
            border-top: 1px solid #f0f0f0;
        }
        .ticket-id    { font-size: 0.78em; color: #bbb; margin-bottom: 4px; }
        .final-price  { font-size: 1.5em; font-weight: bold; color: #667eea; }
        .orig-price   { font-size: 0.85em; color: #bbb; text-decoration: line-through; }

        /* QR box */
        .qr-box {
            width: 62px; height: 62px;
            border: 2px solid #eee; border-radius: 8px;
            display: flex; flex-direction: column;
            align-items: center; justify-content: center;
            font-size: 0.65em; color: #ccc; gap: 4px;
        }
        .qr-grid {
            display: grid; grid-template-columns: repeat(5, 7px);
            grid-template-rows: repeat(5, 7px); gap: 1px;
        }
        .qr-cell { border-radius: 1px; }

        /* Action buttons */
        .actions { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
        .btn-print {
            padding: 13px; background: white; color: #667eea;
            border: 2px solid #667eea; border-radius: 10px;
            font-weight: bold; font-size: 0.95em; cursor: pointer;
            transition: all 0.2s; text-align: center;
        }
        .btn-print:hover { background: #667eea; color: white; }
        .btn-new {
            padding: 13px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white; border: none; border-radius: 10px;
            font-weight: bold; font-size: 0.95em; cursor: pointer;
            transition: all 0.2s; text-decoration: none; display: block; text-align: center;
        }
        .btn-new:hover { opacity: 0.9; }

        @media print {
            body { background: white; padding: 0; }
            .page-wrap { max-width: 100%; }
            .success-banner, .actions { display: none; }
            .ticket-wrap { box-shadow: none; }
        }
    </style>
</head>
<body>
<div class="page-wrap">

    <%-- Banner thành công --%>
    <div class="success-banner">
        <div class="success-icon">🎉</div>
        <h2>Bán vé thành công!</h2>
        <p>
            <c:choose>
                <c:when test="${soldTicket.customerName != null}">
                    Vé đã được ghi nhận cho <strong>${soldTicket.customerName}</strong>
                </c:when>
                <c:otherwise>Vé lẻ đã được ghi nhận</c:otherwise>
            </c:choose>
        </p>
    </div>

    <%-- ── Thẻ vé ── --%>
    <div class="ticket-wrap">

        <%-- Header: phân biệt màu theo loại --%>
        <c:choose>
            <c:when test="${soldTicket.customerName != null}">
                <div class="ticket-header-member">
                    <div class="cinema-label">CINEMA MANAGEMENT</div>
                    <div class="ticket-movie">${soldTicket.movieName}</div>
                    <div class="ticket-badges">
                        <span class="badge badge-type">${soldTicket.ticketType}</span>
                        <c:if test="${soldTicket.pointsEarned > 0 || soldTicket.pointsUsed > 0}">
                            <span class="badge badge-member">⭐ Thành viên</span>
                        </c:if>
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <div class="ticket-header-guest">
                    <div class="cinema-label">CINEMA MANAGEMENT</div>
                    <div class="ticket-movie">${soldTicket.movieName}</div>
                    <div class="ticket-badges">
                        <span class="badge badge-type">${soldTicket.ticketType}</span>
                        <span class="badge badge-guest">Vé lẻ</span>
                    </div>
                </div>
            </c:otherwise>
        </c:choose>

        <%-- Đường cắt vé --%>
        <div class="ticket-cut">
            <div class="cut-line"></div>
        </div>

        <%-- Body: thông tin chung --%>
        <div class="ticket-body">
            <div class="info-grid">
                <div class="info-item seat-big">
                    <label>Ghế</label>
                    <span>${soldTicket.seatNumber}</span>
                </div>
                <div class="info-item">
                    <label>Phòng chiếu</label>
                    <span>${soldTicket.roomName}</span>
                </div>
                <div class="info-item">
                    <label>Ngày chiếu</label>
                    <span><fmt:formatDate value="${soldTicket.showDate}" pattern="dd/MM/yyyy"/></span>
                </div>
                <div class="info-item">
                    <label>Giờ chiếu</label>
                    <span>${soldTicket.showTime}</span>
                </div>
            </div>

            <hr class="info-divider">

            <%-- Section thành viên --%>
            <c:if test="${soldTicket.customerName != null}">
                <div class="member-section">
                    <div class="member-row">
                        <label>👤 Khách hàng</label>
                        <span>${soldTicket.customerName}</span>
                    </div>
                    <c:if test="${soldTicket.pointsUsed > 0}">
                        <div class="member-row">
                            <label>💎 Điểm đã dùng</label>
                            <span class="points-used">
                                -<fmt:formatNumber value="${soldTicket.pointsUsed}" pattern="#,##0"/> điểm
                            </span>
                        </div>
                    </c:if>
                    <c:if test="${soldTicket.pointsEarned > 0}">
                        <div class="member-row">
                            <label>⭐ Điểm tích được</label>
                            <span class="points-earned">
                                +<fmt:formatNumber value="${soldTicket.pointsEarned}" pattern="#,##0"/> điểm
                            </span>
                        </div>
                    </c:if>
                </div>
            </c:if>

            <%-- Section vé lẻ --%>
            <c:if test="${soldTicket.customerName == null}">
                <div class="guest-section">
                    <div class="guest-icon">👤</div>
                    <div class="guest-text">
                        <label>Loại vé</label>
                        <span>Vé lẻ — không tích điểm thành viên</span>
                    </div>
                </div>
            </c:if>

        </div>

        <%-- Footer: giá + QR --%>
        <div class="ticket-footer">
            <div>
                <div class="ticket-id">Mã vé #${soldTicket.ticketId}</div>
                <c:if test="${soldTicket.discountAmount > 0}">
                    <div class="orig-price">
                        <fmt:formatNumber value="${soldTicket.ticketPrice}" pattern="#,##0"/> đ
                    </div>
                </c:if>
                <div class="final-price">
                    <fmt:formatNumber value="${soldTicket.finalPrice}" pattern="#,##0"/> đ
                </div>
            </div>
            <%-- QR giả dạng pixel --%>
            <div class="qr-box">
                <div class="qr-grid" id="qrGrid"></div>
            </div>
        </div>

    </div>

    <%-- Buttons --%>
    <div class="actions">
        <button class="btn-print" onclick="window.print()">🖨️ In vé</button>
        <a href="${pageContext.request.contextPath}/showtimes" class="btn-new">🎟️ Bán vé mới</a>
    </div>

</div>

<script>
    // Tạo QR pattern giả dựa trên ticketId
    const ticketId = ${soldTicket.ticketId};
    const grid = document.getElementById('qrGrid');
    const seed = ticketId * 2654435761;
    const colors = ['#534AB7','#B4B2A9','#534AB7','#D3D1C7','#534AB7'];
    for (let i = 0; i < 25; i++) {
        const cell = document.createElement('div');
        cell.className = 'qr-cell';
        const v = (seed >> i) & 1;
        cell.style.background = v ? '#534AB7' : '#f0f0f0';
        grid.appendChild(cell);
    }
</script>
</body>
</html>
