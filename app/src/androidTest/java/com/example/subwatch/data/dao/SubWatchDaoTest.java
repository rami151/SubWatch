package com.example.subwatch.data.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.subwatch.data.AppDatabase;
import com.example.subwatch.data.entity.Category;
import com.example.subwatch.data.entity.Subscription;
import com.example.subwatch.data.model.SubscriptionWithCategory;
import com.example.subwatch.domain.BillingCycle;
import com.example.subwatch.domain.SubscriptionStatus;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Instrumented Room DAO tests verifying Category and Subscription operations,
 * foreign key cascade (RG12 ON DELETE SET NULL), unique constraints, sorting,
 * and database pre-population (F16).
 */
@RunWith(AndroidJUnit4.class)
public class SubWatchDaoTest {

    private AppDatabase database;
    private CategoryDao categoryDao;
    private SubscriptionDao subscriptionDao;

    @Before
    public void createDb() {
        Context context = ApplicationProvider.getApplicationContext();
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .allowMainThreadQueries()
                .build();
        categoryDao = database.categoryDao();
        subscriptionDao = database.subscriptionDao();
    }

    @After
    public void closeDb() throws IOException {
        database.close();
    }

    @Test
    public void testCategoryInsertAndQuery() {
        Category category = new Category("Vidéo", 0xFF285644);
        long id = categoryDao.insert(category);
        assertTrue(id > 0);

        Category loaded = categoryDao.getCategoryByIdSync(id);
        assertNotNull(loaded);
        assertEquals("Vidéo", loaded.getName());
        assertEquals(0xFF285644, loaded.getColor());

        Category byName = categoryDao.getCategoryByNameSync("Vidéo");
        assertNotNull(byName);
        assertEquals(id, byName.getId());
    }

    @Test
    public void testCategoryUniqueNameConstraint() {
        Category cat1 = new Category("Musique", 0xFF5E8A75);
        Category cat2 = new Category("Musique", 0xFF000000);
        categoryDao.insert(cat1);

        try {
            categoryDao.insert(cat2);
            fail("Inserting duplicate category name should throw SQLiteConstraintException");
        } catch (SQLiteConstraintException expected) {
            // Expected unique constraint failure
        }
    }

    @Test
    public void testCategoryUpdateAndDelete() {
        Category category = new Category("Cloud", 0xFF5B8A99);
        long id = categoryDao.insert(category);

        category.setId(id);
        category.setName("Cloud Storage");
        category.setColor(0xFF112233);
        int updatedRows = categoryDao.update(category);
        assertEquals(1, updatedRows);

        Category updated = categoryDao.getCategoryByIdSync(id);
        assertNotNull(updated);
        assertEquals("Cloud Storage", updated.getName());
        assertEquals(0xFF112233, updated.getColor());

        int deletedRows = categoryDao.delete(category);
        assertEquals(1, deletedRows);
        assertNull(categoryDao.getCategoryByIdSync(id));
    }

    @Test
    public void testSubscriptionInsertAndQueryAllFields() {
        Category category = new Category("Vidéo", 0xFF285644);
        long categoryId = categoryDao.insert(category);

        Instant now = Instant.parse("2026-11-01T20:00:00Z");
        LocalDate start = LocalDate.of(2026, 11, 1);
        LocalDate nextDue = LocalDate.of(2026, 12, 1);

        Subscription sub = new Subscription.Builder()
                .name("Netflix")
                .amountMinor(1599)
                .currency("EUR")
                .billingCycle(BillingCycle.MONTHLY)
                .startDate(start)
                .nextDueDate(nextDue)
                .categoryId(categoryId)
                .isTrial(false)
                .status(SubscriptionStatus.ACTIVE)
                .notes("Formule Standard")
                .createdAt(now)
                .updatedAt(now)
                .build();

        long id = subscriptionDao.insert(sub);
        assertTrue(id > 0);

        Subscription loaded = subscriptionDao.getSubscriptionByIdSync(id);
        assertNotNull(loaded);
        assertEquals("Netflix", loaded.getName());
        assertEquals(1599, loaded.getAmountMinor());
        assertEquals("EUR", loaded.getCurrency());
        assertEquals(BillingCycle.MONTHLY, loaded.getBillingCycle());
        assertEquals(start, loaded.getStartDate());
        assertEquals(nextDue, loaded.getNextDueDate());
        assertEquals(Long.valueOf(categoryId), loaded.getCategoryId());
        assertFalse(loaded.isTrial());
        assertNull(loaded.getTrialEndDate());
        assertEquals(SubscriptionStatus.ACTIVE, loaded.getStatus());
        assertEquals("Formule Standard", loaded.getNotes());
        assertEquals(now, loaded.getCreatedAt());
        assertEquals(now, loaded.getUpdatedAt());
    }

    @Test
    public void testForeignKeyOnDeleteSetNull_RG12() {
        // RG12: Deleting a category used by subscriptions sets category_id to NULL
        Category category = new Category("Jeux", 0xFF7B5E8A);
        long categoryId = categoryDao.insert(category);

        Subscription sub = new Subscription.Builder()
                .name("Xbox Game Pass")
                .amountMinor(1499)
                .currency("EUR")
                .billingCycle(BillingCycle.MONTHLY)
                .startDate(LocalDate.of(2026, 10, 1))
                .nextDueDate(LocalDate.of(2026, 11, 1))
                .categoryId(categoryId)
                .status(SubscriptionStatus.ACTIVE)
                .build();
        long subId = subscriptionDao.insert(sub);

        // Before deletion: SubscriptionWithCategory has category
        SubscriptionWithCategory withCat = subscriptionDao.getSubscriptionWithCategoryByIdSync(subId);
        assertNotNull(withCat);
        assertNotNull(withCat.getCategory());
        assertEquals("Jeux", withCat.getCategory().getName());

        // Delete the category
        category.setId(categoryId);
        categoryDao.delete(category);
        assertNull(categoryDao.getCategoryByIdSync(categoryId));

        // After deletion: Subscription still exists, but category_id is NULL
        Subscription reloadedSub = subscriptionDao.getSubscriptionByIdSync(subId);
        assertNotNull(reloadedSub);
        assertNull(reloadedSub.getCategoryId());

        // SubscriptionWithCategory category relation is NULL ("Non classé")
        SubscriptionWithCategory afterDeletion = subscriptionDao.getSubscriptionWithCategoryByIdSync(subId);
        assertNotNull(afterDeletion);
        assertNull(afterDeletion.getCategory());
    }

    @Test
    public void testSortingAndFilteringQueries() {
        Category cat = new Category("Logiciels", 0xFF6E7B91);
        long catId = categoryDao.insert(cat);

        Subscription sub1 = new Subscription.Builder()
                .name("Zendesk")
                .amountMinor(4900)
                .currency("EUR")
                .billingCycle(BillingCycle.MONTHLY)
                .startDate(LocalDate.of(2026, 1, 1))
                .nextDueDate(LocalDate.of(2026, 11, 20))
                .categoryId(catId)
                .status(SubscriptionStatus.ACTIVE)
                .build();

        Subscription sub2 = new Subscription.Builder()
                .name("Adobe")
                .amountMinor(2400)
                .currency("EUR")
                .billingCycle(BillingCycle.MONTHLY)
                .startDate(LocalDate.of(2026, 1, 1))
                .nextDueDate(LocalDate.of(2026, 11, 05))
                .categoryId(catId)
                .status(SubscriptionStatus.ACTIVE)
                .build();

        Subscription sub3 = new Subscription.Builder()
                .name("Archived App")
                .amountMinor(999)
                .currency("EUR")
                .billingCycle(BillingCycle.MONTHLY)
                .startDate(LocalDate.of(2026, 1, 1))
                .nextDueDate(LocalDate.of(2026, 11, 01))
                .status(SubscriptionStatus.ARCHIVED)
                .build();

        subscriptionDao.insert(sub1);
        subscriptionDao.insert(sub2);
        subscriptionDao.insert(sub3);

        // Active subscriptions sync (should exclude archived sub3)
        List<Subscription> activeList = subscriptionDao.getActiveSubscriptionsSync();
        assertEquals(2, activeList.size());
        assertEquals("Adobe", activeList.get(0).getName()); // 11-05 before 11-20
        assertEquals("Zendesk", activeList.get(1).getName());

        // All subscriptions
        List<Subscription> allList = subscriptionDao.getAllSubscriptionsSync();
        assertEquals(3, allList.size());
    }

    @Test
    public void testDatabasePrepopulation_F16() throws IOException {
        Context context = ApplicationProvider.getApplicationContext();
        AppDatabase prepopulatedDb = Room.inMemoryDatabaseBuilder(context, AppDatabase.class)
                .addCallback(AppDatabase.PREPOPULATE_CALLBACK)
                .allowMainThreadQueries()
                .build();

        // Trigger database creation by executing a query
        List<Category> categories = prepopulatedDb.categoryDao().getAllCategoriesSync();
        assertEquals(7, categories.size());

        assertEquals("Vidéo", categories.get(0).getName());
        assertEquals("Musique", categories.get(1).getName());
        assertEquals("Cloud", categories.get(2).getName());
        assertEquals("Logiciels", categories.get(3).getName());
        assertEquals("Éducation", categories.get(4).getName());
        assertEquals("Jeux", categories.get(5).getName());
        assertEquals("Autre", categories.get(6).getName());

        prepopulatedDb.close();
    }
}
