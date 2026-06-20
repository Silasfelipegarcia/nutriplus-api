package br.com.nutriplus.service;

import br.com.nutriplus.domain.entity.*;
import br.com.nutriplus.domain.enums.CareRelationshipStatus;
import br.com.nutriplus.dto.request.SendMessageRequest;
import br.com.nutriplus.dto.response.ConversationResponse;
import br.com.nutriplus.dto.response.MessageResponse;
import br.com.nutriplus.exception.BusinessException;
import br.com.nutriplus.exception.ResourceNotFoundException;
import br.com.nutriplus.mapper.ProMapper;
import br.com.nutriplus.repository.ConversationThreadRepository;
import br.com.nutriplus.repository.MessageRepository;
import br.com.nutriplus.security.AuthorizationService;
import br.com.nutriplus.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessagingService {

    private final CurrentUser currentUser;
    private final AuthorizationService authorizationService;
    private final ConversationThreadRepository threadRepository;
    private final MessageRepository messageRepository;
    private final ProMapper proMapper;

    public MessagingService(CurrentUser currentUser,
                            AuthorizationService authorizationService,
                            ConversationThreadRepository threadRepository,
                            MessageRepository messageRepository,
                            ProMapper proMapper) {
        this.currentUser = currentUser;
        this.authorizationService = authorizationService;
        this.threadRepository = threadRepository;
        this.messageRepository = messageRepository;
        this.proMapper = proMapper;
    }

    public List<ConversationResponse> listConversations() {
        User user = currentUser.get();
        return threadRepository.findByParticipantUserId(user.getId()).stream()
                .map(t -> toConversation(t, user.getId()))
                .toList();
    }

    public ConversationResponse getConversation(Long threadId) {
        User user = currentUser.get();
        ConversationThread thread = requireThreadAccess(threadId, user);
        return toConversation(thread, user.getId());
    }

    @Transactional
    public MessageResponse sendMessage(Long threadId, SendMessageRequest request) {
        User user = currentUser.get();
        ConversationThread thread = requireThreadAccess(threadId, user);
        CareRelationship care = thread.getCareRelationship();
        if (!care.allowsChat()) {
            throw new BusinessException("Chat disponível apenas com acompanhamento ativo.");
        }
        Message message = messageRepository.save(Message.send(thread, user, request.body()));
        return proMapper.toMessage(message, user.getId());
    }

    private ConversationThread requireThreadAccess(Long threadId, User user) {
        ConversationThread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversa não encontrada"));
        CareRelationship care = thread.getCareRelationship();
        boolean isPatient = care.getPatient().getId().equals(user.getId());
        boolean isNutritionist = care.getNutritionist().getUser().getId().equals(user.getId());
        if (!isPatient && !isNutritionist) {
            throw new BusinessException("Sem acesso a esta conversa.");
        }
        if (isNutritionist && !care.allowsNutritionistAccess()) {
            throw new BusinessException("Vínculo não permite acesso.");
        }
        if (isPatient && care.getStatus() != CareRelationshipStatus.ACTIVE
                && care.getStatus() != CareRelationshipStatus.PRE_ENGAGED) {
            throw new BusinessException("Acompanhamento não ativo.");
        }
        return thread;
    }

    private ConversationResponse toConversation(ConversationThread thread, Long currentUserId) {
        CareRelationship care = thread.getCareRelationship();
        String participantName = currentUserId.equals(care.getPatient().getId())
                ? care.getNutritionist().getUser().getName()
                : care.getPatient().getName();
        List<MessageResponse> messages = messageRepository.findByThreadIdOrderByCreatedAtAsc(thread.getId())
                .stream().map(m -> proMapper.toMessage(m, currentUserId)).toList();
        return new ConversationResponse(
                thread.getId(),
                care.getId(),
                participantName,
                proMapper.toCare(care),
                messages
        );
    }
}
