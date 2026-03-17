package com.datech.mvp.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Service;

@Service
public class TaxCalculator {

    public BigDecimal calculateTax(BigDecimal subtotal, BigDecimal taxRate) {
        validate(subtotal, taxRate);

        BigDecimal taxAmount = subtotal.multiply(taxRate.divide(BigDecimal.valueOf(100)));
        return taxAmount.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateTotal(BigDecimal subtotal, BigDecimal taxRate) {
        BigDecimal tax = calculateTax(subtotal, taxRate);
        return subtotal.add(tax).setScale(2, RoundingMode.HALF_UP);
    }

    private void validate(BigDecimal subtotal, BigDecimal taxRate) {
        if (subtotal.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Subtotal must be >= 0");
        }
        if (taxRate.compareTo(BigDecimal.ZERO) < 0 || taxRate.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("Tax rate must be between 0 and 100");
        }
    }
}
