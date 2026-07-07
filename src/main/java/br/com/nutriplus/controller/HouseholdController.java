package br.com.nutriplus.controller;

import br.com.nutriplus.dto.request.CreateHouseholdInvitationRequest;
import br.com.nutriplus.dto.request.ShareMealPlanRequest;
import br.com.nutriplus.dto.response.*;
import br.com.nutriplus.service.HouseholdService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/households")
public class HouseholdController {

    private final HouseholdService householdService;

    public HouseholdController(HouseholdService householdService) {
        this.householdService = householdService;
    }

    @GetMapping("/me")
    public ResponseEntity<HouseholdResponse> getMyHousehold() {
        HouseholdResponse response = householdService.getMyHousehold();
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/share-plan")
    @ResponseStatus(HttpStatus.CREATED)
    public HouseholdResponse sharePlan(@RequestBody(required = false) ShareMealPlanRequest request) {
        return householdService.shareCurrentPlan(request);
    }

    @PostMapping("/me/invitations")
    @ResponseStatus(HttpStatus.CREATED)
    public HouseholdInvitationCreatedResponse createInvitation(
            @Valid @RequestBody CreateHouseholdInvitationRequest request) {
        return householdService.createInvitation(request);
    }

    @PostMapping("/plan-invitations/{token}/accept")
    public AcceptHouseholdInvitationResponse acceptInvitation(@PathVariable String token) {
        return householdService.acceptInvitation(token);
    }

    @PostMapping("/me/leave")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void leaveHousehold() {
        householdService.leaveHousehold();
    }

    @GetMapping("/me/shopping-list")
    public HouseholdShoppingListResponse aggregatedShoppingList() {
        return householdService.getAggregatedShoppingList();
    }
}
