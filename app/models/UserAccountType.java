package models;

import com.abperf.MoneyUtils;
import java.math.BigDecimal;

public enum UserAccountType {
    BETA_FREE() {
        {
            price = MoneyUtils.ZERO;
            pageViewQuota = 1000;
        }
    },
    BETA_10000() {
        {
            price = MoneyUtils.ONE_HUNDRED;
            paid = true;
            pageViewQuota = 10000;
        }
    };

    /**
     * Total price in US dollars.
     */
    public BigDecimal price;

    public boolean paid;

    public int pageViewQuota;
}
