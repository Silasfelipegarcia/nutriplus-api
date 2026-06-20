package br.com.nutriplus.controller;

import br.com.nutriplus.dto.request.SubmitAppFeedbackRequest;
import br.com.nutriplus.dto.response.AppFeedbackResponse;
import br.com.nutriplus.service.FeedbackService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping("/app")
    @ResponseStatus(HttpStatus.CREATED)
    public AppFeedbackResponse submit(@Valid @RequestBody SubmitAppFeedbackRequest request) {
        return feedbackService.submit(request);
    }

    @GetMapping("/app/latest")
    public AppFeedbackResponse latest() {
        return feedbackService.getLatest();
    }
}
