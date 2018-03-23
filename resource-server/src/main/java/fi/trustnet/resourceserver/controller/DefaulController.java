package fi.trustnet.resourceserver.controller;

import fi.trustnet.resourceserver.domain.Account;
import fi.trustnet.resourceserver.domain.Data;
import fi.trustnet.resourceserver.repository.AccountRepository;
import fi.trustnet.resourceserver.repository.DataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javax.annotation.PostConstruct;

@Controller
public class DefaulController {
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private DataRepository dataRepository;

    @PostConstruct
    public void setup() {
        Account user = new Account();
        user.setUsername("alice");
        user = accountRepository.save(user);

        Data data = new Data();
        data.setAccount(user);
        data.setEntry("{\"data\" : \"value\"},\"onwner\" : \"alice\"");
        dataRepository.save(data);

    }
}
