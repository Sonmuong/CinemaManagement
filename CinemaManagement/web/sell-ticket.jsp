<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
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
            max-width: 1100px;
            margin: 0 auto;
            background: white;
            border-radius: 15px;
            box-shadow: 0 10px 40px rgba(0,0,0,0.2);
            overflow: hidden;
        }
        .header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 28px 30px;
            text-align: center;
        }
        .header h1 { font-size: 1.8em; }
        .content { padding: 28px 30px; }

        /* ── Showtime info ── */
        .movie-info {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 20px 24px;
            border-radius: 10px;
            margin-bottom: 24px;
        }
        .movie-info h2 { font-size: 1.6em; margin-bottom: 12px; }
        .info-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(160px, 1fr)); gap: 10px; }
        .info-item { display: flex; align-items: center; gap: 8px; font-size: 0.92em; }

        /* ── Customer panel ── */
        .customer-panel {
            border: 2px solid #dee2e6;
            border-radius: 12px;
            overflow: hidden;
            margin-bottom: 24px;
        }
        .customer-panel-header {
            background: #f8f9fa;
            padding: 14px 20px;
            display: flex;
            align-items: center;
            justify-content: space-between;
            border-bottom: 1px solid #dee2e6;
        }
        .customer-panel-header h3 { font-size: 1em; color: #444; }
        .customer-panel-body { padding: 18px 20px; }

        /* Trạng thái: chưa tìm khách */
        .search-form { display: flex; gap: 10px; }
        .search-form input {
            flex: 1; padding: 10px 14px;
            border: 2px solid #dee2e6; border-radius: 8px; font-size: 15px;
        }
        .search-form input:focus { outline: none; border-color: #667eea; }

        /* Trạng thái: đã tìm thấy khách */
        .member-card {
            background: linear-gradient(135deg, #eeedfe 0%, #ddd9fd 100%);
            border-radius: 10px;
            padding: 16px 20px;
            display: flex;
            align-items: center;
            gap: 18px;
        }
        .member-avatar {
            width: 52px; height: 52px; border-radius: 50%;
            background: #667eea;
            color: white;
            display: flex; align-items: center; justify-content: center;
            font-size: 1.3em; font-weight: bold; flex-shrink: 0;
        }
        .member-avatar.vip { background: #f59e0b; }
        .member-details { flex: 1; }
        .member-name { font-size: 1.1em; font-weight: bold; color: #3c3489; }
        .member-meta { font-size: 0.85em; color: #534ab7; margin-top: 2px; }
        .member-points { font-size: 1.05em; font-weight: bold; color: #667eea; margin-top: 4px; }
        .vip-badge {
            background: #f59e0b; color: #412402;
            padding: 3px 12px; border-radius: 12px;
            font-size: 0.8em; font-weight: bold;
            display: inline-block; margin-top: 4px;
        }
        .normal-badge {
            background: #dee2e6; color: #495057;
            padding: 3px 12px; border-radius: 12px;
            font-size: 0.8em; font-weight: bold;
            display: inline-block; margin-top: 4px;
        }
        .btn-change-customer {
            padding: 8px 16px; background: white; color: #667eea;
            border: 2px solid #667eea; border-radius: 8px;
            font-weight: bold; cursor: pointer; font-size: 0.85em;
            transition: all 0.2s; white-space: nowrap;
        }
        .btn-change-customer:hover { background: #667eea; color: white; }

        /* Trạng thái: không tìm thấy / vé lẻ */
        .guest-card {
            background: #f8f9fa;
            border-radius: 10px;
            padding: 16px 20px;
            display: flex;
            align-items: center;
            gap: 16px;
        }
        .guest-icon {
            width: 52px; height: 52px; border-radius: 50%;
            background: #dee2e6; color: #888;
            display: flex; align-items: center; justify-content: center;
            font-size: 1.5em; flex-shrink: 0;
        }
        .guest-info { flex: 1; }
        .guest-title { font-size: 1em; font-weight: bold; color: #555; }
        .guest-sub { font-size: 0.85em; color: #888; margin-top: 2px; }

        /* ── Seat map ── */
        .screen {
            background: #2d3436; color: white;
            text-align: center; padding: 12px;
            border-radius: 10px 10px 0 0;
            font-weight: bold; letter-spacing: 3px; font-size: 0.9em;
        }
        .seats-container {
            background: #f8f9fa;
            padding: 16px;
            border-radius: 0 0 10px 10px;
            margin-bottom: 24px;
        }
        .legend {
            display: flex; justify-content: center; gap: 24px;
            margin-bottom: 14px; font-size: 0.88em;
        }
        .legend-item { display: flex; align-items: center; gap: 6px; }
        .legend-box { width: 22px; height: 22px; border-radius: 5px; border: 2px solid #dee2e6; }
        .legend-box.available { background: white; }
        .legend-box.selected  { background: #667eea; border-color: #667eea; }
        .legend-box.booked    { background: #dc3545; border-color: #dc3545; }
        .seat-grid {
            display: grid;
            grid-template-columns: repeat(10, 1fr);
            gap: 7px; margin-bottom: 14px;
        }
        .seat {
            aspect-ratio: 1;
            border: 2px solid #dee2e6; border-radius: 7px;
            display: flex; align-items: center; justify-content: center;
            font-weight: bold; font-size: 0.72em;
            cursor: pointer; transition: all 0.15s;
            background: white; user-select: none;
        }
        .seat:hover:not(.booked) { transform: scale(1.12); border-color: #667eea; }
        .seat.selected { background: #667eea; color: white; border-color: #667eea; transform: scale(1.05); }
        .seat.booked   { background: #dc3545; color: white; cursor: not-allowed; opacity: 0.6; }

        .selected-summary {
            background: #e7f3ff; border: 2px solid #667eea;
            border-radius: 8px; padding: 12px 16px;
            display: flex; align-items: center; gap: 12px; flex-wrap: wrap;
        }
        .selected-tags { display: flex; flex-wrap: wrap; gap: 6px; flex: 1; }
        .seat-tag {
            background: #667eea; color: white;
            padding: 3px 10px; border-radius: 10px;
            font-size: 0.85em; font-weight: bold;
        }

        /* ── Booking form ── */
        .booking-form-card {
            border: 2px solid #dee2e6;
            border-radius: 12px; overflow: hidden;
        }
        .booking-form-header {
            background: #f8f9fa; padding: 14px 20px;
            border-bottom: 1px solid #dee2e6;
            font-weight: bold; color: #444;
        }
        .booking-form-body { padding: 20px; }

        .form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
        .form-group { margin-bottom: 16px; }
        .form-group label { display: block; margin-bottom: 6px; font-weight: 600; color: #444; font-size: 0.9em; }
        select, input[type=text] {
            width: 100%; padding: 10px 14px;
            border: 2px solid #dee2e6; border-radius: 8px; font-size: 15px;
        }
        select:focus, input:focus { outline: none; border-color: #667eea; }

        /* Checkbox dùng điểm */
        .use-points-box {
            background: linear-gradient(135deg, #eeedfe 0%, #ddd9fd 100%);
            border: 2px solid #afa9ec; border-radius: 10px;
            padding: 14px 18px; margin-bottom: 16px;
            display: flex; align-items: center; gap: 12px;
        }
        .use-points-box input[type=checkbox] { width: 18px; height: 18px; cursor: pointer; }
        .use-points-label { cursor: pointer; flex: 1; }
        .use-points-label strong { color: #3c3489; display: block; font-size: 0.95em; }
        .use-points-label span { color: #534ab7; font-size: 0.85em; }

        /* Price summary */
        .price-summary {
            background: #f8f9fa; border-radius: 10px;
            padding: 16px 20px; margin-bottom: 16px;
        }
        .price-row {
            display: flex; justify-content: space-between;
            padding: 6px 0; font-size: 0.95em;
            border-bottom: 1px solid #eee;
        }
        .price-row:last-child { border-bottom: none; }
        .price-row.total {
            font-size: 1.35em; font-weight: bold; color: #667eea;
            padding-top: 10px; border-top: 2px solid #667eea; border-bottom: none;
            margin-top: 4px;
        }
        .price-row.discount { color: #dc3545; }
        .price-row.earned   { color: #28a745; }

        /* Buttons */
        .btn-submit {
            width: 100%; padding: 14px;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white; border: none; border-radius: 10px;
            font-size: 1.1em; font-weight: bold; cursor: pointer;
            transition: all 0.2s; margin-bottom: 10px;
        }
        .btn-submit:hover   { opacity: 0.9; transform: translateY(-2px); }
        .btn-submit:disabled{ background: #ccc; cursor: not-allowed; transform: none; }
        .btn-cancel {
            width: 100%; padding: 12px; background: #f8f9fa;
            color: #666; border: 2px solid #dee2e6; border-radius: 10px;
            font-size: 1em; font-weight: bold; cursor: pointer; transition: all 0.2s;
            text-decoration: none; display: block; text-align: center;
        }
        .btn-cancel:hover { background: #dee2e6; }

        /* Search btn */
        .btn-search {
            padding: 10px 20px; background: #667eea; color: white;
            border: none; border-radius: 8px; font-weight: bold;
            cursor: pointer; transition: all 0.2s; white-space: nowrap;
        }
        .btn-search:hover { background: #5568d3; }

        .note { font-size: 0.82em; color: #999; text-align: center; margin-top: 8px; }
        .msg-not-found {
            color: #856404; background: #fff3cd;
            border: 1px solid #ffc107; border-radius: 8px;
            padding: 10px 14px; font-size: 0.9em; margin-top: 10px;
        }
    </style>
</head>
<body>
<div class="container">
    <div class="header">
        <h1>🎟️ BÁN VÉ XEM PHIM</h1>
    </div>
    <div class="content">

        <%-- ── Thông tin suất chiếu ── --%>
        <div class="movie-info">
            <h2>🎬 ${showtime.movieName}</h2>
            <div class="info-grid">
                <div class="info-item">🏛️ <span><b>Phòng:</b> ${showtime.roomName}</span></div>
                <div class="info-item">📅 <span><b>Ngày:</b> <fmt:formatDate value="${showtime.showDate}" pattern="dd/MM/yyyy"/></span></div>
                <div class="info-item">⏰ <span><b>Giờ:</b> <fmt:formatDate value="${showtime.showTime}" pattern="HH:mm"/></span></div>
                <div class="info-item">💰 <span><b>Giá/vé:</b> <fmt:formatNumber value="${showtime.ticketPrice}" pattern="#,##0"/> đ</span></div>
                <div class="info-item">🪑 <span><b>Còn:</b> ${showtime.seatsAvailable}/${showtime.totalSeats} ghế</span></div>
            </div>
        </div>

        <%-- ── Panel khách hàng ── --%>
        <div class="customer-panel">
            <div class="customer-panel-header">
                <h3>👤 Thông tin khách hàng</h3>
                <c:if test="${customer != null}">
                    <button type="button" class="btn-change-customer" onclick="showSearchForm()">
                        🔄 Đổi khách hàng
                    </button>
                </c:if>
            </div>
            <div class="customer-panel-body">

                <%-- CASE 1: Đã tìm thấy khách hàng --%>
                <c:if test="${customer != null}">
                    <div class="member-card">
                        <div class="member-avatar ${customer.VIP ? 'vip' : ''}">
                            ${fn:substring(customer.fullName, 0, 1)}
                        </div>
                        <div class="member-details">
                            <div class="member-name">${customer.fullName}</div>
                            <div class="member-meta">${customer.phone}
                                <c:if test="${not empty customer.email}"> · ${customer.email}</c:if>
                            </div>
                            <div class="member-points">
                                ⭐ <fmt:formatNumber value="${customer.loyaltyPoints}" pattern="#,##0"/> điểm tích lũy
                            </div>
                            <c:choose>
                                <c:when test="${customer.VIP}">
                                    <span class="vip-badge">⭐ VIP</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="normal-badge">Thành viên thường</span>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                    <%-- Form tìm lại (ẩn) --%>
                    <div id="searchFormHidden" style="display:none; margin-top:14px;">
                        <form action="${pageContext.request.contextPath}/tickets" method="post" class="search-form">
                            <input type="hidden" name="action" value="findCustomer">
                            <input type="hidden" name="showtimeId" value="${showtime.showtimeId}">
                            <input type="text" name="phone" placeholder="Nhập số điện thoại mới...">
                            <button type="submit" class="btn-search">🔍 Tìm</button>
                        </form>
                    </div>
                </c:if>

                <%-- CASE 2: Không tìm thấy / chưa tìm --%>
                <c:if test="${customer == null}">
                    <%-- Sub-case: đã tìm nhưng không thấy → hiện guest card + form lại --%>
                    <c:if test="${customerNotFound}">
                        <div class="guest-card" style="margin-bottom:14px;">
                            <div class="guest-icon">👤</div>
                            <div class="guest-info">
                                <div class="guest-title">Vé lẻ (khách vãng lai)</div>
                                <div class="guest-sub">Không tìm thấy SĐT "${searchedPhone}" — sẽ bán vé lẻ, không tích điểm</div>
                            </div>
                        </div>
                    </c:if>

                    <%-- Form tìm kiếm luôn hiện khi chưa có customer --%>
                    <form action="${pageContext.request.contextPath}/tickets" method="post" class="search-form">
                        <input type="hidden" name="action" value="findCustomer">
                        <input type="hidden" name="showtimeId" value="${showtime.showtimeId}">
                        <input type="text" name="phone"
                               placeholder="📱 Nhập SĐT để tích điểm thành viên..."
                               value="${searchedPhone}">
                        <button type="submit" class="btn-search">🔍 Tìm</button>
                    </form>
                    <p class="note" style="margin-top:8px; text-align:left;">
                        Bỏ trống nếu muốn bán vé lẻ không tích điểm
                    </p>
                </c:if>

            </div>
        </div>

        <%-- ── Sơ đồ ghế ── --%>
        <div class="screen">🎬 MÀN HÌNH CHIẾU 🎬</div>
        <div class="seats-container">
            <div class="legend">
                <div class="legend-item"><div class="legend-box available"></div> Ghế trống</div>
                <div class="legend-item"><div class="legend-box selected"></div> Đang chọn</div>
                <div class="legend-item"><div class="legend-box booked"></div> Đã đặt</div>
            </div>
            <div class="seat-grid" id="seatGrid"></div>
            <div class="selected-summary" id="selectedSummary" style="display:none;">
                <span style="font-weight:bold; color:#555; white-space:nowrap;">🪑 Đã chọn:</span>
                <div class="selected-tags" id="selectedTags"></div>
                <span style="font-weight:bold; color:#667eea; white-space:nowrap;" id="selectedCount"></span>
            </div>
            <p class="note">Click ghế để chọn · Có thể chọn nhiều ghế</p>
        </div>

        <%-- ── Form đặt vé ── --%>
        <div class="booking-form-card">
            <div class="booking-form-header">🧾 Xác nhận đặt vé</div>
            <div class="booking-form-body">
                <form action="${pageContext.request.contextPath}/tickets"
                      method="post" id="bookingForm"
                      onsubmit="return validateAndPrepare()">

                    <input type="hidden" name="action" value="create">
                    <input type="hidden" name="showtimeId" value="${showtime.showtimeId}">
                    <input type="hidden" name="ticketPrice" value="${showtime.ticketPrice}">
                    <div id="seatInputs"></div>

                    <c:if test="${customer != null}">
                        <input type="hidden" name="customerId" value="${customer.customerId}">
                    </c:if>

                    <div class="form-row">
                        <div class="form-group">
                            <label>🎫 Loại vé</label>
                            <select name="ticketType" required>
                                <option value="Normal">Vé Thường</option>
                                <option value="VIP">Vé VIP (+20%)</option>
                                <option value="Student">Vé Sinh Viên (-10%)</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label>🪑 Số ghế đã chọn</label>
                            <input type="text" id="seatDisplay"
                                   value="Chưa chọn ghế" readonly
                                   style="background:#f8f9fa; color:#666;">
                        </div>
                    </div>

                    <%-- Checkbox dùng điểm (chỉ hiện khi có khách có đủ điểm) --%>
                    <c:if test="${customer != null && customer.loyaltyPoints >= 100}">
                        <div class="use-points-box">
                            <input type="checkbox" name="usePoints" value="true"
                                   id="usePoints" onchange="updatePrice()">
                            <label class="use-points-label" for="usePoints">
                                <strong>💎 Dùng điểm tích lũy để giảm giá</strong>
                                <span>
                                    <fmt:formatNumber value="${customer.loyaltyPoints}" pattern="#,##0"/> điểm khả dụng
                                    = <fmt:formatNumber value="${customer.loyaltyPoints / 100 * 10000}" pattern="#,##0"/> đ giảm tối đa
                                </span>
                            </label>
                        </div>
                    </c:if>

                    <%-- Tóm tắt giá --%>
                    <div class="price-summary">
                        <div class="price-row">
                            <span>Số ghế</span>
                            <span id="seatCountDisplay">0 ghế</span>
                        </div>
                        <div class="price-row">
                            <span>Đơn giá</span>
                            <span><fmt:formatNumber value="${showtime.ticketPrice}" pattern="#,##0"/> đ/ghế</span>
                        </div>
                        <div class="price-row discount" id="discountRow" style="display:none;">
                            <span>Giảm giá (điểm)</span>
                            <span id="discountDisplay">0 đ</span>
                        </div>
                        <div class="price-row earned" id="earnRow" style="display:none;">
                            <span>Điểm sẽ tích được</span>
                            <span id="earnDisplay">0 điểm</span>
                        </div>
                        <div class="price-row total">
                            <span>Tổng cộng</span>
                            <span id="totalPrice">0 đ</span>
                        </div>
                    </div>

                    <button type="submit" class="btn-submit" id="submitBtn" disabled>
                        ✅ XÁC NHẬN MUA VÉ
                    </button>
                    <a href="${pageContext.request.contextPath}/showtimes" class="btn-cancel">
                        ❌ Hủy
                    </a>
                </form>
            </div>
        </div>

    </div>
</div>

<script>
    const bookedSeats   = [<c:forEach var="s" items="${bookedSeats}" varStatus="st">"${s}"<c:if test="${!st.last}">,</c:if></c:forEach>];
    const ticketPrice   = ${showtime.ticketPrice};
    const loyaltyPoints = ${customer != null ? customer.loyaltyPoints : 0};
    const hasCustomer   = ${customer != null ? 'true' : 'false'};

    let selectedSeats = [];

    // Tạo sơ đồ ghế
    const rows = ['A','B','C','D','E','F','G','H','I','J'];
    rows.forEach(row => {
        for (let i = 1; i <= 10; i++) {
            const sn  = row + i;
            const div = document.createElement('div');
            div.className   = 'seat';
            div.textContent = sn;
            if (bookedSeats.includes(sn)) {
                div.classList.add('booked');
            } else {
                div.addEventListener('click', () => toggleSeat(div, sn));
            }
            document.getElementById('seatGrid').appendChild(div);
        }
    });

    function toggleSeat(el, sn) {
        const idx = selectedSeats.indexOf(sn);
        if (idx === -1) { selectedSeats.push(sn); el.classList.add('selected'); }
        else { selectedSeats.splice(idx, 1); el.classList.remove('selected'); }
        updateUI();
    }

    function updateUI() {
        const count = selectedSeats.length;
        const summary = document.getElementById('selectedSummary');
        summary.style.display = count > 0 ? 'flex' : 'none';

        document.getElementById('selectedTags').innerHTML =
            selectedSeats.map(s => '<span class="seat-tag">' + s + '</span>').join('');
        document.getElementById('selectedCount').textContent = count + ' ghế';
        document.getElementById('seatDisplay').value =
            count > 0 ? selectedSeats.join(', ') : 'Chưa chọn ghế';
        document.getElementById('seatCountDisplay').textContent = count + ' ghế';

        // Inject hidden inputs
        const container = document.getElementById('seatInputs');
        container.innerHTML = '';
        selectedSeats.forEach(s => {
            const inp = document.createElement('input');
            inp.type = 'hidden'; inp.name = 'seatNumber'; inp.value = s;
            container.appendChild(inp);
        });

        document.getElementById('submitBtn').disabled = count === 0;
        updatePrice();
    }

    function updatePrice() {
        const count = selectedSeats.length;
        const usePointsEl = document.getElementById('usePoints');
        const useDiscount = usePointsEl && usePointsEl.checked;

        let discount = 0;
        if (useDiscount && loyaltyPoints >= 100) {
            const usable = Math.floor(loyaltyPoints / 100) * 100;
            discount = Math.min(usable / 100 * 10000, ticketPrice * count);
            discount = Math.floor(discount / 10000) * 10000;
        }

        const subtotal = ticketPrice * count;
        const total    = Math.max(0, subtotal - discount);
        const earned   = hasCustomer ? Math.floor(total * 0.05) : 0;

        document.getElementById('totalPrice').textContent =
            total.toLocaleString('vi-VN') + ' đ';

        const dRow = document.getElementById('discountRow');
        const dDisp = document.getElementById('discountDisplay');
        if (dRow) {
            dRow.style.display = (useDiscount && discount > 0) ? 'flex' : 'none';
            if (dDisp) dDisp.textContent = '-' + discount.toLocaleString('vi-VN') + ' đ';
        }

        const eRow = document.getElementById('earnRow');
        const eDisp = document.getElementById('earnDisplay');
        if (eRow) {
            eRow.style.display = (hasCustomer && count > 0) ? 'flex' : 'none';
            if (eDisp) eDisp.textContent = '+' + earned.toLocaleString('vi-VN') + ' điểm';
        }
    }

    function showSearchForm() {
        const f = document.getElementById('searchFormHidden');
        if (f) f.style.display = f.style.display === 'none' ? 'block' : 'none';
    }

    function validateAndPrepare() {
        if (selectedSeats.length === 0) { alert('Vui lòng chọn ít nhất 1 ghế!'); return false; }
        const container = document.getElementById('seatInputs');
        container.innerHTML = '';
        selectedSeats.forEach(s => {
            const inp = document.createElement('input');
            inp.type = 'hidden'; inp.name = 'seatNumber'; inp.value = s;
            container.appendChild(inp);
        });
        return true;
    }

    // Init price display
    updatePrice();
</script>
</body>
</html>
