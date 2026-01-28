<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="jakarta.tags.core" %>
<%@taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Quản Lý Khách Hàng - Cinema</title>
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
            max-width: 1200px;
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
        
        .header h1 {
            font-size: 2em;
            margin-bottom: 10px;
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
        
        .toolbar {
            display: flex;
            gap: 15px;
            margin-bottom: 30px;
            flex-wrap: wrap;
        }
        
        .search-box {
            flex: 1;
            display: flex;
            gap: 10px;
        }
        
        .search-box input {
            flex: 1;
            padding: 12px;
            border: 2px solid #dee2e6;
            border-radius: 8px;
            font-size: 16px;
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
        
        .btn:hover {
            background: #5568d3;
            transform: translateY(-2px);
        }
        
        .btn-secondary {
            background: #6c757d;
        }
        
        .btn-secondary:hover {
            background: #5a6268;
        }
        
        table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 20px;
            background: white;
        }
        
        thead {
            background: #667eea;
            color: white;
        }
        
        th, td {
            padding: 15px;
            text-align: left;
            border-bottom: 1px solid #dee2e6;
        }
        
        tbody tr:hover {
            background: #f8f9fa;
        }
        
        .vip-badge {
            background: #ffc107;
            color: #333;
            padding: 5px 15px;
            border-radius: 15px;
            font-weight: bold;
            font-size: 0.85em;
        }
        
        .points {
            color: #667eea;
            font-weight: bold;
            font-size: 1.1em;
        }
        
        .no-results {
            text-align: center;
            padding: 50px;
            color: #666;
            font-size: 1.2em;
        }
        
        .stats {
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
            margin-bottom: 5px;
        }
        
        .stat-label {
            opacity: 0.9;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>👥 QUẢN LÝ KHÁCH HÀNG</h1>
            <p>Thông tin khách hàng và điểm tích lũy</p>
        </div>
        
        <div class="nav">
            <a href="${pageContext.request.contextPath}/">🏠 Trang chủ</a>
            <a href="${pageContext.request.contextPath}/movies">🎥 Phim</a>
            <a href="${pageContext.request.contextPath}/customers">👥 Khách hàng</a>
        </div>
        
        <div class="content">
            <!-- Statistics -->
            <div class="stats">
                <div class="stat-card">
                    <div class="stat-number">${customers.size()}</div>
                    <div class="stat-label">Tổng khách hàng</div>
                </div>
                <div class="stat-card">
                    <div class="stat-number">
                        <c:set var="vipCount" value="0"/>
                        <c:forEach var="c" items="${customers}">
                            <c:if test="${c.VIP}">
                                <c:set var="vipCount" value="${vipCount + 1}"/>
                            </c:if>
                        </c:forEach>
                        ${vipCount}
                    </div>
                    <div class="stat-label">Khách hàng VIP</div>
                </div>
            </div>
            
            <!-- Toolbar -->
            <div class="toolbar">
                <div class="search-box">
                    <form action="customers" method="get" style="display: flex; gap: 10px; width: 100%;">
                        <input type="hidden" name="action" value="search">
                        <input type="text" name="keyword" placeholder="Tìm kiếm theo tên, SĐT, email..." 
                               value="${keyword}" required>
                        <button type="submit" class="btn">🔍 Tìm kiếm</button>
                    </form>
                </div>
                <a href="customers?action=vip" class="btn btn-secondary">⭐ Chỉ VIP</a>
                <a href="customers" class="btn btn-secondary">📋 Tất cả</a>
            </div>
            
            <c:if test="${keyword != null}">
                <p style="margin-bottom: 20px; color: #666;">
                    Kết quả tìm kiếm cho: <strong>"${keyword}"</strong> 
                    (${customers.size()} kết quả)
                </p>
            </c:if>
            
            <c:if test="${isVIPOnly}">
                <p style="margin-bottom: 20px; color: #667eea; font-weight: bold;">
                    ⭐ Hiển thị chỉ khách hàng VIP (≥1000 điểm)
                </p>
            </c:if>
            
            <!-- Customer Table -->
            <c:choose>
                <c:when test="${customers.size() > 0}">
                    <table>
                        <thead>
                            <tr>
                                <th>ID</th>
                                <th>Họ Tên</th>
                                <th>Số Điện Thoại</th>
                                <th>Email</th>
                                <th>Điểm Tích Lũy</th>
                                <th>Hạng</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="customer" items="${customers}">
                                <tr>
                                    <td>${customer.customerId}</td>
                                    <td><strong>${customer.fullName}</strong></td>
                                    <td>${customer.phone}</td>
                                    <td>${customer.email}</td>
                                    <td class="points">
                                        <fmt:formatNumber value="${customer.loyaltyPoints}" pattern="#,##0"/> điểm
                                    </td>
                                    <td>
                                        <c:if test="${customer.VIP}">
                                            <span class="vip-badge">⭐ VIP</span>
                                        </c:if>
                                        <c:if test="${!customer.VIP}">
                                            <span style="color: #999;">Thường</span>
                                        </c:if>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </c:when>
                <c:otherwise>
                    <div class="no-results">
                        <p>😕 Không tìm thấy khách hàng nào</p>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</body>
</html>