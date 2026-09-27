package br.com.fiap.predit.risk.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RiskAssessmentRepository extends JpaRepository<RiskAssessment, UUID> {
    Optional<RiskAssessment> findFirstByVehicleIdOrderByAssessedAtDesc(UUID vehicleId);
    long countByLevelIn(List<RiskLevel> levels);

    @Query("select avg(r.score) from RiskAssessment r where r.assessedAt = " +
            "(select max(r2.assessedAt) from RiskAssessment r2 where r2.vehicle = r.vehicle)")
    Double averageLatestScore();
}
