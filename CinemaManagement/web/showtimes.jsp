<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Quản Lý Suất Chiếu - Cinema</title>
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
            position: relative;
        }
        .header h1 { font-size: 2em; margin-bottom: 10px; }

        .logout-btn {
            position: absolute;
            right: 20px;
            top: 50%;
            transform: translateY(-50%);
            background: rgba(255,255,255,0.2);
            color: white;
            text-decoration: none;
            padding: 8px 16px;
            border-radius: 8px;
            font-size: 0.85em;
            font-weight: bold;
            transition: background 0.2s;
        }
        .logout-btn:hover { background: rgba(255,255,255,0.35); }

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

        .toolbar {
            display: flex;
            gap: 15px;
            margin-bottom: 30px;
            flex-wrap: wrap;
            align-items: center;
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
        .btn:hover { background: #5568d3; transform: translateY(-2px); }
        .btn-danger  { background: #dc3545; }
        .btn-danger:hover  { background: #c82333; }
        .btn-success { background: #28a745; }
        .btn-success:hover { background: #218838; }
        .btn-warning { background: #e6a817; color: #fff; }
        .btn-warning:hover { background: #c8940f; }
        .btn-secondary { background: #6c757d; }
        .btn-secondary:hover { background: #5a6268; }

        .date-filter input {
            padding: 10px;
            border: 2px solid #dee2e6;
            border-radius: 8px;
            font-size: 16px;
        }

        table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 10px;
            background: white;
        }
        thead { background: #667eea; color: white; }
        th, td { padding: 13px 15px; text-align: left; border-bottom: 1px solid #dee2e6; font-size: 0.92em; }
        tbody tr:hover { background: #f8f9fa; }

        tbody tr.cancelled-row { background: #f8f8f8; color: #999; }
        tbody tr.cancelled-row:hover { background: #f0f0f0; }
        tbody tr.cancelled-row td { color: #999; }

        .status-badge {
            padding: 5px 14px;
            border-radius: 12px;
            font-weight: bold;
            font-size: 0.82em;
            display: inline-block;
            white-space: nowrap;
        }
        .status-scheduled { background: #28a745; color: white; }
        .status-cancelled { background: #dc3545; color: white; }

        .price { color: #667eea; font-weight: bold; }

        .progress-bar {
            width: 100%;
            height: 8px;
            background: #e9ecef;
            border-radius: 5px;
            overflow: hidden;
            min-width: 60px;
        }
        .progress-fill {
            height: 100%;
            background: linear-gradient(90deg, #28a745, #20c997);
            transition: width 0.3s;
        }
        .progress-fill.medium { background: linear-gradient(90deg, #ffc107, #fd7e14); }
        .progress-fill.low    { background: linear-gradient(90deg, #dc3545, #e83e8c); }

        .action-group { display: flex; flex-direction: column; gap: 5px; min-width: 130px; }
        .action-group .btn {
            padding: 6px 12px;
            font-size: 0.82em;
            text-align: center;
            display: block;
        }

        .no-results { text-align: center; padding: 50px; color: #666; font-size: 1.2em; }

        .filter-info {
            margin-bottom: 20px;
            color: #667eea;
            font-weight: bold;
            padding: 10px 15px;
            background: #f0f4ff;
            border-radius: 8px;
            border-left: 4px solid #667eea;
        }
    </style>
</head>
<body>
<div class="container">
    <div class="header">
        <h1>🎫 QUẢN LÝ SUẤT CHIẾU</h1>
        <p>Lịch chiếu phim và quản lý phòng chiếu</p>
        <a href="${pageContext.request.contextPath}/login?action=logout" class="logout-btn">🚪 Đăng xuất</a>
    </div>

    <div class="nav">
        <a href="${pageContext.request.contextPath}/">🏠 Trang chủ</a>
        <a href="${pageContext.request.contextPath}/movies">🎥 Phim</a>
        <a href="${pageContext.request.contextPath}/showtimes">🎫 Suất chiếu</a>
        <a href="${pageContext.request.contextPath}/tickets">🎟️ Bán vé</a>
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

        <div class="toolbar">
            <a href="showtimes?action=add" class="btn btn-success">➕ Thêm Suất Chiếu</a>

            <div class="date-filter">
                <form action="showtimes" method="get" style="display:flex; gap:10px; align-items:center;">
                    <input type="hidden" name="action" value="date">
                    <input type="date" name="date" value="${selectedDate}"
                           onchange="this.form.submit()" title="Lọc theo ngày">
                </form>
            </div>

            <a href="showtimes" class="btn btn-secondary">📋 Tất cả</a>
        </div>

        <c:if test="${selectedDate != null}">
            <div class="filter-info">📅 Hiển thị suất chiếu ngày: <strong>${selectedDate}</strong></div>
        </c:if>

        <c:if test="${selectedMovie != null}">
            <div class="filter-info">🎬 Lịch chiếu phim: <strong>${selectedMovie.movieName}</strong></div>
        </c:if>

        <c:choose>
            <c:when test="${not empty showtimes}">
                <table>
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Tên phim</th>
                            <th>Phòng chiếu</th>
                            <th>Ngày chiếu</th>
                            <th>Giờ chiếu</th>
                            <th>Giá vé</th>
                            <th>Ghế trống</th>
                            <th>Lấp đầy</th>
                            <th>Trạng thái</th>
                            <th>Hành động</th>
                        </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="st" items="${showtimes}">
                        <tr class="${st.status == 'Cancelled' ? 'cancelled-row' : ''}">
                            <td>#${st.showtimeId}</td>
                            <td><strong>${st.movieName}</strong></td>
                            <td>${st.roomName}</td>
                            <td><fmt:formatDate value="${st.showDate}" pattern="dd/MM/yyyy"/></td>
                            <td><fmt:formatDate value="${st.showTime}" pattern="HH:mm"/></td>
                            <td class="price">
                                <fmt:formatNumber value="${st.ticketPrice}" pattern="#,##0"/> đ
                            </td>
                            <td>${st.seatsAvailable} / ${st.totalSeats}</td>
                            <td style="min-width:130px;">
                                <div style="display:flex; align-items:center; gap:8px;">
                                    <span style="min-width:45px; font-weight:bold; font-size:0.9em;">
                                        <fmt:formatNumber value="${st.occupancyRate}" pattern="#,##0.0"/>%
                                    </span>
                                    <div class="progress-bar" style="flex:1;">
                                        <div class="progress-fill ${st.occupancyRate >= 70 ? '' : st.occupancyRate >= 40 ? 'medium' : 'low'}"
                                             style="width:${st.occupancyRate}%"></div>
                                    </div>
                                </div>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${st.status == 'Scheduled'}">
                                        <span class="status-badge status-scheduled">🟢 Đang chiếu</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="status-badge status-cancelled">🔴 Đã hủy</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <div class="action-group">
                                    <c:choose>
                                        <c:when test="${st.status == 'Scheduled'}">
                                            <%-- Suất đang chiếu: Bán vé + Hủy suất --%>
                                            <a href="${pageContext.request.contextPath}/tickets?action=sell&showtimeId=${st.showtimeId}"
                                               class="btn btn-success">🎟️ Bán vé</a>
                                            <form action="showtimes" method="post"
                                                  onsubmit="return confirm('Bạn có chắc muốn hủy suất chiếu #${st.showtimeId}?')">
                                                <input type="hidden" name="action" value="cancel">
                                                <input type="hidden" name="showtimeId" value="${st.showtimeId}">
                                                <button type="submit" class="btn btn-danger">❌ Hủy suất</button>
                                            </form>
                                        </c:when>
                                        <c:otherwise>
                                            <%-- Suất đã hủy: Kích hoạt lại + Xóa --%>
                                            <a href="showtimes?action=edit&showtimeId=${st.showtimeId}"
                                               class="btn btn-success">✅ Kích hoạt</a>
                                            <form action="showtimes" method="post"
                                                  onsubmit="return confirm('Xóa vĩnh viễn suất chiếu #${st.showtimeId}? Hành động này không thể hoàn tác!')">
                                                <input type="hidden" name="action" value="delete">
                                                <input type="hidden" name="showtimeId" value="${st.showtimeId}">
                                                <button type="submit" class="btn btn-danger">🗑️ Xóa</button>
                                            </form>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </c:when>
            <c:otherwise>
                <div class="no-results">
                    <p>😕 Không có suất chiếu nào</p>
                </div>
            </c:otherwise>
        </c:choose>

    </div>
</div>
</body>
</html>
