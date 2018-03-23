package fi.trustnet.agent.domain;

import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class Connection extends AbstractPersistable<Long> {
    @ManyToOne
    private Account account;
    private String sender;
    private String removalUrl;
    private String identifier;

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getRemovalUrl() {
        return removalUrl;
    }

    public void setRemovalUrl(String removalUrl) {
        this.removalUrl = removalUrl;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
}
