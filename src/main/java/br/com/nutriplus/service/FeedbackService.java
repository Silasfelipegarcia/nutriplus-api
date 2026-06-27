package br.com.nutriplus.service;

import br.com.nutriplus.domain.entity.User;
import br.com.nutriplus.domain.entity.UserAppFeedback;
import br.com.nutriplus.dto.request.SubmitAppFeedbackRequest;
import br.com.nutriplus.dto.response.AppFeedbackResponse;
import br.com.nutriplus.exception.ResourceNotFoundException;
import br.com.nutriplus.repository.UserAppFeedbackRepository;
import br.com.nutriplus.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FeedbackService {

    private static final String SURVEY_VERSION = "v1";
    private static final String TRIGGER_MANUAL = "MANUAL";
    private static final String SUCCESS_MESSAGE =
            "Obrigado! Sua opinião nos ajuda a melhorar o NutriPlus.";

    private final CurrentUser currentUser;
    private final UserAppFeedbackRepository feedbackRepository;
    private final AuditLogService auditLogService;

    public FeedbackService(CurrentUser currentUser,
                           UserAppFeedbackRepository feedbackRepository,
                           AuditLogService auditLogService) {
        this.currentUser = currentUser;
        this.feedbackRepository = feedbackRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public AppFeedbackResponse submit(SubmitAppFeedbackRequest request) {
        User user = currentUser.get();
        UserAppFeedback feedback = new UserAppFeedback(user);
        feedback.setSurveyVersion(SURVEY_VERSION);
        feedback.setTriggerContext(TRIGGER_MANUAL);
        feedback.setEaseOfUse(request.easeOfUse());
        feedback.setMealPlanQuality(request.mealPlanQuality());
        feedback.setAiHelpfulness(request.aiHelpfulness());
        feedback.setProgressTracking(request.progressTracking());
        feedback.setOverallSatisfaction(request.overallSatisfaction());
        feedback.setImprovementSuggestions(normalizeSuggestions(request.improvementSuggestions()));
        feedback.setAppVersion(trimToNull(request.appVersion()));
        feedback.setPlatform(trimToNull(request.platform()));

        feedback = feedbackRepository.save(feedback);
        auditLogService.log("APP_FEEDBACK_SUBMITTED", "user_app_feedback", user);

        return toResponse(feedback, SUCCESS_MESSAGE);
    }

    @Transactional(readOnly = true)
    public AppFeedbackResponse getLatest() {
        User user = currentUser.get();
        return feedbackRepository.findTopByUserIdOrderByCreatedAtDesc(user.getId())
                .map(f -> toResponse(f, null))
                .orElseThrow(() -> new ResourceNotFoundException("Nenhuma avaliação encontrada"));
    }

    private AppFeedbackResponse toResponse(UserAppFeedback feedback, String message) {
        return new AppFeedbackResponse(
                feedback.getId(),
                feedback.getCreatedAt(),
                message,
                feedback.getEaseOfUse(),
                feedback.getMealPlanQuality(),
                feedback.getAiHelpfulness(),
                feedback.getProgressTracking(),
                feedback.getOverallSatisfaction(),
                feedback.getImprovementSuggestions(),
                feedback.getAppVersion(),
                feedback.getPlatform()
        );
    }

    private static String normalizeSuggestions(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
