package fi.trustnet.agent.domain;

import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

@Entity

public class IssuedCredential extends AbstractPersistable<Long>
{
    @ManyToOne
    private Account account;
    private String issuedto;
    private String identifier;
    private String revocationurl;
    private Boolean revoked;
    @Lob
    @Column
    private String credential;


    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public String getIssuedto() {
        return issuedto;
    }

    public void setIssuedto(String issuedto) {
        this.issuedto = issuedto;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getCredential() {
        return credential;
    }

    public void setCredential(String credential) {
        this.credential = credential;
    }

    public String getRevocationurl() {
        return revocationurl;
    }

    public void setRevocationurl(String revocationurl) {
        this.revocationurl = revocationurl;
    }

    public Boolean getRevoked() {
        return revoked;
    }

    public void setRevoked(Boolean revoked) {
        this.revoked = revoked;
    }
}
