package com.abperf;

import java.math.BigDecimal;

public class MoneyUtils {
    /**
     * Exactly "0.00".
     */
    public static final BigDecimal ZERO = new BigDecimal("0.00");

    public static final BigDecimal ONE_HUNDRED = new BigDecimal("100.00");

    public static BigDecimal percentOf(final BigDecimal fullAmount, final int percentToKeep) {
        return fullAmount.divide(ONE_HUNDRED).multiply(new BigDecimal(percentToKeep));
    }

    /**
     * @param amount Must not be null.
     * @return If "amount" is less than or equal to 0, returns 0. Else returns "amount".
     */
    public static BigDecimal dontAllowBelowZero(BigDecimal amount) {
        return (amount.compareTo(MoneyUtils.ZERO) == 1 ? amount : MoneyUtils.ZERO);
    }

    private MoneyUtils() {
    }
}
