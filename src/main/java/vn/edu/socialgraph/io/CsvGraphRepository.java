package vn.edu.socialgraph.io;

import vn.edu.socialgraph.model.SocialGraph;
import vn.edu.socialgraph.model.User;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Lớp chịu trách nhiệm lưu trữ và đọc dữ liệu đồ thị từ tập tin định dạng CSV.
 * <p>
 * <b>Quy ước cấu trúc file CSV:</b>
 * <ul>
 *   <li>Dòng khai báo người dùng (Đỉnh): {@code U,"id","tên"}</li>
 *   <li>Dòng khai báo quan hệ bạn bè (Cạnh): {@code F,"id1","id2"}</li>
 * </ul>
 * Hỗ trợ đầy đủ ký tự đặc biệt như dấu phẩy, dấu ngoặc kép (escape theo chuẩn RFC 4180)
 * và bảng mã UTF-8 tiếng Việt có dấu.
 */
public final class CsvGraphRepository {

    /**
     * Xuất dữ liệu từ đồ thị hiện tại ra tập tin CSV.
     *
     * @param graph Đối tượng đồ thị {@link SocialGraph} chứa dữ liệu
     * @param file  Đường dẫn tập tin đích {@link Path}
     * @throws IOException nếu có lỗi đọc ghi tập tin
     */
    public void save(SocialGraph graph, Path file) throws IOException {
        List<String> lines = new ArrayList<>();
        
        // 1. Ghi danh sách các người dùng (Nodes)
        for (User user : graph.getUsers()) {
            lines.add("U," + quote(user.getId()) + "," + quote(user.getName()));
        }
        
        // 2. Ghi danh sách các mối quan hệ bạn bè (Edges) - chỉ ghi mỗi cặp 1 lần duy nhất
        Set<String> savedEdges = new HashSet<>();
        for (User user : graph.getUsers()) {
            for (String friendId : graph.neighborIds(user.getId())) {
                String key = edgeKey(user.getId(), friendId);
                if (savedEdges.add(key)) {
                    lines.add("F," + quote(user.getId()) + "," + quote(friendId));
                }
            }
        }
        
        Files.write(file, lines, StandardCharsets.UTF_8);
    }

    /**
     * Đọc và phục hồi cấu trúc đồ thị từ tập tin CSV.
     *
     * @param file Đường dẫn tập tin nguồn {@link Path}
     * @return Đối tượng {@link SocialGraph} đã nạp đầy đủ dữ liệu
     * @throws IOException nếu có lỗi đọc tập tin
     */
    public SocialGraph load(Path file) throws IOException {
        SocialGraph graph = new SocialGraph();
        List<List<String>> rows = Files.readAllLines(file, StandardCharsets.UTF_8).stream()
                .filter(line -> !line.isBlank())
                .map(this::parse)
                .toList();

        // Nạp người dùng trước (Vertex)
        for (List<String> row : rows) {
            if (row.size() == 3 && "U".equals(row.get(0))) {
                graph.addUser(new User(row.get(1), row.get(2)));
            }
        }

        // Sau đó kết nối các cạnh (Edge)
        for (List<String> row : rows) {
            if (row.size() == 3 && "F".equals(row.get(0))) {
                graph.connect(row.get(1), row.get(2));
            }
        }
        return graph;
    }

    /** Tạo khóa duy nhất cho cạnh vô hướng để tránh ghi lặp 2 chiều (u, v) và (v, u). */
    private String edgeKey(String a, String b) {
        return a.compareTo(b) < 0 ? a + "\0" + b : b + "\0" + a;
    }

    /** Bọc chuỗi trong dấu ngoặc kép và nhân đôi dấu ngoặc kép bên trong (RFC 4180). */
    private String quote(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    /** Phân tích cú pháp 1 dòng CSV hỗ trợ trường nằm trong ngoặc kép chứa dấu phẩy. */
    private List<String> parse(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++; // Bỏ qua ký tự " thứ hai
                } else {
                    quoted = !quoted;
                }
            } else if (c == ',' && !quoted) {
                fields.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        if (quoted) {
            throw new IllegalArgumentException("Dòng CSV không hợp lệ (thiếu dấu đóng ngoặc kép): " + line);
        }
        fields.add(current.toString());
        return fields;
    }
}

