package com.datech.mvp.service;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TaxCalculatorTest {

    private final TaxCalculator taxCalculator = new TaxCalculator();

    @Test
    @DisplayName("Calculates 21% VAT correctly for subtotal 1000.00")
    void calculatesTaxCorrectly() {
        BigDecimal subtotal = new BigDecimal("1000.00");
        BigDecimal taxRate = new BigDecimal("21");

        BigDecimal result = taxCalculator.calculateTax(subtotal, taxRate);

        assertEquals(new BigDecimal("210.00"), result);
    }

    @Test
    @DisplayName("Calculates final total correctly for subtotal 1000.00 and VAT 21%")
    void calculatesTotalCorrectly() {
        BigDecimal subtotal = new BigDecimal("1000.00");
        BigDecimal taxRate = new BigDecimal("21");

        BigDecimal result = taxCalculator.calculateTotal(subtotal, taxRate);

        assertEquals(new BigDecimal("1210.00"), result);
    }

    @Test
    @DisplayName("Returns zero tax when VAT rate is 0%")
    void returnsZeroTaxWhenRateIsZero() {
        BigDecimal subtotal = new BigDecimal("1000.00");
        BigDecimal taxRate = new BigDecimal("0");

        BigDecimal result = taxCalculator.calculateTax(subtotal, taxRate);

        assertEquals(new BigDecimal("0.00"), result);
    }

    @Test
    @DisplayName("Returns subtotal unchanged when VAT rate is 0%")
    void returnsSubtotalWhenRateIsZero() {
        BigDecimal subtotal = new BigDecimal("1000.00");
        BigDecimal taxRate = new BigDecimal("0");

        BigDecimal result = taxCalculator.calculateTotal(subtotal, taxRate);

        assertEquals(new BigDecimal("1000.00"), result);
    }

    @Test
    @DisplayName("Calculates 100% VAT correctly")
    void calculatesTaxCorrectlyForOneHundredPercent() {
        BigDecimal subtotal = new BigDecimal("250.00");
        BigDecimal taxRate = new BigDecimal("100");

        BigDecimal result = taxCalculator.calculateTax(subtotal, taxRate);

        assertEquals(new BigDecimal("250.00"), result);
    }

    @Test
    @DisplayName("Calculates total correctly for 100% VAT")
    void calculatesTotalCorrectlyForOneHundredPercent() {
        BigDecimal subtotal = new BigDecimal("250.00");
        BigDecimal taxRate = new BigDecimal("100");

        BigDecimal result = taxCalculator.calculateTotal(subtotal, taxRate);

        assertEquals(new BigDecimal("500.00"), result);
    }

    @Test
    @DisplayName("Rejects negative subtotal")
    void rejectsNegativeSubtotal() {
        BigDecimal subtotal = new BigDecimal("-1.00");
        BigDecimal taxRate = new BigDecimal("21");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> taxCalculator.calculateTax(subtotal, taxRate)
        );

        assertEquals("Subtotal must be >= 0", exception.getMessage());
    }

    @Test
    @DisplayName("Rejects negative VAT rate")
    void rejectsNegativeTaxRate() {
        BigDecimal subtotal = new BigDecimal("1000.00");
        BigDecimal taxRate = new BigDecimal("-5");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> taxCalculator.calculateTax(subtotal, taxRate)
        );

        assertEquals("Tax rate must be between 0 and 100", exception.getMessage());
    }

    @Test
    @DisplayName("Rejects VAT rate greater than 100")
    void rejectsTaxRateGreaterThanOneHundred() {
        BigDecimal subtotal = new BigDecimal("1000.00");
        BigDecimal taxRate = new BigDecimal("101");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> taxCalculator.calculateTax(subtotal, taxRate)
        );

        assertEquals("Tax rate must be between 0 and 100", exception.getMessage());
    }

    @Test
    @DisplayName("Rounds tax amount to two decimal places")
    void roundsTaxToTwoDecimalPlaces() {
        BigDecimal subtotal = new BigDecimal("999.99");
        BigDecimal taxRate = new BigDecimal("21");

        BigDecimal result = taxCalculator.calculateTax(subtotal, taxRate);

        assertEquals(new BigDecimal("210.00"), result);
    }

    @Test
    @DisplayName("Rounds total amount to two decimal places")
    void roundsTotalToTwoDecimalPlaces() {
        BigDecimal subtotal = new BigDecimal("999.99");
        BigDecimal taxRate = new BigDecimal("21");

        BigDecimal result = taxCalculator.calculateTotal(subtotal, taxRate);

        assertEquals(new BigDecimal("1209.99"), result);
    }

    @Test
    @DisplayName("Handles zero subtotal correctly")
    void handlesZeroSubtotalCorrectly() {
        BigDecimal subtotal = new BigDecimal("0.00");
        BigDecimal taxRate = new BigDecimal("21");

        BigDecimal tax = taxCalculator.calculateTax(subtotal, taxRate);
        BigDecimal total = taxCalculator.calculateTotal(subtotal, taxRate);

        assertEquals(new BigDecimal("0.00"), tax);
        assertEquals(new BigDecimal("0.00"), total);
    }
}
