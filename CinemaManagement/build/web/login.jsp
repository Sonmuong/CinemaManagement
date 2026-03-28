<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đăng Nhập - Cinema Management</title>
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

        .login-box {
            background: white;
            border-radius: 20px;
            box-shadow: 0 20px 60px rgba(0,0,0,0.3);
            overflow: hidden;
            width: 100%;
            max-width: 440px;
        }

        .login-header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 40px 30px;
            text-align: center;
        }

        .login-header .logo {
            font-size: 3.5em;
            margin-bottom: 10px;
        }

        .login-header h1 {
            font-size: 1.6em;
            font-weight: 700;
            margin-bottom: 5px;
        }

        .login-header p {
            opacity: 0.85;
            font-size: 0.95em;
        }

        .login-body {
            padding: 40px 35px;
        }

        .form-group {
            margin-bottom: 22px;
        }

        .form-group label {
            display: block;
            margin-bottom: 8px;
            font-weight: 600;
            color: #444;
            font-size: 0.92em;
        }

        .input-wrap {
            position: relative;
        }

        .input-wrap .icon {
            position: absolute;
            left: 14px;
            top: 50%;
            transform: translateY(-50%);
            font-size: 1.1em;
            pointer-events: none;
        }

        .form-group input {
            width: 100%;
            padding: 13px 14px 13px 44px;
            border: 2px solid #dee2e6;
            border-radius: 10px;
            font-size: 15px;
            transition: border-color 0.2s, box-shadow 0.2s;
            outline: none;
            color: #333;
        }

        .form-group input:focus {
            border-color: #667eea;
            box-shadow: 0 0 0 3px rgba(102,126,234,0.15);
        }

        .error-msg {
            background: #fff3cd;
            border-left: 4px solid #dc3545;
            color: #721c24;
            padding: 12px 16px;
            border-radius: 8px;
            margin-bottom: 20px;
            font-size: 0.92em;
            font-weight: 500;
        }

        .btn-login {
            width: 100%;
            padding: 14px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            border: none;
            border-radius: 10px;
            font-size: 1em;
            font-weight: 700;
            cursor: pointer;
            transition: opacity 0.2s, transform 0.2s;
            letter-spacing: 0.5px;
        }

        .btn-login:hover {
            opacity: 0.92;
            transform: translateY(-2px);
        }

        .btn-login:active {
            transform: translateY(0);
        }

        .hint-box {
            margin-top: 22px;
            background: #f0f4ff;
            border-radius: 10px;
            padding: 14px 18px;
            text-align: center;
            font-size: 0.88em;
            color: #555;
        }

        .hint-box strong { color: #667eea; }

        .footer-text {
            text-align: center;
            margin-top: 20px;
            color: #aaa;
            font-size: 0.82em;
        }
    </style>
</head>
<body>
    <div class="login-box">
        <div class="login-header">
            <div class="logo">🎬</div>
            <h1>CINEMA MANAGEMENT</h1>
            <p>Hệ thống quản lý rạp chiếu phim</p>
        </div>

        <div class="login-body">
            <c:if test="${error != null}">
                <div class="error-msg">${error}</div>
            </c:if>

            <form action="${pageContext.request.contextPath}/login" method="post">
                <div class="form-group">
                    <label for="username">👤 Tên đăng nhập</label>
                    <div class="input-wrap">
                        <span class="icon">👤</span>
                        <input type="text" id="username" name="username"
                               placeholder="Nhập tên đăng nhập..." required
                               value="${param.username}" autocomplete="username">
                    </div>
                </div>

                <div class="form-group">
                    <label for="password">🔒 Mật khẩu</label>
                    <div class="input-wrap">
                        <span class="icon">🔒</span>
                        <input type="password" id="password" name="password"
                               placeholder="Nhập mật khẩu..." required
                               autocomplete="current-password">
                    </div>
                </div>

                <button type="submit" class="btn-login">🚀 Đăng Nhập</button>
            </form>

            <div class="hint-box">
                💡 Tài khoản mặc định:<br>
                <strong>admin</strong> / <strong>admin123</strong>
            </div>

            <div class="footer-text">Cinema Management System &copy; 2024</div>
        </div>
    </div>
</body>
</html>
