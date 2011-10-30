package models.location;

import javax.persistence.Entity;
import javax.persistence.Table;
import play.data.validation.MaxSize;
import play.data.validation.MinSize;
import play.data.validation.Required;
import play.db.jpa.Model;

/**
 * Table mapping IP addresses to countries. This does not support IPv6.
 */
@Entity
@Table(name = "ip_address")
public class IpAddress extends Model {
    @Required
    public long fromIp;

    @Required
    public long toIp;

    @Required
    @MinSize(2)
    @MaxSize(2)
    public String countryIso3166a2;

    public static Country findCountryForIp(final String dottedAddress) {
        final String[] quads = dottedAddress.split("\\.");
        final long decimalAddress = (Short.parseShort(quads[0]) << 24)
                | (Short.parseShort(quads[1]) << 16)
                | (Short.parseShort(quads[2]) << 8)
                | Short.parseShort(quads[3]);
        final IpAddress obj = find("fromIp <= ? AND toIp >= ?", decimalAddress, decimalAddress).first();
        return (obj != null ? Country.findOneByIso(obj.countryIso3166a2) : null);
    }
}
