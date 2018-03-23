package fi.trustnet.agent.repository;

import fi.trustnet.agent.domain.Account;
import fi.trustnet.agent.domain.ReceivedCredential;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReceivedCredentialRepository extends JpaRepository<ReceivedCredential, Long> {
    ReceivedCredential findByAccount(Account account);
    ReceivedCredential findByAccountAndIssuer(Account account, String issuer);
}
