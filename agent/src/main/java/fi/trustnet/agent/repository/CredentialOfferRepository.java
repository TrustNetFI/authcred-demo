package fi.trustnet.agent.repository;

import fi.trustnet.agent.domain.Account;
import fi.trustnet.agent.domain.CredentialOffer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CredentialOfferRepository extends JpaRepository<CredentialOffer, Long> {
    CredentialOffer findByAccount(Account account);
    CredentialOffer findByAccountAndOfferid(Account account, String offerid);
}
