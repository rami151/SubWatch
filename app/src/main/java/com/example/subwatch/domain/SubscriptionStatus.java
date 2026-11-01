package com.example.subwatch.domain;

/**
 * Status of a subscription.
 * An active subscription is renewed according to its cycle.
 * An archived subscription is cancelled/inactive and excluded from totals,
 * statistics, and reminders (RG5).
 */
public enum SubscriptionStatus {
    ACTIVE,
    ARCHIVED
}
