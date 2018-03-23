package fi.trustnet.agent.controller;

import fi.trustnet.agent.Sovrin.Credential;
import fi.trustnet.agent.Sovrin.DidAuth;
import fi.trustnet.agent.domain.Account;
import fi.trustnet.agent.domain.CredentialRequest;
import fi.trustnet.agent.domain.IssuedCredential;
import fi.trustnet.agent.repository.AccountRepository;
import fi.trustnet.agent.repository.CredentialRequestRepository;
import fi.trustnet.agent.repository.IssuedCredentialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

import static fi.trustnet.agent.configuration.Globals.DID_AUTH_HEADER;

@Controller
public class CredentialIssuerController {
    @Autowired
    CredentialRequestRepository credentialRequestRepository;
    @Autowired
    AccountRepository accountRepository;
    @Autowired
    IssuedCredentialRepository issuedCredentialRepository;

    @RequestMapping(value="/deletecredentialrequest/{id}", method = RequestMethod.GET)
    public String deleteCredentialRequest(Model model, @PathVariable String id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Account account = accountRepository.findByUsername(auth.getName());
        CredentialRequest credentialRequest = credentialRequestRepository.findByAccountAndIdentifier(account, id);
        credentialRequestRepository.delete(credentialRequest);
        return "redirect:/inbox";
    }

    @RequestMapping(value="/issuecredential/{id}", method = RequestMethod.GET)
    public String issueCredential(Model model, @PathVariable String id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Account account = accountRepository.findByUsername(auth.getName());
        CredentialRequest credentialRequest = credentialRequestRepository.findByAccountAndIdentifier(account, id);
        String credential;
        try {
            credential = Credential.createCredential(account.getDid(), credentialRequest.getRequestingdid());
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            return "redirect:/inbox";
        }
        //save issued credential
        IssuedCredential issuedCredential = new IssuedCredential();
        issuedCredential.setRevocationurl(Credential.getRevocationUrl(credential));
        issuedCredential.setRevoked(false);
        issuedCredential.setAccount(account);
        issuedCredential.setCredential(credential);
        issuedCredential.setIdentifier(UUID.randomUUID().toString());
        issuedCredential.setIssuedto(credentialRequest.getRequestingdid());
        issuedCredentialRepository.save(issuedCredential);

        //send credential to requesting party
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        //add signed token to header to prove user controls the private key
        String didAuth = DidAuth.createToken(account.getUsername(), account.getDid());
        headers.add(DID_AUTH_HEADER, didAuth);

        HttpEntity<String> entity = new HttpEntity<>(credential, headers);

        ResponseEntity<String> responseEntity = restTemplate.postForEntity(credentialRequest.getCredentialUrl(), entity, String.class);
        if (responseEntity.getStatusCode().equals(HttpStatus.CREATED)) {
            credentialRequestRepository.delete(credentialRequest);
        }
        return "redirect:/inbox";
    }
}
