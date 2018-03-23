package fi.trustnet.agent.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.trustnet.agent.domain.Account;
import fi.trustnet.agent.domain.CredentialRequest;
import fi.trustnet.agent.domain.Invitation;
import fi.trustnet.agent.repository.AccountRepository;
import fi.trustnet.agent.repository.CredentialRequestRepository;
import fi.trustnet.agent.repository.InvitationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.PostConstruct;

import static fi.trustnet.agent.configuration.IndyConfig.USER_DID;

@Controller
public class DefaultController {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private InvitationRepository invitationRepository;

    @Autowired
    private CredentialRequestRepository credentialRequestRepository;


    @PostConstruct
    public void init() {
        Account user = new Account();
        user.setUsername("alice");

        //Use pregenerated DID, this was created using indysetup

        user.setDid(USER_DID);

        user = accountRepository.save(user);


        Invitation invitation = new Invitation();
        invitation.setAccount(user);
        invitation.setSender("Company X");
        invitation.setSubject("Connection request");
        invitation.setInvitationUrl("http://localhost:8090/connect");
        invitation.setIdentifier("123456-xx-999");
        invitationRepository.save(invitation);

        CredentialRequest credentialRequest = new CredentialRequest();
        credentialRequest.setAccount(user);
        credentialRequest.setSender("Company Y");
        credentialRequest.setRequestingdid("TaVE1pAaummu3wfbWtVbyu");
        credentialRequest.setSubject("Grant access to your energy metering data at Company X");
        credentialRequest.setPurpose("Process you electricity metering data for creating energy report");
        credentialRequest.setCredentialUrl("http://localhost:8099/credentials");
        credentialRequest.setIdentifier("7890123456");
        credentialRequest.getRequestedscopes().add("electricity_metering_data");
        credentialRequest.getRequestedattributes().add("customerid");
        ObjectMapper objectMapper = new ObjectMapper();
        credentialRequestRepository.save(credentialRequest);
    }

    @RequestMapping("*")
    public String handleDefault() {
        return "redirect:/inbox";
    }
}
