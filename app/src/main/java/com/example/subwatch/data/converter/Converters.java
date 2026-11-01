package com.example.subwatch.data.converter;

import androidx.annotation.Nullable;
import androidx.room.TypeConverter;

import com.example.subwatch.domain.BillingCycle;
import com.example.subwatch.domain.SubscriptionStatus;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Room TypeConverters for java.time types and domain enums.
 * Dates are persisted as ISO-8601 strings (yyyy-MM-dd) per cahier 8.3.
 */
public final class Converters {

    @TypeConverter
    @Nullable
    public static String fromLocalDate(@Nullable LocalDate date) {
        return date != null ? date.toString() : null;
    }

    @TypeConverter
    @Nullable
    public static LocalDate toLocalDate(@Nullable String value) {
        return value != null && !value.isEmpty() ? LocalDate.parse(value) : null;
    }

    @TypeConverter
    @Nullable
    public static String fromBillingCycle(@Nullable BillingCycle cycle) {
        return cycle != null ? cycle.name() : null;
    }

    @TypeConverter
    @Nullable
    public static BillingCycle toBillingCycle(@Nullable String value) {
        return value != null && !value.isEmpty() ? BillingCycle.valueOf(value) : null;
    }

    @TypeConverter
    @Nullable
    public static String fromSubscriptionStatus(@Nullable SubscriptionStatus status) {
        return status != null ? status.name() : null;
    }

    @TypeConverter
    @Nullable
    public static SubscriptionStatus toSubscriptionStatus(@Nullable String value) {
        return value != null && !value.isEmpty() ? SubscriptionStatus.valueOf(value) : null;
    }

    @TypeConverter
    @Nullable
    public static String fromInstant(@Nullable Instant instant) {
        return instant != null ? instant.toString() : null;
    }

    @TypeConverter
    @Nullable
    public static Instant toInstant(@Nullable String value) {
        return value != null && !value.isEmpty() ? Instant.parse(value) : null;
    }
}
