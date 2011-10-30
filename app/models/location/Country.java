package models.location;

import java.io.*;
import java.util.*;
import javax.persistence.*;
import play.data.validation.*;
import play.db.jpa.*;

/**
 * A country in the world.
 */
@Entity
@Table(name = "country")
public class Country extends Model {
    /**
     * Cache results of {@link #findNameById(java.lang.String)}.
     */
    @Transient
    private static Map<Long, String> ID_TO_NAME_MAP = new HashMap<Long, String>(120);

    /**
     * Country name.
     */
    @Required
    @MaxSize(100)
    @Column(unique = true)
    public String name;

    /**
     * ISO-3166, 2 letter code.
     */
    @Required
    @MinSize(2)
    @MaxSize(2)
    @Column(unique = true)
    public String iso3166a2;

    /**
     * ISO-3166, 3 letter code.
     */
    @Required
    @MinSize(3)
    @MaxSize(3)
    @Column(unique = true)
    public String iso3166a3;

    /**
     * ISO-3166, numeric code.
     */
    @Required
    @Column(unique = true)
    public int iso3166num;

    /**
     * Get the name of a country from it's primary key value.
     *
     * @return Name of the country.
     */
    public static String findNameById(final String theId) {
        final Long id;
        try {
            id = Long.valueOf(theId);
        } catch (NumberFormatException e) {
            return "";
        }

        if (!ID_TO_NAME_MAP.containsKey(id)) {
            ID_TO_NAME_MAP.put(id, Country.<Country>findById(id).name);
        }

        return ID_TO_NAME_MAP.get(id);
    }

    public static Country findOneByName(String name) {
        return find("name", name).first();
    }

    public static Country findOneByIso(final String iso) {
        if (iso.equalsIgnoreCase("UK")) {
            throw new IllegalArgumentException("UK should be GB");
        } else if (iso.length() == 2) {
            return find("iso3166a2 = ?", iso.toUpperCase()).first();
        } else if (iso.length() == 3) {
            return find("iso3166a3 = ?", iso.toUpperCase()).first();
        } else {
            throw new IllegalArgumentException("ISO invalid: " + iso);
        }
    }

    @Override
    public String toString() {
        return name;
    }

    public String toStringWithPrefixThe() {
        return (name.startsWith("United") || name.equals("Bahamas")
                || name.equals("British Virgin Islands") || name.equals("Cayman Islands")
                || name.equals("Czech Republic")
                ? "the " + name
                : name);
    }

    /**
     * Generates a HTML {@code <select>} tag with countries in it.
     */
    public static void generateHtmlSelectTag(final File saveTo) {
        final StringBuilder html = new StringBuilder(10000);
        try {
            html.append("<option value=\"\">Select Country</option>\n");

            final List<Country> countries = Country.all().fetch();
            for (final Country c: countries) {
                html.append("<option value=\"").append(c.id).append("\">").append(c.name).append("</option>");
            }
            countries.clear();
        } catch (final Exception ex) {
            play.Logger.error(ex, null);

            html.setLength(0);
            html.append("<option value=\"\">Feature not available at this moment.</option>");
        }

        saveTo.delete();
        try {
            saveTo.createNewFile();
            final FileWriter fw = new FileWriter(saveTo);
            fw.write(html.toString());
            fw.close();
        } catch (final IOException ex) {
            play.Logger.error(ex, ex.getMessage());
        }
    }
}
