package com.trustnet.fi.exampleclient;

import com.trustnet.fi.exampleclient.Sovrin.DidAuth;
import org.springframework.boot.autoconfigure.ldap.embedded.EmbeddedLdapProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import static com.trustnet.fi.exampleclient.Globals.DID_AUTH_HEADER;

@Controller
public class CredentialController {
    public static IssuedCredential issuedCredential;
    public static String token = "";
    public static String dataResponse;

    @PostConstruct
    private void init() {
        issuedCredential = new IssuedCredential();

    }

    @RequestMapping(value = "/credentials", method = RequestMethod.POST)
    ResponseEntity<?> incomingCredential(HttpServletRequest request, @RequestBody String credential) {
        String didAuth = request.getHeader(DID_AUTH_HEADER);
        if (DidAuth.verifyToken(didAuth)) {
            if (issuedCredential == null)
                issuedCredential = new IssuedCredential();
            issuedCredential.setCredential(credential);
        }
        return new ResponseEntity<Object>(HttpStatus.CREATED);
    }
}
