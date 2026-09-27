package br.com.fiap.predit.risk.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface LeadRepository extends JpaRepository<Lead, UUID> {
    List<Lead> findAllByOrderByPriorityDescCreatedAtAsc();
    long countByStatus(LeadStatus status);
    long countByStatusIn(List<LeadStatus> statuses);
}
