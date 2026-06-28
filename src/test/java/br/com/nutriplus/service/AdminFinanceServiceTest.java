package br.com.nutriplus.service;

import br.com.nutriplus.domain.enums.SubscriptionPlan;
import br.com.nutriplus.domain.enums.UserRole;
import br.com.nutriplus.dto.response.AdminFinanceOverviewResponse;
import br.com.nutriplus.repository.ConsultationRepository;
import br.com.nutriplus.repository.PaymentOrderRepository;
import br.com.nutriplus.repository.UserRepository;
import br.com.nutriplus.security.AuthorizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminFinanceServiceTest {

    @Mock private AuthorizationService authorizationService;
    @Mock private PaymentOrderRepository paymentOrderRepository;
    @Mock private ConsultationRepository consultationRepository;
    @Mock private UserRepository userRepository;
    @Mock private SubscriptionPlanCatalogService planCatalogService;

    private AdminFinanceService service;

    @BeforeEach
    void setUp() {
        service = new AdminFinanceService(
                authorizationService,
                paymentOrderRepository,
                consultationRepository,
                userRepository,
                planCatalogService);
    }

    @Test
    void overviewAggregatesSubscriptionAndProRevenue() {
        when(authorizationService.hasRole(UserRole.ADMIN)).thenReturn(true);
        when(paymentOrderRepository.sumApprovedAmountBetween(any(Instant.class), any(Instant.class)))
                .thenReturn(2990L);
        when(paymentOrderRepository.countApprovedBetween(any(Instant.class), any(Instant.class)))
                .thenReturn(2L);
        when(consultationRepository.sumPlatformFeeBetween(any(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(1500L);
        when(consultationRepository.countPaidBetween(any(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(1L);
        when(userRepository.countActivePaidSubscriptions(any(), any(Instant.class))).thenReturn(5L);
        when(userRepository.countActiveSubscriptionsByPlan(any(), any(Instant.class)))
                .thenReturn(List.<Object[]>of(new Object[]{SubscriptionPlan.ESSENTIAL_MONTHLY, 2L}));
        when(planCatalogService.priceCents(SubscriptionPlan.ESSENTIAL_MONTHLY)).thenReturn(2990);

        AdminFinanceOverviewResponse response = service.overview(2026, 6);

        assertThat(response.subscriptionGrossCents()).isEqualTo(2990);
        assertThat(response.subscriptionPaymentCount()).isEqualTo(2);
        assertThat(response.proPlatformFeeCents()).isEqualTo(1500);
        assertThat(response.proConsultationCount()).isEqualTo(1);
        assertThat(response.totalPlatformRevenueCents()).isEqualTo(4490);
        assertThat(response.activePaidSubscriptions()).isEqualTo(5);
        assertThat(response.monthlyRecurringRevenueCents()).isEqualTo(5980);
        assertThat(response.projectedYearlyRevenueCents()).isEqualTo(5980 * 12);
        assertThat(response.history()).hasSize(6);
    }
}
