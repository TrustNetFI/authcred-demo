package fi.trustnet.agent.repository;

import fi.trustnet.agent.domain.Account;
import fi.trustnet.agent.domain.Connection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConnectionRepository extends JpaRepository<Connection, Long> {
    Connection findByAccount(Account account);
    Connection findByAccountAndIdentifier(Account account, String identifier);
}
