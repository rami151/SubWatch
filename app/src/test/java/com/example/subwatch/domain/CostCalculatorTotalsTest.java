package com.example.subwatch.domain;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parameterized tests for subscription totals (T-A through T-D) from docs/billing-cases.md §3.
 * Verifies that totals are summed on exact annual values before rounding, separated by currency,
 * and trials are excluded from totals (D1).
 * Uses {@link CostCalculator}.
 */
@RunWith(Parameterized.class)
public class CostCalculatorTotalsTest {

    private final String id;
    private final List<CostCalculator.Subscription> subscriptions;
    private final Map<String, CostCalculator.Totals> expectedTotals;

    public CostCalculatorTotalsTest(String id,
                                    List<CostCalculator.Subscription> subscriptions,
                                    Map<String, CostCalculator.Totals> expectedTotals) {
        this.id = id;
        this.subscriptions = subscriptions;
        this.expectedTotals = expectedTotals;
    }

    @Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        // T-A: EUR monthly: 15.99, 5.99, 1.99 -> monthly 23.97, annual 287.64
        Map<String, CostCalculator.Totals> expectedTA = new HashMap<>();
        expectedTA.put("EUR", new CostCalculator.Totals(
            new BigDecimal("23.97"),
            new BigDecimal("287.64")
        ));

        // T-B: EUR yearly: 100.00 and 100.00 -> monthly 16.67, annual 200.00
        Map<String, CostCalculator.Totals> expectedTB = new HashMap<>();
        expectedTB.put("EUR", new CostCalculator.Totals(
            new BigDecimal("16.67"),
            new BigDecimal("200.00")
        ));

        // T-C: EUR monthly 15.99 and USD monthly 10.00 -> two separate totals
        Map<String, CostCalculator.Totals> expectedTC = new HashMap<>();
        expectedTC.put("EUR", new CostCalculator.Totals(
            new BigDecimal("15.99"),
            new BigDecimal("191.88")
        ));
        expectedTC.put("USD", new CostCalculator.Totals(
            new BigDecimal("10.00"),
            new BigDecimal("120.00")
        ));

        // T-D: EUR monthly 15.99, 5.99, 1.99 plus Duolingo 12.99 in trial -> trials excluded
        Map<String, CostCalculator.Totals> expectedTD = new HashMap<>();
        expectedTD.put("EUR", new CostCalculator.Totals(
            new BigDecimal("23.97"),
            new BigDecimal("287.64")
        ));

        return Arrays.asList(new Object[][] {
            {
                "T-A",
                Arrays.asList(
                    new CostCalculator.Subscription("EUR", 1599L, BillingCycle.MONTHLY),
                    new CostCalculator.Subscription("EUR", 599L, BillingCycle.MONTHLY),
                    new CostCalculator.Subscription("EUR", 199L, BillingCycle.MONTHLY)
                ),
                expectedTA
            },
            {
                "T-B",
                Arrays.asList(
                    new CostCalculator.Subscription("EUR", 10000L, BillingCycle.YEARLY),
                    new CostCalculator.Subscription("EUR", 10000L, BillingCycle.YEARLY)
                ),
                expectedTB
            },
            {
                "T-C",
                Arrays.asList(
                    new CostCalculator.Subscription("EUR", 1599L, BillingCycle.MONTHLY),
                    new CostCalculator.Subscription("USD", 1000L, BillingCycle.MONTHLY)
                ),
                expectedTC
            },
            {
                "T-D",
                Arrays.asList(
                    new CostCalculator.Subscription("EUR", 1599L, BillingCycle.MONTHLY),
                    new CostCalculator.Subscription("EUR", 599L, BillingCycle.MONTHLY),
                    new CostCalculator.Subscription("EUR", 199L, BillingCycle.MONTHLY),
                    new CostCalculator.Subscription("EUR", 1299L, BillingCycle.MONTHLY, true)
                ),
                expectedTD
            }
        });
    }

    @Test
    public void calculateTotals() {
        CostCalculator calc = new CostCalculator();
        Map<String, CostCalculator.Totals> actual = calc.calculateTotals(subscriptions);
        assertEquals("Case " + id + " totals mismatch", expectedTotals, actual);
    }
}
