package fi.trustnet.agent.repository;

import fi.trustnet.agent.domain.Account;
import fi.trustnet.agent.domain.CredentialRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CredentialRequestRepository extends JpaRepository<CredentialRequest, Long> {
    CredentialRequest findByAccount(Account account);
    CredentialRequest findByAccountAndIdentifier(Account account, String identifier);

}
