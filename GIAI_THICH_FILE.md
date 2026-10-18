# Giải Thích Ý Nghĩa Từng File Trong Dự Án

Dự án áp dụng mô hình phân lớp rõ ràng (Model - Algorithm - IO - UI) phục vụ môn Cấu trúc dữ liệu và giải thuật.

---

## 1. Cấu trúc và Ý nghĩa từng File

| Đường dẫn file | Vai trò / Ý nghĩa chính | Thuật toán / Cấu trúc dữ liệu |
| :--- | :--- | :--- |
| `pom.xml` | File cấu hình Maven, quản lý phiên bản Java (21), thư viện JavaFX Controls và JUnit 5. | Quản lý phụ thuộc & build |
| `model/User.java` | Đại diện cho một đỉnh ($V$) trong đồ thị. Chứa `id` (duy nhất), `name`, các ràng buộc hợp lệ và phương thức `equals/hashCode`. | Đỉnh của đồ thị (Vertex) |
| `model/SocialGraph.java` | Cấu trúc dữ liệu Đồ thị vô hướng mạng xã hội. Quản lý đỉnh, cạnh, truy vấn láng giềng, bạn chung, thống kê $|V|$ và $|E|$. | Danh sách kề (`Map<String, Set<String>>`) |
| `algorithm/BfsAlgorithms.java` | Lớp tiện ích thực hiện: (1) Duyệt đồ thị theo chiều rộng (BFS); (2) Gợi ý bạn mới (FOAF) ở khoảng cách 2, xếp hạng theo số bạn chung giảm dần. | Queue (FIFO), Set visited. Độ phức tạp: $O(V + E)$ |
| `algorithm/DfsAndPathAlgorithms.java` | Lớp tiện ích thực hiện: (1) Duyệt chiều sâu (DFS) đệ quy; (2) Tìm đường đi ngắn nhất giữa 2 đỉnh bằng BFS; (3) Gợi ý bạn bè bằng DFS. | Call stack / Đệ quy & Truy vết parent map. Độ phức tạp: $O(V + E)$ |
| `io/CsvGraphRepository.java` | Lưu và đọc đồ thị từ file `.csv` chuẩn RFC 4180. Dòng `U,"id","tên"` lưu đỉnh, dòng `F,"id1","id2"` lưu cạnh. Hỗ trợ tiếng Việt UTF-8. | Xử lý I/O dòng văn bản & Parsing CSV |
| `ui/GraphPane.java` | Khung trực quan hóa đồ thị: bố trí vòng tròn, kéo thả đỉnh mượt mà, highlight đường đi và tập đỉnh kết quả, tooltip và click chọn đỉnh. | JavaFX Custom Pane, Shape rendering |
| `SocialGraphApp.java` | Điểm vào chính của ứng dụng (`main`). Xây dựng giao diện JavaFX hoàn chỉnh, điều phối tương tác 2 chiều giữa danh sách, form và sơ đồ đồ thị. | JavaFX Application, Event-driven UI |
| `docs/so-do-lop.md` | Tài liệu đặc tả kiến trúc phần mềm, thể hiện sơ đồ quan hệ lớp UML bằng Mermaid. | Tài liệu thiết kế hệ thống |
| `HUONG_DAN_CHAY.md` | Hướng dẫn nhanh cách thiết lập môi trường, chạy kiểm thử, mở ứng dụng và thao tác chức năng. | Hướng dẫn vận hành |
| `GIAI_THICH_FILE.md` | File hiện tại: Giải thích ngắn gọn mục đích và chức năng của từng file trong dự án. | Tài liệu báo cáo dự án |

---

## 2. Các File Kiểm Thử (Unit Tests - `src/test/java`)

| File kiểm thử | Đối tượng kiểm thử | Nội dung kiểm thử chính |
| :--- | :--- | :--- |
| `model/SocialGraphTest.java` | `SocialGraph` & `User` | Tính vô hướng, thêm/xóa đỉnh & cạnh, bạn chung, cập nhật tên, validate rỗng/null, đếm $|V|$ và $|E|$. |
| `algorithm/BfsAlgorithmsTest.java` | `BfsAlgorithms` | Thứ tự duyệt BFS, gợi ý bạn bè chuẩn xác, ưu tiên bạn chung, đỉnh cô lập và kiểm thử tốc độ trên đồ thị 600 đỉnh. |
| `algorithm/DfsAndPathAlgorithmsTest.java` | `DfsAndPathAlgorithms` | Thứ tự duyệt DFS, tìm đường ngắn nhất, xử lý đồ thị rời rạc, đỉnh tới chính nó, đường tắt trong chu trình, stress test 600 đỉnh. |
| `io/CsvGraphRepositoryTest.java` | `CsvGraphRepository` | Lưu và tải khôi phục dữ liệu nguyên vẹn (round-trip), xử lý ký tự đặc biệt (dấu phẩy, ngoặc kép, Unicode). |

