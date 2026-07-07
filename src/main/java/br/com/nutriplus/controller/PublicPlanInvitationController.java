package br.com.nutriplus.controller;

import br.com.nutriplus.dto.response.PlanInvitationPreviewResponse;
import br.com.nutriplus.service.HouseholdService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public/plan-invitations")
public class PublicPlanInvitationController {

    private final HouseholdService householdService;

    public PublicPlanInvitationController(HouseholdService householdService) {
        this.householdService = householdService;
    }

    @GetMapping("/{token}")
    public PlanInvitationPreviewResponse preview(@PathVariable String token) {
        return householdService.previewInvitation(token);
    }
}
