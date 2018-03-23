package fi.trustnet.resourceserver.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.trustnet.resourceserver.Sovrin.AccessToken;
import fi.trustnet.resourceserver.Sovrin.DidAuth;
import fi.trustnet.resourceserver.domain.Account;
import fi.trustnet.resourceserver.domain.Data;
import fi.trustnet.resourceserver.repository.AccountRepository;
import fi.trustnet.resourceserver.repository.DataRepository;
import fi.trustnet.resourceserver.token.Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;

import java.util.Base64;

import static fi.trustnet.resourceserver.configuration.Globals.DID_AUTH_HEADER;
import static fi.trustnet.resourceserver.configuration.Globals.DID_TOKEN;
import static fi.trustnet.resourceserver.configuration.Globals.REVOKED;


@Controller
public class DataController {
    @Autowired
    AccountRepository accountRepository;
    @Autowired
    DataRepository dataRepository;
    @RequestMapping(value = "/data", method = RequestMethod.GET)
    ResponseEntity<?> dataRequest(HttpServletRequest request) {

        //check that caller controls the DID
        String didAuth = request.getHeader(DID_AUTH_HEADER);
        if (DidAuth.verifyToken(didAuth)) {

            String didToken = request.getHeader(DID_TOKEN);
            Token accessToken = AccessToken.decrytAccessToken(didToken);
            if (accessToken == null) {
                return new ResponseEntity<Object>("UNAUTHORIZED", HttpStatus.UNAUTHORIZED);
            }
            String tkn = new String(Base64.getDecoder().decode(didAuth));
            String[] splitToken = tkn.split("\\s+");

            //check that token was issued to the DID caller controls

            if (!splitToken[0].equals(accessToken.getIssuedto())) {
                return new ResponseEntity<Object>("UNAUTHORIZED", HttpStatus.UNAUTHORIZED);
            }

            //check revocation status
            if (isRevoked(accessToken.getRevocationurl())) {
                return new ResponseEntity<Object>("UNAUTHORIZED", HttpStatus.UNAUTHORIZED);
            }

            Account account = accountRepository.findByDid(accessToken.getUserdid());
            Data data = dataRepository.findByAccount(account);
            return new ResponseEntity<Object>(data, HttpStatus.OK);
        }
        else {
            return new ResponseEntity<Object>("UNAUTHORIZED", HttpStatus.UNAUTHORIZED);
        }
    }

    private Boolean isRevoked(String revocationUrl) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> responseEntity = restTemplate.exchange(revocationUrl, HttpMethod.GET, entity, String.class);

        if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
            String revocationResponse = responseEntity.getBody();

            ObjectMapper mapper = new ObjectMapper();

            Boolean revoked = false;

            try {
                JsonNode jn = mapper.readValue(revocationResponse, JsonNode.class);
                revoked = jn.get(REVOKED).asBoolean();
            } catch (Exception e) {
                return true;
            }

            if (revoked) {
                return true;
            } else {
                return false;
            }
        }
        else {
            return false;
        }
    }
}
