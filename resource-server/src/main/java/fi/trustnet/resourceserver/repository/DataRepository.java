package fi.trustnet.resourceserver.repository;

import fi.trustnet.resourceserver.domain.Account;
import fi.trustnet.resourceserver.domain.Data;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DataRepository extends JpaRepository<Data, Long> {
    Data findByAccount(Account account);
}
