package br.com.nutriplus.service;

import br.com.nutriplus.domain.entity.ProductEvent;
import br.com.nutriplus.domain.entity.User;
import br.com.nutriplus.dto.request.ProductEventItemRequest;
import br.com.nutriplus.dto.request.ProductEventsBatchRequest;
import br.com.nutriplus.infrastructure.web.CorrelationIdFilter;
import br.com.nutriplus.repository.ProductEventRepository;
import br.com.nutriplus.security.CurrentUser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductEventService {

    private final ProductEventRepository productEventRepository;
    private final ObjectMapper objectMapper;
    private final CurrentUser currentUser;

    public ProductEventService(ProductEventRepository productEventRepository,
                               ObjectMapper objectMapper,
                               CurrentUser currentUser) {
        this.productEventRepository = productEventRepository;
        this.objectMapper = objectMapper;
        this.currentUser = currentUser;
    }

    @Transactional
    public int ingest(ProductEventsBatchRequest request) {
        User user = currentUser.get();
        String correlationId = MDC.get(CorrelationIdFilter.MDC_KEY);
        List<ProductEvent> events = new ArrayList<>(request.events().size());

        for (ProductEventItemRequest item : request.events()) {
            events.add(ProductEvent.builder()
                    .sessionId(request.sessionId())
                    .user(user)
                    .eventName(item.eventName())
                    .step(item.step())
                    .propertiesJson(toJson(item.properties()))
                    .correlationId(correlationId)
                    .build());
        }

        productEventRepository.saveAll(events);
        return events.size();
    }

    private String toJson(Object value) {
        if (value == null || value instanceof java.util.Map<?, ?> map && map.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
