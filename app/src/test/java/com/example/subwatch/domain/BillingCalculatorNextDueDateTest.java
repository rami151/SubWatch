package com.example.subwatch.domain;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collection;

/**
 * Parameterized tests for {@link BillingCalculator#nextDueDate} and
 * {@link BillingCalculator#nextDueDateWithTrial}.
 * <p>
 * Test cases D01–D25 from docs/billing-cases.md §1.
 */
@RunWith(Parameterized.class)
public class BillingCalculatorNextDueDateTest {

    private final String id;
    private final LocalDate anchor;
    private final BillingCycle cycle;
    private final LocalDate today;
    private final LocalDate expectedNextDue;
    private final boolean isTrial;

    public BillingCalculatorNextDueDateTest(String id, String anchor, BillingCycle cycle,
                                            String today, String expectedNextDue,
                                            boolean isTrial) {
        this.id = id;
        this.anchor = LocalDate.parse(anchor);
        this.cycle = cycle;
        this.today = LocalDate.parse(today);
        this.expectedNextDue = LocalDate.parse(expectedNextDue);
        this.isTrial = isTrial;
    }

    @Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            // { ID, Anchor, Cycle, Today, Expected next due, isTrial }

            // --- RG2: end-of-month clamping ---
            { "D01", "2026-01-31", BillingCycle.MONTHLY,   "2026-02-01", "2026-02-28", false },
            { "D02", "2026-01-31", BillingCycle.MONTHLY,   "2026-03-01", "2026-03-31", false },
            { "D03", "2026-01-31", BillingCycle.MONTHLY,   "2026-04-01", "2026-04-30", false },
            { "D04", "2028-01-31", BillingCycle.MONTHLY,   "2028-02-01", "2028-02-29", false },
            { "D05", "2028-02-29", BillingCycle.YEARLY,    "2028-03-01", "2029-02-28", false },
            { "D06", "2028-02-29", BillingCycle.YEARLY,    "2031-03-01", "2032-02-29", false },
            { "D07", "2026-01-31", BillingCycle.QUARTERLY, "2026-02-01", "2026-04-30", false },
            { "D08", "2026-01-31", BillingCycle.QUARTERLY, "2026-05-01", "2026-07-31", false },

            // --- RG1: weekly, boundary, today == anchor, future anchor ---
            { "D09", "2026-12-28", BillingCycle.WEEKLY,    "2026-12-29", "2027-01-04", false },
            { "D10", "2026-10-12", BillingCycle.MONTHLY,   "2026-11-12", "2026-11-12", false },
            { "D11", "2026-12-01", BillingCycle.MONTHLY,   "2026-10-15", "2026-12-01", false },
            { "D12", "2026-10-15", BillingCycle.MONTHLY,   "2026-10-15", "2026-10-15", false },
            { "D13", "2026-12-15", BillingCycle.MONTHLY,   "2026-12-16", "2027-01-15", false },

            // --- RG2: anchor day 30 and 29 ---
            { "D14", "2026-01-30", BillingCycle.MONTHLY,   "2026-02-01", "2026-02-28", false },
            { "D15", "2026-01-30", BillingCycle.MONTHLY,   "2026-03-01", "2026-03-30", false },
            { "D16", "2026-01-29", BillingCycle.MONTHLY,   "2026-02-01", "2026-02-28", false },
            { "D17", "2028-01-29", BillingCycle.MONTHLY,   "2028-02-01", "2028-02-29", false },
            { "D18", "2028-02-29", BillingCycle.MONTHLY,   "2028-03-01", "2028-03-29", false },

            // --- large n, yearly, quarterly edge ---
            { "D19", "2024-01-31", BillingCycle.MONTHLY,   "2026-10-15", "2026-10-31", false },
            { "D20", "2025-06-10", BillingCycle.YEARLY,    "2026-06-11", "2027-06-10", false },
            { "D21", "2026-11-30", BillingCycle.QUARTERLY, "2026-12-01", "2027-02-28", false },

            // --- trial cases ---
            { "D22", "2026-10-17", BillingCycle.MONTHLY,   "2026-10-15", "2026-10-17", true },
            { "D23", "2026-10-17", BillingCycle.MONTHLY,   "2026-10-18", "2026-11-17", true },

            // --- anchor day preservation ---
            { "D24", "2027-02-28", BillingCycle.YEARLY,    "2027-03-01", "2028-02-28", false },

            // --- weekly, large n ---
            { "D25", "2024-01-01", BillingCycle.WEEKLY,    "2026-10-15", "2026-10-19", false },
        });
    }

    private Clock fixedClock(LocalDate date) {
        return Clock.fixed(date.atStartOfDay(ZoneOffset.UTC).toInstant(), ZoneOffset.UTC);
    }

    @Test
    public void nextDueDate() {
        BillingCalculator calc = new BillingCalculator(fixedClock(today));

        LocalDate actual;
        if (isTrial) {
            actual = calc.nextDueDateWithTrial(anchor, cycle);
        } else {
            actual = calc.nextDueDate(anchor, cycle);
        }

        assertEquals("Case " + id + ": next due date mismatch", expectedNextDue, actual);
    }
}
