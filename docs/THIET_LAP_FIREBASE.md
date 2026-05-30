# Thiết lập Firebase cho CarTracker

## 1. Tạo ứng dụng Android trong Firebase

- Package name: `vn.chiennl.cartracker`
- Tải tệp `google-services.json`.
- Sao chép tệp vào `app/google-services.json`.

Tệp này không được commit công khai nếu trong quá trình quản trị dự án có yêu cầu bảo mật riêng; `.gitignore` hiện đang loại trừ tệp khỏi repository.

## 2. Bật đăng nhập Google

Trong Firebase Console, bật Authentication > Sign-in method > Google. Ứng dụng sử dụng Credential Manager và OAuth Web Client ID được sinh từ cấu hình Firebase.

## 3. Bật Cloud Firestore

Tạo Firestore Database và triển khai quy tắc tại `firebase/firestore.rules`. Dữ liệu được tách theo UID của người đã đăng nhập:

```text
users/{uid}/trips/{tripId}
users/{uid}/trips/{tripId}/points/{pointId}
```

## 4. Kiểm tra

- Mở ứng dụng và chọn **Đăng nhập Google**.
- Ghi một hành trình thử.
- Chọn **Đồng bộ gần nhất**.
- Kiểm tra tài liệu phát sinh trong Firestore.
