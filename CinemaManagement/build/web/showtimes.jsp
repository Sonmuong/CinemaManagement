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

        .btn-danger { background: #dc3545; }
        .btn-danger:hover { background: #c82333; }

        .btn-success { background: #28a745; }
        .btn-success:hover { background: #218838; }

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
        th, td { padding: 15px; text-align: left; border-bottom: 1px solid #dee2e6; }
        tbody tr:hover { background: #f8f9fa; }

        .status-badge {
            padding: 5px 15px;
            border-radius: 15px;
            font-weight: bold;
            font-size: 0.85em;
        }
        .status-scheduled { background: #28a745; color: white; }
        .status-cancelled  { background: #dc3545; color: white; }

        .price { color: #667eea; font-weight: bold; font-size: 1.1em; }

        .progress-bar {
            width: 100%;
            height: 10px;
            background: #e9ecef;
            border-radius: 5px;
            overflow: hidden;
        }
        .progress-fill {
            height: 100%;
            background: linear-gradient(90deg, #28a745, #20c997);
            transition: width 0.3s;
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
        <h1>🎫 QUẢN LÝ SUẤT CHIẾU</h1>
        <p>Lịch chiếu phim và quản lý phòng chiếu</p>
    </div>

    <div class="nav">
        <a href="${pageContext.request.contextPath}/">🏠 Trang chủ</a>
        <a href="${pageContext.request.contextPath}/movies">🎥 Phim</a>
        <a href="${pageContext.request.contextPath}/showtimes">🎫 Suất chiếu</a>
        <a href="${pageContext.request.contextPath}/tickets">🎟️ Bán vé</a>
        <a href="${pageContext.request.contextPath}/customers">👥 Khách hàng</a>
    </div>

    <div class="content">

        <%-- Thông báo từ session --%>
        <c:if test="${sessionScope.message != null}">
            <div class="message ${sessionScope.messageType}">
                ${sessionScope.message}
            </div>
            <c:remove var="message" scope="session"/>
            <c:remove var="messageType" scope="session"/>
        </c:if>

        <%-- Toolbar --%>
        <div class="toolbar">
            <a href="showtimes?action=add" class="btn">➕ Thêm Suất Chiếu</a>

            <div class="date-filter">
                <form action="showtimes" method="get" style="display:flex; gap:10px;">
                    <input type="hidden" name="action" value="date">
                    <input type="date" name="date" value="${selectedDate}"
                           onchange="this.form.submit()">
                </form>
            </div>

            <a href="showtimes" class="btn" style="background:#6c757d;">📋 Tất cả</a>
        </div>

        <%-- Hiển thị bộ lọc đang áp dụng --%>
        <c:if test="${selectedDate != null}">
            <p style="margin-bottom:20px; color:#667eea; font-weight:bold;">
                📅 Hiển thị suất chiếu ngày: ${selectedDate}
            </p>
        </c:if>

        <c:if test="${selectedMovie != null}">
            <p style="margin-bottom:20px; color:#667eea; font-weight:bold;">
                🎬 Lịch chiếu phim: <strong>${selectedMovie.movieName}</strong>
            </p>
        </c:if>

        <%-- Bảng suất chiếu --%>
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
                            <th>Ghế còn trống</th>
                            <th>Tỷ lệ lấp đầy</th>
                            <th>Trạng thái</th>
                            <th>Hành động</th>
                        </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="st" items="${showtimes}">
                        <tr>
                            <td>#${st.showtimeId}</td>
                            <td><strong>${st.movieName}</strong></td>
                            <td>${st.roomName}</td>
                            <td>
                                <fmt:formatDate value="${st.showDate}" pattern="dd/MM/yyyy"/>
                            </td>
                            <td>
                                <fmt:formatDate value="${st.showTime}" pattern="HH:mm"/>
                            </td>
                            <td class="price">
                                <fmt:formatNumber value="${st.ticketPrice}" pattern="#,##0"/> đ
                            </td>
                            <td>
                                ${st.seatsAvailable} / ${st.totalSeats}
                            </td>
                            <td style="min-width:120px;">
                                <div style="display:flex; align-items:center; gap:8px;">
                                    <span style="min-width:40px; font-weight:bold;">
                                        <fmt:formatNumber value="${st.occupancyRate}" pattern="#,##0.0"/>%
                                    </span>
                                    <div class="progress-bar" style="flex:1;">
                                        <div class="progress-fill"
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
                                <c:if test="${st.status == 'Scheduled'}">
                                    <%-- Nút bán vé --%>
                                    <a href="${pageContext.request.contextPath}/tickets?action=sell&showtimeId=${st.showtimeId}"
                                       class="btn btn-success"
                                       style="padding:6px 12px; font-size:0.85em; margin-bottom:5px; display:inline-block;">
                                        🎟️ Bán vé
                                    </a>
                                    <%-- Nút hủy suất chiếu --%>
                                    <form action="showtimes" method="post" style="display:inline;"
                                          onsubmit="return confirm('Bạn có chắc muốn hủy suất chiếu này?')">
                                        <input type="hidden" name="action" value="cancel">
                                        <input type="hidden" name="showtimeId" value="${st.showtimeId}">
                                        <button type="submit" class="btn btn-danger"
                                                style="padding:6px 12px; font-size:0.85em;">
                                            ❌ Hủy
                                        </button>
                                    </form>
                                </c:if>
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
