package fi.trustnet.resourceserver.controller;

import fi.trustnet.resourceserver.Sovrin.AccessToken;
import fi.trustnet.resourceserver.token.Token;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;

import static fi.trustnet.resourceserver.configuration.Globals.DID_TOKEN;

@Controller
public class VerifyAccessTokenController {

    //endpoint for testing purposes
    @RequestMapping(value = "/decryptaccesstoken", method = RequestMethod.POST)
    public ResponseEntity<?> verifyToken(HttpServletRequest request) {
        String accessToken = request.getHeader(DID_TOKEN);
        Token token = AccessToken.decrytAccessToken(accessToken);
        return new ResponseEntity<Object>(token, HttpStatus.OK);
    }
}
