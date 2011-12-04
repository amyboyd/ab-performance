package com.abperf;

import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang.time.DateFormatUtils;
import play.templates.BaseTemplate.RawData;
import play.templates.JavaExtensions;

/**
 * These methods are accessible in Play views.
 *
 * The first parameter of each method is the previous value in the method chain.
 */
final public class TemplateExtensions extends JavaExtensions {
    private TemplateExtensions() {
        // Prevent this class from being instantiated.
    }

    public static RawData colorHTML(final String html) {
        String coloredHTML = escapeHtml(html).toString().
                replaceAll("( [a-z]+\\=)", "<span style=\"color:green;\">$1</span>").
                replaceAll("(&quot;[\\w ]+&quot;)", "<span style=\"color:#E08700;\">$1</span>").
                replace("&lt;style", "<span style=\"color:blue;\">&lt;style</span>").
                replace("&quot;&gt;", "&quot;<span style=\"color:blue;\">&gt;</span>").
                replace("&lt;/style&gt;", "<span style=\"color:blue;\">&lt;/style&gt;</span>");
        return new RawData(coloredHTML);
    }

    /**
     * Replace "aA" with "a<wbr />A". Display using {@code raw()}.
     */
    public static RawData wbrBeforeCaps(final String str) {
        return new RawData(str.replaceAll("([a-z])([A-Z\\.\\-\\_])", "$1<wbr />$2"));
    }

    /**
     * Remove "http://", "https://", "www." from the start. Remove "/" from the end.
     */
    public static String prettyUrl(String url) {
        // Protocol.
        url = url.replaceAll("^https?://", "");
        // www.
        url = url.replaceAll("^www.", "");
        // Trailing slash.
        url = url.replaceAll("/$", "");
        return url;
    }

    /**
     * @return Formatted as "Mon 3rd<br />Jan 2011"
     */
    @SuppressWarnings("deprecation")
    public static RawData commonDateFormatTwoLines(final Date date) {
        return new RawData(format(date, "EE'&nbsp;'d'" + getDateOfMonthSuffix(date.getDate()) + "<br />'MMM,'&nbsp;'yyyy"));
    }

    /**
     * @return Formatted as "Mon 3rd Jan 2011"
     */
    @SuppressWarnings("deprecation")
    public static RawData commonDateFormatOneLine(final Date date) {
        return new RawData(format(date, "EE'&nbsp;'d'" + getDateOfMonthSuffix(date.getDate()) + "&nbsp;'MMM,'&nbsp;'yyyy"));
    }

    /**
     * @return "st", "nd", "rd", "th"
     */
    private static String getDateOfMonthSuffix(final int dateOfMonthNumber) {
        final String dateOfMonthSuffix;
        if (dateOfMonthNumber >= 11 && dateOfMonthNumber <= 13) {
            dateOfMonthSuffix = "th";
        } else {
            switch (dateOfMonthNumber % 10) {
                case 1:
                    dateOfMonthSuffix = "st";
                    break;
                case 2:
                    dateOfMonthSuffix = "nd";
                    break;
                case 3:
                    dateOfMonthSuffix = "rd";
                    break;
                default:
                    dateOfMonthSuffix = "th";
                    break;
            }
        }
        return dateOfMonthSuffix;
    }

    /**
     * Turn an enum like "SOLD_OUT" into "Sold Out".
     * @return The enum's name with underscores replaced with spaces, and words capitalized.
     */
    public static String prettyEnum(final Enum<?> value) {
        return prettyEnum(value.name());
    }

    /**
     * Turn a string like "SOLD_OUT" into "Sold Out".
     * @return The string with underscores replaced with spaces, and words capitalized.
     */
    public static String prettyEnum(final String in) {
        return capitalizeWords(in.replace('_', ' ').toLowerCase());
    }

    /**
     * @return Date formatted as "23 May 1991".
     */
    public static String asDate(final Date date) {
        return DateFormatUtils.format(date, "dd MMMM yyyy");
    }

    public static String padLeft(String str, final Integer size, final char character) {
        final int toPad = size.intValue() - str.length();
        for (int i = 0; i < toPad; i++) {
            str += character;
        }
        return str;
    }

    public static List<?> pickRandomItems(final List<?> list, final Integer limit) {
        if (list.size() > limit) {
            Collections.shuffle(list);
            return list.subList(0, limit);
        } else {
            return list;
        }
    }

    public static String asJavascriptArray(final List<?> list) {
        final StringBuilder sb = new StringBuilder(15 * list.size());
        sb.append('[');

        final Iterator ite = list.iterator();
        int i = 0;
        while (ite.hasNext()) {
            if (i++ > 0) {
                sb.append(',');
            }
            sb.append("'").append(ite.next().toString().replace("'", "\'")).append("'");
        }

        sb.append(']');
        return sb.toString();
    }

    /**
     * Return a string with a maximum length of "length" characters.
     * If there are more than "length" characters, then string ends with an ellipsis ("...").
     */
    public static String ellipsis(final String text, int length) {
        // The letters [iIl1] are slim enough to only count as half a character.
        length += (int) Math.floor(text.replaceAll("[^iIl.,:;]", "").length() / 2.0d);

        try {
            return (text.length() > length
                    ? text.substring(0, length - 3) + "..."
                    : text);
        } catch (StringIndexOutOfBoundsException e) {
            return "";
        }
    }

    /**
     * Escapes a string to the proper CSV format:
     * - enclosed in double-quotes.
     * - any inner double-quotes must be escaped by adding another double-quote in front, i.e. ""
     * - line breaks are left alone.
     */
    public static String toCsvValue(String s) {
        if (s == null || s.isEmpty()) {
            return "";
        }
        if (s.contains("\"")) {
            s = s.replace("\"", "\"\"");
        }
        return '"' + s + '"';
    }

    /**
     * See params and return for an example. "view=list" is replaced with "view=[empty]" (param is removed).
     * @param url "/search?view=list&sort=new"
     * @param param "view"
     * @param value "" (literally empty to remove the param).
     * @return "/search?sort=new"
     */
    public static String rewriteParam(final String url, final String param, final String value) {
        // If no query string, it's an easy addition.
        if (!url.contains("?")) {
            // Only add the param if not empty.
            return (value.isEmpty())
                    ? url
                    : url + "?" + param + "=" + value;
        } // Query string already contains the param and it needs replaced.
        else if (url.contains(param + "=")) {
            return url.replaceFirst("([\\?&])" + param + "=" + "[^&]*", "$1" + param + "=" + value);
        } // The param is not already in the query string so just add it at the end.
        else {
            // Only add the param if not empty.
            return (value.isEmpty())
                    ? url
                    : url + "&" + param + "=" + value;
        }
    }
}
