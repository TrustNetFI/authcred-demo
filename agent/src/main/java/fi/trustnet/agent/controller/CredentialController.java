package fi.trustnet.agent.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.trustnet.agent.Sovrin.DidAuth;
import fi.trustnet.agent.domain.Account;
import fi.trustnet.agent.domain.CredentialOffer;
import fi.trustnet.agent.domain.IssuedCredential;
import fi.trustnet.agent.domain.ReceivedCredential;
import fi.trustnet.agent.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;

import javax.jws.WebParam;
import java.io.IOException;

import static fi.trustnet.agent.configuration.Globals.DID_AUTH_HEADER;

@Controller
public class CredentialController {
    @Autowired
    InvitationRepository invitationRepository;
    @Autowired
    ReceivedCredentialRepository receivedCredentialRepository;
    @Autowired
    AccountRepository accountRepository;
    @Autowired
    CredentialOfferRepository credentialOfferRepository;
    @Autowired
    CredentialRequestRepository credentialRequestRepository;
    @Autowired
    IssuedCredentialRepository issuedCredentialRepository;


    @RequestMapping(value="/viewcredential/{id}", method = RequestMethod.GET)
    public String viewCredential(Model model, @PathVariable String id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Account account = accountRepository.findByUsername(auth.getName());
        model.addAttribute("credential", receivedCredentialRepository.findByAccountAndIssuer(account, id));
        return "credentialview";
    }

    @RequestMapping(value="/viewissuedcredential/{id}", method = RequestMethod.GET)
    public String viewIssuedCredential(Model model, @PathVariable String id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Account account = accountRepository.findByUsername(auth.getName());
        model.addAttribute("credential", issuedCredentialRepository.findByAccountAndIdentifier(account, id));
        return "credentialview";
    }

    @RequestMapping(value="/revokecredential/{id}", method = RequestMethod.GET)
    public String revokeIssuedCredential(Model model, @PathVariable String id){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Account account = accountRepository.findByUsername(auth.getName());
        IssuedCredential issuedCredential = issuedCredentialRepository.findByAccountAndIdentifier(account, id);
        //simple revocation, can be commented out if revocation server not responding
        revokeCredential(issuedCredential.getRevocationurl());
        issuedCredential.setRevoked(true);
        issuedCredentialRepository.save(issuedCredential);
        return "redirect:/inbox";
    }

    private void revokeCredential(String revocationUrl){
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> responseEntity = restTemplate.postForEntity(revocationUrl, entity, String.class);

    }



    @RequestMapping(value="/viewcredentialrequest/{id}", method = RequestMethod.GET)
    public String viewCredentialRequest(Model model, @PathVariable String id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Account account = accountRepository.findByUsername(auth.getName());
        model.addAttribute("credentialrequest", credentialRequestRepository.findByAccountAndIdentifier(account, id));
        return "credentialrequestview";
    }

    @RequestMapping(value="/credentialoffer", method = RequestMethod.POST)
    public ResponseEntity<?> handleCredentialOffer(@RequestBody String body) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            CredentialOffer credentialOffer = objectMapper.readValue(body, CredentialOffer.class);
            Account account = accountRepository.findByDid(credentialOffer.getSubject());
            credentialOffer.setAccount(account);
            credentialOfferRepository.save(credentialOffer);
        }
        catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return new ResponseEntity<Object>(HttpStatus.CREATED);
    }


    @RequestMapping(value="/acceptcredentialoffer/{id}", method = RequestMethod.GET)
    public String acceptCredOffer(Model model, @PathVariable String id) {


        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Account account = accountRepository.findByUsername(auth.getName());

        CredentialOffer credentialOffer = credentialOfferRepository.findByAccountAndOfferid(account, id);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        //add signed token to header to prove user controls the private key
        String didAuth = DidAuth.createToken(account.getUsername(), account.getDid());
        headers.add(DID_AUTH_HEADER, didAuth);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> responseEntity = restTemplate.exchange(credentialOffer.getUrl(), HttpMethod.GET, entity, String.class);
        if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                JsonNode jsonNode = mapper.readTree(responseEntity.getBody());
                System.out.println(jsonNode.toString());
                ReceivedCredential credential = new ReceivedCredential();

                credential.setCredential(jsonNode.toString());
                credential.setIssuer(jsonNode.get("issuer").asText());
                credential.setAccount(account);
                receivedCredentialRepository.save(credential);
                credentialOfferRepository.delete(credentialOffer
                );
            }
            catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }

        return "redirect:/inbox";

    }





}
