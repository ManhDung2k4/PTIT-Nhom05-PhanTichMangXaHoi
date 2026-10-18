package vn.edu.socialgraph.model;

import java.util.Objects;

/**
 * Đại diện cho một người dùng (đỉnh / vertex) trong mạng xã hội.
 * Mỗi người dùng có một định danh duy nhất (id) không thể thay đổi
 * và tên hiển thị (name) có thể cập nhật.
 */
public final class User {
    private final String id;
    private String name;

    /**
     * Khởi tạo đối tượng User mới.
     *
     * @param id   Mã định danh duy nhất của người dùng (không được null hoặc rỗng)
     * @param name Tên hiển thị của người dùng (không được null hoặc rỗng)
     * @throws IllegalArgumentException nếu id hoặc name bị null/khoảng trắng
     */
    public User(String id, String name) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("ID người dùng không được để trống");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Tên người dùng không được để trống");
        }
        this.id = id.trim();
        this.name = name.trim();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    /**
     * Cập nhật tên hiển thị của người dùng.
     *
     * @param name Tên mới (không được null hoặc rỗng)
     * @throws IllegalArgumentException nếu name bị null hoặc khoảng trắng
     */
    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Tên người dùng không được để trống");
        }
        this.name = name.trim();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof User user)) return false;
        return id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return name + " (" + id + ")";
    }
}

