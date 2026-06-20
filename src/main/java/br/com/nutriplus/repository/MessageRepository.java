package br.com.nutriplus.repository;

import br.com.nutriplus.domain.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByThreadIdOrderByCreatedAtAsc(Long threadId);
}
