package br.com.nutriplus.controller;

import br.com.nutriplus.dto.request.BioimpedanceExtractRequest;
import br.com.nutriplus.dto.request.BodyMeasurementRequest;
import br.com.nutriplus.dto.request.ProgressReviewRequest;
import br.com.nutriplus.dto.response.BioimpedanceExtractResponse;
import br.com.nutriplus.dto.response.BodyMeasurementResponse;
import br.com.nutriplus.dto.response.EvolutionReportResponse;
import br.com.nutriplus.dto.response.ProgressReviewResponse;
import br.com.nutriplus.dto.response.GoalTimelineResponse;
import br.com.nutriplus.dto.response.ProgressScheduleResponse;
import br.com.nutriplus.service.GoalTimelineService;
import br.com.nutriplus.service.ProgressService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/progress")
public class ProgressController {

    private final ProgressService progressService;
    private final GoalTimelineService goalTimelineService;

    public ProgressController(ProgressService progressService, GoalTimelineService goalTimelineService) {
        this.progressService = progressService;
        this.goalTimelineService = goalTimelineService;
    }

    @GetMapping("/schedule")
    public ProgressScheduleResponse schedule() {
        return progressService.getSchedule();
    }

    @PostMapping("/measurements")
    public BodyMeasurementResponse saveMeasurement(@Valid @RequestBody BodyMeasurementRequest request) {
        return progressService.saveMeasurement(request);
    }

    @GetMapping("/measurements/latest")
    public BodyMeasurementResponse latestMeasurement() {
        return progressService.getLatestMeasurement();
    }

    @PostMapping("/bioimpedance/extract")
    public BioimpedanceExtractResponse extractBioimpedance(@Valid @RequestBody BioimpedanceExtractRequest request) {
        return progressService.extractBioimpedance(request);
    }

    @PostMapping("/reviews")
    public ProgressReviewResponse generateReview(@RequestBody(required = false) ProgressReviewRequest request) {
        return progressService.generateReview(request);
    }

    @GetMapping("/reviews/latest")
    public ProgressReviewResponse latestReview() {
        return progressService.getLatestReview();
    }

    @GetMapping("/evolution")
    public EvolutionReportResponse evolution() {
        return progressService.getEvolutionReport();
    }

    @GetMapping("/goal-timeline")
    public GoalTimelineResponse goalTimeline() {
        return goalTimelineService.getGoalTimeline();
    }
}
