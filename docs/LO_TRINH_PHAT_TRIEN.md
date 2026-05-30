# Lộ trình phát triển CarTracker

## Mốc 1 — GPS và dữ liệu hành trình (mã nguồn hiện tại)

- Bảng điều khiển màn hình ngang cho Android Box.
- Theo dõi vị trí bằng dịch vụ foreground.
- Lưu tốc độ, tọa độ, độ chính xác, quãng đường và lịch sử chuyến đi.
- Đăng nhập Google/Firebase và đồng bộ chuyến gần nhất.

## Mốc 2 — Bản đồ và giám sát từ xa

- Bản đồ tuyến đường theo thời gian thực.
- Đồng bộ theo lô khi có mạng.
- Màn hình web hoặc ứng dụng thứ hai để theo dõi xe.

## Mốc 3 — OBD-II

- Kết nối Bluetooth với ELM327/OBD-II.
- Lấy RPM, nhiệt độ nước làm mát, tốc độ xe, mã lỗi.
- Kiểm thử tương thích riêng với thiết bị OBD-II thực tế.

## Mốc 4 — Phát hành nội bộ

- Cấu hình ký APK.
- Quyền và chính sách dữ liệu vị trí.
- Tự động build APK bằng GitHub Actions.
