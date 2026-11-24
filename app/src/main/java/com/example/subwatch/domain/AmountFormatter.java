package com.example.subwatch.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Currency;
import java.util.Locale;

/**
 * Utility for parsing and formatting monetary amounts in minor units (RG8).
 */
public final class AmountFormatter {

    private AmountFormatter() {
    }

    /**
     * Returns the number of decimal digits for the given ISO currency code.
     * Defaults to 2 if unknown or unsupported.
     */
    public static int getFractionDigits(String currencyCode) {
        if (currencyCode == null || currencyCode.trim().isEmpty()) {
            return 2;
        }
        try {
            Currency currency = Currency.getInstance(currencyCode);
            int digits = currency.getDefaultFractionDigits();
            return digits >= 0 ? digits : 2;
        } catch (IllegalArgumentException e) {
            return 2;
        }
    }

    /**
     * Parses a string input (e.g., "15,99" or "15.99") into minor currency units.
     *
     * @param amountStr    the amount string in major currency units
     * @param currencyCode ISO 4217 currency code
     * @return the amount in minor units (e.g., 1599 for 15.99 EUR)
     * @throws IllegalArgumentException if the format is invalid, negative, or exceeds currency precision
     */
    public static long parseToMinorUnits(String amountStr, String currencyCode) {
        if (amountStr == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        String clean = amountStr.trim().replace(',', '.').replace(" ", "");
        if (clean.isEmpty()) {
            throw new IllegalArgumentException("Amount cannot be empty");
        }

        BigDecimal value;
        try {
            value = new BigDecimal(clean);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid amount format: " + amountStr, e);
        }

        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be strictly greater than 0");
        }

        int fractionDigits = getFractionDigits(currencyCode);
        BigDecimal scaled = value.movePointRight(fractionDigits);

        // Check if there are remaining fractional digits that would be truncated
        if (scaled.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalArgumentException(
                    "Amount has more decimal places than allowed for currency " + currencyCode
            );
        }

        return scaled.longValueExact();
    }

    /**
     * Formats minor currency units into a decimal input string (e.g., "15,99").
     */
    public static String formatToDecimalString(long amountMinor, String currencyCode) {
        int fractionDigits = getFractionDigits(currencyCode);
        BigDecimal value = BigDecimal.valueOf(amountMinor, fractionDigits);
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.FRANCE);
        symbols.setDecimalSeparator(',');

        DecimalFormat df = new DecimalFormat();
        df.setDecimalFormatSymbols(symbols);
        df.setGroupingUsed(false);
        df.setMinimumFractionDigits(fractionDigits);
        df.setMaximumFractionDigits(fractionDigits);
        return df.format(value);
    }

    /**
     * Formats minor currency units for UI display with currency symbol (e.g., "15,99 €").
     */
    public static String formatForDisplay(long amountMinor, String currencyCode) {
        int fractionDigits = getFractionDigits(currencyCode);
        BigDecimal value = BigDecimal.valueOf(amountMinor, fractionDigits);

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.FRANCE);
        symbols.setDecimalSeparator(',');
        symbols.setGroupingSeparator('\u00A0'); // non-breaking space

        DecimalFormat df = new DecimalFormat();
        df.setDecimalFormatSymbols(symbols);
        df.setGroupingUsed(true);
        df.setMinimumFractionDigits(fractionDigits);
        df.setMaximumFractionDigits(fractionDigits);

        String numberStr = df.format(value);
        String symbol;
        try {
            symbol = Currency.getInstance(currencyCode).getSymbol(Locale.FRANCE);
        } catch (Exception e) {
            symbol = currencyCode;
        }

        return numberStr + "\u00A0" + symbol;
    }
}
