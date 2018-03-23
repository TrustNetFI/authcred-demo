package fi.trustnet.agent.controller;

import fi.trustnet.agent.domain.Account;
import fi.trustnet.agent.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class Inbox {

    @Autowired
    InvitationRepository invitationRepository;
    @Autowired
    CredentialRequestRepository credentialRequestRepository;
    @Autowired
    ReceivedCredentialRepository receivedCredentialRepository;
    @Autowired
    ConnectionRepository connectionRepository;
    @Autowired
    AccountRepository accountRepository;
    @Autowired
    CredentialOfferRepository credentialOfferRepository;
    @Autowired
    IssuedCredentialRepository issuedCredentialRepository;

    @RequestMapping(value = "/inbox", method = RequestMethod.GET)
    public String view(Model model){

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Account account = accountRepository.findByUsername(auth.getName());
        model.addAttribute("invitations", invitationRepository.findByAccount(account));
        model.addAttribute("credentials", receivedCredentialRepository.findByAccount(account));

        //hide pre-created credential request until user has aquired credential from service
        if (receivedCredentialRepository.count() != 0) {
            model.addAttribute("credentialrequests", credentialRequestRepository.findByAccount(account));
        }
        model.addAttribute("connections", connectionRepository.findByAccount(account));
        model.addAttribute("credentialoffers", credentialOfferRepository.findByAccount(account));
        model.addAttribute("issuedcredentials", issuedCredentialRepository.findByAccount(account));

        return "inbox";
    }
}
