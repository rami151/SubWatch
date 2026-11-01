package com.example.subwatch.domain;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Parameterized tests for occurrence sequences (S1 and S2) from docs/billing-cases.md §1.
 * Verifies that occurrence n = anchor + n cycles is always computed from the anchor,
 * catching the "chained from the previous date" bug.
 */
@RunWith(Parameterized.class)
public class BillingCalculatorOccurrenceSequenceTest {

    private final String id;
    private final LocalDate anchor;
    private final BillingCycle cycle;
    private final List<LocalDate> expectedOccurrences;

    public BillingCalculatorOccurrenceSequenceTest(String id, String anchor, BillingCycle cycle,
                                                   List<String> expectedDates) {
        this.id = id;
        this.anchor = LocalDate.parse(anchor);
        this.cycle = cycle;
        this.expectedOccurrences = new ArrayList<>();
        for (String dateStr : expectedDates) {
            this.expectedOccurrences.add(LocalDate.parse(dateStr));
        }
    }

    @Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            {
                "S1",
                "2026-01-31",
                BillingCycle.MONTHLY,
                Arrays.asList(
                    "2026-01-31",
                    "2026-02-28",
                    "2026-03-31",
                    "2026-04-30",
                    "2026-05-31",
                    "2026-06-30"
                )
            },
            {
                "S2",
                "2028-02-29",
                BillingCycle.YEARLY,
                Arrays.asList(
                    "2028-02-29",
                    "2029-02-28",
                    "2030-02-28",
                    "2031-02-28",
                    "2032-02-29"
                )
            }
        });
    }

    @Test
    public void individualOccurrencesMatchAnchorCalculation() {
        BillingCalculator calc = new BillingCalculator(Clock.systemUTC());

        for (int n = 0; n < expectedOccurrences.size(); n++) {
            LocalDate expected = expectedOccurrences.get(n);
            LocalDate actual = calc.occurrence(anchor, cycle, n);
            assertEquals("Case " + id + " occurrence n=" + n + " mismatch", expected, actual);
        }
    }

    @Test
    public void occurrenceSequenceMatchesAnchorCalculation() {
        BillingCalculator calc = new BillingCalculator(Clock.systemUTC());

        List<LocalDate> actual = calc.occurrenceSequence(anchor, cycle, expectedOccurrences.size());
        assertEquals("Case " + id + " sequence mismatch", expectedOccurrences, actual);
    }
}
