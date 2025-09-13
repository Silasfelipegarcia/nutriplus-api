package br.com.nutriplus.application.web;
import br.com.nutriplus.application.service.OnboardingService; import br.com.nutriplus.domain.model.UserProfile; import br.com.nutriplus.application.web.dto.OnboardingDTO;
import jakarta.validation.Valid; import org.springframework.http.ResponseEntity; import org.springframework.web.bind.annotation.*; import java.net.URI; import java.util.UUID;
@RestController public class OnboardingController {
  private final OnboardingService onboardingService;
  public OnboardingController(OnboardingService onboardingService){ this.onboardingService=onboardingService; }
  @PostMapping("/onboarding") public ResponseEntity<Void> onboard(@RequestBody @Valid OnboardingDTO request){
    UserProfile userProfile = UserProfile.builder().id(UUID.randomUUID()).name(request.name()).sex(request.sex()).age(request.age()).heightCm(request.heightCm()).weightKg(request.weightKg()).trainingDaysPerWeek(request.trainingDaysPerWeek()).dislikes(request.dislikes()).style(request.style()).build();
    UUID userId = onboardingService.onboard(userProfile, request.goalType(), request.pace());
    return ResponseEntity.created(URI.create("/users/" + userId)).build();
  }
}