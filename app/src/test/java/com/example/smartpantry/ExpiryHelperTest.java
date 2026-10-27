package com.example.smartpantry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Date;

public class ExpiryHelperTest {

    private final Date today = ExpiryHelper.parse("2026-10-26");

    @Test
    public void validAndInvalidDates() {
        assertTrue(ExpiryHelper.isValidDate("2026-10-31"));
        assertFalse(ExpiryHelper.isValidDate("2026-02-31"));
        assertFalse(ExpiryHelper.isValidDate("31/10/2026"));
        assertFalse(ExpiryHelper.isValidDate(""));
        assertFalse(ExpiryHelper.isValidDate(null));
    }

    @Test
    public void daysUntilAndFlags() {
        assertEquals(Integer.valueOf(0), ExpiryHelper.daysUntil("2026-10-26", today));
        assertEquals(Integer.valueOf(3), ExpiryHelper.daysUntil("2026-10-29", today));
        assertEquals(Integer.valueOf(-1), ExpiryHelper.daysUntil("2026-10-25", today));
        assertNull(ExpiryHelper.daysUntil("", today));

        assertTrue(ExpiryHelper.isExpiringSoon("2026-10-28", today));
        assertFalse(ExpiryHelper.isExpiringSoon("2026-11-10", today));
        assertTrue(ExpiryHelper.isExpired("2026-10-20", today));
        assertFalse(ExpiryHelper.isExpired("2026-10-26", today));
    }

    @Test
    public void quantityFormatting() {
        assertEquals("2", Ingredient.formatQuantity(2.0));
        assertEquals("0.5", Ingredient.formatQuantity(0.5));
        assertEquals("1.25", Ingredient.formatQuantity(1.25));
    }
}
