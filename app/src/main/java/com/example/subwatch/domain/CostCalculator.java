package com.example.subwatch.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Pure-Java cost calculator for SubWatch.
 * <p>
 * Computes annual costs, monthly equivalents, and per-currency totals
 * using exact integer arithmetic on minor currency units (RG3, RG8, D4).
 * <ul>
 *   <li>Annual cost in minor units = stored amount × cycles per year
 *       (weekly 52, monthly 12, quarterly 4, yearly 1).</li>
 *   <li>Monthly equivalent = annual / 12, rounded HALF_UP for display only.</li>
 *   <li>Totals are summed on exact annual values before rounding (D4).</li>
 *   <li>Trials are excluded from totals (D1).</li>
 *   <li>Totals are per-currency, never summed across currencies (RG4).</li>
 * </ul>
 */
public class CostCalculator {

    /**
     * Subscription item for total calculations.
     */
    public static class Subscription {
        private final String currency;
        private final long storedAmountMinorUnits;
        private final BillingCycle cycle;
        private final boolean isTrial;
        private final int decimalPlaces;

        public Subscription(String currency, long storedAmountMinorUnits, BillingCycle cycle,
                            boolean isTrial, int decimalPlaces) {
            this.currency = currency;
            this.storedAmountMinorUnits = storedAmountMinorUnits;
            this.cycle = cycle;
            this.isTrial = isTrial;
            this.decimalPlaces = decimalPlaces;
        }

        public Subscription(String currency, long storedAmountMinorUnits, BillingCycle cycle,
                            boolean isTrial) {
            this(currency, storedAmountMinorUnits, cycle, isTrial, 2);
        }

        public Subscription(String currency, long storedAmountMinorUnits, BillingCycle cycle) {
            this(currency, storedAmountMinorUnits, cycle, false, 2);
        }

        public String getCurrency() {
            return currency;
        }

        public long getStoredAmountMinorUnits() {
            return storedAmountMinorUnits;
        }

        public BillingCycle getCycle() {
            return cycle;
        }

        public boolean isTrial() {
            return isTrial;
        }

        public int getDecimalPlaces() {
            return decimalPlaces;
        }
    }

    /**
     * Monthly and annual totals for a single currency.
     */
    public static class Totals {
        private final BigDecimal monthly;
        private final BigDecimal annual;

        public Totals(BigDecimal monthly, BigDecimal annual) {
            this.monthly = monthly;
            this.annual = annual;
        }

        public BigDecimal getMonthly() {
            return monthly;
        }

        public BigDecimal getAnnual() {
            return annual;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Totals totals = (Totals) o;
            return Objects.equals(monthly, totals.monthly)
                    && Objects.equals(annual, totals.annual);
        }

        @Override
        public int hashCode() {
            return Objects.hash(monthly, annual);
        }

        @Override
        public String toString() {
            return "Totals{monthly=" + monthly + ", annual=" + annual + '}';
        }
    }

    /**
     * Returns the number of billing cycles per year for the given cycle.
     */
    static int cyclesPerYear(BillingCycle cycle) {
        switch (cycle) {
            case WEEKLY:    return 52;
            case MONTHLY:   return 12;
            case QUARTERLY: return 4;
            case YEARLY:    return 1;
            default:
                throw new IllegalArgumentException("Unknown cycle: " + cycle);
        }
    }

    /**
     * Computes the exact annual cost in minor units.
     * annual = storedAmount × cyclesPerYear.
     *
     * @param storedAmountMinorUnits the subscription amount in minor currency units
     * @param cycle                  the billing cycle
     * @return exact annual cost in minor units
     */
    public long annualCostMinorUnits(long storedAmountMinorUnits, BillingCycle cycle) {
        return storedAmountMinorUnits * cyclesPerYear(cycle);
    }

    /**
     * Computes the exact annual cost as a BigDecimal in major currency units.
     *
     * @param storedAmountMinorUnits the subscription amount in minor currency units
     * @param cycle                  the billing cycle
     * @param currencyDecimalPlaces  number of decimal places for the currency
     * @return exact annual cost as BigDecimal
     */
    public BigDecimal annualCost(long storedAmountMinorUnits, BillingCycle cycle,
                                 int currencyDecimalPlaces) {
        long annualMinor = annualCostMinorUnits(storedAmountMinorUnits, cycle);
        return BigDecimal.valueOf(annualMinor, currencyDecimalPlaces);
    }

    /**
     * Computes the monthly equivalent for display, rounded HALF_UP.
     * monthly = annualCost / 12, rounded to currencyDecimalPlaces.
     *
     * @param storedAmountMinorUnits the subscription amount in minor currency units
     * @param cycle                  the billing cycle
     * @param currencyDecimalPlaces  decimal places (e.g. 2 for EUR, 0 for JPY, 3 for KWD)
     * @return monthly equivalent, rounded for display
     */
    public BigDecimal monthlyEquivalent(long storedAmountMinorUnits, BillingCycle cycle,
                                        int currencyDecimalPlaces) {
        BigDecimal annual = annualCost(storedAmountMinorUnits, cycle, currencyDecimalPlaces);
        return annual.divide(BigDecimal.valueOf(12), currencyDecimalPlaces, RoundingMode.HALF_UP);
    }

    /**
     * Calculates totals grouped by currency. Trials are excluded (D1).
     * Totals are summed on exact annual minor-unit values before rounding (D4, RG4).
     *
     * @param subscriptions list of subscriptions
     * @return map of currency code to Totals (monthly and annual in major units)
     */
    public Map<String, Totals> calculateTotals(List<Subscription> subscriptions) {
        if (subscriptions == null) {
            return Collections.emptyMap();
        }

        // Group non-trial subscriptions by currency
        Map<String, List<Subscription>> byCurrency = new LinkedHashMap<>();
        for (Subscription sub : subscriptions) {
            if (sub.isTrial()) {
                continue;
            }
            byCurrency.computeIfAbsent(sub.getCurrency(), k -> new ArrayList<>()).add(sub);
        }

        // Sum exact annual minor units per currency, then round once
        Map<String, Totals> result = new LinkedHashMap<>();
        for (Map.Entry<String, List<Subscription>> entry : byCurrency.entrySet()) {
            String currency = entry.getKey();
            List<Subscription> subs = entry.getValue();

            long totalAnnualMinor = 0;
            int decimalPlaces = subs.get(0).getDecimalPlaces();

            for (Subscription sub : subs) {
                totalAnnualMinor += annualCostMinorUnits(
                        sub.getStoredAmountMinorUnits(), sub.getCycle());
            }

            BigDecimal annual = BigDecimal.valueOf(totalAnnualMinor, decimalPlaces);
            BigDecimal monthly = annual.divide(
                    BigDecimal.valueOf(12), decimalPlaces, RoundingMode.HALF_UP);

            result.put(currency, new Totals(monthly, annual));
        }
        return result;
    }
}
