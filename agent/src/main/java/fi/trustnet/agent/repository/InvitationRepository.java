package fi.trustnet.agent.repository;

import fi.trustnet.agent.domain.Account;
import fi.trustnet.agent.domain.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {
    Invitation findByAccountAndIdentifier(Account account, String identifier);
    Invitation findByAccount(Account account);
}
