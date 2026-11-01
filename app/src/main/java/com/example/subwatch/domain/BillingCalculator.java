package com.example.subwatch.domain;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Pure-Java due-date calculator for SubWatch.
 * <p>
 * Computes billing occurrences from an anchor date and a cycle, following RG1 and RG2:
 * <ul>
 *   <li>Occurrence n = anchor + n cycles (never chained from the previous date).</li>
 *   <li>If the anchor day does not exist in the target month, use the last day of that month;
 *       the anchor day itself never changes (RG2).</li>
 * </ul>
 * "Today" is derived from the injected {@link Clock}, so tests can fix it.
 */
public class BillingCalculator {

    private final Clock clock;

    public BillingCalculator(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock cannot be null");
    }

    /**
     * Returns the occurrence at index {@code n} (0-based) for the given anchor and cycle.
     * Occurrence n = anchor + n cycles, always computed from the anchor.
     *
     * @param anchor the anchor date (start date, or trial end date for trials)
     * @param cycle  the billing cycle
     * @param n      the 0-based occurrence index
     * @return the date of occurrence n
     */
    public LocalDate occurrence(LocalDate anchor, BillingCycle cycle, int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Occurrence index n must be non-negative: " + n);
        }
        switch (cycle) {
            case WEEKLY:
                return anchor.plusWeeks(n);
            case MONTHLY:
                return anchor.plusMonths(n);
            case QUARTERLY:
                return anchor.plusMonths(n * 3L);
            case YEARLY:
                return anchor.plusYears(n);
            default:
                throw new IllegalArgumentException("Unknown cycle: " + cycle);
        }
    }

    /**
     * Returns a list of occurrences from index 0 to {@code count - 1}.
     *
     * @param anchor the anchor date
     * @param cycle  the billing cycle
     * @param count  the number of occurrences to generate
     * @return unmodifiable list of occurrence dates
     */
    public List<LocalDate> occurrenceSequence(LocalDate anchor, BillingCycle cycle, int count) {
        if (count < 0) {
            throw new IllegalArgumentException("count must be non-negative: " + count);
        }
        List<LocalDate> sequence = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            sequence.add(occurrence(anchor, cycle, i));
        }
        return sequence;
    }

    /**
     * Returns the next due date on or after today for a non-trial subscription.
     * Next due = the first occurrence on or after today, so n starts at 0 (RG1).
     *
     * @param anchor the subscription start date
     * @param cycle  the billing cycle
     * @return the first occurrence >= today
     */
    public LocalDate nextDueDate(LocalDate anchor, BillingCycle cycle) {
        LocalDate today = LocalDate.now(clock);
        // If the anchor is in the future, occurrence 0 is the anchor itself
        if (!anchor.isBefore(today)) {
            return anchor;
        }

        // Start searching near the expected index to avoid iterating from 0
        int n = estimateStartIndex(anchor, cycle, today);
        while (true) {
            LocalDate candidate = occurrence(anchor, cycle, n);
            if (!candidate.isBefore(today)) {
                return candidate;
            }
            n++;
        }
    }

    /**
     * Returns the next due date considering a trial period.
     * The trial end date serves as the anchor for billing (RG1, F8):
     * if today is on or before trialEnd, the next due date is trialEnd;
     * after trialEnd, normal billing resumes from that anchor.
     *
     * @param trialEnd the date the trial ends (becomes the billing anchor)
     * @param cycle    the billing cycle
     * @return the next due date
     */
    public LocalDate nextDueDateWithTrial(LocalDate trialEnd, BillingCycle cycle) {
        return nextDueDate(trialEnd, cycle);
    }

    /**
     * Estimates a starting index near the expected next occurrence to avoid
     * iterating from 0 for anchors far in the past.
     */
    private int estimateStartIndex(LocalDate anchor, BillingCycle cycle, LocalDate today) {
        long estimate;
        switch (cycle) {
            case WEEKLY:
                estimate = ChronoUnit.WEEKS.between(anchor, today);
                break;
            case MONTHLY:
                estimate = ChronoUnit.MONTHS.between(anchor, today);
                break;
            case QUARTERLY:
                estimate = ChronoUnit.MONTHS.between(anchor, today) / 3;
                break;
            case YEARLY:
                estimate = ChronoUnit.YEARS.between(anchor, today);
                break;
            default:
                estimate = 0;
        }
        // Back up by 2 to account for end-of-month clamping effects
        return (int) Math.max(0, estimate - 2);
    }
}
