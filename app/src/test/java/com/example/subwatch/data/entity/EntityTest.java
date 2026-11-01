package com.example.subwatch.data.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

import com.example.subwatch.domain.BillingCycle;
import com.example.subwatch.domain.SubscriptionStatus;

import org.junit.Test;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Unit tests for Subscription and Category entities.
 */
public class EntityTest {

    @Test
    public void testCategoryEntity() {
        Category category = new Category("Vidéo", 0xFF285644);
        category.setId(42);

        assertEquals(42, category.getId());
        assertEquals("Vidéo", category.getName());
        assertEquals(0xFF285644, category.getColor());

        Category duplicate = new Category("Vidéo", 0xFF285644);
        duplicate.setId(42);
        assertEquals(category, duplicate);
        assertEquals(category.hashCode(), duplicate.hashCode());

        Category different = new Category("Musique", 0xFF5E8A75);
        assertNotEquals(category, different);
    }

    @Test
    public void testDefaultCategoriesList() {
        assertEquals(7, Category.DEFAULT_CATEGORIES.size());
        assertEquals("Vidéo", Category.DEFAULT_CATEGORIES.get(0).getName());
        assertEquals("Musique", Category.DEFAULT_CATEGORIES.get(1).getName());
        assertEquals("Cloud", Category.DEFAULT_CATEGORIES.get(2).getName());
        assertEquals("Logiciels", Category.DEFAULT_CATEGORIES.get(3).getName());
        assertEquals("Éducation", Category.DEFAULT_CATEGORIES.get(4).getName());
        assertEquals("Jeux", Category.DEFAULT_CATEGORIES.get(5).getName());
        assertEquals("Autre", Category.DEFAULT_CATEGORIES.get(6).getName());
    }

    @Test
    public void testSubscriptionBuilder() {
        Instant now = Instant.parse("2026-11-01T20:00:00Z");
        LocalDate start = LocalDate.of(2026, 11, 1);
        LocalDate nextDue = LocalDate.of(2026, 12, 1);

        Subscription sub = new Subscription.Builder()
                .id(1)
                .name("Netflix")
                .amountMinor(1599)
                .currency("EUR")
                .billingCycle(BillingCycle.MONTHLY)
                .startDate(start)
                .nextDueDate(nextDue)
                .categoryId(10L)
                .isTrial(false)
                .status(SubscriptionStatus.ACTIVE)
                .notes("Premium 4K")
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertEquals(1, sub.getId());
        assertEquals("Netflix", sub.getName());
        assertEquals(1599, sub.getAmountMinor());
        assertEquals("EUR", sub.getCurrency());
        assertEquals(BillingCycle.MONTHLY, sub.getBillingCycle());
        assertEquals(start, sub.getStartDate());
        assertEquals(nextDue, sub.getNextDueDate());
        assertEquals(Long.valueOf(10), sub.getCategoryId());
        assertEquals(false, sub.isTrial());
        assertNull(sub.getTrialEndDate());
        assertEquals(SubscriptionStatus.ACTIVE, sub.getStatus());
        assertEquals("Premium 4K", sub.getNotes());
        assertEquals(now, sub.getCreatedAt());
        assertEquals(now, sub.getUpdatedAt());

        // Test copy builder
        Subscription copy = new Subscription.Builder(sub).name("Netflix Standard").build();
        assertEquals(1, copy.getId());
        assertEquals("Netflix Standard", copy.getName());
        assertEquals(1599, copy.getAmountMinor());
    }
}
