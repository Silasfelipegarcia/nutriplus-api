package br.com.nutriplus.repository;

import br.com.nutriplus.domain.entity.Household;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HouseholdRepository extends JpaRepository<Household, Long> {

    Optional<Household> findByOwner_Id(Long ownerUserId);
}
