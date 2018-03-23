package fi.trustnet.agent.repository;

import fi.trustnet.agent.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;


public interface AccountRepository extends JpaRepository<Account, Long> {
    Account findByUsername(String username);
    Account findByDid(String did);
}
