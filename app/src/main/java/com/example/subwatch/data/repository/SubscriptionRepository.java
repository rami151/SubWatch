package com.example.subwatch.data.repository;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.example.subwatch.data.AppDatabase;
import com.example.subwatch.data.dao.CategoryDao;
import com.example.subwatch.data.dao.SubscriptionDao;
import com.example.subwatch.data.entity.Category;
import com.example.subwatch.data.entity.Subscription;
import com.example.subwatch.data.model.SubscriptionWithCategory;
import com.example.subwatch.domain.BillingCalculator;
import com.example.subwatch.domain.SubscriptionStatus;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Repository coordinating database access for Subscriptions and Categories.
 * All write operations are executed sequentially on a single-thread executor
 * to prevent database concurrency issues and main-thread disk I/O.
 */
public class SubscriptionRepository {

    private static volatile SubscriptionRepository sInstance;

    private final SubscriptionDao subscriptionDao;
    private final CategoryDao categoryDao;
    private final ExecutorService executor;

    public static SubscriptionRepository getInstance(@NonNull Application application) {
        if (sInstance == null) {
            synchronized (SubscriptionRepository.class) {
                if (sInstance == null) {
                    AppDatabase db = AppDatabase.getInstance(application);
                    sInstance = new SubscriptionRepository(
                            db.subscriptionDao(),
                            db.categoryDao(),
                            Executors.newSingleThreadExecutor()
                    );
                }
            }
        }
        return sInstance;
    }

    public SubscriptionRepository(@NonNull SubscriptionDao subscriptionDao,
                                  @NonNull CategoryDao categoryDao,
                                  @NonNull ExecutorService executor) {
        this.subscriptionDao = Objects.requireNonNull(subscriptionDao, "subscriptionDao must not be null");
        this.categoryDao = Objects.requireNonNull(categoryDao, "categoryDao must not be null");
        this.executor = Objects.requireNonNull(executor, "executor must not be null");
    }

    // --- Subscription queries ---

    public LiveData<List<Subscription>> getAllSubscriptions() {
        return subscriptionDao.getAllSubscriptions();
    }

    public LiveData<List<Subscription>> getSubscriptionsByStatus(@NonNull SubscriptionStatus status) {
        return subscriptionDao.getSubscriptionsByStatus(status);
    }

    public LiveData<List<Subscription>> getActiveSubscriptionsSortedByDueDate() {
        return subscriptionDao.getActiveSubscriptionsSortedByDueDate();
    }

    public LiveData<List<Subscription>> getActiveSubscriptionsSortedByName() {
        return subscriptionDao.getActiveSubscriptionsSortedByName();
    }

    public LiveData<List<Subscription>> getActiveSubscriptionsSortedByCost() {
        return subscriptionDao.getActiveSubscriptionsSortedByCost();
    }

    public LiveData<Subscription> getSubscriptionById(long id) {
        return subscriptionDao.getSubscriptionById(id);
    }

    public LiveData<SubscriptionWithCategory> getSubscriptionWithCategoryById(long id) {
        return subscriptionDao.getSubscriptionWithCategoryById(id);
    }

    public LiveData<List<SubscriptionWithCategory>> getAllSubscriptionsWithCategory() {
        return subscriptionDao.getAllSubscriptionsWithCategory();
    }

    public LiveData<List<SubscriptionWithCategory>> getSubscriptionsWithCategoryByStatus(@NonNull SubscriptionStatus status) {
        return subscriptionDao.getSubscriptionsWithCategoryByStatus(status);
    }

    public LiveData<List<SubscriptionWithCategory>> getActiveSubscriptionsWithCategorySortedByDueDate() {
        return subscriptionDao.getActiveSubscriptionsWithCategorySortedByDueDate();
    }

    public LiveData<List<SubscriptionWithCategory>> getActiveSubscriptionsWithCategorySortedByName() {
        return subscriptionDao.getActiveSubscriptionsWithCategorySortedByName();
    }

    public LiveData<List<SubscriptionWithCategory>> getActiveSubscriptionsWithCategorySortedByCost() {
        return subscriptionDao.getActiveSubscriptionsWithCategorySortedByCost();
    }

    public LiveData<Integer> getActiveCount() {
        return subscriptionDao.getActiveCount();
    }

    public LiveData<Integer> getActiveTrialCount() {
        return subscriptionDao.getActiveTrialCount();
    }

    public LiveData<List<Subscription>> getUpcomingSubscriptions(int limit) {
        return subscriptionDao.getUpcomingSubscriptions(limit);
    }

    public LiveData<List<SubscriptionWithCategory>> getUpcomingSubscriptionsWithCategory(int limit) {
        return subscriptionDao.getUpcomingSubscriptionsWithCategory(limit);
    }

    // --- Synchronous queries for background workers / WorkManager ---

    public List<Subscription> getActiveSubscriptionsSync() {
        return subscriptionDao.getActiveSubscriptionsSync();
    }

    public List<Subscription> getAllSubscriptionsSync() {
        return subscriptionDao.getAllSubscriptionsSync();
    }

    public Subscription getSubscriptionByIdSync(long id) {
        return subscriptionDao.getSubscriptionByIdSync(id);
    }

    public SubscriptionWithCategory getSubscriptionWithCategoryByIdSync(long id) {
        return subscriptionDao.getSubscriptionWithCategoryByIdSync(id);
    }

    // --- Subscription mutations ---

    public void insert(@NonNull Subscription subscription) {
        insert(subscription, null);
    }

    public void insert(@NonNull Subscription subscription, @Nullable Consumer<Long> onInserted) {
        executor.execute(() -> {
            long id = subscriptionDao.insert(subscription);
            subscription.setId(id);
            dispatchCallback(onInserted, id);
        });
    }

    public void update(@NonNull Subscription subscription) {
        update(subscription, null);
    }

    public void update(@NonNull Subscription subscription, @Nullable Runnable onComplete) {
        executor.execute(() -> {
            subscriptionDao.update(subscription);
            dispatchCallback(onComplete);
        });
    }

    public void delete(@NonNull Subscription subscription) {
        delete(subscription, null);
    }

    public void delete(@NonNull Subscription subscription, @Nullable Runnable onComplete) {
        executor.execute(() -> {
            subscriptionDao.delete(subscription);
            dispatchCallback(onComplete);
        });
    }

    public void deleteById(long id) {
        deleteById(id, null);
    }

    public void deleteById(long id, @Nullable Runnable onComplete) {
        executor.execute(() -> {
            subscriptionDao.deleteById(id);
            dispatchCallback(onComplete);
        });
    }

    /**
     * Archive an active subscription (F4, RG5).
     */
    public void archive(@NonNull Subscription subscription, @Nullable Runnable onComplete) {
        executor.execute(() -> {
            subscription.setStatus(SubscriptionStatus.ARCHIVED);
            subscription.setUpdatedAt(Instant.now());
            subscriptionDao.update(subscription);
            dispatchCallback(onComplete);
        });
    }

    /**
     * Reactivate an archived subscription (F4, RG11).
     * Recalculates the next due date from the anchor date (RG1) without considering inactive periods.
     */
    public void reactivate(@NonNull Subscription subscription, @Nullable Runnable onComplete) {
        reactivate(subscription, Clock.systemDefaultZone(), onComplete);
    }

    /**
     * Reactivate an archived subscription with an injected Clock (F4, RG11).
     */
    public void reactivate(@NonNull Subscription subscription, @NonNull Clock clock, @Nullable Runnable onComplete) {
        executor.execute(() -> {
            LocalDate anchor = subscription.getTrialEndDate() != null
                    ? subscription.getTrialEndDate()
                    : subscription.getStartDate();
            LocalDate nextDue = new BillingCalculator(clock).nextDueDate(
                    anchor,
                    subscription.getBillingCycle()
            );
            subscription.setStatus(SubscriptionStatus.ACTIVE);
            subscription.setNextDueDate(nextDue);
            subscription.setUpdatedAt(Instant.now(clock));
            subscriptionDao.update(subscription);
            dispatchCallback(onComplete);
        });
    }

    /**
     * Recalculate passed due dates on application launch or reminder execution (RG6, D5).
     */
    public void recalculateOverdueSubscriptions(@Nullable Runnable onComplete) {
        recalculateOverdueSubscriptions(Clock.systemDefaultZone(), onComplete);
    }

    /**
     * Recalculate passed due dates with an injected Clock (RG6, D5).
     */
    public void recalculateOverdueSubscriptions(@NonNull Clock clock, @Nullable Runnable onComplete) {
        executor.execute(() -> {
            LocalDate today = LocalDate.now(clock);
            BillingCalculator calculator = new BillingCalculator(clock);
            List<Subscription> overdueList = subscriptionDao.getOverdueActiveSubscriptionsSync(today);
            for (Subscription sub : overdueList) {
                boolean changed = false;
                // D5: trial end date passed -> is_trial becomes false
                if (sub.isTrial() && sub.getTrialEndDate() != null && !today.isBefore(sub.getTrialEndDate())) {
                    sub.setTrial(false);
                    changed = true;
                }
                LocalDate anchor = sub.getTrialEndDate() != null
                        ? sub.getTrialEndDate()
                        : sub.getStartDate();
                LocalDate nextDue = calculator.nextDueDate(anchor, sub.getBillingCycle());
                if (!nextDue.equals(sub.getNextDueDate())) {
                    sub.setNextDueDate(nextDue);
                    changed = true;
                }
                if (changed) {
                    sub.setUpdatedAt(Instant.now(clock));
                    subscriptionDao.update(sub);
                }
            }
            dispatchCallback(onComplete);
        });
    }

    // --- Category queries & mutations ---

    public LiveData<List<Category>> getAllCategories() {
        return categoryDao.getAllCategories();
    }

    public List<Category> getAllCategoriesSync() {
        return categoryDao.getAllCategoriesSync();
    }

    public LiveData<Category> getCategoryById(long id) {
        return categoryDao.getCategoryById(id);
    }

    public Category getCategoryByIdSync(long id) {
        return categoryDao.getCategoryByIdSync(id);
    }

    public void insertCategory(@NonNull Category category) {
        insertCategory(category, null);
    }

    public void insertCategory(@NonNull Category category, @Nullable Consumer<Long> onInserted) {
        executor.execute(() -> {
            long id = categoryDao.insert(category);
            category.setId(id);
            dispatchCallback(onInserted, id);
        });
    }

    public void updateCategory(@NonNull Category category) {
        updateCategory(category, null);
    }

    public void updateCategory(@NonNull Category category, @Nullable Runnable onComplete) {
        executor.execute(() -> {
            categoryDao.update(category);
            dispatchCallback(onComplete);
        });
    }

    public void deleteCategory(@NonNull Category category) {
        deleteCategory(category, null);
    }

    public void deleteCategory(@NonNull Category category, @Nullable Runnable onComplete) {
        executor.execute(() -> {
            categoryDao.delete(category);
            dispatchCallback(onComplete);
        });
    }

    public void deleteCategoryById(long id, @Nullable Runnable onComplete) {
        executor.execute(() -> {
            categoryDao.deleteById(id);
            dispatchCallback(onComplete);
        });
    }

    private <T> void dispatchCallback(@Nullable Consumer<T> callback, T value) {
        if (callback == null) return;
        try {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                callback.accept(value);
            } else {
                new Handler(Looper.getMainLooper()).post(() -> callback.accept(value));
            }
        } catch (Throwable t) {
            // If Looper is not mocked (e.g. JVM unit tests), execute directly
            callback.accept(value);
        }
    }

    private void dispatchCallback(@Nullable Runnable runnable) {
        if (runnable == null) return;
        try {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                runnable.run();
            } else {
                new Handler(Looper.getMainLooper()).post(runnable);
            }
        } catch (Throwable t) {
            // If Looper is not mocked (e.g. JVM unit tests), execute directly
            runnable.run();
        }
    }
}
