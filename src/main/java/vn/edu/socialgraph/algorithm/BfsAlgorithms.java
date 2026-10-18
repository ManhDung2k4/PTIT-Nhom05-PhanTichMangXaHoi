package vn.edu.socialgraph.algorithm;

import vn.edu.socialgraph.model.SocialGraph;
import vn.edu.socialgraph.model.User;

import java.util.*;

/**
 * Utility class providing BFS algorithms for the social graph.
 * Includes BFS traversal and FOAF friend suggestions.
 */
public final class BfsAlgorithms {
    private BfsAlgorithms() {}

    /**
     * Duyệt toàn bộ thành phần liên thông của đồ thị bắt đầu từ đỉnh {@code startId} theo chiều rộng (BFS).
     * <p>
     * <b>Nguyên lý hoạt động:</b>
     * Sử dụng hàng đợi FIFO (Queue) và tập hợp đỉnh đã thăm (Set visited).
     * Mỗi lần lấy đỉnh ở đầu hàng đợi ra, thăm tất cả các đỉnh kề chưa được thăm và đẩy vào hàng đợi.
     * <p>
     * <b>Độ phức tạp:</b>
     * <ul>
     *   <li>Thời gian: O(V + E) với V là số đỉnh, E là số cạnh trong thành phần liên thông.</li>
     *   <li>Không gian: O(V) để lưu hàng đợi và tập visited.</li>
     * </ul>
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
     * Gợi ý bạn bè mới cho người dùng {@code userId} bằng BFS (Friend of a Friend - FOAF).
     * <p>
     * <b>Quy tắc gợi ý:</b>
     * <ol>
     *   <li>Chỉ xét các người dùng ở khoảng cách đúng bằng 2 (bạn của bạn).</li>
     *   <li>Loại bỏ chính người dùng đó và những người đã là bạn bè trực tiếp (khoảng cách 1).</li>
     *   <li>Đếm số lượng bạn chung (số đường đi độ dài 2 nối giữa 2 người).</li>
     *   <li>Ưu tiên xếp hạng: Người có nhiều bạn chung hơn đứng trước;
     *       nếu bằng điểm thì sắp xếp theo tên hiển thị theo thứ tự bảng chữ cái.</li>
     * </ol>
     * <p>
     * <b>Độ phức tạp:</b>
     * <ul>
     *   <li>Thời gian: O(deg(u) * max_deg) &le; O(V + E).</li>
     *   <li>Không gian: O(V) lưu trữ bảng đếm tần suất và hàng đợi.</li>
     * </ul>
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

