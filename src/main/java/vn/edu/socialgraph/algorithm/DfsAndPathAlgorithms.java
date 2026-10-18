package vn.edu.socialgraph.algorithm;

import vn.edu.socialgraph.model.SocialGraph;
import vn.edu.socialgraph.model.User;

import java.util.*;

/**
 * Utility class providing DFS traversal, shortest path (BFS), and friend suggestions using DFS.
 */
public final class DfsAndPathAlgorithms {
    private DfsAndPathAlgorithms() {}

    /**
     * Duyệt đồ thị theo chiều sâu (Depth-First Search - DFS) bắt đầu từ {@code startId}.
     * <p>
     * <b>Nguyên lý hoạt động:</b>
     * Xuất phát từ đỉnh hiện tại, đệ quy đi sâu vào từng nhánh đỉnh kề chưa được thăm trước
     * khi quay lui (backtracking).
     * <p>
     * <b>Độ phức tạp:</b>
     * <ul>
     *   <li>Thời gian: O(V + E) với V là số đỉnh, E là số cạnh trong thành phần liên thông.</li>
     *   <li>Không gian: O(V) cho ngăn xếp đệ quy (call stack) và tập visited.</li>
     * </ul>
     *
     * @param graph   Đối tượng đồ thị {@link SocialGraph}
     * @param startId ID người dùng bắt đầu duyệt
     * @return Danh sách các {@link User} theo thứ tự thăm của DFS
     */
    public static List<User> dfs(SocialGraph graph, String startId) {
        graph.requireUser(startId);
        List<User> result = new ArrayList<>();
        dfsVisit(graph, startId, new LinkedHashSet<>(), result);
        return result;
    }

    private static void dfsVisit(SocialGraph graph, String id, Set<String> visited, List<User> result) {
        if (!visited.add(id)) return;
        result.add(graph.requireUser(id));
        for (String next : graph.neighborIds(id)) {
            dfsVisit(graph, next, visited, result);
        }
    }

    /**
     * Tìm đường đi ngắn nhất (ít cạnh nhất) giữa 2 người dùng {@code fromId} và {@code toId}.
     * <p>
     * <b>Nguyên lý hoạt động:</b>
     * Vì đồ thị vô hướng không trọng số, thuật toán BFS đảm bảo đường đi đầu tiên tìm thấy
     * tới đỉnh đích luôn là đường đi ngắn nhất (tối thiểu số cạnh trung gian).
     * Bảng {@code parent} được sử dụng để truy vết ngược từ đỉnh đích về đỉnh nguồn.
     * <p>
     * <b>Độ phức tạp:</b>
     * <ul>
     *   <li>Thời gian: O(V + E).</li>
     *   <li>Không gian: O(V) để lưu hàng đợi, tập visited và bảng parent.</li>
     * </ul>
     *
     * @param graph  Đối tượng đồ thị {@link SocialGraph}
     * @param fromId ID người dùng nguồn
     * @param toId   ID người dùng đích
     * @return Danh sách các {@link User} liên tiếp tạo nên đường đi ngắn nhất (rỗng nếu không có đường đi)
     */
    public static List<User> shortestPath(SocialGraph graph, String fromId, String toId) {
        graph.requireUser(fromId);
        graph.requireUser(toId);

        if (fromId.equals(toId)) {
            return List.of(graph.requireUser(fromId));
        }

        Queue<String> queue = new ArrayDeque<>();
        Map<String, String> parent = new HashMap<>();
        Set<String> visited = new HashSet<>();
        
        visited.add(fromId);
        queue.add(fromId);

        while (!queue.isEmpty()) {
            String current = queue.remove();
            if (current.equals(toId)) {
                return buildPath(graph, parent, toId);
            }
            for (String next : graph.neighborIds(current)) {
                if (visited.add(next)) {
                    parent.put(next, current);
                    queue.add(next);
                }
            }
        }
        return List.of();
    }

    /**
     * Tái tạo chuỗi đường đi từ bảng phụ huynh (parent map) bằng cách truy vết ngược từ đỉnh đích.
     */
    private static List<User> buildPath(SocialGraph graph, Map<String, String> parent, String target) {
        LinkedList<User> path = new LinkedList<>();
        for (String id = target; id != null; id = parent.get(id)) {
            path.addFirst(graph.requireUser(id));
        }
        return path;
    }

    /**
     * Gợi ý bạn bè mới bằng phương pháp DFS mở rộng đệ quy tới độ sâu 2.
     * <p>
     * <b>Nguyên lý hoạt động:</b>
     * Từ các bạn trực tiếp (độ sâu 1), tiếp tục duyệt sang bạn bè của họ (độ sâu 2).
     * Loại bỏ chính người dùng và bạn bè trực tiếp, đếm số đường đi để tính số bạn chung,
     * sau đó sắp xếp theo số bạn chung giảm dần (hòa thì xếp theo tên).
     * <p>
     * <b>Độ phức tạp:</b>
     * <ul>
     *   <li>Thời gian: O(V + E).</li>
     *   <li>Không gian: O(V).</li>
     * </ul>
     *
     * @param graph  Đối tượng đồ thị {@link SocialGraph}
     * @param userId ID người dùng cần gợi ý
     * @return Danh sách các {@link User} tiềm năng được sắp xếp theo độ ưu tiên giảm dần
     */
    public static List<User> suggestFriends(SocialGraph graph, String userId) {
        graph.requireUser(userId);
        Set<String> directFriends = graph.neighborIds(userId);
        Map<String, Integer> mutualCounts = new HashMap<>();
        for (String friend : directFriends) {
            collectAtDepthTwo(graph, userId, friend, 1, directFriends, mutualCounts);
        }

        return mutualCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed()
                        .thenComparing(entry -> graph.requireUser(entry.getKey()).getName()))
                .map(entry -> graph.requireUser(entry.getKey()))
                .toList();
    }

    private static void collectAtDepthTwo(SocialGraph graph, String origin, String current, int depth,
                                          Set<String> directFriends, Map<String, Integer> mutualCounts) {
        if (depth == 2) {
            if (!current.equals(origin) && !directFriends.contains(current)) {
                mutualCounts.merge(current, 1, Integer::sum);
            }
            return;
        }
        for (String next : graph.neighborIds(current)) {
            collectAtDepthTwo(graph, origin, next, depth + 1, directFriends, mutualCounts);
        }
    }
}

