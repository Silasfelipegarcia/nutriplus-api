package br.com.nutriplus.repository;

import br.com.nutriplus.domain.entity.ConversationThread;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ConversationThreadRepository extends JpaRepository<ConversationThread, Long> {

    Optional<ConversationThread> findByCareRelationshipId(Long careRelationshipId);

    @Query("""
            SELECT t FROM ConversationThread t
            JOIN t.careRelationship cr
            WHERE cr.patient.id = :userId OR cr.nutritionist.user.id = :userId
            ORDER BY t.createdAt DESC
            """)
    List<ConversationThread> findByParticipantUserId(Long userId);
}
