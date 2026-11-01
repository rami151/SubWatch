package com.example.subwatch.data.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import androidx.lifecycle.LiveData;

import com.example.subwatch.data.dao.CategoryDao;
import com.example.subwatch.data.dao.SubscriptionDao;
import com.example.subwatch.data.entity.Category;
import com.example.subwatch.data.entity.Subscription;
import com.example.subwatch.data.model.SubscriptionWithCategory;
import com.example.subwatch.domain.BillingCycle;
import com.example.subwatch.domain.SubscriptionStatus;

import org.junit.Before;
import org.junit.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Unit tests for SubscriptionRepository logic (RG6, RG11, D5) using synchronous test execution.
 */
public class SubscriptionRepositoryLogicTest {

    private FakeSubscriptionDao fakeSubscriptionDao;
    private FakeCategoryDao fakeCategoryDao;
    private SubscriptionRepository repository;
    private Clock fixedClock;

    @Before
    public void setUp() {
        fakeSubscriptionDao = new FakeSubscriptionDao();
        fakeCategoryDao = new FakeCategoryDao();
        // Direct executor for synchronous test execution
        ExecutorService directExecutor = new DirectExecutorService();
        repository = new SubscriptionRepository(fakeSubscriptionDao, fakeCategoryDao, directExecutor);
        fixedClock = Clock.fixed(Instant.parse("2026-11-01T12:00:00Z"), ZoneOffset.UTC);
    }

    @Test
    public void testReactivateRecalculatesNextDueDateFromAnchor_RG11() {
        // An archived subscription anchored on 2026-01-15, monthly
        // Today is 2026-11-01
        // Reactivating it should calculate next due date from 2026-01-15 -> 2026-11-15
        LocalDate anchor = LocalDate.of(2026, 1, 15);
        Subscription sub = new Subscription.Builder()
                .id(1)
                .name("Spotify")
                .amountMinor(1099)
                .currency("EUR")
                .billingCycle(BillingCycle.MONTHLY)
                .startDate(anchor)
                .nextDueDate(LocalDate.of(2026, 3, 15)) // old due date
                .status(SubscriptionStatus.ARCHIVED)
                .build();
        fakeSubscriptionDao.subscriptions.add(sub);

        AtomicBoolean completed = new AtomicBoolean(false);
        repository.reactivate(sub, fixedClock, () -> completed.set(true));

        assertEquals(SubscriptionStatus.ACTIVE, sub.getStatus());
        assertEquals(LocalDate.of(2026, 11, 15), sub.getNextDueDate());
        assertEquals(1, fakeSubscriptionDao.updateCount);
    }

    @Test
    public void testRecalculateOverdueSubscriptionsAndFlipsExpiredTrials_RG6_D5() {
        // sub1: expired trial (trialEndDate 2026-10-25 < today 2026-11-01)
        Subscription sub1 = new Subscription.Builder()
                .id(1)
                .name("Canal+")
                .amountMinor(2599)
                .currency("EUR")
                .billingCycle(BillingCycle.MONTHLY)
                .startDate(LocalDate.of(2026, 10, 11))
                .isTrial(true)
                .trialEndDate(LocalDate.of(2026, 10, 25))
                .nextDueDate(LocalDate.of(2026, 10, 25)) // overdue
                .status(SubscriptionStatus.ACTIVE)
                .build();

        // sub2: overdue regular monthly subscription (due 2026-10-15)
        Subscription sub2 = new Subscription.Builder()
                .id(2)
                .name("iCloud")
                .amountMinor(299)
                .currency("EUR")
                .billingCycle(BillingCycle.MONTHLY)
                .startDate(LocalDate.of(2026, 5, 15))
                .nextDueDate(LocalDate.of(2026, 10, 15)) // overdue
                .status(SubscriptionStatus.ACTIVE)
                .build();

        fakeSubscriptionDao.subscriptions.add(sub1);
        fakeSubscriptionDao.subscriptions.add(sub2);

        repository.recalculateOverdueSubscriptions(fixedClock, null);

        // sub1: D5: is_trial must become false, and next due date recomputed from trialEnd (2026-10-25 + 1 month = 2026-11-25)
        assertFalse(sub1.isTrial());
        assertEquals(LocalDate.of(2026, 11, 25), sub1.getNextDueDate());

        // sub2: regular monthly from the 15th, next due on/after 2026-11-01 is 2026-11-15
        assertEquals(LocalDate.of(2026, 11, 15), sub2.getNextDueDate());
    }

    // --- Minimal fakes for repository logic test ---

    private static class FakeSubscriptionDao implements SubscriptionDao {
        final List<Subscription> subscriptions = new ArrayList<>();
        int updateCount = 0;

        @Override
        public long insert(Subscription subscription) {
            subscriptions.add(subscription);
            return subscriptions.size();
        }

        @Override
        public List<Long> insertAll(List<Subscription> subscriptions) {
            this.subscriptions.addAll(subscriptions);
            return Collections.emptyList();
        }

        @Override
        public int update(Subscription subscription) {
            updateCount++;
            for (int i = 0; i < subscriptions.size(); i++) {
                if (subscriptions.get(i).getId() == subscription.getId()) {
                    subscriptions.set(i, subscription);
                    return 1;
                }
            }
            return 1;
        }

        @Override
        public int delete(Subscription subscription) {
            subscriptions.remove(subscription);
            return 1;
        }

        @Override
        public int deleteById(long id) {
            subscriptions.removeIf(s -> s.getId() == id);
            return 1;
        }

        @Override
        public LiveData<Subscription> getSubscriptionById(long id) {
            return null;
        }

        @Override
        public Subscription getSubscriptionByIdSync(long id) {
            for (Subscription s : subscriptions) {
                if (s.getId() == id) return s;
            }
            return null;
        }

        @Override
        public LiveData<List<Subscription>> getAllSubscriptions() {
            return null;
        }

        @Override
        public List<Subscription> getAllSubscriptionsSync() {
            return subscriptions;
        }

        @Override
        public LiveData<List<Subscription>> getSubscriptionsByStatus(SubscriptionStatus status) {
            return null;
        }

        @Override
        public LiveData<List<Subscription>> getActiveSubscriptionsSortedByDueDate() {
            return null;
        }

        @Override
        public LiveData<List<Subscription>> getActiveSubscriptionsSortedByName() {
            return null;
        }

        @Override
        public LiveData<List<Subscription>> getActiveSubscriptionsSortedByCost() {
            return null;
        }

        @Override
        public List<Subscription> getActiveSubscriptionsSync() {
            List<Subscription> result = new ArrayList<>();
            for (Subscription s : subscriptions) {
                if (s.getStatus() == SubscriptionStatus.ACTIVE) result.add(s);
            }
            return result;
        }

        @Override
        public LiveData<SubscriptionWithCategory> getSubscriptionWithCategoryById(long id) {
            return null;
        }

        @Override
        public SubscriptionWithCategory getSubscriptionWithCategoryByIdSync(long id) {
            return null;
        }

        @Override
        public LiveData<List<SubscriptionWithCategory>> getAllSubscriptionsWithCategory() {
            return null;
        }

        @Override
        public LiveData<List<SubscriptionWithCategory>> getSubscriptionsWithCategoryByStatus(SubscriptionStatus status) {
            return null;
        }

        @Override
        public LiveData<List<SubscriptionWithCategory>> getActiveSubscriptionsWithCategorySortedByDueDate() {
            return null;
        }

        @Override
        public LiveData<List<SubscriptionWithCategory>> getActiveSubscriptionsWithCategorySortedByName() {
            return null;
        }

        @Override
        public LiveData<List<SubscriptionWithCategory>> getActiveSubscriptionsWithCategorySortedByCost() {
            return null;
        }

        @Override
        public LiveData<Integer> getActiveCount() {
            return null;
        }

        @Override
        public LiveData<Integer> getActiveTrialCount() {
            return null;
        }

        @Override
        public LiveData<List<Subscription>> getUpcomingSubscriptions(int limit) {
            return null;
        }

        @Override
        public LiveData<List<SubscriptionWithCategory>> getUpcomingSubscriptionsWithCategory(int limit) {
            return null;
        }

        @Override
        public List<Subscription> getOverdueActiveSubscriptionsSync(LocalDate today) {
            List<Subscription> result = new ArrayList<>();
            for (Subscription s : subscriptions) {
                if (s.getStatus() == SubscriptionStatus.ACTIVE && s.getNextDueDate().isBefore(today)) {
                    result.add(s);
                }
            }
            return result;
        }
    }

    private static class FakeCategoryDao implements CategoryDao {
        final List<Category> categories = new ArrayList<>();

        @Override
        public long insert(Category category) {
            categories.add(category);
            return categories.size();
        }

        @Override
        public List<Long> insertAll(List<Category> categories) {
            this.categories.addAll(categories);
            return Collections.emptyList();
        }

        @Override
        public void insertAll(Category... categories) {
            Collections.addAll(this.categories, categories);
        }

        @Override
        public int update(Category category) {
            return 1;
        }

        @Override
        public int delete(Category category) {
            categories.remove(category);
            return 1;
        }

        @Override
        public int deleteById(long id) {
            categories.removeIf(c -> c.getId() == id);
            return 1;
        }

        @Override
        public LiveData<Category> getCategoryById(long id) {
            return null;
        }

        @Override
        public Category getCategoryByIdSync(long id) {
            for (Category c : categories) {
                if (c.getId() == id) return c;
            }
            return null;
        }

        @Override
        public Category getCategoryByNameSync(String name) {
            for (Category c : categories) {
                if (c.getName().equals(name)) return c;
            }
            return null;
        }

        @Override
        public LiveData<List<Category>> getAllCategories() {
            return null;
        }

        @Override
        public List<Category> getAllCategoriesSync() {
            return categories;
        }

        @Override
        public int count() {
            return categories.size();
        }
    }

    private static class DirectExecutorService extends java.util.concurrent.AbstractExecutorService {
        private boolean shutdown = false;

        @Override
        public void shutdown() {
            shutdown = true;
        }

        @Override
        public List<Runnable> shutdownNow() {
            shutdown = true;
            return Collections.emptyList();
        }

        @Override
        public boolean isShutdown() {
            return shutdown;
        }

        @Override
        public boolean isTerminated() {
            return shutdown;
        }

        @Override
        public boolean awaitTermination(long timeout, java.util.concurrent.TimeUnit unit) {
            return true;
        }

        @Override
        public void execute(Runnable command) {
            command.run();
        }
    }
}
