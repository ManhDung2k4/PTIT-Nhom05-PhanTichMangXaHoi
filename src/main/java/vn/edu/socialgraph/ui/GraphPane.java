package vn.edu.socialgraph.ui;

import java.util.*;
import java.util.function.Consumer;

import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import vn.edu.socialgraph.model.SocialGraph;
import vn.edu.socialgraph.model.User;

/**
 * Khung trực quan hóa Đồ thị mạng xã hội (Graph Visualization Pane).
 * <p>
 * Tính năng nổi bật:
 * <ul>
 *   <li>Tự động bố trí các đỉnh theo vòng tròn (Circular Layout) cân đối.</li>
 *   <li>Hỗ trợ kéo - thả (Drag and Drop) từng đỉnh linh hoạt.</li>
 *   <li>Highlight trực quan đường đi ngắn nhất (đổi màu cạnh và các đỉnh liên tiếp).</li>
 *   <li>Highlight tập đỉnh kết quả của thuật toán (BFS, DFS, gợi ý bạn bè, bạn chung).</li>
 *   <li>Tooltip hiển thị chi tiết thông tin người dùng và số bạn bè khi rê chuột.</li>
 *   <li>Click vào đỉnh để chọn nhanh người dùng trong danh sách.</li>
 * </ul>
 */
public final class GraphPane extends Pane {
    private SocialGraph graph;
    private final Map<String, Point2D> customPositions = new HashMap<>();

    // Tập hợp ID các đỉnh và cạnh được highlight bởi thuật toán
    private final Set<String> highlightedUserIds = new LinkedHashSet<>();
    private final Set<String> highlightedEdgeKeys = new HashSet<>();
    private String selectedUserId = null;
    private Consumer<User> onUserClicked = null;

    public GraphPane(SocialGraph graph) {
        this.graph = graph;
        setMinHeight(360);
        setStyle("-fx-background-color: #ffffff; " +
                 "-fx-border-color: #cbd5e1; " +
                 "-fx-border-width: 1px; " +
                 "-fx-border-radius: 10px; " +
                 "-fx-background-radius: 10px;");

        widthProperty().addListener((o, a, b) -> redraw());
        heightProperty().addListener((o, a, b) -> redraw());
    }

    public void setGraph(SocialGraph graph) {
        this.graph = graph;
        this.customPositions.clear();
        this.highlightedUserIds.clear();
        this.highlightedEdgeKeys.clear();
        this.selectedUserId = null;
        redraw();
    }

    /** Thiết lập callback xử lý khi người dùng nhấp chuột vào một đỉnh trên sơ đồ. */
    public void setOnUserClicked(Consumer<User> onUserClicked) {
        this.onUserClicked = onUserClicked;
    }

    /** Đặt đỉnh đang được chọn để làm nổi bật. */
    public void setSelectedUser(User user) {
        this.selectedUserId = (user != null) ? user.getId() : null;
        redraw();
    }

    /** Highlight danh sách người dùng (kết quả BFS, DFS, gợi ý bạn bè, v.v.). */
    public void highlightUsers(List<User> users) {
        highlightedUserIds.clear();
        highlightedEdgeKeys.clear();
        if (users != null) {
            for (User u : users) {
                highlightedUserIds.add(u.getId());
            }
        }
        redraw();
    }

    /**
     * Highlight trực quan đường đi ngắn nhất: đổi màu các đỉnh trên đường đi
     * và làm nổi bật các cạnh nối giữa các đỉnh liên tiếp.
     */
    public void highlightPath(List<User> path) {
        highlightedUserIds.clear();
        highlightedEdgeKeys.clear();
        if (path != null && !path.isEmpty()) {
            for (User u : path) {
                highlightedUserIds.add(u.getId());
            }
            for (int i = 0; i < path.size() - 1; i++) {
                String u1 = path.get(i).getId();
                String u2 = path.get(i + 1).getId();
                highlightedEdgeKeys.add(edgeKey(u1, u2));
            }
        }
        redraw();
    }

    /** Xóa toàn bộ trạng thái highlight thuật toán. */
    public void clearHighlights() {
        highlightedUserIds.clear();
        highlightedEdgeKeys.clear();
        redraw();
    }

    /** Đặt lại vị trí tất cả các node về bố cục vòng tròn ban đầu. */
    public void resetPositions() {
        customPositions.clear();
        redraw();
    }

    public void redraw() {
        getChildren().clear();
        if (graph == null) return;

        List<User> all = List.copyOf(graph.getUsers());
        if (all.isEmpty() || getWidth() <= 0 || getHeight() <= 0) return;

        double centerX = getWidth() / 2.0;
        double centerY = getHeight() / 2.0;
        double radius = Math.max(60, Math.min(getWidth(), getHeight()) / 2.0 - 70);

        // 1. Tính toán tọa độ vị trí các node
        Map<String, Point2D> points = new HashMap<>();
        for (int i = 0; i < all.size(); i++) {
            String userId = all.get(i).getId();
            if (customPositions.containsKey(userId)) {
                points.put(userId, customPositions.get(userId));
            } else {
                double angle = 2 * Math.PI * i / all.size() - Math.PI / 2;
                Point2D pt = new Point2D(centerX + radius * Math.cos(angle), centerY + radius * Math.sin(angle));
                points.put(userId, pt);
            }
        }

        // 2. Vẽ các đường nối cạnh (Edges)
        for (User user : all) {
            for (String friendId : graph.neighborIds(user.getId())) {
                if (user.getId().compareTo(friendId) < 0 && points.containsKey(friendId)) {
                    Point2D a = points.get(user.getId());
                    Point2D b = points.get(friendId);

                    Line edge = new Line(a.getX(), a.getY(), b.getX(), b.getY());
                    String key = edgeKey(user.getId(), friendId);

                    if (highlightedEdgeKeys.contains(key)) {
                        // Cạnh thuộc đường đi ngắn nhất: Màu đỏ cam rực rỡ, nét đậm
                        edge.setStroke(Color.web("#ef4444"));
                        edge.setStrokeWidth(4.5);
                    } else {
                        // Cạnh thông thường
                        edge.setStroke(Color.web("#cbd5e1"));
                        edge.setStrokeWidth(2.0);
                    }
                    getChildren().add(edge);
                }
            }
        }

        // 3. Vẽ các đỉnh (Nodes)
        DropShadow normalShadow = new DropShadow(6, Color.web("#000000", 0.15));
        normalShadow.setOffsetY(2);

        DropShadow glowShadow = new DropShadow(14, Color.web("#f59e0b", 0.85));

        for (User user : all) {
            String uid = user.getId();
            Point2D pt = points.get(uid);

            boolean isSelected = uid.equals(selectedUserId);
            boolean isHighlighted = highlightedUserIds.contains(uid);

            Circle circle = new Circle(22);
            if (isSelected) {
                // Đỉnh đang được chọn: Màu tím đậm sang trọng
                circle.setFill(Color.web("#7c3aed"));
                circle.setStroke(Color.web("#4c1d95"));
                circle.setStrokeWidth(3.0);
                circle.setEffect(normalShadow);
            } else if (isHighlighted) {
                // Đỉnh kết quả thuật toán: Màu cam phát sáng
                circle.setFill(Color.web("#f59e0b"));
                circle.setStroke(Color.web("#b45309"));
                circle.setStrokeWidth(3.0);
                circle.setEffect(glowShadow);
            } else {
                // Đỉnh thông thường: Màu xanh lam hiện đại
                circle.setFill(Color.web("#2563eb"));
                circle.setStroke(Color.web("#1d4ed8"));
                circle.setStrokeWidth(2.0);
                circle.setEffect(normalShadow);
            }

            Label label = new Label(user.getName() + "\n(" + user.getId() + ")");
            label.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #ffffff; -fx-alignment: center;");
            label.setPrefWidth(90);
            label.setLayoutX(-45);
            label.setLayoutY(-15);
            label.setMouseTransparent(true);

            Group group = new Group(circle, label);
            group.setLayoutX(pt.getX());
            group.setLayoutY(pt.getY());
            group.setStyle("-fx-cursor: hand;");

            // Hiển thị thông tin người dùng khi rê chuột
            int friendCount = graph.neighborIds(uid).size();
            Tooltip.install(group, new Tooltip(
                String.format("Người dùng: %s\nID: %s\nSố bạn bè: %d", user.getName(), user.getId(), friendCount)
            ));

            // Kéo - thả đỉnh (Drag and Drop)
            final class DragContext { double x, y; boolean dragged; }
            DragContext drag = new DragContext();

            group.setOnMousePressed(e -> {
                drag.x = group.getLayoutX() - e.getSceneX();
                drag.y = group.getLayoutY() - e.getSceneY();
                drag.dragged = false;
                group.toFront();
            });

            group.setOnMouseDragged(e -> {
                drag.dragged = true;
                double newX = e.getSceneX() + drag.x;
                double newY = e.getSceneY() + drag.y;
                group.setLayoutX(newX);
                group.setLayoutY(newY);
                customPositions.put(uid, new Point2D(newX, newY));
                redraw();
            });

            // Nhấp chuột vào đỉnh để chọn
            group.setOnMouseClicked(e -> {
                if (!drag.dragged && onUserClicked != null) {
                    onUserClicked.accept(user);
                }
            });

            getChildren().add(group);
        }
    }

    private String edgeKey(String a, String b) {
        return a.compareTo(b) < 0 ? a + "\0" + b : b + "\0" + a;
    }
}