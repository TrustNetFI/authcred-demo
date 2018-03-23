package fi.trustnet.resourceserver.token;

public class Token {
    private String userdid;
    private String revocationurl;
    private String scope;
    private String exp;
    private String issuedto;

    public String getUserdid() {
        return userdid;
    }

    public void setUserdid(String userdid) {
        this.userdid = userdid;
    }

    public String getRevocationurl() {
        return revocationurl;
    }

    public void setRevocationurl(String revocationurl) {
        this.revocationurl = revocationurl;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getExp() {
        return exp;
    }

    public void setExp(String exp) {
        this.exp = exp;
    }

    public String getIssuedto() {
        return issuedto;
    }

    public void setIssuedto(String issuedto) {
        this.issuedto = issuedto;
    }
}
