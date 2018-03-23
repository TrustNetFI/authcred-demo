package fi.trustnet.resourceserver.controller;

import fi.trustnet.resourceserver.Sovrin.AccessToken;
import fi.trustnet.resourceserver.Sovrin.Credential;
import fi.trustnet.resourceserver.Sovrin.DidAuth;
import fi.trustnet.resourceserver.domain.Account;
import fi.trustnet.resourceserver.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.util.Base64;

import static fi.trustnet.resourceserver.configuration.Globals.DID_AUTH_HEADER;

@Controller
public class AccessTokenController {
    @Autowired
    AccountRepository accountRepository;

    @RequestMapping(value = "/accesstoken", method = RequestMethod.POST)
    public ResponseEntity<?> issueToken(HttpServletRequest request, @RequestBody String credential) {
       String didAuth = request.getHeader(DID_AUTH_HEADER);
       if (DidAuth.verifyToken(didAuth)) {
            if(!Credential.verifyCredential(credential)) {
                return new ResponseEntity<Object>(HttpStatus.UNAUTHORIZED);
            }

            String issuer = Credential.getIssuer(credential);
            String subject = Credential.getSubject(credential);

            Account account = accountRepository.findByDid(issuer);

            String tkn = new String(Base64.getDecoder().decode(didAuth));
            String[] splitToken = tkn.split("\\s+");
            if ((account == null)||!(account.getDid().equals(issuer))||!subject.equals(splitToken[0]))
                return new ResponseEntity<Object>(HttpStatus.UNAUTHORIZED);

            String revocationUrl = Credential.getRevocationUrl(credential);
            String scope = Credential.getScope(credential);
            String accesToken = AccessToken.createAccessToken(issuer, subject, revocationUrl,scope);

            return new ResponseEntity<Object>(accesToken, HttpStatus.OK);

        }
        else {
           return new ResponseEntity<Object>(HttpStatus.UNAUTHORIZED);
       }
    }
}
