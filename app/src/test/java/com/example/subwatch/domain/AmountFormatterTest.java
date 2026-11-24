package com.example.subwatch.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class AmountFormatterTest {

    @Test
    public void parseToMinorUnits_eur() {
        assertEquals(1599L, AmountFormatter.parseToMinorUnits("15,99", "EUR"));
        assertEquals(1599L, AmountFormatter.parseToMinorUnits("15.99", "EUR"));
        assertEquals(1000L, AmountFormatter.parseToMinorUnits("10", "EUR"));
        assertEquals(1050L, AmountFormatter.parseToMinorUnits("10.5", "EUR"));
        assertEquals(6L, AmountFormatter.parseToMinorUnits("0,06", "EUR"));
    }

    @Test
    public void parseToMinorUnits_jpy_zeroDecimals() {
        assertEquals(1000L, AmountFormatter.parseToMinorUnits("1000", "JPY"));
        assertEquals(500L, AmountFormatter.parseToMinorUnits("500", "JPY"));
    }

    @Test
    public void parseToMinorUnits_kwd_threeDecimals() {
        assertEquals(12500L, AmountFormatter.parseToMinorUnits("12,500", "KWD"));
        assertEquals(12500L, AmountFormatter.parseToMinorUnits("12.5", "KWD"));
    }

    @Test
    public void parseToMinorUnits_invalidInputs() {
        assertThrows(IllegalArgumentException.class, () -> AmountFormatter.parseToMinorUnits("", "EUR"));
        assertThrows(IllegalArgumentException.class, () -> AmountFormatter.parseToMinorUnits("0", "EUR"));
        assertThrows(IllegalArgumentException.class, () -> AmountFormatter.parseToMinorUnits("-5", "EUR"));
        assertThrows(IllegalArgumentException.class, () -> AmountFormatter.parseToMinorUnits("abc", "EUR"));
        // Excess precision for EUR (3 decimals where 2 allowed)
        assertThrows(IllegalArgumentException.class, () -> AmountFormatter.parseToMinorUnits("15.999", "EUR"));
    }

    @Test
    public void formatToDecimalString() {
        assertEquals("15,99", AmountFormatter.formatToDecimalString(1599L, "EUR"));
        assertEquals("10,00", AmountFormatter.formatToDecimalString(1000L, "EUR"));
        assertEquals("1000", AmountFormatter.formatToDecimalString(1000L, "JPY"));
        assertEquals("12,500", AmountFormatter.formatToDecimalString(12500L, "KWD"));
    }

    @Test
    public void formatForDisplay() {
        String eurDisplay = AmountFormatter.formatForDisplay(1599L, "EUR");
        assertTrue(eurDisplay.contains("15,99"));
        assertTrue(eurDisplay.contains("€"));

        String jpyDisplay = AmountFormatter.formatForDisplay(1000L, "JPY");
        assertTrue(jpyDisplay.contains("1"));
        assertTrue(jpyDisplay.contains("000"));
    }
}
