package br.com.nutriplus.controller;

import br.com.nutriplus.dto.request.FoodExtraRequest;
import br.com.nutriplus.dto.request.MealCheckinRequest;
import br.com.nutriplus.dto.response.CheckinAdherenceHistoryResponse;
import br.com.nutriplus.dto.response.CheckinStatsResponse;
import br.com.nutriplus.dto.response.CoachInsightResponse;
import br.com.nutriplus.dto.response.DailyFoodExtraResponse;
import br.com.nutriplus.dto.response.TodayCheckinsResponse;
import br.com.nutriplus.dto.response.TodayMealCheckinResponse;
import br.com.nutriplus.service.CheckinService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/checkins")
public class CheckinController {

    private final CheckinService checkinService;

    public CheckinController(CheckinService checkinService) {
        this.checkinService = checkinService;
    }

    @GetMapping("/today")
    public TodayCheckinsResponse today() {
        return checkinService.getToday();
    }

    @PostMapping
    public TodayMealCheckinResponse save(@Valid @RequestBody MealCheckinRequest request) {
        return checkinService.saveCheckin(request);
    }

    @PostMapping("/extras")
    public DailyFoodExtraResponse addExtra(@Valid @RequestBody FoodExtraRequest request) {
        return checkinService.addFoodExtra(request);
    }

    @PostMapping("/balance-insight")
    public CoachInsightResponse balanceInsight() {
        return checkinService.getBalanceCoachInsight();
    }

    @GetMapping("/stats")
    public CheckinStatsResponse stats() {
        return checkinService.getStats();
    }

    @GetMapping("/adherence")
    public CheckinAdherenceHistoryResponse adherence(@RequestParam(defaultValue = "7") int days) {
        return checkinService.getAdherenceHistory(days);
    }
}
