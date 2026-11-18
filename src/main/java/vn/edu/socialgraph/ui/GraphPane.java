package vn.edu.socialgraph.ui;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import vn.edu.socialgraph.model.SocialGraph;
import vn.edu.socialgraph.model.User;

public final class GraphPane extends Pane {
    private SocialGraph graph;
    private final Map<String, Point2D> customPositions = new HashMap<>();

    // Tập hợp ID các đỉnh và cạnh được highlight bởi thuật toán
    private final Set<String> highlightedUserIds = new LinkedHashSet<>();
    private final Set<String> highlightedEdgeKeys = new HashSet<>();

    // Người 1 (Nguồn) và Người 2 (Mục tiêu)
    private String sourceUserId = null;
    private String targetUserId = null;

    private Consumer<User> onUserClicked = null;
    private Consumer<User> onUserSourceSelected = null;
    private Consumer<User> onUserTargetSelected = null;

    public GraphPane(SocialGraph graph) {
        this.graph = graph;
        setMinHeight(300);
        setStyle("-fx-background-color: #ffffff; " +
                 "-fx-border-color: #e2e8f0; " +
                 "-fx-border-width: 1px; " +
                 "-fx-border-radius: 8px; " +
                 "-fx-background-radius: 8px;");

        widthProperty().addListener((o, a, b) -> redraw());
        heightProperty().addListener((o, a, b) -> redraw());
    }

    public void setGraph(SocialGraph graph) {
        this.graph = graph;
        this.customPositions.clear();
        this.highlightedUserIds.clear();
        this.highlightedEdgeKeys.clear();
        this.sourceUserId = null;
        this.targetUserId = null;
        redraw();
    }

    /** Thiết lập callback khi click chuột trái vào đỉnh. */
    public void setOnUserClicked(Consumer<User> onUserClicked) {
        this.onUserClicked = onUserClicked;
    }

    /** Thiết lập callback khi đỉnh được chọn làm Người 1 (Nguồn). */
    public void setOnUserSourceSelected(Consumer<User> onUserSourceSelected) {
        this.onUserSourceSelected = onUserSourceSelected;
    }

    /** Thiết lập callback khi đỉnh được chọn làm Người 2 (Mục tiêu). */
    public void setOnUserTargetSelected(Consumer<User> onUserTargetSelected) {
        this.onUserTargetSelected = onUserTargetSelected;
    }

    /** Đặt đỉnh đang được chọn làm Người 1 (Nguồn). */
    public void setSelectedUser(User user) {
        setSourceUser(user);
    }

    /** Đặt Người 1 (Nguồn). */
    public void setSourceUser(User user) {
        this.sourceUserId = (user != null) ? user.getId() : null;
        redraw();
    }

    /** Đặt Người 2 (Mục tiêu). */
    public void setTargetUser(User user) {
        this.targetUserId = (user != null) ? user.getId() : null;
        redraw();
    }

    /** Cập nhật đồng thời cả Người 1 và Người 2. */
    public void setSourceAndTarget(User source, User target) {
        this.sourceUserId = (source != null) ? source.getId() : null;
        this.targetUserId = (target != null) ? target.getId() : null;
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
        double radius = Math.max(55, Math.min(getWidth(), getHeight()) / 2.0 - 65);

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

        // 2. Vẽ các đường nối cạnh (Edges) với màu sắc êm dịu
        for (User user : all) {
            for (String friendId : graph.neighborIds(user.getId())) {
                if (user.getId().compareTo(friendId) < 0 && points.containsKey(friendId)) {
                    Point2D a = points.get(user.getId());
                    Point2D b = points.get(friendId);

                    Line edge = new Line(a.getX(), a.getY(), b.getX(), b.getY());
                    String key = edgeKey(user.getId(), friendId);

                    if (highlightedEdgeKeys.contains(key)) {
                        // Cạnh thuộc đường đi ngắn nhất: Màu đỏ san hô nhẹ nhàng, nét vừa phải
                        edge.setStroke(Color.web("#f87171"));
                        edge.setStrokeWidth(3.5);
                    } else {
                        // Cạnh thông thường: Xám nhạt thanh lịch
                        edge.setStroke(Color.web("#cbd5e1"));
                        edge.setStrokeWidth(1.6);
                    }
                    getChildren().add(edge);
                }
            }
        }

        // 3. Hiệu ứng đổ bóng nhẹ nhàng, không gây chói
        DropShadow normalShadow = new DropShadow(4, Color.web("#000000", 0.08));
        normalShadow.setOffsetY(1.5);

        DropShadow glowShadow = new DropShadow(10, Color.web("#fbbf24", 0.40));
        DropShadow sourceGlow = new DropShadow(10, Color.web("#818cf8", 0.45));
        DropShadow targetGlow = new DropShadow(10, Color.web("#34d399", 0.45));

        for (User user : all) {
            String uid = user.getId();
            Point2D pt = points.get(uid);

            boolean isSource = uid.equals(sourceUserId);
            boolean isTarget = uid.equals(targetUserId);
            boolean isHighlighted = highlightedUserIds.contains(uid);

            Circle circle = new Circle(23);
            if (isSource) {
                // Người 1 (Nguồn): Màu tím pastel dịu nhẹ
                circle.setFill(Color.web("#818cf8"));
                circle.setStroke(Color.web("#4f46e5"));
                circle.setStrokeWidth(2.8);
                circle.setEffect(sourceGlow);
            } else if (isTarget) {
                // Người 2 (Mục tiêu): Màu xanh mint/ngọc nhẹ nhàng
                circle.setFill(Color.web("#34d399"));
                circle.setStroke(Color.web("#059669"));
                circle.setStrokeWidth(2.8);
                circle.setEffect(targetGlow);
            } else if (isHighlighted) {
                // Đỉnh kết quả thuật toán: Màu vàng hổ phách dịu
                circle.setFill(Color.web("#fbbf24"));
                circle.setStroke(Color.web("#d97706"));
                circle.setStrokeWidth(2.5);
                circle.setEffect(glowShadow);
            } else {
                // Đỉnh thông thường: Màu xanh lam pastel nhã nhặn
                circle.setFill(Color.web("#60a5fa"));
                circle.setStroke(Color.web("#2563eb"));
                circle.setStrokeWidth(1.8);
                circle.setEffect(normalShadow);
            }

            // Chỉ hiển thị Tên + (ID) trong vòng tròn — không có tag phụ để tránh tràn
            Label label = new Label(user.getName() + "\n(" + user.getId() + ")");
            label.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #ffffff; -fx-alignment: center;");
            label.setPrefWidth(92);
            label.setLayoutX(-46);
            label.setLayoutY(-13);
            label.setMouseTransparent(true);

            // Badge nhỏ ở góc trên-phải để đánh dấu vai trò (không làm lệch text)
            Group group;
            if (isSource || isTarget) {
                Circle badge = new Circle(10);
                badge.setLayoutX(17);
                badge.setLayoutY(-17);
                if (isSource && isTarget) {
                    badge.setFill(Color.web("#f59e0b"));
                    badge.setStroke(Color.web("#92400e"));
                } else if (isSource) {
                    badge.setFill(Color.web("#6366f1"));
                    badge.setStroke(Color.web("#3730a3"));
                } else {
                    badge.setFill(Color.web("#10b981"));
                    badge.setStroke(Color.web("#065f46"));
                }
                badge.setStrokeWidth(1.5);
                badge.setMouseTransparent(true);

                Label badgeLabel = new Label(isSource && isTarget ? "★" : (isSource ? "1" : "2"));
                badgeLabel.setStyle("-fx-font-size: 9px; -fx-font-weight: bold; -fx-text-fill: #ffffff; -fx-alignment: center;");
                badgeLabel.setPrefWidth(20);
                badgeLabel.setLayoutX(7);
                badgeLabel.setLayoutY(-24);
                badgeLabel.setMouseTransparent(true);

                group = new Group(circle, label, badge, badgeLabel);
            } else {
                group = new Group(circle, label);
            }
            group.setLayoutX(pt.getX());
            group.setLayoutY(pt.getY());
            group.setStyle("-fx-cursor: hand;");

            // Hiển thị thông tin người dùng khi rê chuột
            int friendCount = graph.neighborIds(uid).size();
            String roleText = isSource ? " ⭐ (Người 1 - Nguồn)" : (isTarget ? " 🎯 (Người 2 - Mục tiêu)" : "");
            Tooltip tooltip = new Tooltip(String.format(
                "Người dùng: %s%s\nID: %s\nSố bạn bè: %d\n💡 Chuột trái: Chọn Người 1 (Nguồn)\n💡 Chuột phải: Đặt Người 2 (Mục tiêu)",
                user.getName(), roleText, user.getId(), friendCount
            ));
            tooltip.setStyle("-fx-font-size: 12px;");
            Tooltip.install(group, tooltip);

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

            // Menu ngữ cảnh chuột phải
            ContextMenu contextMenu = new ContextMenu();
            MenuItem mItemSource = new MenuItem("📌 Đặt làm Người 1 (Nguồn)");
            mItemSource.setOnAction(ev -> {
                setSourceUser(user);
                if (onUserSourceSelected != null) onUserSourceSelected.accept(user);
            });
            MenuItem mItemTarget = new MenuItem("🎯 Đặt làm Người 2 (Mục tiêu)");
            mItemTarget.setOnAction(ev -> {
                setTargetUser(user);
                if (onUserTargetSelected != null) onUserTargetSelected.accept(user);
            });
            MenuItem mItemClear = new MenuItem("✕ Bỏ chọn đỉnh này");
            mItemClear.setOnAction(ev -> {
                if (isSource) setSourceUser(null);
                if (isTarget) setTargetUser(null);
            });
            contextMenu.getItems().addAll(mItemSource, mItemTarget, mItemClear);

            group.setOnContextMenuRequested(ev -> {
                contextMenu.show(group, ev.getScreenX(), ev.getScreenY());
                ev.consume();
            });

            // Nhấp chuột trái vào đỉnh để chọn Người 1
            group.setOnMouseClicked(e -> {
                if (!drag.dragged && e.getButton() == MouseButton.PRIMARY) {
                    if (onUserClicked != null) {
                        onUserClicked.accept(user);
                    }
                    if (onUserSourceSelected != null) {
                        onUserSourceSelected.accept(user);
                    }
                }
            });

            getChildren().add(group);
        }
    }

    private String edgeKey(String a, String b) {
        return a.compareTo(b) < 0 ? a + "\0" + b : b + "\0" + a;
    }
}
