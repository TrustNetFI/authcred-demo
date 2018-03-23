package fi.trustnet.resourceserver.controller;

import java.util.Base64;

import fi.trustnet.resourceserver.Sovrin.Credential;
import fi.trustnet.resourceserver.Sovrin.DidAuth;
import fi.trustnet.resourceserver.domain.Account;
import fi.trustnet.resourceserver.repository.AccountRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import static fi.trustnet.resourceserver.configuration.Globals.DID_AUTH_HEADER;
import static fi.trustnet.resourceserver.configuration.IndyConfig.RS_DID;

@RestController
public class CredentialController {

    @Autowired
    AccountRepository accountRepository;

    @RequestMapping(value = "/credential", method = RequestMethod.GET)
    public ResponseEntity<?> createVerifiableCredential(HttpServletRequest request){

        String didAuth = request.getHeader(DID_AUTH_HEADER);
        if (DidAuth.verifyToken(didAuth)) {
            try {
                String tkn = new String(Base64.getDecoder().decode(didAuth));
                String[] splitToken = tkn.split("\\s+");
                Account account = accountRepository.findByDid(splitToken[0]);
                String credential = Credential.createCredential(RS_DID,account.getDid());
                return new ResponseEntity<Object>(credential, HttpStatus.OK);
            } catch (Exception e) {
                return new ResponseEntity<Object>("Could not create credential", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        else return new ResponseEntity<Object>("Could not create credential", HttpStatus.UNAUTHORIZED);
    }
}
