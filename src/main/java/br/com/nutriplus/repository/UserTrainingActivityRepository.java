package br.com.nutriplus.repository;

import br.com.nutriplus.domain.entity.UserTrainingActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserTrainingActivityRepository extends JpaRepository<UserTrainingActivity, Long> {

    List<UserTrainingActivity> findByUserIdOrderByIdAsc(Long userId);

    void deleteByUserId(Long userId);
}
