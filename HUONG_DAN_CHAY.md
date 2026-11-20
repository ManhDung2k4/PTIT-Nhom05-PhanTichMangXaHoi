# Hướng Dẫn Chạy Ứng Dụng Mạng Xã Hội

## 1. Yêu cầu hệ thống
- **JDK:** Java 21 trở lên (đã cấu hình biến môi trường `JAVA_HOME`).
- **Maven:** Phiên bản 3.9 trở lên.

---

## 2. Các lệnh thực thi (Terminal / PowerShell)

Mở terminal tại thư mục dự án (`ccdlgt-java\ccdlgt-java`):

```powershell
# 1. Chạy kiểm thử tự động (Unit Tests)
mvn test

# 2. Khởi chạy giao diện trực quan JavaFX
mvn javafx:run

# 3. Đóng gói ứng dụng thành file JAR
mvn clean package
```

---

## 3. Hướng dẫn sử dụng giao diện (GUI)

1. **Quản lý người dùng (Cột bên trái):**
   - **Tìm kiếm:** Gõ tên hoặc ID vào ô tìm kiếm để lọc nhanh danh sách.
   - **Thêm:** Nhập `Mã ID` (vd: `u7`), `Họ tên` (vd: `Lan Anh`) → Bấm **Thêm**.
   - **Sửa tên:** Chọn người dùng từ danh sách → Nhập tên mới → Bấm **Sửa tên**.
   - **Xóa:** Chọn người dùng → Bấm **Xóa** (tự động xóa toàn bộ liên kết liên quan).

2. **Chọn Đỉnh xuất phát (Nguồn) và Đỉnh đích (Mục tiêu):**
   - **Đỉnh xuất phát (Người 1 - Màu tím):** Click chuột trái vào đỉnh trên sơ đồ hoặc chọn trong danh sách rồi bấm **📌 Chọn Người 1**.
   - **Đỉnh đích (Người 2 - Màu xanh lục):** Click chuột phải vào đỉnh trên sơ đồ chọn **🎯 Đặt làm Người 2 (Mục tiêu)**, hoặc chọn trong danh sách rồi bấm **🎯 Đặt làm mục tiêu**.
   - **Đổi chỗ / Xóa:** Bấm **⇄ Đổi chỗ 1 & 2** để hoán đổi nhanh, hoặc **✕ Xóa chọn** để chọn lại.

3. **Thao tác quan hệ & Thuật toán:**
   - **Thao tác giữa 2 đỉnh (Xuất phát ➔ Đích):**
     - **Duyệt BFS (Nguồn ➔ Đích):** Duyệt theo chiều rộng từ đỉnh xuất phát cho đến khi chạm tới đỉnh đích; hiển thị chuỗi thứ tự duyệt và đường đi ngắn nhất tìm được.
     - **Duyệt DFS (Nguồn ➔ Đích):** Duyệt theo chiều sâu từ đỉnh xuất phát cho đến khi chạm tới đỉnh đích; hiển thị chuỗi thứ tự duyệt và đường đi theo DFS.
     - **Đường ngắn nhất:** Tìm đường nối ít cạnh nhất giữa 2 đỉnh.
     - **Kết bạn / Hủy kết bạn:** Bấm **➕ Kết bạn** hoặc **➖ Hủy kết bạn**.
     - **Bạn chung:** Xem danh sách và highlight các bạn chung của 2 người.
   - **Thao tác trên Đỉnh xuất phát (Người 1):**
     - **Xem bạn bè:** Xem danh sách bạn bè trực tiếp của Người 1.
     - **Gợi ý bạn bè (BFS / DFS):** Tìm bạn của bạn (độ sâu 2), xếp hạng theo số bạn chung.
     - **Duyệt toàn bộ BFS / DFS:** Xem thứ tự duyệt toàn bộ thành phần liên thông từ Người 1.
     - **Bỏ Highlight:** Xóa toàn bộ trạng thái đánh dấu trên sơ đồ.

4. **Tương tác trên sơ đồ đồ thị:**
   - **Kéo - thả:** Giữ chuột vào đỉnh bất kỳ để tùy chỉnh vị trí thẩm mỹ.
   - **Chuột trái:** Chọn nhanh Người 1.
   - **Chuột phải:** Menu ngữ cảnh (Đặt Người 1, Đặt Người 2, Bỏ chọn).
   - **Rê chuột (Tooltip):** Xem tên, ID, vai trò và số lượng bạn bè.
   - **Lưu / Mở CSV:** Xuất hoặc nạp dữ liệu mạng xã hội ra file `.csv` chuẩn RFC 4180.
