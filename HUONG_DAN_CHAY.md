# Hướng Dẫn Chạy Ứng Dụng Mạng Xã Hội

## 1. Yêu cầu hệ thống
- **JDK:** Java 21 trở lên (đã cấu hình biến môi trường `JAVA_HOME`).
- **Maven:** Phiên bản 3.9 trở lên.

---

## 2. Các lệnh thực thi (Terminal / PowerShell)

Mở terminal tại thư mục gốc của dự án (`ccdlgt-java`):

```powershell
# 1. Chạy toàn bộ kiểm thử tự động (Unit Tests)
mvn test

# 2. Khởi chạy giao diện đồ họa JavaFX
mvn javafx:run

# 3. Đóng gói ứng dụng thành file JAR
mvn clean package
```

---

## 3. Hướng dẫn sử dụng giao diện (GUI)

1. **Quản lý người dùng:**
   - **Thêm:** Nhập `ID` (vd: `u7`), `Tên` (vd: `Lan`) $\rightarrow$ Bấm **Thêm**.
   - **Sửa tên:** Chọn người dùng từ danh sách $\rightarrow$ Nhập tên mới $\rightarrow$ Bấm **Sửa tên**.
   - **Xóa:** Chọn người dùng $\rightarrow$ Bấm **Xóa** (tự động xóa toàn bộ liên kết liên quan).
2. **Quan hệ bạn bè & Thuật toán:**
   - **Chọn người thứ nhất:** Click chọn từ danh sách bên trái hoặc click trực tiếp vào đỉnh trên sơ đồ.
   - **Chọn người thứ hai:** Nhập `ID` vào ô *Người 2* (hoặc chọn user rồi bấm **Đặt làm mục tiêu**).
   - **Kết bạn / Hủy kết bạn:** Bấm nút **Kết bạn** hoặc **Hủy kết bạn**.
   - **Chạy thuật toán:**
     - **Xem bạn bè / Bạn chung:** Xem và highlight bạn bè trực tiếp hoặc bạn chung.
     - **Gợi ý (BFS / DFS):** Tìm bạn của bạn (khoảng cách 2), xếp hạng theo số bạn chung nhiều nhất.
     - **Duyệt BFS / DFS:** Xem thứ tự duyệt toàn bộ mạng lưới từ người được chọn.
     - **Đường ngắn nhất:** Tìm đường nối ít cạnh nhất giữa 2 người (đổi màu nổi bật các đỉnh và cạnh trên sơ đồ).
3. **Thao tác trên sơ đồ đồ thị:**
   - **Kéo - thả:** Giữ chuột vào đỉnh bất kỳ để di chuyển vị trí theo ý muốn.
   - **Rê chuột (Tooltip):** Xem thông tin chi tiết tên, ID, số lượng bạn bè.
   - **Căn lại sơ đồ:** Đưa các đỉnh về vị trí vòng tròn mặc định.
   - **Lưu / Mở CSV:** Xuất hoặc nạp dữ liệu mạng xã hội ra file `.csv`.

