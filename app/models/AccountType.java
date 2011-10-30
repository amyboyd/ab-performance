package models;

import com.abperf.MoneyUtils;
import java.math.BigDecimal;

public enum AccountType {
    BETA() {
        {
            price = MoneyUtils.ZERO;
        }
    },
    FREE() {
        {
            price = MoneyUtils.ZERO;
        }
    },
    MONTHLY() {
        {
            price = new BigDecimal("10.00");
            paid = true;
            monthly = true;
            timePeriod = "month";
        }
    },
    YEARLY() {
        {
            price = new BigDecimal("100.00");
            paid = true;
            yearly = true;
            timePeriod = "year";
        }
    };

    /**
     * Total price in US dollars.
     */
    public BigDecimal price;

    public boolean paid;

    public boolean monthly;

    public boolean yearly;

    public String timePeriod;

    /**
     * In text like "less than $6 per month", the number 6 comes from here.
     */
    public static int getYearlyPriceAsPerMonth() {
        return (int) java.lang.Math.ceil(YEARLY.price.floatValue() / 12);
    }

    /**
     * In text like "save $2.30 per month by paying yearly", the number 2 comes from here.
     */
    public static BigDecimal getSavingsPerMonthAsYearlyAccountInsteadOfMonthlyAccount() {
        return MONTHLY.price.subtract(YEARLY.price.divide(new BigDecimal("12.00"))).setScale(2);
    }
}
