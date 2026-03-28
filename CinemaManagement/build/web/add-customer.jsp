<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Thêm Khách Hàng - Cinema</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }

        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            padding: 30px 20px;
        }

        .container {
            max-width: 600px;
            margin: 0 auto;
            background: white;
            border-radius: 15px;
            box-shadow: 0 10px 40px rgba(0,0,0,0.25);
            overflow: hidden;
        }

        .header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 28px 30px;
            text-align: center;
        }
        .header h1 { font-size: 1.8em; margin-bottom: 5px; }
        .header p  { opacity: 0.85; font-size: 0.95em; }

        .nav {
            background: #f8f9fa;
            padding: 12px 30px;
            display: flex;
            gap: 12px;
            border-bottom: 2px solid #dee2e6;
            flex-wrap: wrap;
        }
        .nav a {
            text-decoration: none;
            color: #667eea;
            padding: 8px 16px;
            border-radius: 5px;
            font-weight: bold;
            font-size: 0.9em;
            transition: all 0.3s;
        }
        .nav a:hover { background: #667eea; color: white; }

        .form-body { padding: 35px 40px; }

        .message {
            padding: 14px;
            border-radius: 8px;
            margin-bottom: 20px;
            font-weight: bold;
        }
        .message.success { background: #d4edda; color: #155724; border: 1px solid #c3e6cb; }
        .message.error   { background: #f8d7da; color: #721c24; border: 1px solid #f5c6cb; }

        .form-group { margin-bottom: 22px; }

        label {
            display: block;
            margin-bottom: 7px;
            font-weight: 600;
            color: #444;
            font-size: 0.92em;
        }
        label span.req { color: #e53935; margin-left: 2px; }

        input[type=text],
        input[type=email],
        input[type=tel],
        input[type=number] {
            width: 100%;
            padding: 11px 14px;
            border: 2px solid #dee2e6;
            border-radius: 8px;
            font-size: 15px;
            transition: border-color 0.2s;
            outline: none;
        }
        input:focus { border-color: #667eea; }

        .hint {
            font-size: 0.82em;
            color: #888;
            margin-top: 5px;
        }

        .btn-row { display: flex; gap: 12px; margin-top: 10px; }

        .btn {
            flex: 1;
            padding: 13px;
            border: none;
            border-radius: 8px;
            font-size: 1em;
            font-weight: bold;
            cursor: pointer;
            transition: all 0.2s;
            text-align: center;
            text-decoration: none;
            display: inline-block;
        }
        .btn-primary { background: #667eea; color: white; }
        .btn-primary:hover { background: #5568d3; transform: translateY(-2px); }
        .btn-secondary { background: #6c757d; color: white; }
        .btn-secondary:hover { background: #5a6268; }

        .info-box {
            background: #e7f3ff;
            padding: 14px 18px;
            border-radius: 8px;
            margin-bottom: 24px;
            border-left: 4px solid #667eea;
            font-size: 0.9em;
            color: #555;
            line-height: 1.7;
        }
    </style>
</head>
<body>
<div class="container">
    <div class="header">
        <h1>👤 THÊM KHÁCH HÀNG MỚI</h1>
        <p>Đăng ký thành viên và tích điểm</p>
    </div>

    <div class="nav">
        <a href="${pageContext.request.contextPath}/">🏠 Trang chủ</a>
        <a href="${pageContext.request.contextPath}/customers">👥 Khách hàng</a>
    </div>

    <div class="form-body">

        <c:if test="${sessionScope.message != null}">
            <div class="message ${sessionScope.messageType}">
                ${sessionScope.message}
            </div>
            <c:remove var="message" scope="session"/>
            <c:remove var="messageType" scope="session"/>
        </c:if>

        <div class="info-box">
            💡 Khách hàng tích lũy điểm mỗi lần mua vé (5% giá vé). Đạt 1,000 điểm sẽ được nâng hạng VIP. 100 điểm = 10,000 VNĐ giảm giá.
        </div>

        <form action="${pageContext.request.contextPath}/customers" method="post">
            <input type="hidden" name="action" value="addCustomer">

            <div class="form-group">
                <label>Họ và tên <span class="req">*</span></label>
                <input type="text" name="fullName" required maxlength="200"
                       placeholder="Nhập họ và tên đầy đủ...">
            </div>

            <div class="form-group">
                <label>Số điện thoại <span class="req">*</span></label>
                <input type="tel" name="phone" required maxlength="15"
                       pattern="[0-9]{10,11}"
                       placeholder="VD: 0901234567">
                <p class="hint">Nhập 10–11 chữ số, không có khoảng trắng hay dấu gạch ngang.</p>
            </div>

            <div class="form-group">
                <label>Email</label>
                <input type="email" name="email" maxlength="200"
                       placeholder="VD: khachhang@email.com">
            </div>

            <div class="form-group">
                <label>Điểm tích lũy ban đầu</label>
                <input type="number" name="loyaltyPoints" min="0" step="1" value="0"
                       placeholder="0">
                <p class="hint">Thường để 0, trừ khi chuyển điểm từ hệ thống cũ.</p>
            </div>

            <div class="btn-row">
                <button type="submit" class="btn btn-primary">✅ Thêm khách hàng</button>
                <a href="${pageContext.request.contextPath}/customers" class="btn btn-secondary">❌ Hủy</a>
            </div>
        </form>
    </div>
</div>
</body>
</html>
