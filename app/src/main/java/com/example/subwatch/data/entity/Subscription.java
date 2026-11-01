package com.example.subwatch.data.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.example.subwatch.domain.BillingCycle;
import com.example.subwatch.domain.SubscriptionStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Room entity representing a subscription (cahier 8.3).
 * Foreign key to Category cascades on delete with SET_NULL (RG12).
 */
@Entity(
        tableName = "subscription",
        foreignKeys = @ForeignKey(
                entity = Category.class,
                parentColumns = "id",
                childColumns = "category_id",
                onDelete = ForeignKey.SET_NULL
        ),
        indices = {
                @Index("next_due_date"),
                @Index("category_id")
        }
)
public class Subscription {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private long id;

    @NonNull
    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "amount_minor")
    private long amountMinor;

    @NonNull
    @ColumnInfo(name = "currency")
    private String currency;

    @NonNull
    @ColumnInfo(name = "billing_cycle")
    private BillingCycle billingCycle;

    @NonNull
    @ColumnInfo(name = "start_date")
    private LocalDate startDate;

    @NonNull
    @ColumnInfo(name = "next_due_date")
    private LocalDate nextDueDate;

    @Nullable
    @ColumnInfo(name = "category_id")
    private Long categoryId;

    @ColumnInfo(name = "is_trial")
    private boolean isTrial;

    @Nullable
    @ColumnInfo(name = "trial_end_date")
    private LocalDate trialEndDate;

    @NonNull
    @ColumnInfo(name = "status")
    private SubscriptionStatus status;

    @Nullable
    @ColumnInfo(name = "last_notified_due")
    private LocalDate lastNotifiedDue;

    @Nullable
    @ColumnInfo(name = "notes")
    private String notes;

    @NonNull
    @ColumnInfo(name = "created_at")
    private Instant createdAt;

    @NonNull
    @ColumnInfo(name = "updated_at")
    private Instant updatedAt;

    public Subscription(@NonNull String name,
                        long amountMinor,
                        @NonNull String currency,
                        @NonNull BillingCycle billingCycle,
                        @NonNull LocalDate startDate,
                        @NonNull LocalDate nextDueDate,
                        @Nullable Long categoryId,
                        boolean isTrial,
                        @Nullable LocalDate trialEndDate,
                        @NonNull SubscriptionStatus status,
                        @Nullable LocalDate lastNotifiedDue,
                        @Nullable String notes,
                        @NonNull Instant createdAt,
                        @NonNull Instant updatedAt) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.amountMinor = amountMinor;
        this.currency = Objects.requireNonNull(currency, "currency must not be null");
        this.billingCycle = Objects.requireNonNull(billingCycle, "billingCycle must not be null");
        this.startDate = Objects.requireNonNull(startDate, "startDate must not be null");
        this.nextDueDate = Objects.requireNonNull(nextDueDate, "nextDueDate must not be null");
        this.categoryId = categoryId;
        this.isTrial = isTrial;
        this.trialEndDate = trialEndDate;
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.lastNotifiedDue = lastNotifiedDue;
        this.notes = notes;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt must not be null");
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

    public long getAmountMinor() {
        return amountMinor;
    }

    public void setAmountMinor(long amountMinor) {
        this.amountMinor = amountMinor;
    }

    @NonNull
    public String getCurrency() {
        return currency;
    }

    public void setCurrency(@NonNull String currency) {
        this.currency = Objects.requireNonNull(currency, "currency must not be null");
    }

    @NonNull
    public BillingCycle getBillingCycle() {
        return billingCycle;
    }

    public void setBillingCycle(@NonNull BillingCycle billingCycle) {
        this.billingCycle = Objects.requireNonNull(billingCycle, "billingCycle must not be null");
    }

    @NonNull
    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(@NonNull LocalDate startDate) {
        this.startDate = Objects.requireNonNull(startDate, "startDate must not be null");
    }

    @NonNull
    public LocalDate getNextDueDate() {
        return nextDueDate;
    }

    public void setNextDueDate(@NonNull LocalDate nextDueDate) {
        this.nextDueDate = Objects.requireNonNull(nextDueDate, "nextDueDate must not be null");
    }

    @Nullable
    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(@Nullable Long categoryId) {
        this.categoryId = categoryId;
    }

    public boolean isTrial() {
        return isTrial;
    }

    public void setTrial(boolean trial) {
        isTrial = trial;
    }

    @Nullable
    public LocalDate getTrialEndDate() {
        return trialEndDate;
    }

    public void setTrialEndDate(@Nullable LocalDate trialEndDate) {
        this.trialEndDate = trialEndDate;
    }

    @NonNull
    public SubscriptionStatus getStatus() {
        return status;
    }

    public void setStatus(@NonNull SubscriptionStatus status) {
        this.status = Objects.requireNonNull(status, "status must not be null");
    }

    @Nullable
    public LocalDate getLastNotifiedDue() {
        return lastNotifiedDue;
    }

    public void setLastNotifiedDue(@Nullable LocalDate lastNotifiedDue) {
        this.lastNotifiedDue = lastNotifiedDue;
    }

    @Nullable
    public String getNotes() {
        return notes;
    }

    public void setNotes(@Nullable String notes) {
        this.notes = notes;
    }

    @NonNull
    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(@NonNull Instant createdAt) {
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
    }

    @NonNull
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(@NonNull Instant updatedAt) {
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt must not be null");
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Subscription that = (Subscription) o;
        return id == that.id &&
                amountMinor == that.amountMinor &&
                isTrial == that.isTrial &&
                name.equals(that.name) &&
                currency.equals(that.currency) &&
                billingCycle == that.billingCycle &&
                startDate.equals(that.startDate) &&
                nextDueDate.equals(that.nextDueDate) &&
                Objects.equals(categoryId, that.categoryId) &&
                Objects.equals(trialEndDate, that.trialEndDate) &&
                status == that.status &&
                Objects.equals(lastNotifiedDue, that.lastNotifiedDue) &&
                Objects.equals(notes, that.notes) &&
                createdAt.equals(that.createdAt) &&
                updatedAt.equals(that.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, amountMinor, currency, billingCycle, startDate,
                nextDueDate, categoryId, isTrial, trialEndDate, status, lastNotifiedDue,
                notes, createdAt, updatedAt);
    }

    @NonNull
    @Override
    public String toString() {
        return "Subscription{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", amountMinor=" + amountMinor +
                ", currency='" + currency + '\'' +
                ", billingCycle=" + billingCycle +
                ", startDate=" + startDate +
                ", nextDueDate=" + nextDueDate +
                ", categoryId=" + categoryId +
                ", isTrial=" + isTrial +
                ", trialEndDate=" + trialEndDate +
                ", status=" + status +
                ", lastNotifiedDue=" + lastNotifiedDue +
                ", notes='" + notes + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    public static class Builder {
        private long id;
        private String name;
        private long amountMinor;
        private String currency = "EUR";
        private BillingCycle billingCycle = BillingCycle.MONTHLY;
        private LocalDate startDate;
        private LocalDate nextDueDate;
        private Long categoryId;
        private boolean isTrial;
        private LocalDate trialEndDate;
        private SubscriptionStatus status = SubscriptionStatus.ACTIVE;
        private LocalDate lastNotifiedDue;
        private String notes;
        private Instant createdAt;
        private Instant updatedAt;

        public Builder() {
        }

        public Builder(Subscription subscription) {
            this.id = subscription.id;
            this.name = subscription.name;
            this.amountMinor = subscription.amountMinor;
            this.currency = subscription.currency;
            this.billingCycle = subscription.billingCycle;
            this.startDate = subscription.startDate;
            this.nextDueDate = subscription.nextDueDate;
            this.categoryId = subscription.categoryId;
            this.isTrial = subscription.isTrial;
            this.trialEndDate = subscription.trialEndDate;
            this.status = subscription.status;
            this.lastNotifiedDue = subscription.lastNotifiedDue;
            this.notes = subscription.notes;
            this.createdAt = subscription.createdAt;
            this.updatedAt = subscription.updatedAt;
        }

        public Builder id(long id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder amountMinor(long amountMinor) {
            this.amountMinor = amountMinor;
            return this;
        }

        public Builder currency(String currency) {
            this.currency = currency;
            return this;
        }

        public Builder billingCycle(BillingCycle billingCycle) {
            this.billingCycle = billingCycle;
            return this;
        }

        public Builder startDate(LocalDate startDate) {
            this.startDate = startDate;
            return this;
        }

        public Builder nextDueDate(LocalDate nextDueDate) {
            this.nextDueDate = nextDueDate;
            return this;
        }

        public Builder categoryId(Long categoryId) {
            this.categoryId = categoryId;
            return this;
        }

        public Builder isTrial(boolean isTrial) {
            this.isTrial = isTrial;
            return this;
        }

        public Builder trialEndDate(LocalDate trialEndDate) {
            this.trialEndDate = trialEndDate;
            return this;
        }

        public Builder status(SubscriptionStatus status) {
            this.status = status;
            return this;
        }

        public Builder lastNotifiedDue(LocalDate lastNotifiedDue) {
            this.lastNotifiedDue = lastNotifiedDue;
            return this;
        }

        public Builder notes(String notes) {
            this.notes = notes;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Subscription build() {
            Instant now = Instant.now();
            if (createdAt == null) createdAt = now;
            if (updatedAt == null) updatedAt = now;
            Subscription subscription = new Subscription(
                    name, amountMinor, currency, billingCycle, startDate,
                    nextDueDate, categoryId, isTrial, trialEndDate, status,
                    lastNotifiedDue, notes, createdAt, updatedAt
            );
            subscription.setId(id);
            return subscription;
        }
    }
}
