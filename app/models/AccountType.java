package models;

import com.abperf.MoneyUtils;
import java.math.BigDecimal;

public enum AccountType {
    BETA_FREE() {
        {
            price = MoneyUtils.ZERO;
            pageViewQuota = 1000;
        }
    },
    BETA_5000() {
        {
            price = MoneyUtils.ONE_HUNDRED;
            paid = true;
            pageViewQuota = 5000;
        }
    },
    UNLIMITED() {
        {
            price = MoneyUtils.ZERO;
            paid = false;
            pageViewQuota = Integer.MAX_VALUE;
        }
    };

    public BigDecimal price;

    public boolean paid;

    public int pageViewQuota;
}
