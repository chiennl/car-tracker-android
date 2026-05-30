# CarTracker for Android Box

Ứng dụng Android theo dõi vị trí phương tiện theo thời gian thực dành cho Android Box trên ô tô.

## Trạng thái phiên bản 0.1.0

Bản mã nguồn khởi đầu gồm:

- Giao diện bảng điều khiển viết bằng Kotlin và Jetpack Compose, tối ưu màn hình ngang Android Box.
- Theo dõi GPS bằng `ForegroundService`, hiển thị tốc độ, tọa độ, độ chính xác và tổng quãng đường.
- Lưu lịch sử hành trình cục bộ bằng Room Database.
- Tùy chọn tự khởi động ghi hành trình sau khi Android Box khởi động lại.
- Khung đăng nhập Google bằng Credential Manager và Firebase Authentication.
- Đồng bộ hành trình gần nhất lên Firebase Firestore sau khi cấu hình Firebase.

## Lưu ý về tự khởi động và quyền vị trí

Thiết bị đích ban đầu là Android Box chạy Android 13. Để tự ghi hành trình sau khi bật thiết bị, người dùng cần cấp quyền vị trí nền trong phần cài đặt ứng dụng.

Android 14 trở lên giới hạn việc dịch vụ định vị được khởi chạy từ nền/khởi động máy. Vì vậy ứng dụng sẽ hiển thị thông báo yêu cầu người dùng mở ứng dụng để tiếp tục theo dõi trên các phiên bản Android mới hơn, thay vì tự khởi động gây lỗi quyền.

## Mở dự án

1. Mở thư mục dự án bằng Android Studio.
2. Chọn JDK 17.
3. Đồng bộ Gradle.
4. Cài ứng dụng lên Android Box hoặc thiết bị thử nghiệm.

Nếu bản sao repository chưa có Gradle Wrapper, Android Studio có thể tạo wrapper; hoặc chạy `gradle wrapper --gradle-version 8.9` khi máy đã cài Gradle.

## Cấu hình Firebase để đăng nhập và đồng bộ

1. Tạo dự án Firebase và thêm Android app với package name: `vn.chiennl.cartracker`.
2. Bật **Authentication > Sign-in method > Google**.
3. Tạo/bảo đảm có OAuth Web client phù hợp.
4. Tải tệp `google-services.json`, đặt vào thư mục `app/`.
5. Bật **Cloud Firestore** và thiết lập quy tắc truy cập theo người dùng.

Khi chưa có `app/google-services.json`, ứng dụng vẫn build/chạy chế độ cục bộ; nút đăng nhập Google bị vô hiệu hóa.

## Cấu trúc dữ liệu Firestore

```text
users/{uid}/trips/{tripId}
users/{uid}/trips/{tripId}/points/{pointId}
```

## Quy tắc Firestore tham khảo

Chỉ sử dụng khi đã rà soát theo nhu cầu bảo mật chính thức của dự án:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{uid}/{document=**} {
      allow read, write: if request.auth != null && request.auth.uid == uid;
    }
  }
}
```

## Đưa mã nguồn lên GitHub

Do phiên tạo mã nguồn không có quyền ghi trực tiếp vào repository GitHub của chủ sở hữu, thực hiện trên máy đã đăng nhập GitHub:

```bash
git clone https://github.com/chiennl/car-tracker-android.git
```

Sao chép toàn bộ nội dung gói mã nguồn vào thư mục vừa clone, sau đó chạy:

```bash
git add .
git commit -m "feat: initialize CarTracker Android app with GPS tracking and Firebase sync"
git push origin main
```

Trên Windows PowerShell, sau khi sao chép tệp vào repository, có thể chạy:

```powershell
.\scripts\dong-bo-github.ps1
```

## Hướng phát triển tiếp theo

- Chạy thử trên Android Box, hiệu chỉnh mức tiêu thụ pin và tần suất lấy tọa độ.
- Thêm bản đồ hiển thị tuyến đường.
- Tích hợp OBD-II qua Bluetooth để lấy RPM, nhiệt độ và dữ liệu nhiên liệu.
- Tạo bản APK phát hành nội bộ và cơ chế cập nhật.
