package com.example.subwatch.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.subwatch.data.entity.Subscription;
import com.example.subwatch.data.model.SubscriptionWithCategory;
import com.example.subwatch.domain.SubscriptionStatus;

import java.time.LocalDate;
import java.util.List;

/**
 * Data Access Object for Subscription entity (cahier 8.3).
 */
@Dao
public interface SubscriptionDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insert(Subscription subscription);

    @Insert(onConflict = OnConflictStrategy.ABORT)
    List<Long> insertAll(List<Subscription> subscriptions);

    @Update
    int update(Subscription subscription);

    @Delete
    int delete(Subscription subscription);

    @Query("DELETE FROM subscription WHERE id = :id")
    int deleteById(long id);

    @Query("SELECT * FROM subscription WHERE id = :id")
    LiveData<Subscription> getSubscriptionById(long id);

    @Query("SELECT * FROM subscription WHERE id = :id")
    Subscription getSubscriptionByIdSync(long id);

    @Query("SELECT * FROM subscription ORDER BY next_due_date ASC")
    LiveData<List<Subscription>> getAllSubscriptions();

    @Query("SELECT * FROM subscription ORDER BY next_due_date ASC")
    List<Subscription> getAllSubscriptionsSync();

    @Query("SELECT * FROM subscription WHERE status = :status ORDER BY next_due_date ASC")
    LiveData<List<Subscription>> getSubscriptionsByStatus(SubscriptionStatus status);

    @Query("SELECT * FROM subscription WHERE status = 'ACTIVE' ORDER BY next_due_date ASC")
    LiveData<List<Subscription>> getActiveSubscriptionsSortedByDueDate();

    @Query("SELECT * FROM subscription WHERE status = 'ACTIVE' ORDER BY name COLLATE NOCASE ASC")
    LiveData<List<Subscription>> getActiveSubscriptionsSortedByName();

    @Query("SELECT * FROM subscription WHERE status = 'ACTIVE' ORDER BY amount_minor DESC")
    LiveData<List<Subscription>> getActiveSubscriptionsSortedByCost();

    @Query("SELECT * FROM subscription WHERE status = 'ACTIVE' ORDER BY next_due_date ASC")
    List<Subscription> getActiveSubscriptionsSync();

    @Transaction
    @Query("SELECT * FROM subscription WHERE id = :id")
    LiveData<SubscriptionWithCategory> getSubscriptionWithCategoryById(long id);

    @Transaction
    @Query("SELECT * FROM subscription WHERE id = :id")
    SubscriptionWithCategory getSubscriptionWithCategoryByIdSync(long id);

    @Transaction
    @Query("SELECT * FROM subscription ORDER BY next_due_date ASC")
    LiveData<List<SubscriptionWithCategory>> getAllSubscriptionsWithCategory();

    @Transaction
    @Query("SELECT * FROM subscription WHERE status = :status ORDER BY next_due_date ASC")
    LiveData<List<SubscriptionWithCategory>> getSubscriptionsWithCategoryByStatus(SubscriptionStatus status);

    @Transaction
    @Query("SELECT * FROM subscription WHERE status = 'ACTIVE' ORDER BY next_due_date ASC")
    LiveData<List<SubscriptionWithCategory>> getActiveSubscriptionsWithCategorySortedByDueDate();

    @Transaction
    @Query("SELECT * FROM subscription WHERE status = 'ACTIVE' ORDER BY name COLLATE NOCASE ASC")
    LiveData<List<SubscriptionWithCategory>> getActiveSubscriptionsWithCategorySortedByName();

    @Transaction
    @Query("SELECT * FROM subscription WHERE status = 'ACTIVE' ORDER BY amount_minor DESC")
    LiveData<List<SubscriptionWithCategory>> getActiveSubscriptionsWithCategorySortedByCost();

    @Query("SELECT COUNT(*) FROM subscription WHERE status = 'ACTIVE'")
    LiveData<Integer> getActiveCount();

    @Query("SELECT COUNT(*) FROM subscription WHERE status = 'ACTIVE' AND is_trial = 1")
    LiveData<Integer> getActiveTrialCount();

    @Query("SELECT * FROM subscription WHERE status = 'ACTIVE' ORDER BY next_due_date ASC LIMIT :limit")
    LiveData<List<Subscription>> getUpcomingSubscriptions(int limit);

    @Transaction
    @Query("SELECT * FROM subscription WHERE status = 'ACTIVE' ORDER BY next_due_date ASC LIMIT :limit")
    LiveData<List<SubscriptionWithCategory>> getUpcomingSubscriptionsWithCategory(int limit);

    @Query("SELECT * FROM subscription WHERE status = 'ACTIVE' AND next_due_date < :today ORDER BY next_due_date ASC")
    List<Subscription> getOverdueActiveSubscriptionsSync(LocalDate today);
}
