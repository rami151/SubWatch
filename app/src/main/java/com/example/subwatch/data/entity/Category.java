package com.example.subwatch.data.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Room entity representing a subscription category (cahier 8.3).
 */
@Entity(
        tableName = "category",
        indices = {@Index(value = "name", unique = true)}
)
public class Category {

    /**
     * The 7 pre-filled categories required by cahier section 4.5 (F16).
     */
    public static final List<Category> DEFAULT_CATEGORIES = Collections.unmodifiableList(Arrays.asList(
            new Category("Vidéo", 0xFF285644),
            new Category("Musique", 0xFF5E8A75),
            new Category("Cloud", 0xFF5B8A99),
            new Category("Logiciels", 0xFF6E7B91),
            new Category("Éducation", 0xFF8A5A00),
            new Category("Jeux", 0xFF7B5E8A),
            new Category("Autre", 0xFF5C675F)
    ));

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private long id;

    @NonNull
    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "color")
    private int color;

    public Category(@NonNull String name, int color) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.color = color;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = Objects.requireNonNull(name, "name must not be null");
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return id == category.id &&
                color == category.color &&
                name.equals(category.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, color);
    }

    @NonNull
    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", color=" + String.format("#%08X", color) +
                '}';
    }
}
