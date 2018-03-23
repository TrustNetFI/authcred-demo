package fi.trustnet.agent.controller;

import fi.trustnet.agent.Sovrin.DidAuth;
import fi.trustnet.agent.domain.Account;
import fi.trustnet.agent.domain.Connection;
import fi.trustnet.agent.domain.Invitation;
import fi.trustnet.agent.repository.AccountRepository;
import fi.trustnet.agent.repository.ConnectionRepository;
import fi.trustnet.agent.repository.ReceivedCredentialRepository;
import fi.trustnet.agent.repository.InvitationRepository;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;


@Controller
public class InvitationController {

    public static final String RETURN_URL = "http://localhost:8080/invitationcb";
    public static final String CLAIMOFFER_URL = "http://localhost:8080/credentialoffer";

    @Autowired
    InvitationRepository invitationRepository;
    @Autowired
    ReceivedCredentialRepository receivedCredentialRepository;
    @Autowired
    AccountRepository accountRepository;
    @Autowired
    ConnectionRepository connectionRepository;

    @RequestMapping(value="/invitationcb/{id}", method = RequestMethod.GET)
    public String invitationCb(Model model, @PathVariable String id, @RequestParam Boolean success) {
        if (success)
        {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Account account = accountRepository.findByUsername(auth.getName());

            Invitation invitation = invitationRepository.findByAccountAndIdentifier(account,id);
            Connection connection = new Connection();
            connection.setIdentifier(invitation.getIdentifier());
            connection.setRemovalUrl(invitation.getInvitationUrl());
            connection.setSender(invitation.getSender());
            connection.setAccount(account);
            connectionRepository.save(connection);
            invitationRepository.delete(invitation);
        }
        return "redirect:/inbox";
    }



    @RequestMapping(value="/acceptinvitation/{id}", method = RequestMethod.GET)
    public RedirectView acceptInvitation(Model model, @PathVariable String id) {


        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Account account = accountRepository.findByUsername(auth.getName());

        Invitation invitation = invitationRepository.findByAccountAndIdentifier(account,id);
        String token = DidAuth.createToken(account.getUsername(), account.getDid());
        //redirect, must send necessary data using query string
        return new RedirectView(invitation.getInvitationUrl() +"?token=" + token +"" +
                "&returnurl=" +RETURN_URL + "/" + invitation.getIdentifier() + "&claimofferurl=" + CLAIMOFFER_URL);

    }
}
