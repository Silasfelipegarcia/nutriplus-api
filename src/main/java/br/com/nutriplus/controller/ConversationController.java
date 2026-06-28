package br.com.nutriplus.controller;

import br.com.nutriplus.dto.request.SendMessageRequest;
import br.com.nutriplus.dto.response.ConversationResponse;
import br.com.nutriplus.dto.response.MessageResponse;
import br.com.nutriplus.service.MessagingService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/conversations")
public class ConversationController {

    private final MessagingService messagingService;

    public ConversationController(MessagingService messagingService) {
        this.messagingService = messagingService;
    }

    @GetMapping
    public List<ConversationResponse> list() {
        return messagingService.listConversations();
    }

    @GetMapping("/{threadId}")
    public ConversationResponse get(@PathVariable Long threadId,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "50") int size) {
        return messagingService.getConversation(threadId, page, size);
    }

    @PostMapping("/{threadId}/messages")
    public MessageResponse send(@PathVariable Long threadId,
                                @Valid @RequestBody SendMessageRequest request) {
        return messagingService.sendMessage(threadId, request);
    }
}
