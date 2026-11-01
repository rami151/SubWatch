package com.example.subwatch.data.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.example.subwatch.domain.BillingCycle;
import com.example.subwatch.domain.SubscriptionStatus;

import org.junit.Test;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Unit tests for Room TypeConverters.
 */
public class ConvertersTest {

    @Test
    public void testLocalDateConversion() {
        LocalDate date = LocalDate.of(2026, 11, 1);
        String formatted = Converters.fromLocalDate(date);
        assertEquals("2026-11-01", formatted);
        assertEquals(date, Converters.toLocalDate(formatted));

        assertNull(Converters.fromLocalDate(null));
        assertNull(Converters.toLocalDate(null));
        assertNull(Converters.toLocalDate(""));
    }

    @Test
    public void testBillingCycleConversion() {
        for (BillingCycle cycle : BillingCycle.values()) {
            String str = Converters.fromBillingCycle(cycle);
            assertEquals(cycle.name(), str);
            assertEquals(cycle, Converters.toBillingCycle(str));
        }

        assertNull(Converters.fromBillingCycle(null));
        assertNull(Converters.toBillingCycle(null));
        assertNull(Converters.toBillingCycle(""));
    }

    @Test
    public void testSubscriptionStatusConversion() {
        for (SubscriptionStatus status : SubscriptionStatus.values()) {
            String str = Converters.fromSubscriptionStatus(status);
            assertEquals(status.name(), str);
            assertEquals(status, Converters.toSubscriptionStatus(str));
        }

        assertNull(Converters.fromSubscriptionStatus(null));
        assertNull(Converters.toSubscriptionStatus(null));
        assertNull(Converters.toSubscriptionStatus(""));
    }

    @Test
    public void testInstantConversion() {
        Instant now = Instant.parse("2026-11-01T20:30:00Z");
        String formatted = Converters.fromInstant(now);
        assertEquals("2026-11-01T20:30:00Z", formatted);
        assertEquals(now, Converters.toInstant(formatted));

        assertNull(Converters.fromInstant(null));
        assertNull(Converters.toInstant(null));
        assertNull(Converters.toInstant(""));
    }
}
