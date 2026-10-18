package vn.edu.socialgraph;

import java.io.File;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
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

/**
 * Ứng dụng mô phỏng và phân tích mạng xã hội bằng đồ thị vô hướng trên nền JavaFX.
 * <p>
 * Tích hợp đầy đủ các thuật toán cốt lõi: BFS, DFS, gợi ý bạn bè (FOAF), tìm đường đi ngắn nhất,
 * đọc/ghi file CSV và trực quan hóa tương tác 2 chiều giữa danh sách và sơ đồ đồ thị.
 */
public final class SocialGraphApp extends Application {
    private SocialGraph graph = sampleGraph();
    private final CsvGraphRepository repository = new CsvGraphRepository();
    private final ListView<User> users = new ListView<>();
    private final TextArea output = new TextArea();
    private final GraphPane graphPane = new GraphPane(graph);
    private final Label statsLabel = new Label();

    @Override
    public void start(Stage stage) {
        // --- 1. Form quản lý người dùng ---
        TextField idInput = new TextField(); 
        idInput.setPromptText("Ví dụ: u1");
        
        TextField nameInput = new TextField(); 
        nameInput.setPromptText("Ví dụ: Nguyễn Văn A");
        
        TextField targetIdInput = new TextField(); 
        targetIdInput.setPromptText("ID người thứ 2 (Ví dụ: u3)");

        Button btnAdd = createStyledButton("Thêm", "#2563eb", "#ffffff");
        btnAdd.setOnAction(e -> mutate(() -> {
            graph.addUser(new User(idInput.getText().trim(), nameInput.getText().trim()));
            idInput.clear();
            nameInput.clear();
        }));

        Button btnRename = createStyledButton("Sửa tên", "#0284c7", "#ffffff");
        btnRename.setOnAction(e -> mutate(() -> {
            graph.updateUser(selectedId(), nameInput.getText().trim());
        }));

        Button btnRemove = createStyledButton("Xóa", "#ef4444", "#ffffff");
        btnRemove.setOnAction(e -> mutate(() -> {
            String id = selectedId();
            graph.removeUser(id);
            idInput.clear();
            nameInput.clear();
        }));

        GridPane userForm = new GridPane();
        userForm.setHgap(8); 
        userForm.setVgap(8);
        userForm.add(new Label("ID:"), 0, 0); 
        userForm.add(idInput, 1, 0);
        userForm.add(new Label("Tên:"), 0, 1); 
        userForm.add(nameInput, 1, 1);
        GridPane.setHgrow(idInput, Priority.ALWAYS);
        GridPane.setHgrow(nameInput, Priority.ALWAYS);

        HBox userBtnBox = new HBox(8, btnAdd, btnRename, btnRemove);
        userBtnBox.setAlignment(Pos.CENTER_RIGHT);

        VBox userSection = createCard("Quản Lý Người Dùng", new VBox(10, users, userForm, userBtnBox));
        VBox.setVgrow(users, Priority.ALWAYS);

        // Lắng nghe sự kiện chọn user trên ListView để điền nhanh vào form & highlight node
        users.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                idInput.setText(newVal.getId());
                nameInput.setText(newVal.getName());
                graphPane.setSelectedUser(newVal);
            }
        });

        // Tương tác 2 chiều: click vào đỉnh trên GraphPane tự chọn user trong ListView
        graphPane.setOnUserClicked(clickedUser -> {
            users.getSelectionModel().select(clickedUser);
            users.scrollTo(clickedUser);
        });

        // --- 2. Thao tác cạnh & Thuật toán ---
        Button btnSetTarget = createStyledButton("Đặt làm mục tiêu", "#475569", "#ffffff");
        btnSetTarget.setOnAction(e -> {
            User sel = users.getSelectionModel().getSelectedItem();
            if (sel != null) targetIdInput.setText(sel.getId());
        });

        Button btnConnect = createStyledButton("Kết bạn", "#16a34a", "#ffffff");
        btnConnect.setOnAction(e -> mutate(() -> graph.connect(selectedId(), targetIdInput.getText().trim())));

        Button btnDisconnect = createStyledButton("Hủy kết bạn", "#dc2626", "#ffffff");
        btnDisconnect.setOnAction(e -> mutate(() -> graph.disconnect(selectedId(), targetIdInput.getText().trim())));

        HBox edgeBox = new HBox(8, new Label("Người 2:"), targetIdInput, btnSetTarget, btnConnect, btnDisconnect);
        edgeBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(targetIdInput, Priority.ALWAYS);

        // Nút bấm thuật toán
        Button btnFriends = new Button("Xem bạn bè");
        btnFriends.setOnAction(e -> show("Danh sách bạn bè", () -> {
            List<User> list = graph.friendsOf(selectedId());
            graphPane.highlightUsers(list);
            return list;
        }));

        Button btnCommon = new Button("Bạn chung");
        btnCommon.setOnAction(e -> show("Danh sách bạn chung", () -> {
            List<User> list = graph.commonFriends(selectedId(), targetIdInput.getText().trim());
            graphPane.highlightUsers(list);
            return list;
        }));

        Button btnSuggestBfs = new Button("Gợi ý (BFS)");
        btnSuggestBfs.setOnAction(e -> show("Gợi ý bạn bè (Thuật toán BFS)", () -> {
            List<User> list = BfsAlgorithms.suggestFriends(graph, selectedId());
            graphPane.highlightUsers(list);
            return list;
        }));

        Button btnSuggestDfs = new Button("Gợi ý (DFS)");
        btnSuggestDfs.setOnAction(e -> show("Gợi ý bạn bè (Thuật toán DFS)", () -> {
            List<User> list = DfsAndPathAlgorithms.suggestFriends(graph, selectedId());
            graphPane.highlightUsers(list);
            return list;
        }));

        Button btnBfs = new Button("Duyệt BFS");
        btnBfs.setOnAction(e -> show("Thứ tự duyệt BFS", () -> {
            List<User> list = BfsAlgorithms.bfs(graph, selectedId());
            graphPane.highlightUsers(list);
            return list;
        }));

        Button btnDfs = new Button("Duyệt DFS");
        btnDfs.setOnAction(e -> show("Thứ tự duyệt DFS", () -> {
            List<User> list = DfsAndPathAlgorithms.dfs(graph, selectedId());
            graphPane.highlightUsers(list);
            return list;
        }));

        Button btnPath = createStyledButton("Đường ngắn nhất", "#ea580c", "#ffffff");
        btnPath.setOnAction(e -> show("Đường đi ngắn nhất (BFS)", () -> {
            List<User> path = DfsAndPathAlgorithms.shortestPath(graph, selectedId(), targetIdInput.getText().trim());
            graphPane.highlightPath(path);
            return path;
        }));

        Button btnClearHighlight = new Button("Bỏ Highlight");
        btnClearHighlight.setOnAction(e -> graphPane.clearHighlights());

        FlowPane algoBox = new FlowPane(8, 8, 
            btnFriends, btnCommon, btnSuggestBfs, btnSuggestDfs, btnBfs, btnDfs, btnPath, btnClearHighlight);
        
        VBox actionSection = createCard("Thao Tác Quan Hệ & Thuật Toán Đồ Thị", new VBox(10, edgeBox, new Separator(), algoBox));

        // --- 3. Đồ thị & Thao tác sơ đồ ---
        Button btnResetLayout = createStyledButton("Căn lại sơ đồ", "#64748b", "#ffffff");
        btnResetLayout.setOnAction(e -> graphPane.resetPositions());

        Button btnSample = createStyledButton("Nạp đồ thị mẫu", "#0284c7", "#ffffff");
        btnSample.setOnAction(e -> {
            graph = sampleGraph();
            refresh();
            output.setText("✅ Đã nạp đồ thị mẫu thành công.");
        });

        Button btnClearAll = createStyledButton("Xóa tất cả", "#991b1b", "#ffffff");
        btnClearAll.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Bạn có chắc chắn muốn xóa toàn bộ đồ thị không?", ButtonType.YES, ButtonType.NO);
            alert.setTitle("Xác nhận xóa");
            alert.showAndWait().ifPresent(res -> {
                if (res == ButtonType.YES) {
                    graph.clear();
                    refresh();
                    output.setText("⚠️ Đã xóa sạch toàn bộ người dùng và liên kết trong mạng xã hội.");
                }
            });
        });

        statsLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #334155; -fx-font-size: 12px;");

        HBox graphHeaderTools = new HBox(10, statsLabel, new Region(), btnResetLayout, btnSample, btnClearAll);
        HBox.setHgrow(graphHeaderTools.getChildren().get(1), Priority.ALWAYS);
        graphHeaderTools.setAlignment(Pos.CENTER_LEFT);

        VBox graphSection = createCard("Sơ Đồ Mạng Xã Hội (Kéo thả đỉnh để đổi vị trí, click đỉnh để chọn)", 
            new VBox(8, graphHeaderTools, graphPane));
        VBox.setVgrow(graphPane, Priority.ALWAYS);

        // --- 4. Bảng Output log & Lưu/Mở file ---
        output.setEditable(false);
        output.setWrapText(true);
        output.setStyle("-fx-font-family: 'Consolas', 'Courier New', monospace; -fx-font-size: 12px;");
        
        Button btnSave = createStyledButton("Lưu File CSV", "#334155", "#ffffff");
        btnSave.setOnAction(e -> save(stage));

        Button btnLoad = createStyledButton("Mở File CSV", "#334155", "#ffffff");
        btnLoad.setOnAction(e -> load(stage));

        HBox fileBox = new HBox(8, btnSave, btnLoad);
        fileBox.setAlignment(Pos.CENTER_RIGHT);

        VBox outputSection = createCard("Kết Quả Thuật Toán & Nhật Ký", new VBox(8, output, fileBox));
        VBox.setVgrow(output, Priority.ALWAYS);

        // --- Layout Tổng ---
        userSection.setPrefWidth(330);

        VBox rightLayout = new VBox(12, actionSection, graphSection, outputSection);
        VBox.setVgrow(graphSection, Priority.ALWAYS);
        VBox.setVgrow(outputSection, Priority.SOMETIMES);

        HBox mainLayout = new HBox(14, userSection, rightLayout);
        HBox.setHgrow(rightLayout, Priority.ALWAYS);
        mainLayout.setPadding(new Insets(14));
        mainLayout.setStyle("-fx-background-color: #f8fafc;");

        refresh();

        Scene scene = new Scene(mainLayout, 1150, 850);
        scene.getStylesheets().add("data:text/css," + encodeCss());

        stage.setTitle("Phân Tích Mạng Xã Hội Bằng Đồ Thị - Social Graph Analyzer");
        stage.setScene(scene);
        stage.show();
    }

    private String selectedId() {
        User selected = users.getSelectionModel().getSelectedItem();
        if (selected == null) {
            throw new IllegalArgumentException("Vui lòng chọn một người dùng từ danh sách bên trái hoặc click vào đỉnh trên sơ đồ!");
        }
        return selected.getId();
    }

    private void show(String title, Supplier<List<User>> algorithmSupplier) {
        act(() -> {
            long startTime = System.nanoTime();
            List<User> values = algorithmSupplier.get();
            long endTime = System.nanoTime();
            double durationMs = (endTime - startTime) / 1_000_000.0;

            String resultText = values.isEmpty() ? "(Không tìm thấy kết quả hoặc không có liên kết)" : 
                values.stream().map(User::toString).collect(Collectors.joining("  →  "));

            output.setText(String.format("=== %s ===\n⏱ Thời gian chạy thuật toán: %.3f ms\n📌 Số lượng kết quả: %d\n👉 Chi tiết: %s", 
                title, durationMs, values.size(), resultText));
        });
    }

    private void refresh() {
        users.setItems(FXCollections.observableArrayList(graph.getUsers()));
        graphPane.setGraph(graph);
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
            output.setText("❌ Lỗi thao tác: " + ex.getMessage()); 
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
                    output.setText("✅ Đã lưu dữ liệu mạng xã hội thành công vào tập tin:\n" + file.getAbsolutePath()); 
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
                    refresh(); 
                    output.setText("✅ Đã tải dữ liệu thành công từ tập tin:\n" + file.getAbsolutePath() +
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

    // --- Helper UI Components ---
    private VBox createCard(String title, Region content) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        
        VBox card = new VBox(8, titleLabel, content);
        card.setPadding(new Insets(12));
        card.setStyle("-fx-background-color: #ffffff; " +
                   "-fx-border-color: #e2e8f0; " +
                   "-fx-border-width: 1px; " +
                   "-fx-border-radius: 8px; " +
                   "-fx-background-radius: 8px;");
        return card;
    }

    private Button createStyledButton(String text, String bgColor, String textColor) {
        Button btn = new Button(text);
        btn.setStyle(String.format(
            "-fx-background-color: %s; -fx-text-fill: %s; -fx-font-weight: bold; -fx-background-radius: 6px; -fx-cursor: hand;", 
            bgColor, textColor));
        return btn;
    }

    private String encodeCss() {
        String css = 
            ".button { -fx-padding: 6px 12px; -fx-background-radius: 6px; -fx-cursor: hand; } " +
            ".button:hover { -fx-opacity: 0.9; } " +
            ".text-field { -fx-padding: 6px; -fx-background-radius: 6px; -fx-border-color: #cbd5e1; -fx-border-radius: 6px; } " +
            ".list-view { -fx-background-radius: 6px; -fx-border-color: #cbd5e1; -fx-border-radius: 6px; } " +
            ".text-area { -fx-background-radius: 6px; -fx-border-color: #cbd5e1; -fx-border-radius: 6px; }";
        return css.replace(" ", "%20").replace("#", "%23").replace("\n", "");
    }

    public static void main(String[] args) { 
        launch(args); 
    }
}