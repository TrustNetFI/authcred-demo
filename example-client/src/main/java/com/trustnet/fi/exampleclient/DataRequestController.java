package com.trustnet.fi.exampleclient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trustnet.fi.exampleclient.Sovrin.DidAuth;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import static com.trustnet.fi.exampleclient.CredentialController.dataResponse;
import static com.trustnet.fi.exampleclient.CredentialController.token;
import static com.trustnet.fi.exampleclient.Globals.*;
import static com.trustnet.fi.exampleclient.IndyConfig.CLIENT_DID;
import static com.trustnet.fi.exampleclient.IndyConfig.CLIENT_WALLET_NAME;


@Controller
public class DataRequestController {

    public static final String DATAREQUEST_URL = "http://localhost:8090/data";


    @RequestMapping(value = "/datarequest", method = RequestMethod.GET)
    public String makeDataRequest(Model model){

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
/*
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler(){
            protected boolean hasError(HttpStatus statusCode) {
                return false;
            }});
*/
        String didAuth = DidAuth.createToken(CLIENT_WALLET_NAME, CLIENT_DID);
        headers.add(DID_AUTH_HEADER, didAuth);
        headers.add(DID_TOKEN, token);

        ObjectMapper objectMapper = new ObjectMapper();

        HttpEntity<String> entity = new HttpEntity<>(headers);
        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange(DATAREQUEST_URL, HttpMethod.GET, entity, String.class);
            if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
                dataResponse = responseEntity.getBody();
            }
        }
        catch (RestClientResponseException e) {
            if (e.getRawStatusCode()==401) {
                dataResponse = "UNAUTHORIZED";
            }
        }

        return "redirect:/";
    }
}
