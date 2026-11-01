package com.example.subwatch.data.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Embedded;
import androidx.room.Relation;

import com.example.subwatch.data.entity.Category;
import com.example.subwatch.data.entity.Subscription;

import java.util.Objects;

/**
 * POJO for querying a Subscription alongside its associated Category (RG12).
 * When categoryId is null (or the referenced category was deleted with ON DELETE SET NULL),
 * category is null, representing "Non classé".
 */
public class SubscriptionWithCategory {

    @Embedded
    public Subscription subscription;

    @Nullable
    @Relation(
            parentColumn = "category_id",
            entityColumn = "id"
    )
    public Category category;

    public SubscriptionWithCategory() {
    }

    @androidx.room.Ignore
    public SubscriptionWithCategory(@NonNull Subscription subscription, @Nullable Category category) {
        this.subscription = Objects.requireNonNull(subscription, "subscription must not be null");
        this.category = category;
    }

    @NonNull
    public Subscription getSubscription() {
        return subscription;
    }

    public void setSubscription(@NonNull Subscription subscription) {
        this.subscription = subscription;
    }

    @Nullable
    public Category getCategory() {
        return category;
    }

    public void setCategory(@Nullable Category category) {
        this.category = category;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubscriptionWithCategory that = (SubscriptionWithCategory) o;
        return Objects.equals(subscription, that.subscription) &&
                Objects.equals(category, that.category);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subscription, category);
    }

    @NonNull
    @Override
    public String toString() {
        return "SubscriptionWithCategory{" +
                "subscription=" + subscription +
                ", category=" + category +
                '}';
    }
}
