<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>${movie == null ? 'Thêm Phim Mới' : 'Sửa Phim'} - Cinema</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }

        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            padding: 30px 20px;
        }

        .container {
            max-width: 750px;
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

        .form-body { padding: 35px 40px; }

        .form-row {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 20px;
        }

        .form-group { margin-bottom: 22px; }
        .form-group.full { grid-column: 1 / -1; }

        label {
            display: block;
            margin-bottom: 7px;
            font-weight: 600;
            color: #444;
            font-size: 0.92em;
        }
        label span.req { color: #e53935; margin-left: 2px; }

        input[type=text],
        input[type=number],
        textarea,
        select {
            width: 100%;
            padding: 11px 14px;
            border: 2px solid #dee2e6;
            border-radius: 8px;
            font-size: 15px;
            font-family: inherit;
            transition: border-color 0.2s;
            outline: none;
        }
        input:focus, textarea:focus, select:focus { border-color: #667eea; }
        textarea { resize: vertical; min-height: 70px; }

        /* Genre checkboxes */
        .genre-grid {
            display: flex;
            flex-wrap: wrap;
            gap: 10px;
            padding: 12px;
            border: 2px solid #dee2e6;
            border-radius: 8px;
            background: #fafafa;
        }
        .genre-item {
            display: flex;
            align-items: center;
            gap: 6px;
            background: white;
            border: 2px solid #dee2e6;
            border-radius: 20px;
            padding: 5px 14px;
            cursor: pointer;
            transition: all 0.2s;
            font-size: 0.9em;
        }
        .genre-item:hover { border-color: #667eea; }
        .genre-item input[type=checkbox] { display: none; }
        .genre-item.checked {
            background: #667eea;
            border-color: #667eea;
            color: white;
        }

        /* Custom genre input */
        .custom-genre-row {
            display: flex;
            gap: 8px;
            margin-top: 10px;
        }
        .custom-genre-row input {
            flex: 1;
            padding: 8px 12px;
            border: 2px solid #dee2e6;
            border-radius: 8px;
            font-size: 0.9em;
        }
        .btn-add-genre {
            padding: 8px 16px;
            background: #667eea;
            color: white;
            border: none;
            border-radius: 8px;
            font-weight: bold;
            cursor: pointer;
            font-size: 0.9em;
            white-space: nowrap;
        }
        .btn-add-genre:hover { background: #5568d3; }

        /* Buttons */
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

        .divider {
            border: none;
            border-top: 1px solid #e9ecef;
            margin: 25px 0;
        }

        .section-title {
            font-size: 0.82em;
            font-weight: 700;
            text-transform: uppercase;
            letter-spacing: 1px;
            color: #667eea;
            margin-bottom: 18px;
        }
    </style>
</head>
<body>
<div class="container">
    <div class="header">
        <h1>${movie == null ? '🎬 THÊM PHIM MỚI' : '✏️ CHỈNH SỬA PHIM'}</h1>
        <p>${movie == null ? 'Nhập thông tin phim mới vào hệ thống' : 'Cập nhật thông tin phim'}</p>
    </div>

    <div class="form-body">
        <form action="${pageContext.request.contextPath}/movies" method="post" id="movieForm">
            <input type="hidden" name="action" value="${movie == null ? 'create' : 'update'}">
            <c:if test="${movie != null}">
                <input type="hidden" name="movieId" value="${movie.movieId}">
            </c:if>

            <%-- ── Thông tin cơ bản ─────────────────────────── --%>
            <div class="section-title">📋 Thông tin cơ bản</div>

            <div class="form-group">
                <label>Tên phim <span class="req">*</span></label>
                <input type="text" name="movieName" required maxlength="200"
                       placeholder="Nhập tên phim..."
                       value="${movie != null ? movie.movieName : ''}">
            </div>

            <div class="form-row">
                <div class="form-group">
                    <label>Thời lượng (phút) <span class="req">*</span></label>
                    <input type="number" name="duration" required min="1" max="500"
                           placeholder="VD: 120"
                           value="${movie != null ? movie.duration : ''}">
                </div>
                <div class="form-group">
                    <label>Năm phát hành <span class="req">*</span></label>
                    <input type="number" name="releaseYear" required min="1900" max="2100"
                           placeholder="VD: 2024"
                           value="${movie != null ? movie.releaseYear : ''}">
                </div>
                <div class="form-group">
                    <label>Quốc gia <span class="req">*</span></label>
                    <input type="text" name="country" required maxlength="100"
                           placeholder="VD: Mỹ, Hàn Quốc..."
                           value="${movie != null ? movie.country : ''}">
                </div>
                <div class="form-group">
                    <label>Giới hạn độ tuổi <span class="req">*</span></label>
                    <select name="ageRestriction" required>
                        <option value="">-- Chọn --</option>
                        <c:forEach var="age" items="${[0, 13, 16, 18]}">
                            <option value="${age}"
                                ${movie != null && movie.ageRestriction == age ? 'selected' : ''}>
                                ${age == 0 ? 'Mọi lứa tuổi (P)' : age == 13 ? '13+' : age == 16 ? '16+' : '18+'}
                            </option>
                        </c:forEach>
                    </select>
                </div>
            </div>

            <hr class="divider">

            <%-- ── Đạo diễn & diễn viên ───────────────────────── --%>
            <div class="section-title">🎭 Đạo diễn & Diễn viên</div>

            <div class="form-group">
                <label>Đạo diễn <span class="req">*</span></label>
                <input type="text" name="director" required maxlength="200"
                       placeholder="Tên đạo diễn..."
                       value="${movie != null ? movie.director : ''}">
            </div>

            <div class="form-group">
                <label>Diễn viên chính</label>
                <textarea name="mainActors" placeholder="VD: Tom Hanks, Emma Watson, ..."
                          maxlength="500">${movie != null ? movie.mainActors : ''}</textarea>
            </div>

            <hr class="divider">

            <%-- ── Thể loại ────────────────────────────────────── --%>
            <div class="section-title">🏷️ Thể loại phim</div>

            <div class="form-group">
                <label>Chọn thể loại (có thể chọn nhiều)</label>
                <div class="genre-grid" id="genreGrid">
                    <c:forEach var="genre" items="${allGenres}">
                        <c:set var="isChecked" value="false"/>
                        <c:if test="${movie != null}">
                            <c:forEach var="mg" items="${movie.genres}">
                                <c:if test="${mg == genre}">
                                    <c:set var="isChecked" value="true"/>
                                </c:if>
                            </c:forEach>
                        </c:if>
                        <label class="genre-item ${isChecked ? 'checked' : ''}"
                               onclick="toggleGenre(this)">
                            <input type="checkbox" name="genres" value="${genre}"
                                   ${isChecked ? 'checked' : ''}>
                            ${genre}
                        </label>
                    </c:forEach>
                </div>

                <%-- Thêm thể loại mới chưa có trong DB --%>
                <div class="custom-genre-row">
                    <input type="text" id="newGenreInput" placeholder="Thêm thể loại mới...">
                    <button type="button" class="btn-add-genre" onclick="addCustomGenre()">
                        ➕ Thêm
                    </button>
                </div>
            </div>

            <hr class="divider">

            <%-- ── Trạng thái ─────────────────────────────────── --%>
            <div class="section-title">📊 Trạng thái</div>

            <div class="form-group">
                <label>Trạng thái chiếu <span class="req">*</span></label>
                <select name="status" required>
                    <option value="Active"
                        ${movie == null || movie.status == 'Active' ? 'selected' : ''}>
                        🟢 Đang chiếu
                    </option>
                    <option value="Inactive"
                        ${movie != null && movie.status == 'Inactive' ? 'selected' : ''}>
                        🔴 Ngừng chiếu
                    </option>
                </select>
            </div>

            <%-- ── Nút submit ─────────────────────────────────── --%>
            <div class="btn-row">
                <button type="submit" class="btn btn-primary">
                    ${movie == null ? '✅ Thêm phim' : '💾 Lưu thay đổi'}
                </button>
                <a href="${pageContext.request.contextPath}/movies" class="btn btn-secondary">
                    ❌ Hủy
                </a>
            </div>
        </form>
    </div>
</div>

<script>
    // Toggle checkbox style cho thể loại
    function toggleGenre(label) {
        const cb = label.querySelector('input[type=checkbox]');
        cb.checked = !cb.checked;
        label.classList.toggle('checked', cb.checked);
    }

    // Thêm thể loại mới vào grid
    function addCustomGenre() {
        const input = document.getElementById('newGenreInput');
        const name  = input.value.trim();
        if (!name) { alert('Vui lòng nhập tên thể loại!'); return; }

        // Kiểm tra trùng
        const existing = document.querySelectorAll('#genreGrid input[type=checkbox]');
        for (const cb of existing) {
            if (cb.value.toLowerCase() === name.toLowerCase()) {
                alert('Thể loại này đã tồn tại!');
                input.value = '';
                return;
            }
        }

        // Tạo label mới
        const label = document.createElement('label');
        label.className = 'genre-item checked';
        label.onclick = function() { toggleGenre(this); };
        label.innerHTML = `<input type="checkbox" name="genres" value="${name}" checked>${name}`;
        document.getElementById('genreGrid').appendChild(label);
        input.value = '';
    }

    // Cho phép nhấn Enter trong ô nhập thể loại
    document.getElementById('newGenreInput').addEventListener('keydown', function(e) {
        if (e.key === 'Enter') { e.preventDefault(); addCustomGenre(); }
    });
</script>
</body>
</html>
