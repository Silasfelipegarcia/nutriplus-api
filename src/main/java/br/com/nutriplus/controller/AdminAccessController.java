package br.com.nutriplus.controller;

import br.com.nutriplus.domain.enums.AdminUserAccessStatus;
import br.com.nutriplus.domain.enums.RegistrationSource;
import br.com.nutriplus.domain.enums.UserRole;
import br.com.nutriplus.dto.request.RejectUserAccessRequest;
import br.com.nutriplus.dto.request.UpdateLoginEnabledRequest;
import br.com.nutriplus.dto.request.UpdateUserAdminRequest;
import br.com.nutriplus.dto.response.AdminAccessSummaryResponse;
import br.com.nutriplus.dto.response.AdminUserAccessResponse;
import br.com.nutriplus.dto.response.PagedAdminUserAccessResponse;
import br.com.nutriplus.service.AdminAccessService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/access")
public class AdminAccessController {

    private final AdminAccessService adminAccessService;

    public AdminAccessController(AdminAccessService adminAccessService) {
        this.adminAccessService = adminAccessService;
    }

    @GetMapping("/summary")
    public AdminAccessSummaryResponse summary() {
        return adminAccessService.summary();
    }

    @GetMapping("/pending")
    public List<AdminUserAccessResponse> pending() {
        return adminAccessService.listPending();
    }

    @GetMapping("/approved")
    public List<AdminUserAccessResponse> approved() {
        return adminAccessService.listApproved();
    }

    @GetMapping("/users")
    public PagedAdminUserAccessResponse searchUsers(
            @RequestParam(required = false) AdminUserAccessStatus status,
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) RegistrationSource registrationSource,
            @RequestParam(required = false) Boolean hasNutritionProfile,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return adminAccessService.searchUsers(
                status, role, registrationSource, hasNutritionProfile, search, page, size);
    }

    @GetMapping("/admins")
    public List<AdminUserAccessResponse> admins() {
        return adminAccessService.listAdmins();
    }

    @PatchMapping("/users/{id}/login-enabled")
    public AdminUserAccessResponse setLoginEnabled(@PathVariable Long id,
                                                   @Valid @RequestBody UpdateLoginEnabledRequest request) {
        return adminAccessService.setLoginEnabled(id, request);
    }

    @PostMapping("/users/{id}/reject")
    public AdminUserAccessResponse rejectAccess(@PathVariable Long id,
                                                @Valid @RequestBody(required = false) RejectUserAccessRequest request) {
        return adminAccessService.rejectAccess(id, request);
    }

    @PatchMapping("/users/{id}/admin")
    public AdminUserAccessResponse setUserAdmin(@PathVariable Long id,
                                                @Valid @RequestBody UpdateUserAdminRequest request) {
        return adminAccessService.setUserAdmin(id, request);
    }

    @DeleteMapping("/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        adminAccessService.deleteUser(id);
    }
}
