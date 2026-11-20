package vn.edu.socialgraph.algorithm;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import vn.edu.socialgraph.model.SocialGraph;
import vn.edu.socialgraph.model.User;


public final class BfsAlgorithms {
    private BfsAlgorithms() {}

    /**
     * Duyệt toàn bộ thành phần liên thông của đồ thị bắt đầu từ đỉnh {@code startId} theo chiều rộng (BFS).
     * 
     * Nguyên lý hoạt động:
     * Sử dụng hàng đợi FIFO (Queue) và tập hợp đỉnh đã thăm (Set visited).
     * Mỗi lần lấy đỉnh ở đầu hàng đợi ra, thăm tất cả các đỉnh kề chưa được thăm và đẩy vào hàng đợi.
     * 
     * Độ phức tạp:
     * 
     *   Thời gian: O(V + E) với V là số đỉnh, E là số cạnh trong thành phần liên thông.
     *   Không gian: O(V) để lưu hàng đợi và tập visited.
     * 
     *
     * @param graph   Đối tượng đồ thị {@link SocialGraph}
     * @param startId ID của người dùng bắt đầu duyệt
     * @return Danh sách các {@link User} theo thứ tự thăm của BFS
     */
    public static List<User> bfs(SocialGraph graph, String startId) {
        graph.requireUser(startId);
        List<User> result = new ArrayList<>();
        Set<String> visited = new LinkedHashSet<>();
        Queue<String> queue = new ArrayDeque<>();
        
        visited.add(startId);
        queue.add(startId);

        while (!queue.isEmpty()) {
            String current = queue.remove();
            result.add(graph.requireUser(current));
            for (String next : graph.neighborIds(current)) {
                if (visited.add(next)) {
                    queue.add(next);
                }
            }
        }
        return result;
    }

    /**
     * Duyệt theo chiều rộng (BFS) từ đỉnh xuất phát {@code startId} cho đến khi gặp đỉnh đích {@code targetId}.
     * Quá trình duyệt dừng lại ngay khi đỉnh đích được thăm.
     *
     * @param graph    Đối tượng đồ thị {@link SocialGraph}
     * @param startId  ID người dùng xuất phát
     * @param targetId ID người dùng đích
     * @return Danh sách các {@link User} theo thứ tự thăm của BFS cho đến khi gặp đích
     */
    public static List<User> bfs(SocialGraph graph, String startId, String targetId) {
        graph.requireUser(startId);
        graph.requireUser(targetId);

        List<User> result = new ArrayList<>();
        Set<String> visited = new LinkedHashSet<>();
        Queue<String> queue = new ArrayDeque<>();

        visited.add(startId);
        queue.add(startId);

        while (!queue.isEmpty()) {
            String current = queue.remove();
            result.add(graph.requireUser(current));
            if (current.equals(targetId)) {
                break;
            }
            for (String next : graph.neighborIds(current)) {
                if (visited.add(next)) {
                    queue.add(next);
                }
            }
        }
        return result;
    }

    /**
     * Gợi ý bạn bè mới cho người dùng {@code userId} bằng BFS (Friend of a Friend - FOAF).
     * 
     * Quy tắc gợi ý:
     * 
     *   Chỉ xét các người dùng ở khoảng cách đúng bằng 2 (bạn của bạn).
     *   Loại bỏ chính người dùng đó và những người đã là bạn bè trực tiếp (khoảng cách 1).
     *   Đếm số lượng bạn chung (số đường đi độ dài 2 nối giữa 2 người).
     *   Ưu tiên xếp hạng: Người có nhiều bạn chung hơn đứng trước;
     *       nếu bằng điểm thì sắp xếp theo tên hiển thị theo thứ tự bảng chữ cái.
     * 
     * 
     * Độ phức tạp:
     * 
     *   Thời gian: O(deg(u) * max_deg) &le; O(V + E).
     *   Không gian: O(V) lưu trữ bảng đếm tần suất và hàng đợi.
     * 
     *
     * @param graph  Đối tượng đồ thị {@link SocialGraph}
     * @param userId ID người dùng cần nhận gợi ý
     * @return Danh sách các {@link User} tiềm năng được sắp xếp theo độ ưu tiên giảm dần
     */
    public static List<User> suggestFriends(SocialGraph graph, String userId) {
        graph.requireUser(userId);
        Set<String> directFriends = graph.neighborIds(userId);
        Map<String, Integer> mutualCounts = new HashMap<>();
        Queue<NodeDepth> queue = new ArrayDeque<>();
        Set<String> expanded = new HashSet<>();
        
        queue.add(new NodeDepth(userId, 0));

        while (!queue.isEmpty()) {
            NodeDepth current = queue.remove();
            if (current.depth() >= 2 || !expanded.add(current.id())) {
                continue;
            }
            for (String next : graph.neighborIds(current.id())) {
                int nextDepth = current.depth() + 1;
                if (nextDepth == 2) {
                    // Nếu ở độ sâu 2, không phải là chính mình và chưa kết bạn trực tiếp
                    if (!next.equals(userId) && !directFriends.contains(next)) {
                        mutualCounts.merge(next, 1, Integer::sum);
                    }
                } else if (nextDepth < 2) {
                    queue.add(new NodeDepth(next, nextDepth));
                }
            }
        }

        return mutualCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed()
                        .thenComparing(entry -> graph.requireUser(entry.getKey()).getName()))
                .map(entry -> graph.requireUser(entry.getKey()))
                .toList();
    }

    /** Bản ghi phụ trợ lưu trữ mã đỉnh và độ sâu tương ứng trong quá trình duyệt BFS. */
    private record NodeDepth(String id, int depth) {}
}

