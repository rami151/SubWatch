package com.example.subwatch.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.subwatch.data.entity.Category;

import java.util.List;

/**
 * Data Access Object for Category entity.
 */
@Dao
public interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insert(Category category);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    List<Long> insertAll(List<Category> categories);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(Category... categories);

    @Update
    int update(Category category);

    @Delete
    int delete(Category category);

    @Query("DELETE FROM category WHERE id = :id")
    int deleteById(long id);

    @Query("SELECT * FROM category WHERE id = :id")
    LiveData<Category> getCategoryById(long id);

    @Query("SELECT * FROM category WHERE id = :id")
    Category getCategoryByIdSync(long id);

    @Query("SELECT * FROM category WHERE name = :name LIMIT 1")
    Category getCategoryByNameSync(String name);

    @Query("SELECT * FROM category ORDER BY id ASC")
    LiveData<List<Category>> getAllCategories();

    @Query("SELECT * FROM category ORDER BY id ASC")
    List<Category> getAllCategoriesSync();

    @Query("SELECT COUNT(*) FROM category")
    int count();
}
