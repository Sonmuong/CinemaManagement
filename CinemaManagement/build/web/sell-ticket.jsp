<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Bán Vé - Cinema</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }

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

        .content { padding: 30px; }

        .movie-info {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 25px;
            border-radius: 10px;
            margin-bottom: 30px;
        }
        .movie-info h2 { font-size: 2em; margin-bottom: 15px; }

        .info-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 15px;
            margin-top: 15px;
        }
        .info-item { display: flex; align-items: center; gap: 10px; }

        .customer-search {
            background: #f8f9fa;
            padding: 20px;
            border-radius: 10px;
            margin-bottom: 30px;
        }
        .search-form { display: flex; gap: 10px; margin-bottom: 15px; }
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
        .customer-info h3 { color: #667eea; margin-bottom: 10px; }
        .points-display { color: #28a745; font-weight: bold; font-size: 1.2em; }

        .screen {
            background: #2d3436;
            color: white;
            text-align: center;
            padding: 15px;
            border-radius: 10px 10px 0 0;
            margin-bottom: 20px;
            font-weight: bold;
            letter-spacing: 3px;
        }

        .seats-container {
            max-width: 860px;
            margin: 0 auto;
            padding: 20px;
            background: #f8f9fa;
            border-radius: 10px;
            margin-bottom: 30px;
        }

        .seat-grid {
            display: grid;
            grid-template-columns: repeat(10, 1fr);
            gap: 8px;
            margin-bottom: 20px;
        }

        .seat {
            aspect-ratio: 1;
            border: 2px solid #dee2e6;
            border-radius: 8px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-weight: bold;
            font-size: 0.75em;
            cursor: pointer;
            transition: all 0.2s;
            background: white;
            user-select: none;
        }
        .seat:hover:not(.booked) {
            transform: scale(1.1);
            border-color: #667eea;
            box-shadow: 0 2px 8px rgba(102,126,234,0.4);
        }
        .seat.selected {
            background: #667eea;
            color: white;
            border-color: #667eea;
            transform: scale(1.05);
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
            margin-bottom: 20px;
        }
        .legend-item { display: flex; align-items: center; gap: 8px; font-size: 0.9em; }
        .legend-box {
            width: 26px; height: 26px;
            border-radius: 5px;
            border: 2px solid #dee2e6;
        }
        .legend-box.available { background: white; }
        .legend-box.selected  { background: #667eea; border-color: #667eea; }
        .legend-box.booked    { background: #dc3545; border-color: #dc3545; }

        /* Selected seats summary */
        .selected-summary {
            background: #e7f3ff;
            border: 2px solid #667eea;
            border-radius: 10px;
            padding: 15px 20px;
            margin-bottom: 10px;
            display: flex;
            align-items: center;
            gap: 15px;
            flex-wrap: wrap;
        }
        .selected-summary .label { font-weight: bold; color: #555; }
        .selected-tags { display: flex; flex-wrap: wrap; gap: 8px; flex: 1; }
        .seat-tag {
            background: #667eea;
            color: white;
            padding: 4px 12px;
            border-radius: 12px;
            font-size: 0.88em;
            font-weight: bold;
        }
        .selected-count {
            font-size: 1.1em;
            font-weight: bold;
            color: #667eea;
            white-space: nowrap;
        }

        .form-section {
            background: white;
            padding: 25px;
            border-radius: 10px;
        }
        .form-group { margin-bottom: 20px; }
        label { display: block; margin-bottom: 8px; font-weight: bold; color: #333; }
        input[type=text], select {
            width: 100%;
            padding: 12px;
            border: 2px solid #dee2e6;
            border-radius: 8px;
            font-size: 16px;
        }
        .checkbox-group { display: flex; align-items: center; gap: 10px; }
        .checkbox-group input { width: auto; }

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
            font-size: 1.05em;
        }
        .price-row.total {
            font-size: 1.5em;
            font-weight: bold;
            color: #667eea;
            padding-top: 10px;
            border-top: 2px solid #667eea;
        }

        .btn {
            padding: 14px 30px;
            background: #667eea;
            color: white;
            border: none;
            border-radius: 8px;
            font-weight: bold;
            font-size: 1.05em;
            cursor: pointer;
            transition: all 0.3s;
            width: 100%;
        }
        .btn:hover { background: #5568d3; transform: translateY(-2px); }
        .btn:disabled { background: #ccc; cursor: not-allowed; transform: none; }
        .btn-secondary { background: #6c757d; margin-top: 10px; }
        .btn-secondary:hover { background: #5a6268; }

        .error-message {
            background: #f8d7da;
            color: #721c24;
            padding: 15px;
            border-radius: 8px;
            margin-bottom: 20px;
        }

        .note { font-size: 0.85em; color: #888; margin-top: 5px; }
    </style>
</head>
<body>
<div class="container">
    <div class="header">
        <h1>🎟️ BÁN VÉ XEM PHIM</h1>
        <p>Chọn ghế và thông tin khách hàng</p>
    </div>

    <div class="content">

        <%-- Thông tin suất chiếu --%>
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
                    <span><strong>Giá/vé:</strong>
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

        <%-- Tìm khách hàng --%>
        <div class="customer-search">
            <h3>👤 Tìm Khách Hàng (không bắt buộc)</h3>
            <form action="${pageContext.request.contextPath}/tickets" method="post" class="search-form">
                <input type="hidden" name="action" value="findCustomer">
                <input type="hidden" name="showtimeId" value="${showtime.showtimeId}">
                <input type="text" name="phone" placeholder="Nhập số điện thoại..."
                       value="${searchedPhone}" pattern="[0-9]{10}">
                <button type="submit" class="btn" style="width:auto; padding:12px 30px;">
                    🔍 Tìm
                </button>
            </form>

            <c:if test="${customer != null}">
                <div class="customer-info">
                    <h3>✅ Tìm thấy khách hàng</h3>
                    <p><strong>Họ tên:</strong> ${customer.fullName}</p>
                    <p><strong>Email:</strong> ${customer.email}</p>
                    <p class="points-display">
                        ⭐ Điểm tích lũy:
                        <fmt:formatNumber value="${customer.loyaltyPoints}" pattern="#,##0"/>
                        <c:if test="${customer.VIP}"> (VIP)</c:if>
                    </p>
                    <p style="color:#28a745; margin-top:8px;">
                        💡 Có thể sử dụng điểm để giảm giá vé
                    </p>
                </div>
            </c:if>

            <c:if test="${customerMessage != null}">
                <div class="error-message">${customerMessage}</div>
            </c:if>
        </div>

        <%-- Màn hình & sơ đồ ghế --%>
        <div class="screen">🎬 &nbsp; MÀN HÌNH CHIẾU &nbsp; 🎬</div>

        <div class="seats-container">
            <div class="legend">
                <div class="legend-item">
                    <div class="legend-box available"></div><span>Ghế trống</span>
                </div>
                <div class="legend-item">
                    <div class="legend-box selected"></div><span>Đang chọn</span>
                </div>
                <div class="legend-item">
                    <div class="legend-box booked"></div><span>Đã đặt</span>
                </div>
            </div>

            <div class="seat-grid" id="seatGrid"></div>

            <%-- Hiển thị ghế đã chọn --%>
            <div class="selected-summary" id="selectedSummary" style="display:none;">
                <span class="label">🪑 Ghế đã chọn:</span>
                <div class="selected-tags" id="selectedTags"></div>
                <span class="selected-count" id="selectedCount"></span>
            </div>
            <p class="note" style="text-align:center;">
                💡 Click vào ghế để chọn / bỏ chọn. Có thể chọn nhiều ghế cùng lúc.
            </p>
        </div>

        <%-- Form đặt vé --%>
        <div class="form-section">
            <form action="${pageContext.request.contextPath}/tickets" method="post" id="bookingForm" onsubmit="return validateForm()">
                <input type="hidden" name="action" value="create">
                <input type="hidden" name="showtimeId" value="${showtime.showtimeId}">
                <input type="hidden" name="ticketPrice" value="${showtime.ticketPrice}">
                <%-- Các ghế đã chọn sẽ được inject bởi JS --%>
                <div id="seatInputs"></div>

                <c:if test="${customer != null}">
                    <input type="hidden" name="customerId" value="${customer.customerId}">
                </c:if>

                <div class="form-group">
                    <label>🎫 Loại Vé</label>
                    <select name="ticketType" required>
                        <option value="Normal">Vé Thường</option>
                        <option value="VIP">Vé VIP</option>
                        <option value="Student">Vé Sinh Viên</option>
                    </select>
                </div>

                <c:if test="${customer != null && customer.loyaltyPoints >= 100}">
                    <div class="form-group">
                        <div class="checkbox-group">
                            <input type="checkbox" name="usePoints" value="true" id="usePoints"
                                   onchange="updatePrice()">
                            <label for="usePoints" style="margin:0; cursor:pointer;">
                                💎 Sử dụng điểm tích lũy để giảm giá
                                (<fmt:formatNumber value="${customer.loyaltyPoints}" pattern="#,##0"/> điểm
                                = <fmt:formatNumber value="${customer.loyaltyPoints / 100 * 10000}" pattern="#,##0"/> đ)
                            </label>
                        </div>
                    </div>
                </c:if>

                <div class="price-summary">
                    <div class="price-row">
                        <span>Số ghế đã chọn:</span>
                        <span id="seatCountDisplay">0 ghế</span>
                    </div>
                    <div class="price-row">
                        <span>Giá mỗi vé:</span>
                        <span><fmt:formatNumber value="${showtime.ticketPrice}" pattern="#,##0"/> đ</span>
                    </div>
                    <c:if test="${customer != null && customer.loyaltyPoints >= 100}">
                        <div class="price-row" id="discountRow" style="display:none; color:#dc3545;">
                            <span>Giảm giá (điểm):</span>
                            <span id="discountDisplay">0 đ</span>
                        </div>
                    </c:if>
                    <div class="price-row total">
                        <span>TỔNG CỘNG:</span>
                        <span id="totalPrice">0 đ</span>
                    </div>
                </div>

                <button type="submit" class="btn" id="submitBtn" disabled>
                    ✅ XÁC NHẬN MUA VÉ
                </button>
                <a href="${pageContext.request.contextPath}/showtimes">
                    <button type="button" class="btn btn-secondary">❌ HỦY</button>
                </a>
            </form>
        </div>

    </div>
</div>

<script>
    // Danh sách ghế đã đặt từ server
    const bookedSeats = [
        <c:forEach var="seat" items="${bookedSeats}" varStatus="s">
            "${seat}"<c:if test="${!s.last}">,</c:if>
        </c:forEach>
    ];

    const ticketPrice   = ${showtime.ticketPrice};
    const totalSeats    = ${showtime.totalSeats};
    const loyaltyPoints = ${customer != null ? customer.loyaltyPoints : 0};

    const rows        = ['A','B','C','D','E','F','G','H','I','J'];
    const seatsPerRow = 10;

    const seatGrid    = document.getElementById('seatGrid');
    let selectedSeats = []; // mảng các ghế đang chọn

    // ── Tạo sơ đồ ghế ──────────────────────────────────────────
    rows.forEach(row => {
        for (let i = 1; i <= seatsPerRow; i++) {
            const seatNum = row + i;
            const div     = document.createElement('div');
            div.className      = 'seat';
            div.textContent    = seatNum;
            div.dataset.seat   = seatNum;

            if (bookedSeats.includes(seatNum)) {
                div.classList.add('booked');
            } else {
                div.addEventListener('click', () => toggleSeat(div, seatNum));
            }
            seatGrid.appendChild(div);
        }
    });

    // ── Toggle chọn / bỏ chọn ghế ──────────────────────────────
    function toggleSeat(el, seatNum) {
        const idx = selectedSeats.indexOf(seatNum);
        if (idx === -1) {
            // Chọn thêm
            selectedSeats.push(seatNum);
            el.classList.add('selected');
        } else {
            // Bỏ chọn
            selectedSeats.splice(idx, 1);
            el.classList.remove('selected');
        }
        updateUI();
    }

    // ── Cập nhật UI sau mỗi thay đổi ──────────────────────────
    function updateUI() {
        const count   = selectedSeats.length;
        const summary = document.getElementById('selectedSummary');
        const tags    = document.getElementById('selectedTags');
        const countEl = document.getElementById('selectedCount');
        const inputs  = document.getElementById('seatInputs');

        // Hiển thị / ẩn khung tóm tắt
        summary.style.display = count > 0 ? 'flex' : 'none';

        // Render tags ghế đã chọn
        tags.innerHTML = selectedSeats
            .map(s => `<span class="seat-tag">${s}</span>`)
            .join('');
        countEl.textContent = count + ' ghế';

        // Inject hidden inputs vào form (một input cho mỗi ghế)
        inputs.innerHTML = selectedSeats
            .map(s => `<input type="hidden" name="seatNumber" value="${s}">`)
            .join('');

        // Cập nhật số ghế + giá
        document.getElementById('seatCountDisplay').textContent = count + ' ghế';
        document.getElementById('submitBtn').disabled = count === 0;
        updatePrice();
    }

    // ── Tính lại tổng tiền ─────────────────────────────────────
    function updatePrice() {
        const count      = selectedSeats.length;
        const usePoints  = document.getElementById('usePoints');
        const useDiscount = usePoints && usePoints.checked;

        let discount = 0;
        if (useDiscount && loyaltyPoints >= 100) {
            // Tối đa: toàn bộ điểm chia 100 * 10000, không quá tổng giá vé
            const maxDiscount = Math.floor(loyaltyPoints / 100) * 10000;
            discount = Math.min(maxDiscount, ticketPrice * count);
        }

        const total = Math.max(0, ticketPrice * count - discount);

        document.getElementById('totalPrice').textContent =
            total.toLocaleString('vi-VN') + ' đ';

        const discountRow = document.getElementById('discountRow');
        const discountDisp = document.getElementById('discountDisplay');
        if (discountRow) {
            discountRow.style.display = useDiscount && discount > 0 ? 'flex' : 'none';
            if (discountDisp) discountDisp.textContent = '-' + discount.toLocaleString('vi-VN') + ' đ';
        }
    }

    // ── Validate trước khi submit ──────────────────────────────
    function validateForm() {
        if (selectedSeats.length === 0) {
            alert('Vui lòng chọn ít nhất 1 ghế!');
            return false;
        }
        return true;
    }
</script>
</body>
</html>
