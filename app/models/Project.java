package models;

import java.util.*;
import javax.persistence.*;
import play.data.validation.*;
import play.db.jpa.Model;

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

    public Project(final String name, final AccountType accountType, final User user) {
        this.name = name;
        this.accountType = accountType;
        this.user = user;
    }

    public boolean hasReachedPageViewQuota() {
        return pageViews >= pageViewQuota;
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
    }
}
