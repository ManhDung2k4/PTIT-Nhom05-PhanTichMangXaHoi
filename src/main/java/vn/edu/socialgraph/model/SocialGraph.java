package vn.edu.socialgraph.model;

import java.util.*;

/**
 * Cấu trúc dữ liệu Đồ thị mạng xã hội (Social Graph).
 * <p>
 * Đồ thị được biểu diễn dưới dạng <b>Danh sách kề (Adjacency List)</b> vô hướng:
 * <ul>
 *   <li>Mỗi đỉnh (Vertex) là một {@link User}.</li>
 *   <li>Mỗi cạnh (Edge) biểu diễn mối quan hệ bạn bè 2 chiều giữa 2 người dùng.</li>
 * </ul>
 * Đồ thị hỗ trợ các thao tác cơ bản: thêm/xóa đỉnh, thêm/hủy cạnh, truy vấn láng giềng, tìm bạn chung.
 */
public final class SocialGraph {
    private final Map<String, User> users = new LinkedHashMap<>();
    private final Map<String, Set<String>> adjacency = new LinkedHashMap<>();

    /**
     * Thêm một người dùng mới vào đồ thị.
     * Độ phức tạp: O(1).
     *
     * @param user Đối tượng người dùng cần thêm
     * @throws NullPointerException     nếu user bị null
     * @throws IllegalArgumentException nếu ID người dùng đã tồn tại
     */
    public void addUser(User user) {
        Objects.requireNonNull(user, "Người dùng không được null");
        if (users.containsKey(user.getId())) {
            throw new IllegalArgumentException("ID đã tồn tại trong hệ thống: " + user.getId());
        }
        users.put(user.getId(), user);
        adjacency.put(user.getId(), new LinkedHashSet<>());
    }

    /**
     * Cập nhật tên của một người dùng đã có trong đồ thị.
     * Độ phức tạp: O(1).
     *
     * @param id      Mã ID người dùng cần cập nhật
     * @param newName Tên mới cần đặt
     */
    public void updateUser(String id, String newName) {
        requireUser(id).setName(newName);
    }

    /**
     * Xóa một người dùng và hủy bỏ toàn bộ quan hệ bạn bè liên quan.
     * Độ phức tạp: O(deg(id)) trong đó deg(id) là bậc của đỉnh.
     *
     * @param id Mã ID người dùng cần xóa
     * @throws IllegalArgumentException nếu người dùng không tồn tại
     */
    public void removeUser(String id) {
        requireUser(id);
        Set<String> neighbors = adjacency.get(id);
        if (neighbors != null) {
            for (String friend : neighbors) {
                Set<String> friendNeighbors = adjacency.get(friend);
                if (friendNeighbors != null) {
                    friendNeighbors.remove(id);
                }
            }
        }
        adjacency.remove(id);
        users.remove(id);
    }

    /**
     * Thiết lập quan hệ bạn bè 2 chiều giữa 2 người dùng.
     * Độ phức tạp: O(1).
     *
     * @param firstId  Mã ID người dùng thứ nhất
     * @param secondId Mã ID người dùng thứ hai
     * @throws IllegalArgumentException nếu tự kết bạn với chính mình hoặc user không tồn tại
     */
    public void connect(String firstId, String secondId) {
        requireUser(firstId);
        requireUser(secondId);
        if (firstId.equals(secondId)) {
            throw new IllegalArgumentException("Không thể tự kết bạn với chính mình");
        }
        adjacency.get(firstId).add(secondId);
        adjacency.get(secondId).add(firstId);
    }

    /**
     * Hủy quan hệ bạn bè 2 chiều giữa 2 người dùng.
     * Độ phức tạp: O(1).
     *
     * @param firstId  Mã ID người dùng thứ nhất
     * @param secondId Mã ID người dùng thứ hai
     * @throws IllegalArgumentException nếu user không tồn tại
     */
    public void disconnect(String firstId, String secondId) {
        requireUser(firstId);
        requireUser(secondId);
        adjacency.get(firstId).remove(secondId);
        adjacency.get(secondId).remove(firstId);
    }

    /**
     * Lấy đối tượng người dùng, nếu không có sẽ ném ra ngoại lệ.
     * Độ phức tạp: O(1).
     *
     * @param id Mã ID người dùng
     * @return Đối tượng {@link User}
     * @throws IllegalArgumentException nếu không tìm thấy người dùng
     */
    public User requireUser(String id) {
        User user = users.get(id);
        if (user == null) {
            throw new IllegalArgumentException("Không tìm thấy người dùng: " + id);
        }
        return user;
    }

    /**
     * Tìm kiếm người dùng theo ID (trả về Optional an toàn).
     * Độ phức tạp: O(1).
     *
     * @param id Mã ID người dùng
     * @return {@link Optional} chứa đối tượng User nếu tìm thấy
     */
    public Optional<User> findUser(String id) {
        return Optional.ofNullable(users.get(id));
    }

    /**
     * Kiểm tra người dùng có tồn tại trong mạng xã hội không.
     * Độ phức tạp: O(1).
     *
     * @param id Mã ID người dùng
     * @return true nếu tồn tại, ngược lại false
     */
    public boolean hasUser(String id) {
        return users.containsKey(id);
    }

    /**
     * Kiểm tra 2 người dùng có phải là bạn bè trực tiếp hay không.
     * Độ phức tạp: O(1).
     *
     * @param firstId  Mã ID người thứ nhất
     * @param secondId Mã ID người thứ hai
     * @return true nếu là bạn bè, ngược lại false
     */
    public boolean hasEdge(String firstId, String secondId) {
        if (!hasUser(firstId) || !hasUser(secondId)) return false;
        return adjacency.get(firstId).contains(secondId);
    }

    /**
     * Lấy toàn bộ danh sách người dùng trong hệ thống (chế độ chỉ đọc).
     *
     * @return Tập hợp các đối tượng {@link User}
     */
    public Collection<User> getUsers() {
        return Collections.unmodifiableCollection(users.values());
    }

    /**
     * Trả về tổng số lượng người dùng (|V| - Vertices) trong đồ thị.
     */
    public int getUserCount() {
        return users.size();
    }

    /**
     * Trả về tổng số lượng mối quan hệ bạn bè (|E| - Edges) trong đồ thị vô hướng.
     */
    public int getEdgeCount() {
        int totalDegree = 0;
        for (Set<String> neighbors : adjacency.values()) {
            totalDegree += neighbors.size();
        }
        return totalDegree / 2;
    }

    /**
     * Lấy tập hợp mã ID của tất cả bạn bè trực tiếp của một người dùng.
     * Độ phức tạp: O(1).
     *
     * @param id Mã ID người dùng
     * @return Tập hợp ID bạn bè (chỉ đọc)
     */
    public Set<String> neighborIds(String id) {
        requireUser(id);
        return Collections.unmodifiableSet(adjacency.get(id));
    }

    /**
     * Lấy danh sách các đối tượng bạn bè trực tiếp của một người dùng.
     * Độ phức tạp: O(deg(id)).
     *
     * @param id Mã ID người dùng
     * @return Danh sách các {@link User} là bạn bè
     */
    public List<User> friendsOf(String id) {
        return neighborIds(id).stream().map(users::get).toList();
    }

    /**
     * Tìm danh sách bạn chung (Mutual Friends) giữa 2 người dùng.
     * Độ phức tạp: O(min(deg(u), deg(v))).
     *
     * @param firstId  Mã ID người thứ nhất
     * @param secondId Mã ID người thứ hai
     * @return Danh sách các {@link User} là bạn chung
     */
    public List<User> commonFriends(String firstId, String secondId) {
        requireUser(firstId);
        requireUser(secondId);
        return adjacency.get(firstId).stream()
                .filter(adjacency.get(secondId)::contains)
                .map(users::get)
                .toList();
    }

    /**
     * Xóa toàn bộ dữ liệu người dùng và các liên kết trong đồ thị.
     */
    public void clear() {
        users.clear();
        adjacency.clear();
    }
}

