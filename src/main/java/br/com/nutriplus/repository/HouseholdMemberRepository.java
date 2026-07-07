package br.com.nutriplus.repository;

import br.com.nutriplus.domain.entity.HouseholdMember;
import br.com.nutriplus.domain.enums.HouseholdMemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface HouseholdMemberRepository extends JpaRepository<HouseholdMember, Long> {

    Optional<HouseholdMember> findByUserIdAndStatus(Long userId, HouseholdMemberStatus status);

    List<HouseholdMember> findByHouseholdIdAndStatus(Long householdId, HouseholdMemberStatus status);

    long countByHouseholdIdAndStatus(Long householdId, HouseholdMemberStatus status);

    @Query("SELECT hm FROM HouseholdMember hm JOIN FETCH hm.household h JOIN FETCH h.owner WHERE hm.user.id = :userId AND hm.status = :status")
    Optional<HouseholdMember> findActiveWithHousehold(Long userId, HouseholdMemberStatus status);
}
