package me.deadlight.ezchestshop.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
    /**
     * Convert a Location to a String
     *
     * @param loc
     * @return
     */
    public static String LocationtoString(Location loc) {
        if (loc == null)
            return null;
        String sloc = "";
        sloc += ("W:" + loc.getWorld().getName() + ",");
        sloc += ("X:" + loc.getX() + ",");
        sloc += ("Y:" + loc.getY() + ",");
        sloc += ("Z:" + loc.getZ());
        return sloc;
    }

    /**
     * Convert a Location to a String with the Location rounded as defined via the
     * decimal argument
     *
     * @param loc
     * @param decimals
     * @return
     */
    public static String LocationRoundedtoString(Location loc, int decimals) {
        if (loc == null)
            return null;
        String sloc = "";
        sloc += ("W:" + loc.getWorld().getName() + ",");
        if (decimals <= 0) {
            sloc += ("X:" + (int) NumberUtils.round(loc.getX(), decimals) + ",");
            sloc += ("Y:" + (int) NumberUtils.round(loc.getY(), decimals) + ",");
            sloc += ("Z:" + (int) NumberUtils.round(loc.getZ(), decimals));
        } else {
            sloc += ("X:" + NumberUtils.round(loc.getX(), decimals) + ",");
            sloc += ("Y:" + NumberUtils.round(loc.getY(), decimals) + ",");
            sloc += ("Z:" + NumberUtils.round(loc.getZ(), decimals));
        }
        return sloc;
    }

    /**
     * Convert a String to a Location
     *
     * @param sloc
     * @return
     */
    public static Location StringtoLocation(String sloc) {
        if (sloc == null)
            return null;
        String[] slocs = sloc.split(",");
        World w = Bukkit.getWorld(slocs[0].split(":")[1]);
        Double x = Double.valueOf(slocs[1].split(":")[1]);
        Double y = Double.valueOf(slocs[2].split(":")[1]);
        Double z = Double.valueOf(slocs[3].split(":")[1]);
        Location loc = new Location(w, x, y, z);

        if (sloc.contains("Yaw:") && sloc.contains("Pitch:")) {
            loc.setYaw(Float.valueOf(slocs[4].split(":")[1]));
            loc.setPitch(Float.valueOf(slocs[5].split(":")[1]));
        }
        return loc;
    }
    /**
     * Split a String by "_" and capitalize each First word, then join them together
     * via " "
     *
     * @param string
     * @return
     */
    public static String capitalizeFirstSplit(String string) {
        string = string.toLowerCase();
        String n_string = "";
        for (String s : string.split("_")) {
            n_string += s.subSequence(0, 1).toString().toUpperCase()
                    + s.subSequence(1, s.length()).toString().toLowerCase() + " ";
        }
        return n_string;
    }
    /**
     * Apply & color translating, as well as #ffffff hex color encoding to a String.
     * Versions below 1.16 will only get the last hex color symbol applied to them.
     *
     * @param str
     * @return
     */
    public static String colorify(String str) {
        if (str == null)
            return null;
        return translateHexColorCodes("#", "", ChatColor.translateAlternateColorCodes('&', str));
    }


    /**
     * Apply hex color coding to a String. possibility to add a special start or end
     * tag to the String.
     * Versions below 1.16 will only get the last hex color symbol applied to them.
     *
     * @param startTag
     * @param endTag
     * @param message
     * @return
     */
    public static String translateHexColorCodes(String startTag, String endTag, String message) {
        final Pattern hexPattern = Pattern.compile(startTag + "([A-Fa-f0-9]{6})" + endTag);
        final char COLOR_CHAR = ChatColor.COLOR_CHAR;
        Matcher matcher = hexPattern.matcher(message);
        StringBuffer buffer = new StringBuffer(message.length() + 4 * 8);
        while (matcher.find()) {
            String group = matcher.group(1);
            matcher.appendReplacement(buffer, COLOR_CHAR + "x"
                    + COLOR_CHAR + group.charAt(0) + COLOR_CHAR + group.charAt(1)
                    + COLOR_CHAR + group.charAt(2) + COLOR_CHAR + group.charAt(3)
                    + COLOR_CHAR + group.charAt(4) + COLOR_CHAR + group.charAt(5));
        }
        return matcher.appendTail(buffer).toString();
    }
}
