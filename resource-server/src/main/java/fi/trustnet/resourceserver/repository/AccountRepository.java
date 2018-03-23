package fi.trustnet.resourceserver.repository;


import fi.trustnet.resourceserver.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;


public interface AccountRepository extends JpaRepository<Account, Long> {
    Account findByUsername(String username);
    Account findByDid(String did);
}
