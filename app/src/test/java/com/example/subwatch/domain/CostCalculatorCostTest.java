package com.example.subwatch.domain;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;

/**
 * Parameterized tests for cost calculations (C01–C11) from docs/billing-cases.md §2.
 * Tests exact annual calculation in minor units, annual formatted cost, and monthly equivalent.
 * Uses {@link CostCalculator}.
 */
@RunWith(Parameterized.class)
public class CostCalculatorCostTest {

    private final String id;
    private final String currency;
    private final int decimals;
    private final BillingCycle cycle;
    private final long storedAmountMinorUnits;
    private final long expectedAnnualMinorUnits;
    private final BigDecimal expectedAnnual;
    private final BigDecimal expectedMonthlyEquivalent;

    public CostCalculatorCostTest(String id, String currency, int decimals, BillingCycle cycle,
                                  long storedAmountMinorUnits, long expectedAnnualMinorUnits,
                                  String expectedAnnual, String expectedMonthlyEquivalent) {
        this.id = id;
        this.currency = currency;
        this.decimals = decimals;
        this.cycle = cycle;
        this.storedAmountMinorUnits = storedAmountMinorUnits;
        this.expectedAnnualMinorUnits = expectedAnnualMinorUnits;
        this.expectedAnnual = new BigDecimal(expectedAnnual);
        this.expectedMonthlyEquivalent = new BigDecimal(expectedMonthlyEquivalent);
    }

    @Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            // ID, Currency, Decimals, Cycle, Stored, Expected Annual (minor), Expected Annual, Expected Monthly
            { "C01", "EUR", 2, BillingCycle.MONTHLY,   1599L,  19188L,  "191.88",  "15.99" },
            { "C02", "EUR", 2, BillingCycle.WEEKLY,    1000L,  52000L,  "520.00",  "43.33" },
            { "C03", "EUR", 2, BillingCycle.WEEKLY,    999L,   51948L,  "519.48",  "43.29" },
            { "C04", "EUR", 2, BillingCycle.QUARTERLY, 3000L,  12000L,  "120.00",  "10.00" },
            { "C05", "EUR", 2, BillingCycle.QUARTERLY, 1000L,  4000L,   "40.00",   "3.33"  },
            { "C06", "EUR", 2, BillingCycle.YEARLY,    12000L, 12000L,  "120.00",  "10.00" },
            { "C07", "EUR", 2, BillingCycle.YEARLY,    9999L,  9999L,   "99.99",   "8.33"  },
            { "C08", "EUR", 2, BillingCycle.YEARLY,    10000L, 10000L,  "100.00",  "8.33"  },
            { "C09", "JPY", 0, BillingCycle.MONTHLY,   1000L,  12000L,  "12000",   "1000"  },
            { "C10", "KWD", 3, BillingCycle.MONTHLY,   12500L, 150000L, "150.000", "12.500"},
            { "C11", "EUR", 2, BillingCycle.YEARLY,    6L,     6L,      "0.06",    "0.01"  },
        });
    }

    @Test
    public void annualCostMinorUnits() {
        CostCalculator calc = new CostCalculator();
        long actual = calc.annualCostMinorUnits(storedAmountMinorUnits, cycle);
        assertEquals("Case " + id + " annual minor units mismatch",
                expectedAnnualMinorUnits, actual);
    }

    @Test
    public void annualCost() {
        CostCalculator calc = new CostCalculator();
        BigDecimal actual = calc.annualCost(storedAmountMinorUnits, cycle, decimals);
        assertEquals("Case " + id + " annual cost mismatch", expectedAnnual, actual);
    }

    @Test
    public void monthlyEquivalent() {
        CostCalculator calc = new CostCalculator();
        BigDecimal actual = calc.monthlyEquivalent(storedAmountMinorUnits, cycle, decimals);
        assertEquals("Case " + id + " monthly equivalent mismatch",
                expectedMonthlyEquivalent, actual);
    }
}
