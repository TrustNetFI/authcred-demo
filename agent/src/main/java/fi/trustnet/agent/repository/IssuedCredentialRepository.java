package fi.trustnet.agent.repository;

import fi.trustnet.agent.domain.Account;
import fi.trustnet.agent.domain.IssuedCredential;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IssuedCredentialRepository extends JpaRepository<IssuedCredential, Long> {
    IssuedCredential findByAccount(Account account);
    IssuedCredential findByAccountAndIdentifier(Account account, String identifier);

}
