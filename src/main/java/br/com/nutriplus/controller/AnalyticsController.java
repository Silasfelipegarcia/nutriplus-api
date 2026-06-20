package br.com.nutriplus.controller;

import br.com.nutriplus.dto.request.ProductEventsBatchRequest;
import br.com.nutriplus.dto.response.ProductEventsAcceptedResponse;
import br.com.nutriplus.service.ProductEventService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/analytics")
public class AnalyticsController {

    private final ProductEventService productEventService;

    public AnalyticsController(ProductEventService productEventService) {
        this.productEventService = productEventService;
    }

    @PostMapping("/events")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ProductEventsAcceptedResponse ingest(@Valid @RequestBody ProductEventsBatchRequest request) {
        int accepted = productEventService.ingest(request);
        return new ProductEventsAcceptedResponse(accepted);
    }
}
