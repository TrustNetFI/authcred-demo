package fi.trustnet.agent.controller;

import fi.trustnet.agent.Sovrin.DidAuth;
import fi.trustnet.agent.domain.Account;
import fi.trustnet.agent.domain.Connection;
import fi.trustnet.agent.repository.AccountRepository;
import fi.trustnet.agent.repository.ConnectionRepository;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;

import static fi.trustnet.agent.configuration.Globals.DID_AUTH_HEADER;

@Controller
public class ConnectionController {
    AccountRepository accountRepository;
    ConnectionRepository connectionRepository;

    @RequestMapping(value="/deleteconnection/{id}", method = RequestMethod.GET)
    public String deleteConnection(Model model, @PathVariable String id) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Account account = accountRepository.findByUsername(auth.getName());

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        //add signed token to header to prove user controls the private key
        String token = DidAuth.createToken(account.getUsername(), account.getDid());
        headers.add(DID_AUTH_HEADER, token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        Connection connection = connectionRepository.findByAccountAndIdentifier(account, id);

        ResponseEntity<String> responseEntity = restTemplate.exchange(connection.getRemovalUrl(), HttpMethod.DELETE, entity, String.class);
        if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
            connectionRepository.delete(connection);
        }

        return "redirect:/inbox";
    }
}
