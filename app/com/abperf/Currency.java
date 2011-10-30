package com.abperf;

import java.math.BigDecimal;
import play.templates.BaseTemplate.RawData;

public enum Currency {
    USD("$%s USD", "US dollars"),
    EUR("&euro;%s", "euros"),
    GBP("&pound;%s", "British pound sterling"),
    AUD("$%s AUD", "Austrlian dollars");

    public String format, title;

    Currency(String format, String title) {
        this.format = format;
        this.title = title;
    }

    public RawData format(BigDecimal price) {
        if (price == null) {
            price = new BigDecimal("0.00").setScale(2);
        }
        else if (price.scale() != 2) {
            try {
                price = price.setScale(2);
            } catch (ArithmeticException ex) {
                play.Logger.info("Arithmetic exception in currency formatter, for price " + price);
            }
        }
        return new RawData(String.format(format, price));
    }

    @Override
    public String toString() {
        return name() + " - " + title;
    }
}
