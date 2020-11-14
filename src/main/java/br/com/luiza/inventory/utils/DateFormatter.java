package br.com.luiza.inventory.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFormatter {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

    public static String format(Date date) {
        return DATE_FORMAT.format(date);
    }

}
