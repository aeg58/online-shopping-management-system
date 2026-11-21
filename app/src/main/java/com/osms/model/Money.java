package com.osms.model;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/** Formats amounts the same way as the templates: 1,234.50 ₺ */
public final class Money {

    private Money() {
    }

    public static String format(BigDecimal amount) {
        DecimalFormat format = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.US));
        return format.format(amount) + " ₺";
    }
}
