package br.com.nutriplus.controller;

import br.com.nutriplus.dto.request.BodyMeasurementRequest;
import br.com.nutriplus.dto.response.BodyMeasurementResponse;
import br.com.nutriplus.dto.response.EvolutionReportResponse;
import br.com.nutriplus.dto.response.ProgressReviewResponse;
import br.com.nutriplus.dto.response.ProgressScheduleResponse;
import br.com.nutriplus.service.ProgressService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/progress")
public class ProgressController {

    private final ProgressService progressService;

    public ProgressController(ProgressService progressService) {
        this.progressService = progressService;
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

    @PostMapping("/reviews")
    public ProgressReviewResponse generateReview() {
        return progressService.generateReview();
    }

    @GetMapping("/reviews/latest")
    public ProgressReviewResponse latestReview() {
        return progressService.getLatestReview();
    }

    @GetMapping("/evolution")
    public EvolutionReportResponse evolution() {
        return progressService.getEvolutionReport();
    }
}
