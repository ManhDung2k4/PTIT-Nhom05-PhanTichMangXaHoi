package vn.edu.socialgraph;

import java.io.File;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import vn.edu.socialgraph.algorithm.BfsAlgorithms;
import vn.edu.socialgraph.algorithm.DfsAndPathAlgorithms;
import vn.edu.socialgraph.io.CsvGraphRepository;
import vn.edu.socialgraph.model.SocialGraph;
import vn.edu.socialgraph.model.User;
import vn.edu.socialgraph.ui.GraphPane;


public final class SocialGraphApp extends Application {
    private SocialGraph graph = sampleGraph();
    private final CsvGraphRepository repository = new CsvGraphRepository();

    private final ObservableList<User> masterUserList = FXCollections.observableArrayList();
    private final ListView<User> userListView = new ListView<>();
    private final TextArea outputArea = new TextArea();
    private final GraphPane graphPane = new GraphPane(graph);
    private final Label statsLabel = new Label();

    // Đối tượng thao tác thuật toán: Người 1 (Nguồn) và Người 2 (Mục tiêu)
    private final TextField sourceIdInput = new TextField();
    private final Label sourceNameLabel = new Label("(Chưa chọn)");

    private final TextField targetIdInput = new TextField();
    private final Label targetNameLabel = new Label("(Chưa chọn)");

    @Override
    public void start(Stage stage) {
        // =========================================================================
        // 1. CỘT TRÁI: QUẢN LÝ NGƯỜI DÙNG (USER MANAGEMENT)
        // =========================================================================
        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Tìm theo tên hoặc ID...");
        searchField.setStyle("-fx-padding: 7px 10px; -fx-background-radius: 6px; -fx-border-color: #cbd5e1; -fx-border-radius: 6px;");

        FilteredList<User> filteredUsers = new FilteredList<>(masterUserList, p -> true);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredUsers.setPredicate(user -> {
                if (newVal == null || newVal.isBlank()) return true;
                String lower = newVal.toLowerCase().trim();
                return user.getName().toLowerCase().contains(lower) || user.getId().toLowerCase().contains(lower);
            });
        });

        userListView.setItems(filteredUsers);
        userListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    int friends = graph.hasUser(user.getId()) ? graph.neighborIds(user.getId()).size() : 0;
                    setText(String.format("%s  (%s)  •  %d bạn", user.getName(), user.getId(), friends));
                }
            }
        });

        TextField idInput = new TextField();
        idInput.setPromptText("Ví dụ: u7");

        TextField nameInput = new TextField();
        nameInput.setPromptText("Ví dụ: Lan Anh");

        Button btnAdd = createStyledButton("Thêm", "#eff6ff", "#1d4ed8", "#bfdbfe");
        btnAdd.setOnAction(e -> mutate(() -> {
            String id = idInput.getText().trim();
            String name = nameInput.getText().trim();
            graph.addUser(new User(id, name));
            idInput.clear();
            nameInput.clear();
            outputArea.setText("✅ Đã thêm người dùng mới: " + name + " (" + id + ")");
        }));

        Button btnRename = createStyledButton("Sửa tên", "#f0f9ff", "#0369a1", "#bae6fd");
        btnRename.setOnAction(e -> mutate(() -> {
            String id = idInput.getText().trim();
            if (id.isEmpty()) {
                User sel = userListView.getSelectionModel().getSelectedItem();
                if (sel != null) id = sel.getId();
            }
            if (id.isEmpty()) throw new IllegalArgumentException("Vui lòng chọn hoặc nhập ID người dùng cần đổi tên!");
            String newName = nameInput.getText().trim();
            graph.updateUser(id, newName);
            updateRoleLabels();
            outputArea.setText("✅ Đã đổi tên người dùng [" + id + "] thành: " + newName);
        }));

        Button btnRemove = createStyledButton("Xóa", "#fef2f2", "#991b1b", "#fecaca");
        btnRemove.setOnAction(e -> mutate(() -> {
            String id = idInput.getText().trim();
            if (id.isEmpty()) {
                User sel = userListView.getSelectionModel().getSelectedItem();
                if (sel != null) id = sel.getId();
            }
            if (id.isEmpty()) throw new IllegalArgumentException("Vui lòng chọn hoặc nhập ID người dùng cần xóa!");

            if (id.equals(sourceIdInput.getText().trim())) setSourceUser(null);
            if (id.equals(targetIdInput.getText().trim())) setTargetUser(null);

            graph.removeUser(id);
            idInput.clear();
            nameInput.clear();
            outputArea.setText("🗑️ Đã xóa người dùng [" + id + "] và toàn bộ các mối quan hệ bạn bè liên quan.");
        }));

        Button btnClearForm = createStyledButton("Làm mới", "#f8fafc", "#475569", "#e2e8f0");
        btnClearForm.setOnAction(e -> {
            idInput.clear();
            nameInput.clear();
            userListView.getSelectionModel().clearSelection();
        });

        GridPane userForm = new GridPane();
        userForm.setHgap(8);
        userForm.setVgap(8);
        userForm.add(new Label("Mã ID:"), 0, 0);
        userForm.add(idInput, 1, 0);
        userForm.add(new Label("Họ tên:"), 0, 1);
        userForm.add(nameInput, 1, 1);
        GridPane.setHgrow(idInput, Priority.ALWAYS);
        GridPane.setHgrow(nameInput, Priority.ALWAYS);

        HBox userBtnBox = new HBox(6, btnAdd, btnRename, btnRemove, btnClearForm);
        userBtnBox.setAlignment(Pos.CENTER_RIGHT);

        VBox userSection = createCard("Quản Lý Người Dùng", new VBox(8, searchField, userListView, userForm, userBtnBox));
        VBox.setVgrow(userListView, Priority.ALWAYS);
        userSection.setPrefWidth(320);

        // Lắng nghe chọn user trên ListView: điền nhanh vào form
        userListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                idInput.setText(newVal.getId());
                nameInput.setText(newVal.getName());
            }
        });

        // =========================================================================
        // 2. KHU VỰC THAO TÁC QUAN HỆ & THUẬT TOÁN ĐỒ THỊ
        // =========================================================================
        sourceIdInput.setPromptText("ID Người 1");
        sourceIdInput.setPrefWidth(90);
        sourceNameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #5b21b6; -fx-font-size: 12px;");

        targetIdInput.setPromptText("ID Người 2");
        targetIdInput.setPrefWidth(90);
        targetNameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #047857; -fx-font-size: 12px;");

        // Khi gõ trực tiếp vào ô ID thì cập nhật tên và hiển thị trên đồ thị
        sourceIdInput.textProperty().addListener((o, oldV, newV) -> {
            User u = graph.findUser(newV.trim()).orElse(null);
            sourceNameLabel.setText(u != null ? u.getName() : "(Chưa có)");
            graphPane.setSourceUser(u);
        });
        targetIdInput.textProperty().addListener((o, oldV, newV) -> {
            User u = graph.findUser(newV.trim()).orElse(null);
            targetNameLabel.setText(u != null ? u.getName() : "(Chưa có)");
            graphPane.setTargetUser(u);
        });

        Button btnPickSource = createStyledButton("📌 Chọn Người 1", "#ede9fe", "#5b21b6", "#ddd6fe");
        btnPickSource.setTooltip(new Tooltip("Lấy người dùng đang chọn trong danh sách gán làm Người 1"));
        btnPickSource.setOnAction(e -> {
            User sel = userListView.getSelectionModel().getSelectedItem();
            if (sel != null) {
                setSourceUser(sel);
            } else {
                outputArea.setText("⚠️ Hãy click chọn một người dùng trong danh sách bên trái trước!");
            }
        });

        Button btnSetTarget = createStyledButton("🎯 Đặt làm mục tiêu", "#ecfdf5", "#047857", "#a7f3d0");
        btnSetTarget.setTooltip(new Tooltip("Lấy người dùng đang chọn trong danh sách gán làm Người 2 (Mục tiêu)"));
        btnSetTarget.setOnAction(e -> {
            User sel = userListView.getSelectionModel().getSelectedItem();
            if (sel != null) {
                setTargetUser(sel);
            } else {
                outputArea.setText("⚠️ Hãy click chọn một người dùng trong danh sách bên trái trước!");
            }
        });

        Button btnSwap = createStyledButton("⇄ Đổi chỗ 1 & 2", "#f1f5f9", "#334155", "#cbd5e1");
        btnSwap.setOnAction(e -> {
            String sId = sourceIdInput.getText().trim();
            String tId = targetIdInput.getText().trim();
            User u1 = graph.findUser(sId).orElse(null);
            User u2 = graph.findUser(tId).orElse(null);
            setSourceUser(u2);
            setTargetUser(u1);
        });

        Button btnClearSelection = createStyledButton("✕ Xóa chọn", "#f8fafc", "#64748b", "#e2e8f0");
        btnClearSelection.setOnAction(e -> {
            setSourceUser(null);
            setTargetUser(null);
            graphPane.clearHighlights();
        });

        // Hàng điều khiển chọn Người 1 & Người 2 (Đỉnh xuất phát & Đỉnh đích)
        HBox person1Box = new HBox(8, new Label("🚩 Đỉnh xuất phát (Nguồn):"), sourceIdInput, sourceNameLabel, btnPickSource);
        person1Box.setAlignment(Pos.CENTER_LEFT);

        HBox person2Box = new HBox(8, new Label("🎯 Đỉnh đích (Mục tiêu):  "), targetIdInput, targetNameLabel, btnSetTarget, btnSwap, btnClearSelection);
        person2Box.setAlignment(Pos.CENTER_LEFT);

        VBox selectorBox = new VBox(6, person1Box, person2Box);
        selectorBox.setPadding(new Insets(8));
        selectorBox.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 6px; -fx-border-color: #e2e8f0; -fx-border-radius: 6px;");

        // Các nút thao tác giữa 2 đỉnh (Xuất phát ➔ Đích)
        Button btnConnect = createStyledButton("➕ Kết bạn", "#f0fdf4", "#166534", "#bbf7d0");
        btnConnect.setOnAction(e -> mutate(() -> {
            String u1 = requireSourceId();
            String u2 = requireTargetId();
            graph.connect(u1, u2);
            outputArea.setText("🤝 Đã thiết lập quan hệ bạn bè giữa [" + u1 + "] và [" + u2 + "] thành công!");
        }));

        Button btnDisconnect = createStyledButton("➖ Hủy kết bạn", "#fef2f2", "#991b1b", "#fecaca");
        btnDisconnect.setOnAction(e -> mutate(() -> {
            String u1 = requireSourceId();
            String u2 = requireTargetId();
            graph.disconnect(u1, u2);
            outputArea.setText("💔 Đã hủy kết bạn giữa [" + u1 + "] và [" + u2 + "].");
        }));

        Button btnCommon = createStyledButton("👥 Bạn chung", "#f0fdfa", "#115e59", "#99f6e4");
        btnCommon.setOnAction(e -> show("Danh sách bạn chung (Mutual Friends)", () -> {
            String u1 = requireSourceId();
            String u2 = requireTargetId();
            List<User> list = graph.commonFriends(u1, u2);
            graphPane.highlightUsers(list);
            return list;
        }));

        Button btnBfsTarget = createStyledButton("🔍 Duyệt BFS (Nguồn ➔ Đích)", "#f0f9ff", "#0369a1", "#bae6fd");
        btnBfsTarget.setOnAction(e -> act(() -> {
            String u1 = requireSourceId();
            String u2 = requireTargetId();
            long startTime = System.nanoTime();
            List<User> visited = BfsAlgorithms.bfs(graph, u1, u2);
            List<User> path = DfsAndPathAlgorithms.shortestPath(graph, u1, u2);
            long endTime = System.nanoTime();
            double durationMs = (endTime - startTime) / 1_000_000.0;

            graphPane.highlightTraversalAndPath(visited, path);

            StringBuilder sb = new StringBuilder();
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            sb.append("📌 THUẬT TOÁN DUYỆT THEO CHIỀU RỘNG (BFS) TỪ NGUỒN ĐẾN ĐÍCH\n");
            sb.append("────────────────────────────────────────────────────────────────────────────────\n");
            sb.append(String.format("⏱ Thời gian chạy: %.3f ms\n", durationMs));
            sb.append(String.format("🚩 Đỉnh xuất phát (Nguồn): %s (%s)   ⟶   🎯 Đỉnh đích: %s (%s)\n",
                    graph.requireUser(u1).getName(), u1, graph.requireUser(u2).getName(), u2));
            sb.append("────────────────────────────────────────────────────────────────────────────────\n");

            sb.append(String.format("👉 Thứ tự các đỉnh duyệt BFS (cho đến khi gặp đích - tổng %d đỉnh):\n", visited.size()));
            for (int i = 0; i < visited.size(); i++) {
                User u = visited.get(i);
                int friends = graph.neighborIds(u.getId()).size();
                String mark = u.getId().equals(u2) ? "  <-- [ĐÍCH ĐẾN]" : (u.getId().equals(u1) ? "  <-- [XUẤT PHÁT]" : "");
                sb.append(String.format("   %2d. %-18s [ID: %-5s]   (Số bạn bè: %d)%s\n", (i + 1), u.getName(), u.getId(), friends, mark));
            }
            sb.append("\n👉 Chuỗi thứ tự duyệt:\n   ");
            sb.append(visited.stream().map(u -> u.getName() + " (" + u.getId() + ")").collect(Collectors.joining("  ⟶  ")));
            sb.append("\n\n");

            if (path.isEmpty()) {
                sb.append("⚠️ Không tìm thấy đường đi từ đỉnh xuất phát tới đỉnh đích (2 đỉnh không liên thông)!\n");
            } else {
                sb.append(String.format("👉 Đường đi tìm được từ Nguồn đến Đích (Đường ngắn nhất bằng BFS - %d cạnh):\n   ", path.size() - 1));
                sb.append(path.stream().map(u -> u.getName() + " (" + u.getId() + ")").collect(Collectors.joining("  ⟶  ")));
                sb.append("\n");
            }
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

            outputArea.setText(sb.toString());
        }));

        Button btnDfsTarget = createStyledButton("🔍 Duyệt DFS (Nguồn ➔ Đích)", "#faf5ff", "#6b21a8", "#e9d5ff");
        btnDfsTarget.setOnAction(e -> act(() -> {
            String u1 = requireSourceId();
            String u2 = requireTargetId();
            long startTime = System.nanoTime();
            List<User> visited = DfsAndPathAlgorithms.dfs(graph, u1, u2);
            List<User> path = DfsAndPathAlgorithms.dfsPath(graph, u1, u2);
            long endTime = System.nanoTime();
            double durationMs = (endTime - startTime) / 1_000_000.0;

            graphPane.highlightTraversalAndPath(visited, path);

            StringBuilder sb = new StringBuilder();
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            sb.append("📌 THUẬT TOÁN DUYỆT THEO CHIỀU SÂU (DFS) TỪ NGUỒN ĐẾN ĐÍCH\n");
            sb.append("────────────────────────────────────────────────────────────────────────────────\n");
            sb.append(String.format("⏱ Thời gian chạy: %.3f ms\n", durationMs));
            sb.append(String.format("🚩 Đỉnh xuất phát (Nguồn): %s (%s)   ⟶   🎯 Đỉnh đích: %s (%s)\n",
                    graph.requireUser(u1).getName(), u1, graph.requireUser(u2).getName(), u2));
            sb.append("────────────────────────────────────────────────────────────────────────────────\n");

            sb.append(String.format("👉 Thứ tự các đỉnh duyệt DFS (cho đến khi gặp đích - tổng %d đỉnh):\n", visited.size()));
            for (int i = 0; i < visited.size(); i++) {
                User u = visited.get(i);
                int friends = graph.neighborIds(u.getId()).size();
                String mark = u.getId().equals(u2) ? "  <-- [ĐÍCH ĐẾN]" : (u.getId().equals(u1) ? "  <-- [XUẤT PHÁT]" : "");
                sb.append(String.format("   %2d. %-18s [ID: %-5s]   (Số bạn bè: %d)%s\n", (i + 1), u.getName(), u.getId(), friends, mark));
            }
            sb.append("\n👉 Chuỗi thứ tự duyệt:\n   ");
            sb.append(visited.stream().map(u -> u.getName() + " (" + u.getId() + ")").collect(Collectors.joining("  ⟶  ")));
            sb.append("\n\n");

            if (path.isEmpty()) {
                sb.append("⚠️ Không tìm thấy đường đi từ đỉnh xuất phát tới đỉnh đích (2 đỉnh không liên thông)!\n");
            } else {
                sb.append(String.format("👉 Đường đi tìm được từ Nguồn đến Đích (Đường đi theo DFS - %d cạnh):\n   ", path.size() - 1));
                sb.append(path.stream().map(u -> u.getName() + " (" + u.getId() + ")").collect(Collectors.joining("  ⟶  ")));
                sb.append("\n");
            }
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

            outputArea.setText(sb.toString());
        }));

        Button btnPath = createStyledButton("🚩 Đường ngắn nhất", "#fff7ed", "#9a3412", "#fed7aa");
        btnPath.setOnAction(e -> show("Đường đi ngắn nhất giữa 2 người (BFS)", () -> {
            String u1 = requireSourceId();
            String u2 = requireTargetId();
            List<User> path = DfsAndPathAlgorithms.shortestPath(graph, u1, u2);
            graphPane.highlightPath(path);
            return path;
        }));

        FlowPane twoPersonActionBox = new FlowPane(8, 8, btnConnect, btnDisconnect, btnCommon, btnBfsTarget, btnDfsTarget, btnPath);

        // Các nút thuật toán trên Đỉnh xuất phát (Người 1)
        Button btnFriends = createStyledButton("📋 Xem bạn bè", "#eff6ff", "#1e40af", "#bfdbfe");
        btnFriends.setOnAction(e -> show("Danh sách bạn bè của Người 1", () -> {
            String id = requireSourceId();
            List<User> list = graph.friendsOf(id);
            graphPane.highlightUsers(list);
            return list;
        }));

        Button btnSuggestBfs = createStyledButton("💡 Gợi ý (BFS)", "#eef2ff", "#3730a3", "#c7d2fe");
        btnSuggestBfs.setOnAction(e -> show("Gợi ý bạn bè mới cho Người 1 (Thuật toán BFS - FOAF)", () -> {
            String id = requireSourceId();
            List<User> list = BfsAlgorithms.suggestFriends(graph, id);
            graphPane.highlightUsers(list);
            return list;
        }));

        Button btnSuggestDfs = createStyledButton("💡 Gợi ý (DFS)", "#faf5ff", "#6b21a8", "#e9d5ff");
        btnSuggestDfs.setOnAction(e -> show("Gợi ý bạn bè mới cho Người 1 (Thuật toán DFS)", () -> {
            String id = requireSourceId();
            List<User> list = DfsAndPathAlgorithms.suggestFriends(graph, id);
            graphPane.highlightUsers(list);
            return list;
        }));

        Button btnBfsAll = createStyledButton("🌐 Duyệt toàn bộ BFS", "#f0f9ff", "#075985", "#bae6fd");
        btnBfsAll.setOnAction(e -> show("Thứ tự duyệt toàn bộ thành phần liên thông theo BFS từ Người 1", () -> {
            String id = requireSourceId();
            List<User> list = BfsAlgorithms.bfs(graph, id);
            graphPane.highlightUsers(list);
            return list;
        }));

        Button btnDfsAll = createStyledButton("🌐 Duyệt toàn bộ DFS", "#ecfeff", "#155e75", "#a5f3fc");
        btnDfsAll.setOnAction(e -> show("Thứ tự duyệt toàn bộ thành phần liên thông theo DFS từ Người 1", () -> {
            String id = requireSourceId();
            List<User> list = DfsAndPathAlgorithms.dfs(graph, id);
            graphPane.highlightUsers(list);
            return list;
        }));

        Button btnClearHighlight = createStyledButton("✨ Bỏ Highlight", "#f8fafc", "#475569", "#cbd5e1");
        btnClearHighlight.setOnAction(e -> graphPane.clearHighlights());

        FlowPane onePersonActionBox = new FlowPane(8, 8, btnFriends, btnSuggestBfs, btnSuggestDfs, btnBfsAll, btnDfsAll, btnClearHighlight);

        VBox actionSection = createCard("Thao Tác Quan Hệ & Thuật Toán Đồ Thị",
                new VBox(8, selectorBox, twoPersonActionBox, new Separator(), onePersonActionBox));

        // =========================================================================
        // 3. KHU VỰC SƠ ĐỒ ĐỒ THỊ (GRAPH VISUALIZATION)
        // =========================================================================
        graphPane.setOnUserSourceSelected(u -> {
            setSourceUser(u);
            userListView.getSelectionModel().select(u);
            userListView.scrollTo(u);
        });

        graphPane.setOnUserTargetSelected(this::setTargetUser);

        Button btnResetLayout = createStyledButton("🔄 Căn lại sơ đồ", "#f1f5f9", "#334155", "#cbd5e1");
        btnResetLayout.setOnAction(e -> graphPane.resetPositions());

        Button btnSample = createStyledButton("📥 Nạp mẫu", "#f0f9ff", "#0369a1", "#bae6fd");
        btnSample.setOnAction(e -> {
            graph = sampleGraph();
            setSourceUser(graph.findUser("u1").orElse(null));
            setTargetUser(graph.findUser("u6").orElse(null));
            refresh();
            outputArea.setText("✅ Đã nạp dữ liệu đồ thị mẫu thành công.");
        });

        Button btnClearAll = createStyledButton("🗑️ Xóa tất cả", "#fef2f2", "#991b1b", "#fecaca");
        btnClearAll.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Bạn có chắc chắn muốn xóa toàn bộ đồ thị không?", ButtonType.YES, ButtonType.NO);
            alert.setTitle("Xác nhận xóa sạch");
            alert.showAndWait().ifPresent(res -> {
                if (res == ButtonType.YES) {
                    graph.clear();
                    setSourceUser(null);
                    setTargetUser(null);
                    refresh();
                    outputArea.setText("⚠️ Đã xóa sạch toàn bộ mạng xã hội.");
                }
            });
        });

        statsLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155; -fx-font-size: 12px;");

        HBox graphHeader = new HBox(10, statsLabel, new Region(), btnResetLayout, btnSample, btnClearAll);
        HBox.setHgrow(graphHeader.getChildren().get(1), Priority.ALWAYS);
        graphHeader.setAlignment(Pos.CENTER_LEFT);

        VBox graphSection = createCard("Sơ Đồ Mạng Xã Hội (Chuột trái: Chọn Người 1 | Chuột phải: Đặt Người 2 | Kéo thả: Đổi vị trí)",
                new VBox(8, graphHeader, graphPane));
        VBox.setVgrow(graphPane, Priority.ALWAYS);

        // =========================================================================
        // 4. BẢNG NHẬT KÝ & LƯU/MỞ FILE CSV (RỘNG RÃI, THOÁNG ĐÃNG)
        // =========================================================================
        outputArea.setEditable(false);
        outputArea.setWrapText(true);
        outputArea.setPrefRowCount(14);
        outputArea.setStyle("-fx-font-family: 'Consolas', 'Courier New', monospace; -fx-font-size: 13px; -fx-line-spacing: 2px;");
        VBox.setVgrow(outputArea, Priority.ALWAYS);

        Button btnSave = createStyledButton("💾 Lưu File CSV", "#f8fafc", "#334155", "#cbd5e1");
        btnSave.setOnAction(e -> save(stage));

        Button btnLoad = createStyledButton("📂 Mở File CSV", "#f8fafc", "#334155", "#cbd5e1");
        btnLoad.setOnAction(e -> load(stage));

        Button btnClearLog = createStyledButton("🧹 Xóa nhật ký", "#f8fafc", "#64748b", "#e2e8f0");
        btnClearLog.setOnAction(e -> outputArea.clear());

        HBox fileBox = new HBox(8, btnSave, btnLoad, btnClearLog);
        fileBox.setAlignment(Pos.CENTER_RIGHT);

        VBox outputSection = createCard("Kết Quả Thuật Toán & Nhật Ký Hệ Thống", new VBox(8, outputArea, fileBox));
        outputSection.setMinHeight(240);
        VBox.setVgrow(outputSection, Priority.ALWAYS);

        // SplitPane giữa Sơ đồ đồ thị và Bảng kết quả (Người dùng có thể kéo thả kích thước tùy ý)
        SplitPane centerSplit = new SplitPane(graphSection, outputSection);
        centerSplit.setOrientation(Orientation.VERTICAL);
        centerSplit.setDividerPositions(0.50);
        centerSplit.setStyle("-fx-background-color: transparent; -fx-box-border: transparent;");
        VBox.setVgrow(centerSplit, Priority.ALWAYS);

        // =========================================================================
        // TỔNG HỢP GIAO DIỆN CHÍNH
        // =========================================================================
        VBox rightLayout = new VBox(10, actionSection, centerSplit);
        VBox.setVgrow(centerSplit, Priority.ALWAYS);

        HBox rootLayout = new HBox(12, userSection, rightLayout);
        HBox.setHgrow(rightLayout, Priority.ALWAYS);
        rootLayout.setPadding(new Insets(12));
        rootLayout.setStyle("-fx-background-color: #f8fafc;");

        // Đặt mặc định Người 1 và Người 2 mẫu khi khởi động
        setSourceUser(graph.findUser("u1").orElse(null));
        setTargetUser(graph.findUser("u6").orElse(null));

        refresh();

        Scene scene = new Scene(rootLayout, 1240, 920);
        scene.getStylesheets().add("data:text/css," + encodeCss());

        stage.setTitle("Phân Tích Mạng Xã Hội Bằng Đồ Thị - Social Graph Analyzer");
        stage.setScene(scene);
        stage.show();
    }

    // =========================================================================
    // HELPER METHODS: QUẢN LÝ NGƯỜI 1 & NGƯỜI 2
    // =========================================================================
    private void setSourceUser(User user) {
        if (user != null) {
            sourceIdInput.setText(user.getId());
            sourceNameLabel.setText(user.getName());
        } else {
            sourceIdInput.clear();
            sourceNameLabel.setText("(Chưa chọn)");
        }
        graphPane.setSourceUser(user);
    }

    private void setTargetUser(User user) {
        if (user != null) {
            targetIdInput.setText(user.getId());
            targetNameLabel.setText(user.getName());
        } else {
            targetIdInput.clear();
            targetNameLabel.setText("(Chưa chọn)");
        }
        graphPane.setTargetUser(user);
    }

    private void updateRoleLabels() {
        String sId = sourceIdInput.getText().trim();
        sourceNameLabel.setText(graph.findUser(sId).map(User::getName).orElse("(Chưa chọn)"));

        String tId = targetIdInput.getText().trim();
        targetNameLabel.setText(graph.findUser(tId).map(User::getName).orElse("(Chưa chọn)"));
    }

    private String requireSourceId() {
        String id = sourceIdInput.getText().trim();
        if (id.isEmpty()) {
            User sel = userListView.getSelectionModel().getSelectedItem();
            if (sel != null) {
                setSourceUser(sel);
                return sel.getId();
            }
            throw new IllegalArgumentException("Vui lòng chọn Người 1 (Nguồn) từ danh sách bên trái hoặc click chuột trái vào đỉnh trên sơ đồ!");
        }
        if (!graph.hasUser(id)) {
            throw new IllegalArgumentException("Không tìm thấy Người 1 có mã ID: " + id);
        }
        return id;
    }

    private String requireTargetId() {
        String id = targetIdInput.getText().trim();
        if (id.isEmpty()) {
            User sel = userListView.getSelectionModel().getSelectedItem();
            if (sel != null && !sel.getId().equals(sourceIdInput.getText().trim())) {
                setTargetUser(sel);
                return sel.getId();
            }
            throw new IllegalArgumentException("Vui lòng chọn Người 2 (Mục tiêu) bằng cách: chọn user trong danh sách rồi bấm 'Đặt làm mục tiêu', hoặc click chuột phải vào đỉnh trên sơ đồ!");
        }
        if (!graph.hasUser(id)) {
            throw new IllegalArgumentException("Không tìm thấy Người 2 (Mục tiêu) có mã ID: " + id);
        }
        return id;
    }

    // =========================================================================
    // CÁC THAO TÁC DỮ LIỆU & THỰC THI THUẬT TOÁN (ĐỊNH DẠNG KẾT QUẢ ĐẸP MẮT)
    // =========================================================================
    private void show(String title, Supplier<List<User>> algorithmSupplier) {
        act(() -> {
            long startTime = System.nanoTime();
            List<User> values = algorithmSupplier.get();
            long endTime = System.nanoTime();
            double durationMs = (endTime - startTime) / 1_000_000.0;

            StringBuilder sb = new StringBuilder();
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            sb.append(String.format("📌 %s\n", title));
            sb.append("────────────────────────────────────────────────────────────────────────────────\n");
            sb.append(String.format("⏱ Thời gian thực thi: %.3f ms   |   📊 Số lượng phần tử kết quả: %d\n", durationMs, values.size()));
            sb.append("────────────────────────────────────────────────────────────────────────────────\n");

            if (values.isEmpty()) {
                sb.append("  (Không tìm thấy kết quả phù hợp hoặc không tồn tại đường nối giữa các đỉnh)\n");
            } else {
                sb.append("👉 Danh sách kết quả chi tiết:\n");
                for (int i = 0; i < values.size(); i++) {
                    User u = values.get(i);
                    int friends = graph.hasUser(u.getId()) ? graph.neighborIds(u.getId()).size() : 0;
                    sb.append(String.format("   %2d. %-18s [ID: %-5s]   (Số bạn bè: %d)\n", (i + 1), u.getName(), u.getId(), friends));
                }
                sb.append("\n👉 Chuỗi liên kết trực quan:\n   ");
                sb.append(values.stream().map(u -> u.getName() + " (" + u.getId() + ")").collect(Collectors.joining("  ⟶  ")));
                sb.append("\n");
            }
            sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

            outputArea.setText(sb.toString());
        });
    }

    private void refresh() {
        masterUserList.setAll(graph.getUsers());
        graphPane.setGraph(graph);
        updateRoleLabels();
        statsLabel.setText(String.format("📊 Thống kê đồ thị: |V| = %d người dùng, |E| = %d quan hệ bạn bè",
                graph.getUserCount(), graph.getEdgeCount()));
    }

    private void mutate(Runnable action) {
        act(() -> {
            action.run();
            refresh();
        });
    }

    private void act(Runnable action) {
        try {
            action.run();
        } catch (Exception ex) {
            outputArea.setText("❌ Lỗi thao tác: " + ex.getMessage());
        }
    }

    private void save(Stage stage) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Lưu Mạng Xã Hội Vào File CSV");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files (*.csv)", "*.csv"));
        chooser.setInitialFileName("social-network.csv");
        File file = chooser.showSaveDialog(stage);
        if (file != null) {
            act(() -> {
                try {
                    repository.save(graph, file.toPath());
                    outputArea.setText("✅ Đã lưu dữ liệu mạng xã hội thành công vào tập tin:\n" + file.getAbsolutePath());
                } catch (Exception ex) {
                    throw new RuntimeException("Không thể lưu file: " + ex.getMessage(), ex);
                }
            });
        }
    }

    private void load(Stage stage) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Mở Dữ Liệu Mạng Xã Hội Từ File CSV");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files (*.csv)", "*.csv"));
        File file = chooser.showOpenDialog(stage);
        if (file != null) {
            act(() -> {
                try {
                    graph = repository.load(file.toPath());
                    setSourceUser(graph.getUsers().stream().findFirst().orElse(null));
                    setTargetUser(null);
                    refresh();
                    outputArea.setText("✅ Đã tải dữ liệu thành công từ tập tin:\n" + file.getAbsolutePath() +
                            String.format("\nĐã nạp: %d người dùng, %d quan hệ bạn bè.", graph.getUserCount(), graph.getEdgeCount()));
                } catch (Exception ex) {
                    throw new RuntimeException("Không thể đọc file: " + ex.getMessage(), ex);
                }
            });
        }
    }

    private static SocialGraph sampleGraph() {
        SocialGraph graph = new SocialGraph();
        graph.addUser(new User("u1", "An"));
        graph.addUser(new User("u2", "Bình"));
        graph.addUser(new User("u3", "Chi"));
        graph.addUser(new User("u4", "Dũng"));
        graph.addUser(new User("u5", "Hoa"));
        graph.addUser(new User("u6", "Huy"));

        graph.connect("u1", "u2");
        graph.connect("u1", "u3");
        graph.connect("u2", "u3");
        graph.connect("u2", "u4");
        graph.connect("u3", "u5");
        graph.connect("u4", "u6");
        graph.connect("u5", "u6");
        return graph;
    }

    // =========================================================================
    // HELPER UI COMPONENTS & CSS STYLING
    // =========================================================================
    private VBox createCard(String title, Region content) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        VBox card = new VBox(8, titleLabel, content);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: #ffffff; " +
                "-fx-border-color: #e2e8f0; " +
                "-fx-border-width: 1px; " +
                "-fx-border-radius: 8px; " +
                "-fx-background-radius: 8px;");
        return card;
    }

    private Button createStyledButton(String text, String bgColor, String textColor, String borderColor) {
        Button btn = new Button(text);
        btn.setStyle(String.format(
                "-fx-background-color: %s; -fx-text-fill: %s; -fx-border-color: %s; -fx-border-width: 1px; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 6px 12px; -fx-font-size: 12px;",
                bgColor, textColor, borderColor));
        return btn;
    }

    private String encodeCss() {
        String css =
                ".button:hover { -fx-opacity: 0.85; } " +
                ".text-field { -fx-padding: 6px; -fx-background-radius: 6px; -fx-border-color: #cbd5e1; -fx-border-radius: 6px; } " +
                ".list-view { -fx-background-radius: 6px; -fx-border-color: #cbd5e1; -fx-border-radius: 6px; } " +
                ".list-cell:filled:selected { -fx-background-color: #ede9fe; -fx-text-fill: #3b0764; -fx-font-weight: bold; } " +
                ".list-cell:filled:hover { -fx-background-color: #f8fafc; } " +
                ".text-area { -fx-background-radius: 6px; -fx-border-color: #cbd5e1; -fx-border-radius: 6px; } " +
                ".split-pane *.split-pane-divider { -fx-padding: 3px; -fx-background-color: #e2e8f0; }";
        return css.replace(" ", "%20").replace("#", "%23").replace("\n", "");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
