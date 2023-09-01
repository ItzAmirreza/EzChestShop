package me.deadlight.ezchestshop.utils;

import me.deadlight.ezchestshop.data.Config;

import java.text.DecimalFormat;

public class NumberUtils {

    /**
     * Round a double to a certain precision.
     *
     * @param value The value to round
     * @param precision The precision to round to
     * @return The rounded value
     */
    public static double round(double value, int precision) {
        int scale = (int) Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
    }

    /**
     * Check if a String can be safely converted into a numeric value.
     *
     * @param strNum The String to check
     * @return Whether the String is numeric
     */
    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    /**
     * Check if a String can be safely converted into an integer value.
     *
     * @param str The String to check
     * @return Whether the String is an integer
     */
    public static boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * The different types of number formatting.
     */
    public enum FormatType {
        GUI, CHAT, HOLOGRAM
    }

    /**
     * Format a number according to the specified format type. The format type is configured in the config.yml.
     *
     * @param number The number to format
     * @param type The format type
     * @return The formatted number
     */
    public static String formatNumber(double number, FormatType type) {
        String result = "Error";
        DecimalFormat decimalFormat;
        switch (type) {
            case GUI:
                decimalFormat = new DecimalFormat(Config.display_numberformat_gui);
                result = decimalFormat.format(number);
                break;
            case CHAT:
                decimalFormat = new DecimalFormat(Config.display_numberformat_chat);
                result = decimalFormat.format(number);
                break;
            case HOLOGRAM:
                decimalFormat = new DecimalFormat(Config.display_numberformat_holo);
                result = decimalFormat.format(number);
                break;
        }
        return result;
    }
}
