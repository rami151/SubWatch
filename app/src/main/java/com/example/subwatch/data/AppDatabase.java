package com.example.subwatch.data;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.subwatch.data.converter.Converters;
import com.example.subwatch.data.dao.CategoryDao;
import com.example.subwatch.data.dao.SubscriptionDao;
import com.example.subwatch.data.entity.Category;
import com.example.subwatch.data.entity.Subscription;

/**
 * Room database for SubWatch (cahier 8.3).
 * Contains Subscription and Category tables with foreign keys and exportSchema enabled.
 */
@Database(
        entities = {Subscription.class, Category.class},
        version = 1,
        exportSchema = true
)
@TypeConverters(Converters.class)
public abstract class AppDatabase extends RoomDatabase {

    public static final String DATABASE_NAME = "subwatch.db";

    private static volatile AppDatabase sInstance;

    public abstract SubscriptionDao subscriptionDao();

    public abstract CategoryDao categoryDao();

    public static final RoomDatabase.Callback PREPOPULATE_CALLBACK = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            prepopulateCategories(db);
        }
    };

    /**
     * Inserts the 7 pre-filled categories required by cahier section 4.5 (F16).
     */
    public static void prepopulateCategories(@NonNull SupportSQLiteDatabase db) {
        for (Category category : Category.DEFAULT_CATEGORIES) {
            db.execSQL(
                    "INSERT INTO category (name, color) VALUES (?, ?)",
                    new Object[]{category.getName(), category.getColor()}
            );
        }
    }

    public static AppDatabase getInstance(@NonNull Context context) {
        if (sInstance == null) {
            synchronized (AppDatabase.class) {
                if (sInstance == null) {
                    sInstance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    DATABASE_NAME
                            )
                            .addCallback(PREPOPULATE_CALLBACK)
                            .build();
                }
            }
        }
        return sInstance;
    }
}
