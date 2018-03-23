package com.trustnet.fi.exampleclient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trustnet.fi.exampleclient.Sovrin.DidAuth;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;

import static com.trustnet.fi.exampleclient.CredentialController.issuedCredential;
import static com.trustnet.fi.exampleclient.CredentialController.token;
import static com.trustnet.fi.exampleclient.Globals.DID_AUTH_HEADER;
import static com.trustnet.fi.exampleclient.IndyConfig.CLIENT_DID;
import static com.trustnet.fi.exampleclient.IndyConfig.CLIENT_WALLET_NAME;

@Controller
public class TokenController {
    public static final String TOKEN_URL = "http://localhost:8090/accesstoken";


    @RequestMapping(value = "/token", method = RequestMethod.GET)
    public String tokenRequest(Model model){
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        String didAuth = DidAuth.createToken(CLIENT_WALLET_NAME, CLIENT_DID);
        headers.add(DID_AUTH_HEADER, didAuth);

        ObjectMapper objectMapper = new ObjectMapper();

        HttpEntity<String> entity = new HttpEntity<>(issuedCredential.getCredential(), headers);

        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(TOKEN_URL, HttpMethod.POST, entity, String.class);
            if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
                token = responseEntity.getBody();
            }
        }
        catch (Exception e) {
            token = "";
        }
        return "redirect:/";
    }


}
