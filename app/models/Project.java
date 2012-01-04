package models;

import com.abperf.Currency;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;
import javax.persistence.*;
import models.Domain.DomainAccess;
import org.apache.commons.lang.StringUtils;
import play.data.validation.*;
import play.db.jpa.Model;
import play.libs.Codec;

@Entity
@Table(name = "project")
public class Project extends Model {
    /**
     * Company or project name.
     */
    @Required
    @MaxSize(40)
    @MinSize(2)
    public String name;

    @Enumerated(EnumType.STRING)
    public AccountType accountType;

    @OneToMany(mappedBy = "project")
    public Set<Domain> domains;

    @ManyToOne
    public User user;

    public int pageViews, pageViewQuota;

    /**
     * Null or 0.00 if free.
     */
    public BigDecimal price;

    @Enumerated(EnumType.STRING)
    public Currency currency;

    @Temporal(TemporalType.TIMESTAMP)
    public Date paymentReceivedAt;

    /**
     * How many page views the project had at the time of the report being generated.
     * Should be a multiple of 1000.
     */
    public int reportPageViews;

    /**
     * When the latest report was generated.
     */
    public Date reportGeneratedAt;

    public String reportOutputDir;

    public Project(final String name, final AccountType accountType, final User user) {
        this.name = name;
        this.accountType = accountType;
        this.user = user;
    }

    public boolean hasReachedPageViewQuota() {
        return pageViews >= pageViewQuota;
    }

    public boolean isReportReady() {
        return false; // @todo
    }

    @Override
    public String toString() {
        return name;
    }

    @PrePersist
    protected void prePersist() {
        if (accountType == null) {
            throw new IllegalStateException("User must have an account type");
        }
    }

    public void setAccountType(final AccountType accountType) {
        this.accountType = accountType;
        this.pageViewQuota = accountType.pageViewQuota;
        this.price = accountType.price;
        this.currency = Currency.USD;
    }

    public boolean waitingForPayment() {
        return !isFree() && paymentReceivedAt == null;
    }

    public boolean isFree() {
        return (price == null || price.doubleValue() == 0);
    }

    /**
     * @return String with 12 alpha-numeric characters.
     */
    public String getUUID() {
        return StringUtils.reverse(Codec.hexSHA1(id.toString()).
                substring(0, 12)).
                toUpperCase().
                intern();
    }

    public String publicDomainsAsString() {
        return domainsAsString(DomainAccess.PUBLIC);
    }

    public String privateDomainsAsString() {
        return domainsAsString(DomainAccess.PRIVATE);
    }

    private String domainsAsString(DomainAccess access) {
        StringBuilder sb = new StringBuilder(50);
        int i = 0;

        for (Domain domain: domains) {
            if (domain.access.equals(access)) {
                if (i++ > 0) {
                    sb.append('\n');
                }
                sb.append(domain.domain);
            }
        }

        return sb.toString();
    }
}
