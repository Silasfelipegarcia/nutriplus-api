package br.com.nutriplus.service;

import br.com.nutriplus.domain.enums.ConsultationStatus;
import br.com.nutriplus.domain.enums.SubscriptionPlan;
import br.com.nutriplus.domain.enums.SubscriptionPlans;
import br.com.nutriplus.domain.enums.UserRole;
import br.com.nutriplus.dto.response.AdminFinanceOverviewResponse;
import br.com.nutriplus.dto.response.AdminFinanceOverviewResponse.MonthlyFinancePoint;
import br.com.nutriplus.exception.BusinessException;
import br.com.nutriplus.repository.ConsultationRepository;
import br.com.nutriplus.repository.PaymentOrderRepository;
import br.com.nutriplus.repository.UserRepository;
import br.com.nutriplus.security.AuthorizationService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
public class AdminFinanceService {

    private static final Set<SubscriptionPlan> PAID_PLANS = EnumSet.of(
            SubscriptionPlan.ESSENTIAL_MONTHLY,
            SubscriptionPlan.ESSENTIAL_YEARLY,
            SubscriptionPlan.ATHLETE_MONTHLY,
            SubscriptionPlan.ATHLETE_YEARLY
    );

    private static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");

    private final AuthorizationService authorizationService;
    private final PaymentOrderRepository paymentOrderRepository;
    private final ConsultationRepository consultationRepository;
    private final UserRepository userRepository;
    private final SubscriptionPlanCatalogService planCatalogService;

    public AdminFinanceService(AuthorizationService authorizationService,
                               PaymentOrderRepository paymentOrderRepository,
                               ConsultationRepository consultationRepository,
                               UserRepository userRepository,
                               SubscriptionPlanCatalogService planCatalogService) {
        this.authorizationService = authorizationService;
        this.paymentOrderRepository = paymentOrderRepository;
        this.consultationRepository = consultationRepository;
        this.userRepository = userRepository;
        this.planCatalogService = planCatalogService;
    }

    public AdminFinanceOverviewResponse overview(int year, int month) {
        requireAdmin();
        YearMonth ym = YearMonth.of(year, month);

        MonthTotals current = totalsForMonth(ym);
        int mrr = calculateMonthlyRecurringRevenue();
        int active = (int) userRepository.countActivePaidSubscriptions(PAID_PLANS, Instant.now());

        List<MonthlyFinancePoint> history = new ArrayList<>();
        YearMonth cursor = ym.minusMonths(5);
        for (int i = 0; i < 6; i++) {
            MonthTotals point = totalsForMonth(cursor);
            history.add(new MonthlyFinancePoint(
                    cursor.getYear(),
                    cursor.getMonthValue(),
                    point.subscriptionGrossCents(),
                    point.proPlatformFeeCents(),
                    point.totalPlatformRevenueCents()));
            cursor = cursor.plusMonths(1);
        }

        return new AdminFinanceOverviewResponse(
                year,
                month,
                current.subscriptionGrossCents(),
                current.subscriptionPaymentCount(),
                current.proPlatformFeeCents(),
                current.proConsultationCount(),
                current.totalPlatformRevenueCents(),
                active,
                mrr,
                mrr * 12,
                history
        );
    }

    private MonthTotals totalsForMonth(YearMonth ym) {
        Instant from = ym.atDay(1).atStartOfDay(ZONE).toInstant();
        Instant to = ym.plusMonths(1).atDay(1).atStartOfDay(ZONE).toInstant();
        LocalDateTime fromLocal = ym.atDay(1).atStartOfDay();
        LocalDateTime toLocal = ym.plusMonths(1).atDay(1).atStartOfDay();

        long subscriptionGross = paymentOrderRepository.sumApprovedAmountBetween(from, to);
        long subscriptionCount = paymentOrderRepository.countApprovedBetween(from, to);
        long proFees = consultationRepository.sumPlatformFeeBetween(ConsultationStatus.PAID, fromLocal, toLocal);
        long proCount = consultationRepository.countPaidBetween(ConsultationStatus.PAID, fromLocal, toLocal);

        int gross = Math.toIntExact(subscriptionGross);
        int fees = Math.toIntExact(proFees);
        return new MonthTotals(
                gross,
                (int) subscriptionCount,
                fees,
                (int) proCount,
                gross + fees
        );
    }

    private int calculateMonthlyRecurringRevenue() {
        int total = 0;
        for (Object[] row : userRepository.countActiveSubscriptionsByPlan(PAID_PLANS, Instant.now())) {
            SubscriptionPlan plan = (SubscriptionPlan) row[0];
            long count = (Long) row[1];
            total += monthlyEquivalentCents(plan) * (int) count;
        }
        return total;
    }

    private int monthlyEquivalentCents(SubscriptionPlan plan) {
        int price = planCatalogService.priceCents(plan);
        if (SubscriptionPlans.isYearlyPlan(plan)) {
            return Math.max(0, Math.round(price / 12.0f));
        }
        return price;
    }

    private void requireAdmin() {
        if (!authorizationService.hasRole(UserRole.ADMIN)) {
            throw new BusinessException("Acesso restrito a administradores.");
        }
    }

    private record MonthTotals(
            int subscriptionGrossCents,
            int subscriptionPaymentCount,
            int proPlatformFeeCents,
            int proConsultationCount,
            int totalPlatformRevenueCents
    ) {
    }
}
