package com.vuhlog.money_keeper.util;

import java.text.NumberFormat;
import java.util.Locale;

public class FormatUtil {
    public static String formatCurrency(long amount) {
        Locale vietnamLocale = new Locale("vi", "VN");
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(vietnamLocale);
        currencyFormatter.setMinimumFractionDigits(0);
        currencyFormatter.setMaximumFractionDigits(0);
        return currencyFormatter.format(amount);
    }
}
