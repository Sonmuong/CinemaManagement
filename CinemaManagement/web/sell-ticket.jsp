<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Bán Vé - Cinema</title>
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
        
        .content {
            padding: 30px;
        }
        
        .movie-info {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 25px;
            border-radius: 10px;
            margin-bottom: 30px;
        }
        
        .movie-info h2 {
            font-size: 2em;
            margin-bottom: 15px;
        }
        
        .info-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 15px;
            margin-top: 15px;
        }
        
        .info-item {
            display: flex;
            align-items: center;
            gap: 10px;
        }
        
        .customer-search {
            background: #f8f9fa;
            padding: 20px;
            border-radius: 10px;
            margin-bottom: 30px;
        }
        
        .search-form {
            display: flex;
            gap: 10px;
            margin-bottom: 15px;
        }
        
        .search-form input {
            flex: 1;
            padding: 12px;
            border: 2px solid #dee2e6;
            border-radius: 8px;
            font-size: 16px;
        }
        
        .customer-info {
            background: white;
            padding: 15px;
            border-radius: 8px;
            border: 2px solid #667eea;
        }
        
        .customer-info h3 {
            color: #667eea;
            margin-bottom: 10px;
        }
        
        .points-display {
            color: #28a745;
            font-weight: bold;
            font-size: 1.2em;
        }
        
        .screen {
            background: #2d3436;
            color: white;
            text-align: center;
            padding: 15px;
            border-radius: 10px 10px 0 0;
            margin-bottom: 30px;
            font-weight: bold;
        }
        
        .seats-container {
            max-width: 800px;
            margin: 0 auto;
            padding: 20px;
            background: #f8f9fa;
            border-radius: 10px;
        }
        
        .seat-grid {
            display: grid;
            grid-template-columns: repeat(10, 1fr);
            gap: 10px;
            margin-bottom: 30px;
        }
        
        .seat {
            aspect-ratio: 1;
            border: 2px solid #dee2e6;
            border-radius: 8px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-weight: bold;
            cursor: pointer;
            transition: all 0.3s;
            background: white;
        }
        
        .seat:hover:not(.booked):not(.selected) {
            transform: scale(1.1);
            border-color: #667eea;
        }
        
        .seat.selected {
            background: #667eea;
            color: white;
            border-color: #667eea;
        }
        
        .seat.booked {
            background: #dc3545;
            color: white;
            cursor: not-allowed;
            opacity: 0.6;
        }
        
        .legend {
            display: flex;
            justify-content: center;
            gap: 30px;
            margin-bottom: 30px;
        }
        
        .legend-item {
            display: flex;
            align-items: center;
            gap: 10px;
        }
        
        .legend-box {
            width: 30px;
            height: 30px;
            border-radius: 5px;
            border: 2px solid #dee2e6;
        }
        
        .legend-box.available {
            background: white;
        }
        
        .legend-box.selected {
            background: #667eea;
            border-color: #667eea;
        }
        
        .legend-box.booked {
            background: #dc3545;
            border-color: #dc3545;
        }
        
        .form-section {
            background: white;
            padding: 25px;
            border-radius: 10px;
            margin-top: 30px;
        }
        
        .form-group {
            margin-bottom: 20px;
        }
        
        label {
            display: block;
            margin-bottom: 8px;
            font-weight: bold;
            color: #333;
        }
        
        input, select {
            width: 100%;
            padding: 12px;
            border: 2px solid #dee2e6;
            border-radius: 8px;
            font-size: 16px;
        }
        
        .checkbox-group {
            display: flex;
            align-items: center;
            gap: 10px;
        }
        
        .checkbox-group input {
            width: auto;
        }
        
        .price-summary {
            background: #e7f3ff;
            padding: 20px;
            border-radius: 10px;
            margin-bottom: 20px;
        }
        
        .price-row {
            display: flex;
            justify-content: space-between;
            margin-bottom: 10px;
            font-size: 1.1em;
        }
        
        .price-row.total {
            font-size: 1.5em;
            font-weight: bold;
            color: #667eea;
            padding-top: 10px;
            border-top: 2px solid #667eea;
        }
        
        .btn {
            padding: 15px 30px;
            background: #667eea;
            color: white;
            border: none;
            border-radius: 8px;
            font-weight: bold;
            font-size: 1.1em;
            cursor: pointer;
            transition: all 0.3s;
            width: 100%;
        }
        
        .btn:hover {
            background: #5568d3;
            transform: translateY(-2px);
        }
        
        .btn:disabled {
            background: #ccc;
            cursor: not-allowed;
            transform: none;
        }
        
        .btn-secondary {
            background: #6c757d;
            margin-top: 10px;
        }
        
        .error-message {
            background: #f8d7da;
            color: #721c24;
            padding: 15px;
            border-radius: 8px;
            margin-bottom: 20px;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>🎟️ BÁN VÉ XEM PHIM</h1>
            <p>Chọn ghế và thông tin khách hàng</p>
        </div>
        
        <div class="content">
            <div class="movie-info">
                <h2>🎬 ${showtime.movieName}</h2>
                <div class="info-grid">
                    <div class="info-item">
                        <span>🏛️</span>
                        <span><strong>Phòng:</strong> ${showtime.roomName}</span>
                    </div>
                    <div class="info-item">
                        <span>📅</span>
                        <span><strong>Ngày:</strong> 
                            <fmt:formatDate value="${showtime.showDate}" pattern="dd/MM/yyyy"/>
                        </span>
                    </div>
                    <div class="info-item">
                        <span>⏰</span>
                        <span><strong>Giờ:</strong> 
                            <fmt:formatDate value="${showtime.showTime}" pattern="HH:mm"/>
                        </span>
                    </div>
                    <div class="info-item">
                        <span>💰</span>
                        <span><strong>Giá vé:</strong> 
                            <fmt:formatNumber value="${showtime.ticketPrice}" pattern="#,##0"/> đ
                        </span>
                    </div>
                    <div class="info-item">
                        <span>🪑</span>
                        <span><strong>Còn trống:</strong> 
                            ${showtime.seatsAvailable}/${showtime.totalSeats} ghế
                        </span>
                    </div>
                </div>
            </div>
            
            <div class="customer-search">
                <h3>👤 Tìm Khách Hàng (không bắt buộc)</h3>
                <form action="tickets" method="post" class="search-form">
                    <input type="hidden" name="action" value="findCustomer">
                    <input type="hidden" name="showtimeId" value="${showtime.showtimeId}">
                    <input type="text" name="phone" placeholder="Nhập số điện thoại..." 
                           value="${searchedPhone}" pattern="[0-9]{10}">
                    <button type="submit" class="btn" style="width: auto; padding: 12px 30px;">
                        🔍 Tìm
                    </button>
                </form>
                
                <c:if test="${customer != null}">
                    <div class="customer-info">
                        <h3>✅ Tìm thấy khách hàng</h3>
                        <p><strong>Họ tên:</strong> ${customer.fullName}</p>
                        <p><strong>Email:</strong> ${customer.email}</p>
                        <p class="points-display">
                            ⭐ Điểm tích lũy: <fmt:formatNumber value="${customer.loyaltyPoints}" pattern="#,##0"/>
                            <c:if test="${customer.VIP}"> (VIP)</c:if>
                        </p>
                        <p style="color: #28a745; margin-top: 10px;">
                            💡 Có thể sử dụng điểm để giảm giá vé
                        </p>
                    </div>
                </c:if>
                
                <c:if test="${customerMessage != null}">
                    <div class="error-message">${customerMessage}</div>
                </c:if>
            </div>
            
            <div class="screen">
                🎬 MÀN HÌNH CHIẾU 🎬
            </div>
            
            <div class="seats-container">
                <div class="legend">
                    <div class="legend-item">
                        <div class="legend-box available"></div>
                        <span>Ghế trống</span>
                    </div>
                    <div class="legend-item">
                        <div class="legend-box selected"></div>
                        <span>Ghế đang chọn</span>
                    </div>
                    <div class="legend-item">
                        <div class="legend-box booked"></div>
                        <span>Đã đặt</span>
                    </div>
                </div>
                
                <div class="seat-grid" id="seatGrid">
                    <!-- Seats will be generated by JavaScript -->
                </div>
            </div>
            
            <div class="form-section">
                <form action="tickets" method="post" id="bookingForm">
                    <input type="hidden" name="action" value="create">
                    <input type="hidden" name="showtimeId" value="${showtime.showtimeId}">
                    <input type="hidden" name="seatNumber" id="selectedSeat">
                    <input type="hidden" name="ticketPrice" value="${showtime.ticketPrice}">
                    
                    <c:if test="${customer != null}">
                        <input type="hidden" name="customerId" value="${customer.customerId}">
                    </c:if>
                    
                    <div class="form-group">
                        <label>🪑 Ghế Đã Chọn</label>
                        <input type="text" id="seatDisplay" readonly placeholder="Chưa chọn ghế">
                    </div>
                    
                    <div class="form-group">
                        <label>🎫 Loại Vé</label>
                        <select name="ticketType" required>
                            <option value="Normal">Vé Thường</option>
                            <option value="VIP">Vé VIP</option>
                            <option value="Student">Vé Sinh Viên</option>
                        </select>
                    </div>
                    
                    <c:if test="${customer != null && customer.loyaltyPoints >= 100}">
                        <div class="checkbox-group">
                            <input type="checkbox" name="usePoints" value="true" id="usePoints">
                            <label for="usePoints" style="margin: 0;">
                                💎 Sử dụng điểm tích lũy để giảm giá 
                                (${Math.floor(customer.loyaltyPoints / 100) * 100} điểm = 
                                <fmt:formatNumber value="${Math.floor(customer.loyaltyPoints / 100) * 10000}" 
                                                  pattern="#,##0"/> đ)
                            </label>
                        </div>
                    </c:if>
                    
                    <div class="price-summary">
                        <div class="price-row">
                            <span>Giá vé:</span>
                            <span><fmt:formatNumber value="${showtime.ticketPrice}" pattern="#,##0"/> đ</span>
                        </div>
                        <div class="price-row total">
                            <span>TỔNG CỘNG:</span>
                            <span id="totalPrice">
                                <fmt:formatNumber value="${showtime.ticketPrice}" pattern="#,##0"/> đ
                            </span>
                        </div>
                    </div>
                    
                    <button type="submit" class="btn" id="submitBtn" disabled>
                        ✅ XÁC NHẬN MUA VÉ
                    </button>
                    
                    <a href="${pageContext.request.contextPath}/showtimes">
                        <button type="button" class="btn btn-secondary">
                            ❌ HỦY
                        </button>
                    </a>
                </form>
            </div>
        </div>
    </div>
    
    <script>
        const bookedSeats = [
            <c:forEach var="seat" items="${bookedSeats}" varStatus="status">
                "${seat}"<c:if test="${!status.last}">,</c:if>
            </c:forEach>
        ];
        
        const rows = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H'];
        const seatsPerRow = 10;
        const seatGrid = document.getElementById('seatGrid');
        let selectedSeat = null;
        
        // Generate seats
        rows.forEach(row => {
            for (let i = 1; i <= seatsPerRow; i++) {
                const seatNumber = row + i;
                const seat = document.createElement('div');
                seat.className = 'seat';
                seat.textContent = seatNumber;
                seat.dataset.seat = seatNumber;
                
                if (bookedSeats.includes(seatNumber)) {
                    seat.classList.add('booked');
                } else {
                    seat.onclick = () => selectSeat(seat, seatNumber);
                }
                
                seatGrid.appendChild(seat);
            }
        });
        
        function selectSeat(element, seatNumber) {
            // Deselect previous seat
            if (selectedSeat) {
                document.querySelector(`.seat[data-seat="${selectedSeat}"]`).classList.remove('selected');
            }
            
            // Select new seat
            element.classList.add('selected');
            selectedSeat = seatNumber;
            
            // Update form
            document.getElementById('selectedSeat').value = seatNumber;
            document.getElementById('seatDisplay').value = seatNumber;
            document.getElementById('submitBtn').disabled = false;
        }
    </script>
</body>
</html>