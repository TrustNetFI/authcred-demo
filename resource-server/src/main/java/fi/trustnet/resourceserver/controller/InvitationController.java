package fi.trustnet.resourceserver.controller;

import fi.trustnet.resourceserver.Sovrin.DidAuth;
import fi.trustnet.resourceserver.domain.Account;
import fi.trustnet.resourceserver.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import java.util.Base64;

import static fi.trustnet.resourceserver.configuration.IndyConfig.RS_DID;
import static fi.trustnet.resourceserver.configuration.IndyConfig.RS_WALLETNAME;


@Controller
public class InvitationController {
    @Autowired
    AccountRepository accountRepository;

    @RequestMapping(value = "/connect", method = RequestMethod.GET)
    public String viewConnectionProposal(Model model, @RequestParam("token") String token, @RequestParam("returnurl") String returnUrl,
                                         @RequestParam("claimofferurl") String claimofferurl){
        model.addAttribute("returnurl", returnUrl);
        model.addAttribute("token", token);
        model.addAttribute("claimofferurl", claimofferurl);
        return "connection";
    }

    @RequestMapping(value = "/connect", method = RequestMethod.DELETE)
    public ResponseEntity<String> deleteConnection(HttpServletRequest request){
        String token = request.getHeader("didauth");
        if (DidAuth.verifyToken(token)){
            String tkn = new String(Base64.getDecoder().decode(token));
            String[] splitToken = tkn.split("\\s+");
            Account account = accountRepository.findByDid(splitToken[0]);
            account.setDid("");
            accountRepository.save(account);
            return new ResponseEntity<String>("connection removed", HttpStatus.OK);
        }

        return new ResponseEntity<String>("could not remove connection", HttpStatus.INTERNAL_SERVER_ERROR);
    }



    @RequestMapping(value = "/acceptconnection", method = RequestMethod.GET)
    public RedirectView acceptConnection(Model model, @RequestParam("token") String token, @RequestParam("returnurl") String returnUrl,
                                         @RequestParam("claimofferurl") String claimofferurl){

        if (DidAuth.verifyToken(token))
        {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Account account = accountRepository.findByUsername(auth.getName());
            String tkn = new String(Base64.getDecoder().decode(token));
            String[] splitToken = tkn.split("\\s+");
            account.setDid(splitToken[0]);
            accountRepository.save(account);
            //Send claim offer to connection
            postCredentialOffer(account.getDid(),claimofferurl);

        }
        return new RedirectView(returnUrl + "?success=true");
    }

    private void postCredentialOffer(String recipientDid, String invitationUrl) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        String didToken = DidAuth.createToken(RS_WALLETNAME,RS_DID);
        headers.add("didauth", didToken);

        String claimOffer = "{\"offerid\":\"oa92kdsksl\",\"type\" : \"customer info credential\", " +
                "\"info\" : \"credential containing customer ID and delegation capabilities\", " +
                "\"issuerdid\":\" " + RS_DID +"\"," +
                "\"url\" : \"http://localhost:8090/credential\", \"subject\":\"" +recipientDid + "\",\"issuer\":\"Company X\"}";


        HttpEntity<String> entity = new HttpEntity<>(claimOffer, headers);
        ResponseEntity<String> responseEntity = restTemplate.exchange( invitationUrl, HttpMethod.POST, entity, String.class);
        
    }


    @RequestMapping(value = "/cancelconnection/{returnUrl}", method = RequestMethod.GET)
    public RedirectView cancelConnection(Model model, @PathVariable String returnUrl){

        return new RedirectView(returnUrl + "?success=false");
    }

}
