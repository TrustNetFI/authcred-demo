package fi.trustnet.agent.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.util.LinkedList;

@Entity
public class CredentialRequest extends AbstractPersistable<Long> {
    @ManyToOne
    @JsonIgnore
    Account account;

    private String sender;
    private String subject;
    private String credentialurl;
    private String identifier;
    private String purpose;
    private String requestingdid;
    LinkedList<String> requestedattributes;
    LinkedList<String> requestedscopes;

    public String getRequestingdid() {
        return requestingdid;
    }

    public void setRequestingdid(String requestingdid) {
        this.requestingdid = requestingdid;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

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

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getCredentialUrl() {
        return credentialurl;
    }

    public void setCredentialUrl(String credentialurl) {
        this.credentialurl = credentialurl;
    }

    public LinkedList<String> getRequestedattributes() {
        if (requestedattributes == null)
            requestedattributes = new LinkedList<>();
        return requestedattributes;
    }

    public void setRequestedattributes(LinkedList<String> requestedattributes) {
        this.requestedattributes = requestedattributes;
    }

    public LinkedList<String> getRequestedscopes() {
        if (requestedscopes == null)
            requestedscopes = new LinkedList<>();
        return requestedscopes;
    }

    public void setRequestedscopes(LinkedList<String> requestedscopes) {
        this.requestedscopes = requestedscopes;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }
}
