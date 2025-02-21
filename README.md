# Android-Travel-App
## Mô tả dự án: Ứng dụng đặt tour du lịch trực tuyến

### 1. Mục tiêu dự án:

Xây dựng một ứng dụng di động thông minh, cho phép người dùng dễ dàng tìm kiếm, lựa chọn và đặt các tour du lịch trực tuyến một cách nhanh chóng và tiện lợi. Ứng dụng cũng cung cấp một nền tảng quản lý cho phép người quản trị theo dõi và xử lý các đơn đặt tour một cách hiệu quả.

### 2. Đối tượng mục tiêu:

- Người dùng: Khách hàng có nhu cầu tìm kiếm và đặt tour du lịch trực tuyến.
- Người quản trị: Nhân viên hoặc chủ sở hữu các công ty du lịch, có nhiệm vụ quản lý các tour du lịch và các đơn đặt hàng.
### 3. Tính năng:

#### 3.1. Ứng dụng người dùng:

- Xem thông tin tour:
    - Hiển thị danh sách các tour du lịch theo nhiều tiêu chí (ví dụ: địa điểm, thời gian, giá cả...).
    - Xem chi tiết thông tin về tour (lịch trình, hình ảnh, đánh giá...).
- Tìm kiếm và lọc tour:
    - Tìm kiếm tour theo địa điểm, thời gian, giá cả, hoặc các tiêu chí khác.
    - Lọc tour theo loại hình du lịch (ví dụ: du lịch biển, du lịch núi, du lịch khám phá...).
- Đặt tour:
    - Chọn tour yêu thích và đặt tour trực tuyến.
    - Nhập thông tin cá nhân và thông tin liên hệ.
    - Lựa chọn phương thức thanh toán.
- Quản lý đặt chỗ:
    - Xem lại lịch sử đặt tour.
    - Hủy đặt tour (nếu có).
    - Nhận thông báo về tình trạng đặt tour.
- Thông tin người dùng:
    - Quản lý thông tin cá nhân.
    - Xem lịch sử đánh giá và đánh giá tour.
#### 3.2. Ứng dụng quản trị:

- Quản lý tour:
    - Thêm, sửa, xóa thông tin tour.
    - Cập nhật lịch trình, hình ảnh, giá cả của tour.
- Quản lý đơn đặt hàng:
    - Xem danh sách các đơn đặt hàng.
    - Xác nhận hoặc hủy đơn đặt hàng.
    - Xem thông tin chi tiết về khách hàng và tour.
- Thống kê và báo cáo:
    - Xem thống kê về số lượng tour đã bán.
    - Xem báo cáo về doanh thu.
- Quản lý người dùng:
    - Xem danh sách người dùng.
    - Phân quyền người dùng.
### 4. Công nghệ:

- Nền tảng: Android
- Ngôn ngữ lập trình: Java
- Cơ sở dữ liệu: Firebase Realtime Database hoặc Cloud Firestore
- Xác thực người dùng: Firebase Authentication
- Thông báo: Firebase Cloud Messaging (FCM)
### 5. Mô hình hoạt động:

- Ứng dụng người dùng và ứng dụng quản trị sẽ giao tiếp với nhau thông qua Firebase.
- Firebase đóng vai trò là server trung gian, lưu trữ dữ liệu và xử lý các yêu cầu từ ứng dụng.
- Khi người dùng đặt tour, thông tin đặt tour sẽ được lưu trữ trên Firebase.
- Ứng dụng quản trị sẽ nhận thông báo về đơn đặt hàng mới và có thể xem thông tin chi tiết.

<img width="833" alt="Image" src="https://github.com/user-attachments/assets/ae3171cb-abf9-41a0-8edf-bd1295b92828" />