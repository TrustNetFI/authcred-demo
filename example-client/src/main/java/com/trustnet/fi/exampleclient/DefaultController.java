package com.trustnet.fi.exampleclient;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import static com.trustnet.fi.exampleclient.CredentialController.dataResponse;
import static com.trustnet.fi.exampleclient.CredentialController.issuedCredential;
import static com.trustnet.fi.exampleclient.CredentialController.token;

@Controller
public class DefaultController {
    @RequestMapping(value = "/inbox", method = RequestMethod.GET)
    public String view(Model model){
        model.addAttribute("credential", issuedCredential);
        model.addAttribute("token", token);
        model.addAttribute("data", dataResponse);
        return "mainview";
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String handleDefault(Model model){
        return "redirect:/inbox";
    }

}
