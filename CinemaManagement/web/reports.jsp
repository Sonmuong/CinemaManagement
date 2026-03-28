<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Thống Kê & Báo Cáo - Cinema</title>
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
        .header p { opacity: 0.9; }

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

        /* Tab navigation */
        .tabs {
            display: flex;
            background: #f0f2ff;
            border-bottom: 3px solid #667eea;
            overflow-x: auto;
            padding: 0 20px;
        }
        .tab-btn {
            padding: 14px 22px;
            border: none;
            background: transparent;
            color: #555;
            font-size: 0.95em;
            font-weight: 600;
            cursor: pointer;
            white-space: nowrap;
            border-bottom: 3px solid transparent;
            margin-bottom: -3px;
            transition: all 0.2s;
        }
        .tab-btn:hover { color: #667eea; background: rgba(102,126,234,0.08); }
        .tab-btn.active { color: #667eea; border-bottom-color: #667eea; background: white; }

        .content { padding: 30px; }

        /* Section panels */
        .tab-panel { display: none; }
        .tab-panel.active { display: block; }

        /* Summary cards */
        .summary-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 20px;
            margin-bottom: 30px;
        }
        .stat-card {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 22px 20px;
            border-radius: 12px;
            text-align: center;
            box-shadow: 0 4px 15px rgba(102,126,234,0.3);
        }
        .stat-card .icon { font-size: 2em; margin-bottom: 8px; }
        .stat-card .number { font-size: 2em; font-weight: bold; margin-bottom: 4px; }
        .stat-card .label { font-size: 0.88em; opacity: 0.9; }

        .stat-card.green { background: linear-gradient(135deg, #28a745 0%, #20c997 100%); }
        .stat-card.orange { background: linear-gradient(135deg, #fd7e14 0%, #ffc107 100%); }
        .stat-card.red { background: linear-gradient(135deg, #dc3545 0%, #e83e8c 100%); }
        .stat-card.teal { background: linear-gradient(135deg, #17a2b8 0%, #6f42c1 100%); }

        /* Filter bar */
        .filter-bar {
            display: flex;
            gap: 12px;
            margin-bottom: 25px;
            flex-wrap: wrap;
            align-items: center;
            background: #f8f9fa;
            padding: 15px 20px;
            border-radius: 10px;
        }
        .filter-bar label { font-weight: 600; color: #555; font-size: 0.9em; }
        .filter-bar select, .filter-bar input {
            padding: 8px 14px;
            border: 2px solid #dee2e6;
            border-radius: 7px;
            font-size: 0.9em;
            outline: none;
            transition: border-color 0.2s;
        }
        .filter-bar select:focus, .filter-bar input:focus { border-color: #667eea; }
        .btn-filter {
            padding: 8px 20px;
            background: #667eea;
            color: white;
            border: none;
            border-radius: 7px;
            font-weight: bold;
            cursor: pointer;
            transition: all 0.2s;
        }
        .btn-filter:hover { background: #5568d3; }

        /* Tables */
        .section-title {
            font-size: 1.1em;
            font-weight: bold;
            color: #2E4057;
            margin-bottom: 15px;
            padding-bottom: 8px;
            border-bottom: 2px solid #e9ecef;
        }

        table {
            width: 100%;
            border-collapse: collapse;
            background: white;
            border-radius: 8px;
            overflow: hidden;
            box-shadow: 0 2px 10px rgba(0,0,0,0.07);
            margin-bottom: 30px;
        }
        thead { background: #667eea; color: white; }
        th {
            padding: 13px 15px;
            text-align: left;
            font-weight: 600;
            font-size: 0.9em;
            letter-spacing: 0.3px;
        }
        th.center, td.center { text-align: center; }
        th.right, td.right { text-align: right; }
        td {
            padding: 12px 15px;
            border-bottom: 1px solid #f0f0f0;
            font-size: 0.92em;
        }
        tbody tr:hover { background: #f8f9fa; }
        tbody tr:nth-child(even) { background: #fafbff; }
        tbody tr:nth-child(even):hover { background: #f0f4ff; }

        /* Revenue number */
        .revenue { color: #28a745; font-weight: bold; }
        .revenue-negative { color: #dc3545; font-weight: bold; }

        /* Rank badge */
        .rank-badge {
            display: inline-block;
            width: 28px; height: 28px;
            border-radius: 50%;
            background: #667eea;
            color: white;
            font-weight: bold;
            font-size: 0.85em;
            line-height: 28px;
            text-align: center;
        }
        .rank-badge.gold { background: #ffc107; color: #333; }
        .rank-badge.silver { background: #adb5bd; }
        .rank-badge.bronze { background: #cd7f32; }

        /* VIP badge */
        .vip-badge {
            background: #ffc107;
            color: #333;
            padding: 3px 10px;
            border-radius: 12px;
            font-size: 0.8em;
            font-weight: bold;
        }

        /* Progress bar */
        .progress-wrap { background: #e9ecef; border-radius: 10px; height: 10px; overflow: hidden; min-width: 80px; }
        .progress-fill {
            height: 100%;
            border-radius: 10px;
            background: linear-gradient(90deg, #28a745, #20c997);
            transition: width 0.4s;
        }
        .progress-fill.medium { background: linear-gradient(90deg, #ffc107, #fd7e14); }
        .progress-fill.low { background: linear-gradient(90deg, #dc3545, #e83e8c); }

        .occ-cell { display: flex; align-items: center; gap: 10px; }
        .occ-text { min-width: 45px; font-weight: bold; }

        /* No data */
        .no-data { text-align: center; padding: 40px; color: #888; font-size: 1.05em; }

        /* Summary report highlight boxes */
        .highlight-box {
            background: linear-gradient(135deg, #e8f5e9, #c8e6c9);
            border-left: 5px solid #28a745;
            border-radius: 8px;
            padding: 16px 20px;
            margin-bottom: 15px;
        }
        .highlight-box.danger {
            background: linear-gradient(135deg, #fce4ec, #f8bbd0);
            border-left-color: #e53935;
        }
        .highlight-box .hl-title { font-size: 0.85em; color: #555; margin-bottom: 4px; }
        .highlight-box .hl-value { font-size: 1.3em; font-weight: bold; color: #2E4057; }
        .highlight-box .hl-sub { font-size: 0.85em; color: #28a745; margin-top: 2px; }
        .highlight-box.danger .hl-sub { color: #e53935; }

        .two-col { display: grid; grid-template-columns: 1fr 1fr; gap: 15px; margin-bottom: 20px; }

        @media (max-width: 700px) {
            .two-col { grid-template-columns: 1fr; }
            .summary-grid { grid-template-columns: 1fr 1fr; }
        }
    </style>
</head>
<body>
<div class="container">
    <div class="header">
        <h1>📊 THỐNG KÊ & BÁO CÁO</h1>
        <p>Tổng hợp doanh thu, vé bán, tỷ lệ lấp đầy và khách hàng</p>
    </div>

    <div class="nav">
        <a href="${pageContext.request.contextPath}/">🏠 Trang chủ</a>
        <a href="${pageContext.request.contextPath}/movies">🎥 Phim</a>
        <a href="${pageContext.request.contextPath}/showtimes">🎫 Suất chiếu</a>
        <a href="${pageContext.request.contextPath}/tickets">🎟️ Bán vé</a>
        <a href="${pageContext.request.contextPath}/customers">👥 Khách hàng</a>
        <a href="${pageContext.request.contextPath}/reports" style="color:#764ba2; text-decoration:underline;">📊 Báo cáo</a>
    </div>

    <!-- TAB NAV -->
    <div class="tabs">
        <button class="tab-btn ${activeTab == null || activeTab == 'dashboard' ? 'active' : ''}"
                onclick="showTab('dashboard')">🏠 Tổng quan</button>
        <button class="tab-btn ${activeTab == 'revenue' ? 'active' : ''}"
                onclick="showTab('revenue')">💰 Doanh thu</button>
        <button class="tab-btn ${activeTab == 'tickets' ? 'active' : ''}"
                onclick="showTab('tickets')">🎟️ Vé theo phim/suất</button>
        <button class="tab-btn ${activeTab == 'occupancy' ? 'active' : ''}"
                onclick="showTab('occupancy')">🏛️ Tỷ lệ lấp đầy</button>
        <button class="tab-btn ${activeTab == 'top10' ? 'active' : ''}"
                onclick="showTab('top10')">🏆 Top 10 phim</button>
        <button class="tab-btn ${activeTab == 'customers' ? 'active' : ''}"
                onclick="showTab('customers')">⭐ Khách hàng điểm cao</button>
        <button class="tab-btn ${activeTab == 'summary' ? 'active' : ''}"
                onclick="showTab('summary')">📋 Tổng kết 6 tháng</button>
    </div>

    <div class="content">

        <%-- ============ TỔNG QUAN ============ --%>
        <div id="tab-dashboard" class="tab-panel ${activeTab == null || activeTab == 'dashboard' ? 'active' : ''}">

            <c:if test="${summary != null}">
                <div class="summary-grid">
                    <div class="stat-card green">
                        <div class="icon">💰</div>
                        <div class="number">
                            <fmt:formatNumber value="${summary.total_revenue / 1000000}" pattern="#,##0.0"/>M
                        </div>
                        <div class="label">Doanh thu 6 tháng (VNĐ)</div>
                    </div>
                    <div class="stat-card">
                        <div class="icon">🎟️</div>
                        <div class="number">${summary.total_tickets}</div>
                        <div class="label">Tổng vé đã bán</div>
                    </div>
                    <div class="stat-card orange">
                        <div class="icon">🎬</div>
                        <div class="number">${summary.total_showtimes}</div>
                        <div class="label">Suất chiếu</div>
                    </div>
                    <div class="stat-card teal">
                        <div class="icon">⭐</div>
                        <div class="number">${summary.vip_customers}</div>
                        <div class="label">Khách hàng VIP</div>
                    </div>
                </div>
            </c:if>

            <div class="two-col">
                <div>
                    <div class="section-title">🏆 Top 10 phim tháng này</div>
                    <c:choose>
                        <c:when test="${not empty top10}">
                            <table>
                                <thead><tr>
                                    <th class="center">#</th>
                                    <th>Tên phim</th>
                                    <th class="center">Vé</th>
                                    <th class="right">Doanh thu</th>
                                </tr></thead>
                                <tbody>
                                <c:forEach var="item" items="${top10}">
                                    <tr>
                                        <td class="center">
                                            <c:choose>
                                                <c:when test="${item.rank == 1}"><span class="rank-badge gold">1</span></c:when>
                                                <c:when test="${item.rank == 2}"><span class="rank-badge silver">2</span></c:when>
                                                <c:when test="${item.rank == 3}"><span class="rank-badge bronze">3</span></c:when>
                                                <c:otherwise><span class="rank-badge">${item.rank}</span></c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td><strong>${item.movie_name}</strong></td>
                                        <td class="center">${item.ticket_count}</td>
                                        <td class="right revenue">
                                            <fmt:formatNumber value="${item.total_revenue}" pattern="#,##0"/> đ
                                        </td>
                                    </tr>
                                </c:forEach>
                                </tbody>
                            </table>
                        </c:when>
                        <c:otherwise><div class="no-data">😕 Chưa có dữ liệu tháng này</div></c:otherwise>
                    </c:choose>
                </div>

                <div>
                    <div class="section-title">⭐ Top 5 khách hàng điểm cao</div>
                    <c:choose>
                        <c:when test="${not empty topCustomers}">
                            <table>
                                <thead><tr>
                                    <th>#</th>
                                    <th>Họ tên</th>
                                    <th class="right">Điểm</th>
                                    <th class="center">Hạng</th>
                                </tr></thead>
                                <tbody>
                                <c:forEach var="c" items="${topCustomers}">
                                    <tr>
                                        <td><span class="rank-badge">${c.rank}</span></td>
                                        <td><strong>${c.full_name}</strong><br>
                                            <small style="color:#888">${c.phone}</small></td>
                                        <td class="right revenue">
                                            <fmt:formatNumber value="${c.loyalty_points}" pattern="#,##0"/>
                                        </td>
                                        <td class="center">
                                            <c:if test="${c.is_vip}"><span class="vip-badge">⭐ VIP</span></c:if>
                                            <c:if test="${!c.is_vip}"><span style="color:#999">Thường</span></c:if>
                                        </td>
                                    </tr>
                                </c:forEach>
                                </tbody>
                            </table>
                        </c:when>
                        <c:otherwise><div class="no-data">😕 Chưa có dữ liệu</div></c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>

        <%-- ============ DOANH THU ============ --%>
        <div id="tab-revenue" class="tab-panel ${activeTab == 'revenue' ? 'active' : ''}">
            <div class="filter-bar">
                <form action="reports" method="get" style="display:flex;gap:12px;align-items:center;flex-wrap:wrap;">
                    <input type="hidden" name="action" value="revenue">
                    <label>Chu kỳ:</label>
                    <select name="period">
                        <option value="day" ${selectedPeriod == 'day' ? 'selected' : ''}>Theo ngày</option>
                        <option value="week" ${selectedPeriod == 'week' ? 'selected' : ''}>Theo tuần</option>
                        <option value="month" ${selectedPeriod == 'month' || selectedPeriod == null ? 'selected' : ''}>Theo tháng</option>
                        <option value="quarter" ${selectedPeriod == 'quarter' ? 'selected' : ''}>Theo quý</option>
                        <option value="year" ${selectedPeriod == 'year' ? 'selected' : ''}>Theo năm</option>
                    </select>
                    <label>Năm:</label>
                    <input type="number" name="year" value="${selectedYear != null ? selectedYear : currentYear}"
                           min="2020" max="2030" style="width:90px;">
                    <label>Tháng:</label>
                    <select name="month">
                        <option value="">-- Tất cả --</option>
                        <c:forEach var="m" begin="1" end="12">
                            <option value="${m}" ${selectedMonth == m ? 'selected' : ''}>${m}</option>
                        </c:forEach>
                    </select>
                    <button type="submit" class="btn-filter">🔍 Xem</button>
                </form>
            </div>

            <div class="section-title">💰 Thống kê doanh thu</div>
            <c:choose>
                <c:when test="${not empty revenueData}">
                    <table>
                        <thead><tr>
                            <th>Kỳ</th>
                            <th class="center">Số vé bán</th>
                            <th class="right">Doanh thu (VNĐ)</th>
                        </tr></thead>
                        <tbody>
                        <c:set var="grandTotal" value="0"/>
                        <c:set var="grandTickets" value="0"/>
                        <c:forEach var="row" items="${revenueData}">
                            <c:set var="grandTotal" value="${grandTotal + row.total_revenue}"/>
                            <c:set var="grandTickets" value="${grandTickets + row.ticket_count}"/>
                            <tr>
                                <td><strong>${row.period_label}</strong></td>
                                <td class="center">${row.ticket_count}</td>
                                <td class="right revenue">
                                    <fmt:formatNumber value="${row.total_revenue}" pattern="#,##0"/> đ
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                        <tfoot>
                            <tr style="background:#f0f4ff; font-weight:bold;">
                                <td>TỔNG CỘNG</td>
                                <td class="center">${grandTickets}</td>
                                <td class="right revenue">
                                    <fmt:formatNumber value="${grandTotal}" pattern="#,##0"/> đ
                                </td>
                            </tr>
                        </tfoot>
                    </table>
                </c:when>
                <c:otherwise><div class="no-data">😕 Không có dữ liệu cho bộ lọc đã chọn</div></c:otherwise>
            </c:choose>
        </div>

        <%-- ============ VÉ THEO PHIM/SUẤT ============ --%>
        <div id="tab-tickets" class="tab-panel ${activeTab == 'tickets' ? 'active' : ''}">

            <div class="section-title">🎬 Vé bán theo từng phim</div>
            <c:choose>
                <c:when test="${not empty ticketsByMovie}">
                    <table>
                        <thead><tr>
                            <th>Tên phim</th>
                            <th class="center">Số vé bán</th>
                            <th class="right">Doanh thu (VNĐ)</th>
                            <th class="right">Giá vé TB</th>
                        </tr></thead>
                        <tbody>
                        <c:forEach var="row" items="${ticketsByMovie}">
                            <tr>
                                <td><strong>${row.movie_name}</strong></td>
                                <td class="center">${row.ticket_count}</td>
                                <td class="right revenue">
                                    <fmt:formatNumber value="${row.total_revenue}" pattern="#,##0"/> đ
                                </td>
                                <td class="right">
                                    <fmt:formatNumber value="${row.avg_price}" pattern="#,##0"/> đ
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </c:when>
                <c:otherwise><div class="no-data">😕 Chưa có dữ liệu</div></c:otherwise>
            </c:choose>

            <div class="section-title">📅 Vé bán theo từng suất chiếu</div>
            <c:choose>
                <c:when test="${not empty ticketsByShowtime}">
                    <table>
                        <thead><tr>
                            <th>Phim</th>
                            <th>Phòng</th>
                            <th class="center">Ngày - Giờ</th>
                            <th class="center">Vé bán / Tổng ghế</th>
                            <th class="center">Tỷ lệ lấp đầy</th>
                            <th class="right">Doanh thu</th>
                        </tr></thead>
                        <tbody>
                        <c:forEach var="row" items="${ticketsByShowtime}">
                            <tr>
                                <td><strong>${row.movie_name}</strong></td>
                                <td>${row.room_name}</td>
                                <td class="center">
                                    <fmt:formatDate value="${row.show_date}" pattern="dd/MM/yyyy"/><br>
                                    <small>${row.show_time}</small>
                                </td>
                                <td class="center">${row.tickets_sold} / ${row.total_seats}</td>
                                <td class="center">
                                    <div class="occ-cell">
                                        <span class="occ-text">${row.occupancy_rate}%</span>
                                        <div class="progress-wrap" style="flex:1;">
                                            <div class="progress-fill ${row.occupancy_rate >= 70 ? '' : row.occupancy_rate >= 40 ? 'medium' : 'low'}"
                                                 style="width:${row.occupancy_rate}%"></div>
                                        </div>
                                    </div>
                                </td>
                                <td class="right revenue">
                                    <fmt:formatNumber value="${row.total_revenue}" pattern="#,##0"/> đ
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </c:when>
                <c:otherwise><div class="no-data">😕 Chưa có dữ liệu</div></c:otherwise>
            </c:choose>
        </div>

        <%-- ============ TỶ LỆ LẤP ĐẦY ============ --%>
        <div id="tab-occupancy" class="tab-panel ${activeTab == 'occupancy' ? 'active' : ''}">
            <div class="section-title">🏛️ Tỷ lệ lấp đầy theo phòng chiếu</div>
            <c:choose>
                <c:when test="${not empty occupancyData}">
                    <table>
                        <thead><tr>
                            <th>Phòng chiếu</th>
                            <th class="center">Tổng ghế</th>
                            <th class="center">Số suất chiếu</th>
                            <th class="center">Tổng vé bán</th>
                            <th class="center">Tỷ lệ lấp đầy TB</th>
                        </tr></thead>
                        <tbody>
                        <c:forEach var="row" items="${occupancyData}">
                            <tr>
                                <td><strong>${row.room_name}</strong></td>
                                <td class="center">${row.total_seats}</td>
                                <td class="center">${row.total_showtimes}</td>
                                <td class="center">${row.total_tickets_sold}</td>
                                <td class="center">
                                    <div class="occ-cell">
                                        <span class="occ-text"
                                              style="color: ${row.avg_occupancy_rate >= 70 ? '#28a745' : row.avg_occupancy_rate >= 40 ? '#fd7e14' : '#dc3545'}; font-weight:bold;">
                                            ${row.avg_occupancy_rate}%
                                        </span>
                                        <div class="progress-wrap" style="flex:1;">
                                            <div class="progress-fill ${row.avg_occupancy_rate >= 70 ? '' : row.avg_occupancy_rate >= 40 ? 'medium' : 'low'}"
                                                 style="width:${row.avg_occupancy_rate}%"></div>
                                        </div>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </c:when>
                <c:otherwise><div class="no-data">😕 Chưa có dữ liệu</div></c:otherwise>
            </c:choose>
        </div>

        <%-- ============ TOP 10 PHIM ============ --%>
        <div id="tab-top10" class="tab-panel ${activeTab == 'top10' ? 'active' : ''}">
            <div class="filter-bar">
                <form action="reports" method="get" style="display:flex;gap:12px;align-items:center;flex-wrap:wrap;">
                    <input type="hidden" name="action" value="top10">
                    <label>Năm:</label>
                    <input type="number" name="year" value="${selectedYear != null ? selectedYear : currentYear}"
                           min="2020" max="2030" style="width:90px;">
                    <label>Tháng:</label>
                    <select name="month">
                        <c:forEach var="m" begin="1" end="12">
                            <option value="${m}" ${selectedMonth == m ? 'selected' : ''}>${m}</option>
                        </c:forEach>
                    </select>
                    <button type="submit" class="btn-filter">🔍 Xem</button>
                </form>
            </div>

            <div class="section-title">🏆 Top 10 phim doanh thu cao nhất
                tháng ${selectedMonth}/${selectedYear}
            </div>
            <c:choose>
                <c:when test="${not empty top10Data}">
                    <table>
                        <thead><tr>
                            <th class="center">Hạng</th>
                            <th>Tên phim</th>
                            <th class="center">Số vé bán</th>
                            <th class="right">Doanh thu (VNĐ)</th>
                        </tr></thead>
                        <tbody>
                        <c:forEach var="item" items="${top10Data}">
                            <tr>
                                <td class="center">
                                    <c:choose>
                                        <c:when test="${item.rank == 1}"><span class="rank-badge gold">🥇</span></c:when>
                                        <c:when test="${item.rank == 2}"><span class="rank-badge silver">🥈</span></c:when>
                                        <c:when test="${item.rank == 3}"><span class="rank-badge bronze">🥉</span></c:when>
                                        <c:otherwise><span class="rank-badge">${item.rank}</span></c:otherwise>
                                    </c:choose>
                                </td>
                                <td><strong>${item.movie_name}</strong></td>
                                <td class="center">${item.ticket_count}</td>
                                <td class="right revenue">
                                    <fmt:formatNumber value="${item.total_revenue}" pattern="#,##0"/> đ
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </c:when>
                <c:otherwise><div class="no-data">😕 Không có dữ liệu tháng này</div></c:otherwise>
            </c:choose>
        </div>

        <%-- ============ KHÁCH HÀNG ĐIỂM CAO ============ --%>
        <div id="tab-customers" class="tab-panel ${activeTab == 'customers' ? 'active' : ''}">
            <div class="section-title">⭐ Khách hàng có điểm tích lũy cao nhất (Top 20)</div>
            <c:choose>
                <c:when test="${not empty topCustomers}">
                    <table>
                        <thead><tr>
                            <th class="center">#</th>
                            <th>Họ tên</th>
                            <th>Số điện thoại</th>
                            <th>Email</th>
                            <th class="center">Tổng vé</th>
                            <th class="right">Tổng chi tiêu</th>
                            <th class="right">Điểm tích lũy</th>
                            <th class="center">Hạng</th>
                        </tr></thead>
                        <tbody>
                        <c:forEach var="c" items="${topCustomers}">
                            <tr>
                                <td class="center"><span class="rank-badge">${c.rank}</span></td>
                                <td><strong>${c.full_name}</strong></td>
                                <td>${c.phone}</td>
                                <td>${c.email}</td>
                                <td class="center">${c.total_tickets}</td>
                                <td class="right revenue">
                                    <fmt:formatNumber value="${c.total_spent}" pattern="#,##0"/> đ
                                </td>
                                <td class="right revenue">
                                    <fmt:formatNumber value="${c.loyalty_points}" pattern="#,##0"/>
                                </td>
                                <td class="center">
                                    <c:if test="${c.is_vip}"><span class="vip-badge">⭐ VIP</span></c:if>
                                    <c:if test="${!c.is_vip}"><span style="color:#999;">Thường</span></c:if>
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </c:when>
                <c:otherwise><div class="no-data">😕 Chưa có dữ liệu</div></c:otherwise>
            </c:choose>
        </div>

        <%-- ============ TỔNG KẾT 6 THÁNG ============ --%>
        <div id="tab-summary" class="tab-panel ${activeTab == 'summary' ? 'active' : ''}">
            <div class="section-title">📋 Tổng kết kinh doanh 6 tháng gần nhất</div>

            <c:if test="${summary != null}">
                <div class="summary-grid" style="margin-bottom:25px;">
                    <div class="stat-card green">
                        <div class="icon">💰</div>
                        <div class="number">
                            <fmt:formatNumber value="${summary.total_revenue / 1000000}" pattern="#,##0.1"/>M
                        </div>
                        <div class="label">Tổng doanh thu (VNĐ)</div>
                    </div>
                    <div class="stat-card">
                        <div class="icon">🎟️</div>
                        <div class="number">${summary.total_tickets}</div>
                        <div class="label">Tổng vé đã bán</div>
                    </div>
                    <div class="stat-card orange">
                        <div class="icon">📊</div>
                        <div class="number">${summary.avg_occupancy}%</div>
                        <div class="label">Tỷ lệ lấp đầy TB</div>
                    </div>
                    <div class="stat-card teal">
                        <div class="icon">⭐</div>
                        <div class="number">${summary.vip_customers}</div>
                        <div class="label">Khách hàng VIP (&ge;1000 điểm)</div>
                    </div>
                </div>

                <div class="two-col">
                    <div class="highlight-box">
                        <div class="hl-title">🏆 Phim doanh thu cao nhất</div>
                        <div class="hl-value">${summary.best_movie}</div>
                        <div class="hl-sub">
                            <fmt:formatNumber value="${summary.best_movie_revenue}" pattern="#,##0"/> VNĐ
                        </div>
                    </div>
                    <div class="highlight-box danger">
                        <div class="hl-title">📉 Phim doanh thu thấp nhất</div>
                        <div class="hl-value">${summary.worst_movie}</div>
                        <div class="hl-sub">
                            <fmt:formatNumber value="${summary.worst_movie_revenue}" pattern="#,##0"/> VNĐ
                        </div>
                    </div>
                </div>
            </c:if>

            <div class="section-title" style="margin-top:10px;">
                ⭐ Danh sách khách hàng thân thiết (VIP - từ 1000 điểm trở lên)
            </div>
            <c:choose>
                <c:when test="${not empty vipCustomers}">
                    <table>
                        <thead><tr>
                            <th class="center">#</th>
                            <th>Họ tên</th>
                            <th>Số điện thoại</th>
                            <th>Email</th>
                            <th class="right">Điểm tích lũy</th>
                            <th class="right">Tổng chi tiêu</th>
                        </tr></thead>
                        <tbody>
                        <c:set var="vipRank" value="0"/>
                        <c:forEach var="c" items="${vipCustomers}">
                            <c:if test="${c.is_vip}">
                                <c:set var="vipRank" value="${vipRank + 1}"/>
                                <tr>
                                    <td class="center"><span class="rank-badge gold">${vipRank}</span></td>
                                    <td><strong>${c.full_name}</strong></td>
                                    <td>${c.phone}</td>
                                    <td>${c.email}</td>
                                    <td class="right revenue">
                                        <fmt:formatNumber value="${c.loyalty_points}" pattern="#,##0"/>
                                    </td>
                                    <td class="right revenue">
                                        <fmt:formatNumber value="${c.total_spent}" pattern="#,##0"/> đ
                                    </td>
                                </tr>
                            </c:if>
                        </c:forEach>
                        </tbody>
                    </table>
                    <c:if test="${vipRank == 0}">
                        <div class="no-data">😕 Chưa có khách hàng VIP</div>
                    </c:if>
                </c:when>
                <c:otherwise><div class="no-data">😕 Chưa có dữ liệu</div></c:otherwise>
            </c:choose>
        </div>

    </div><%-- end content --%>
</div><%-- end container --%>

<script>
    function showTab(name) {
        document.querySelectorAll('.tab-panel').forEach(p => p.classList.remove('active'));
        document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
        document.getElementById('tab-' + name).classList.add('active');
        event.currentTarget.classList.add('active');

        // Reload page with correct action for server-side data
        const actionMap = {
            dashboard: '',
            revenue: 'revenue',
            tickets: 'tickets',
            occupancy: 'occupancy',
            top10: 'top10',
            customers: 'customers',
            summary: 'summary'
        };
        const action = actionMap[name];
        const base = window.location.pathname;
        window.location.href = base + (action ? '?action=' + action : '');
    }
</script>
</body>
</html>
