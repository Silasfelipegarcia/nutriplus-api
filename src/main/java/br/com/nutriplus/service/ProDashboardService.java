package br.com.nutriplus.service;

import br.com.nutriplus.domain.entity.Consultation;
import br.com.nutriplus.domain.entity.Nutritionist;
import br.com.nutriplus.domain.enums.CareRelationshipStatus;
import br.com.nutriplus.domain.enums.ConsultationStatus;
import br.com.nutriplus.dto.response.CareRelationshipResponse;
import br.com.nutriplus.dto.response.ProDashboardResponse;
import br.com.nutriplus.dto.response.RevenueReportResponse;
import br.com.nutriplus.mapper.ProMapper;
import br.com.nutriplus.repository.CareRelationshipRepository;
import br.com.nutriplus.repository.ConsultationRepository;
import br.com.nutriplus.security.AuthorizationService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProDashboardService {

    private final AuthorizationService authorizationService;
    private final CareRelationshipRepository careRelationshipRepository;
    private final ConsultationRepository consultationRepository;
    private final ProMapper proMapper;

    public ProDashboardService(AuthorizationService authorizationService,
                               CareRelationshipRepository careRelationshipRepository,
                               ConsultationRepository consultationRepository,
                               ProMapper proMapper) {
        this.authorizationService = authorizationService;
        this.careRelationshipRepository = careRelationshipRepository;
        this.consultationRepository = consultationRepository;
        this.proMapper = proMapper;
    }

    public ProDashboardResponse getDashboard() {
        Nutritionist nutritionist = authorizationService.requireNutritionist();
        var all = careRelationshipRepository.findByNutritionistIdOrderByUpdatedAtDesc(nutritionist.getId());

        int active = (int) all.stream().filter(c -> c.getStatus() == CareRelationshipStatus.ACTIVE).count();
        int preEngaged = (int) all.stream().filter(c -> c.getStatus() == CareRelationshipStatus.PRE_ENGAGED).count();
        int pending = (int) all.stream().filter(c -> c.getStatus() == CareRelationshipStatus.PENDING_PAYMENT).count();

        YearMonth now = YearMonth.now();
        LocalDateTime from = now.atDay(1).atStartOfDay();
        LocalDateTime to = now.plusMonths(1).atDay(1).atStartOfDay();
        List<Consultation> paid = consultationRepository.findPaidByNutritionistBetween(
                nutritionist.getId(), ConsultationStatus.PAID, from, to);

        int gross = paid.stream().mapToInt(Consultation::getAmountCents).sum();
        int net = paid.stream().mapToInt(Consultation::getNetCents).sum();

        List<CareRelationshipResponse> recent = all.stream().limit(10).map(proMapper::toCare).toList();

        return new ProDashboardResponse(active, preEngaged, pending, paid.size(), gross, net, recent);
    }

    public RevenueReportResponse getRevenueReport(int year, int month) {
        Nutritionist nutritionist = authorizationService.requireNutritionist();
        YearMonth ym = YearMonth.of(year, month);
        LocalDateTime from = ym.atDay(1).atStartOfDay();
        LocalDateTime to = ym.plusMonths(1).atDay(1).atStartOfDay();

        List<Consultation> paid = consultationRepository.findPaidByNutritionistBetween(
                nutritionist.getId(), ConsultationStatus.PAID, from, to);

        int gross = paid.stream().mapToInt(Consultation::getAmountCents).sum();
        int platformFee = paid.stream().mapToInt(Consultation::getPlatformFeeCents).sum();
        int net = paid.stream().mapToInt(Consultation::getNetCents).sum();
        int count = paid.size();
        int avg = count > 0 ? gross / count : 0;

        List<RevenueReportResponse.MonthlyRevenuePoint> history = new ArrayList<>();
        YearMonth cursor = ym.minusMonths(5);
        for (int i = 0; i < 6; i++) {
            LocalDateTime hFrom = cursor.atDay(1).atStartOfDay();
            LocalDateTime hTo = cursor.plusMonths(1).atDay(1).atStartOfDay();
            var monthPaid = consultationRepository.findPaidByNutritionistBetween(
                    nutritionist.getId(), ConsultationStatus.PAID, hFrom, hTo);
            int mGross = monthPaid.stream().mapToInt(Consultation::getAmountCents).sum();
            int mNet = monthPaid.stream().mapToInt(Consultation::getNetCents).sum();
            history.add(new RevenueReportResponse.MonthlyRevenuePoint(
                    cursor.getYear(), cursor.getMonthValue(), mGross, mNet));
            cursor = cursor.plusMonths(1);
        }

        return new RevenueReportResponse(year, month, gross, platformFee, net, count, avg, history);
    }
}
