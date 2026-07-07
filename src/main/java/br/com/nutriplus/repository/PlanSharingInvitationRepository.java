package br.com.nutriplus.repository;

import br.com.nutriplus.domain.entity.PlanSharingInvitation;
import br.com.nutriplus.domain.enums.PlanSharingInvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PlanSharingInvitationRepository extends JpaRepository<PlanSharingInvitation, Long> {

    Optional<PlanSharingInvitation> findByToken(String token);

    @Query("SELECT i FROM PlanSharingInvitation i JOIN FETCH i.household h JOIN FETCH h.owner JOIN FETCH i.inviter WHERE i.token = :token")
    Optional<PlanSharingInvitation> findByTokenWithDetails(String token);

    List<PlanSharingInvitation> findByHouseholdIdAndStatus(Long householdId, PlanSharingInvitationStatus status);
}
