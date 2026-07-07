package br.com.nutriplus.service;

import br.com.nutriplus.domain.entity.*;
import br.com.nutriplus.domain.enums.HouseholdMemberRole;
import br.com.nutriplus.domain.enums.HouseholdMemberStatus;
import br.com.nutriplus.domain.enums.PlanSharingInvitationStatus;
import br.com.nutriplus.dto.request.CreateHouseholdInvitationRequest;
import br.com.nutriplus.dto.request.ShareMealPlanRequest;
import br.com.nutriplus.dto.response.*;
import br.com.nutriplus.exception.BusinessException;
import br.com.nutriplus.exception.ResourceNotFoundException;
import br.com.nutriplus.repository.*;
import br.com.nutriplus.security.CurrentUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class HouseholdService {

    private static final int MAX_MEMBERS = 4;
    private static final int INVITE_EXPIRY_DAYS = 7;

    private final CurrentUser currentUser;
    private final HouseholdRepository householdRepository;
    private final HouseholdMemberRepository memberRepository;
    private final PlanSharingInvitationRepository invitationRepository;
    private final MealPlanRepository mealPlanRepository;
    private final ShoppingListRepository shoppingListRepository;
    private final MealPlanService mealPlanService;
    private final EmailSender emailSender;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${nutriplus.pro.invite-base-url:http://localhost:4200}")
    private String inviteBaseUrl;

    public HouseholdService(CurrentUser currentUser,
                            HouseholdRepository householdRepository,
                            HouseholdMemberRepository memberRepository,
                            PlanSharingInvitationRepository invitationRepository,
                            MealPlanRepository mealPlanRepository,
                            ShoppingListRepository shoppingListRepository,
                            MealPlanService mealPlanService,
                            EmailSender emailSender) {
        this.currentUser = currentUser;
        this.householdRepository = householdRepository;
        this.memberRepository = memberRepository;
        this.invitationRepository = invitationRepository;
        this.mealPlanRepository = mealPlanRepository;
        this.shoppingListRepository = shoppingListRepository;
        this.mealPlanService = mealPlanService;
        this.emailSender = emailSender;
    }

    public HouseholdResponse getMyHousehold() {
        User user = currentUser.get();
        return memberRepository.findActiveWithHousehold(user.getId(), HouseholdMemberStatus.ACTIVE)
                .map(m -> toResponse(m.getHousehold()))
                .orElse(null);
    }

    @Transactional
    public HouseholdResponse shareCurrentPlan(ShareMealPlanRequest request) {
        User user = currentUser.get();
        memberRepository.findByUserIdAndStatus(user.getId(), HouseholdMemberStatus.ACTIVE)
                .filter(m -> m.getRole() != HouseholdMemberRole.OWNER)
                .ifPresent(m -> {
                    throw new BusinessException("Você já participa de um plano familiar. Saia do grupo antes de criar outro.");
                });

        MealPlan basePlan = resolveBasePlan(user, request != null ? request.mealPlanId() : null);

        Household household = householdRepository.findByOwner_Id(user.getId())
                .orElseGet(() -> {
                    Household created = householdRepository.save(Household.builder()
                            .owner(user)
                            .baseMealPlan(basePlan)
                            .build());
                    memberRepository.save(HouseholdMember.builder()
                            .household(created)
                            .user(user)
                            .role(HouseholdMemberRole.OWNER)
                            .status(HouseholdMemberStatus.ACTIVE)
                            .joinedAt(LocalDateTime.now())
                            .build());
                    return created;
                });

        if (household.getBaseMealPlan() == null || !household.getBaseMealPlan().getId().equals(basePlan.getId())) {
            household.setBaseMealPlan(basePlan);
            household = householdRepository.save(household);
        }

        return toResponse(household);
    }

    @Transactional
    public HouseholdInvitationCreatedResponse createInvitation(CreateHouseholdInvitationRequest request) {
        User user = currentUser.get();
        Household household = requireOwnedHousehold(user);
        assertMemberCapacity(household);

        String email = request.email().trim().toLowerCase();
        if (email.equalsIgnoreCase(user.getEmail())) {
            throw new BusinessException("Você não pode convidar o seu próprio e-mail.");
        }

        long pendingInvites = invitationRepository.findByHouseholdIdAndStatus(
                household.getId(), PlanSharingInvitationStatus.PENDING).size();
        if (pendingInvites + activeMemberCount(household.getId()) >= MAX_MEMBERS) {
            throw new BusinessException("Limite de convites e membros do plano familiar atingido.");
        }

        String token = generateToken();
        PlanSharingInvitation invitation = PlanSharingInvitation.create(
                household,
                user,
                email,
                request.name(),
                token,
                LocalDateTime.now().plusDays(INVITE_EXPIRY_DAYS)
        );
        invitation = invitationRepository.save(invitation);

        String inviteUrl = buildInviteUrl(token);
        emailSender.sendHouseholdPlanInvitation(
                email,
                request.name(),
                user.getName(),
                inviteUrl,
                INVITE_EXPIRY_DAYS
        );

        return new HouseholdInvitationCreatedResponse(
                invitation.getId(),
                invitation.getInviteeEmail(),
                invitation.getInviteeName(),
                inviteUrl,
                invitation.getExpiresAt()
        );
    }

    @Transactional
    public PlanInvitationPreviewResponse previewInvitation(String token) {
        PlanSharingInvitation invitation = invitationRepository.findByTokenWithDetails(token)
                .orElseThrow(() -> new ResourceNotFoundException("Convite não encontrado"));

        if (invitation.getStatus() == PlanSharingInvitationStatus.PENDING
                && invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            invitation.setStatus(PlanSharingInvitationStatus.EXPIRED);
            invitationRepository.save(invitation);
        }

        boolean expired = invitation.getStatus() != PlanSharingInvitationStatus.PENDING
                || invitation.getExpiresAt().isBefore(LocalDateTime.now());

        return new PlanInvitationPreviewResponse(
                invitation.getToken(),
                invitation.getInviter().getName(),
                invitation.getInviteeName(),
                expired,
                true,
                invitation.getExpiresAt()
        );
    }

    @Transactional
    public AcceptHouseholdInvitationResponse acceptInvitation(String token) {
        User user = currentUser.get();
        PlanSharingInvitation invitation = invitationRepository.findByTokenWithDetails(token)
                .orElseThrow(() -> new ResourceNotFoundException("Convite não encontrado"));

        if (!invitation.isValid()) {
            throw new BusinessException("Este convite expirou ou já foi utilizado.");
        }

        if (!invitation.getInviteeEmail().equalsIgnoreCase(user.getEmail())) {
            throw new BusinessException("Este convite foi enviado para outro e-mail. Entre com a conta correta.");
        }

        memberRepository.findByUserIdAndStatus(user.getId(), HouseholdMemberStatus.ACTIVE)
                .ifPresent(m -> {
                    if (!m.getHousehold().getId().equals(invitation.getHousehold().getId())) {
                        throw new BusinessException("Você já participa de outro plano familiar.");
                    }
                });

        Household household = invitation.getHousehold();
        Optional<HouseholdMember> existing = memberRepository.findByUserIdAndStatus(
                user.getId(), HouseholdMemberStatus.ACTIVE);

        if (existing.isEmpty()) {
            assertMemberCapacity(household);
            memberRepository.save(HouseholdMember.builder()
                    .household(household)
                    .user(user)
                    .role(HouseholdMemberRole.MEMBER)
                    .status(HouseholdMemberStatus.ACTIVE)
                    .joinedAt(LocalDateTime.now())
                    .build());
        }

        invitation.setStatus(PlanSharingInvitationStatus.ACCEPTED);
        invitation.setAcceptedUser(user);
        invitation.setAcceptedAt(LocalDateTime.now());
        invitationRepository.save(invitation);

        MealPlan basePlan = household.getBaseMealPlan();
        if (basePlan == null) {
            throw new BusinessException("O plano base da família não está disponível. Peça para quem convidou compartilhar o plano novamente.");
        }

        boolean started = mealPlanService.enqueueHouseholdSharedGeneration(
                user,
                household.getId(),
                basePlan.getId()
        );

        return new AcceptHouseholdInvitationResponse(
                household.getId(),
                started,
                started
                        ? "Plano familiar aceito! Estamos gerando seu cardápio com os mesmos alimentos, ajustado ao seu perfil."
                        : "Você entrou no plano familiar. A geração do seu cardápio já está em andamento."
        );
    }

    @Transactional
    public void leaveHousehold() {
        User user = currentUser.get();
        HouseholdMember member = memberRepository.findByUserIdAndStatus(user.getId(), HouseholdMemberStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException("Você não participa de um plano familiar."));

        if (member.getRole() == HouseholdMemberRole.OWNER) {
            throw new BusinessException("Quem criou o plano familiar não pode sair. Convide os membros ou encerre os convites pendentes.");
        }

        member.setStatus(HouseholdMemberStatus.LEFT);
        memberRepository.save(member);
    }

    public HouseholdShoppingListResponse getAggregatedShoppingList() {
        User user = currentUser.get();
        HouseholdMember member = memberRepository.findActiveWithHousehold(user.getId(), HouseholdMemberStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException("Você não participa de um plano familiar."));

        List<HouseholdMember> members = memberRepository.findByHouseholdIdAndStatus(
                member.getHousehold().getId(), HouseholdMemberStatus.ACTIVE);

        Map<String, AggregatedShoppingItemResponse> aggregated = new LinkedHashMap<>();
        for (HouseholdMember activeMember : members) {
            List<ShoppingList> lists = shoppingListRepository.findByUserIdWithItemsOrderByCreatedAtDesc(
                    activeMember.getUser().getId());
            if (lists.isEmpty()) {
                continue;
            }
            ShoppingList latest = lists.getFirst();
            if (latest.getItems() == null) {
                continue;
            }
            for (ShoppingListItem item : latest.getItems()) {
                String key = normalizeItemKey(item.getItemName());
                AggregatedShoppingItemResponse current = aggregated.get(key);
                if (current == null) {
                    aggregated.put(key, new AggregatedShoppingItemResponse(
                            item.getItemName(),
                            item.getQuantity(),
                            item.getCategory(),
                            1
                    ));
                } else {
                    aggregated.put(key, new AggregatedShoppingItemResponse(
                            current.itemName(),
                            mergeQuantity(current.quantity(), item.getQuantity()),
                            current.category() != null ? current.category() : item.getCategory(),
                            current.memberCount() + 1
                    ));
                }
            }
        }

        return new HouseholdShoppingListResponse(member.getHousehold().getId(), new ArrayList<>(aggregated.values()));
    }

    private Household requireOwnedHousehold(User user) {
        Household household = householdRepository.findByOwner_Id(user.getId())
                .orElseThrow(() -> new BusinessException("Compartilhe seu plano alimentar antes de convidar familiares."));
        memberRepository.findByUserIdAndStatus(user.getId(), HouseholdMemberStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException("Você precisa estar no plano familiar para convidar."));
        return household;
    }

    private MealPlan resolveBasePlan(User user, Long mealPlanId) {
        if (mealPlanId != null) {
            MealPlan plan = mealPlanRepository.findById(mealPlanId)
                    .orElseThrow(() -> new ResourceNotFoundException("Plano alimentar não encontrado"));
            if (!plan.getUser().getId().equals(user.getId())) {
                throw new BusinessException("Este plano não pertence a você.");
            }
            return plan;
        }
        return mealPlanRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .findFirst()
                .orElseThrow(() -> new BusinessException("Gere seu plano alimentar antes de compartilhar com a família."));
    }

    private void assertMemberCapacity(Household household) {
        if (activeMemberCount(household.getId()) >= MAX_MEMBERS) {
            throw new BusinessException("O plano familiar já atingiu o limite de " + MAX_MEMBERS + " pessoas.");
        }
    }

    private long activeMemberCount(Long householdId) {
        return memberRepository.countByHouseholdIdAndStatus(householdId, HouseholdMemberStatus.ACTIVE);
    }

    private HouseholdResponse toResponse(Household household) {
        List<HouseholdMember> members = memberRepository.findByHouseholdIdAndStatus(
                household.getId(), HouseholdMemberStatus.ACTIVE);
        List<HouseholdInvitationResponse> invites = invitationRepository
                .findByHouseholdIdAndStatus(household.getId(), PlanSharingInvitationStatus.PENDING)
                .stream()
                .map(i -> new HouseholdInvitationResponse(
                        i.getId(),
                        i.getInviteeEmail(),
                        i.getInviteeName(),
                        i.getStatus().name(),
                        i.getExpiresAt(),
                        i.getCreatedAt()
                ))
                .toList();

        return new HouseholdResponse(
                household.getId(),
                household.getOwner().getId(),
                household.getOwner().getName(),
                household.getBaseMealPlan() != null ? household.getBaseMealPlan().getId() : null,
                members.size(),
                MAX_MEMBERS,
                members.stream()
                        .map(m -> new HouseholdMemberResponse(
                                m.getUser().getId(),
                                m.getUser().getName(),
                                m.getRole().name(),
                                m.getStatus().name(),
                                m.getJoinedAt()
                        ))
                        .collect(Collectors.toList()),
                invites,
                household.getCreatedAt()
        );
    }

    private String generateToken() {
        byte[] bytes = new byte[24];
        secureRandom.nextBytes(bytes);
        StringBuilder sb = new StringBuilder(48);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private String buildInviteUrl(String token) {
        String base = inviteBaseUrl.endsWith("/") ? inviteBaseUrl.substring(0, inviteBaseUrl.length() - 1) : inviteBaseUrl;
        return base + "/plano-familia/" + token;
    }

    private static String normalizeItemKey(String name) {
        return name == null ? "" : name.trim().toLowerCase(Locale.ROOT);
    }

    private static String mergeQuantity(String a, String b) {
        if (a == null || a.isBlank()) {
            return b;
        }
        if (b == null || b.isBlank()) {
            return a;
        }
        if (a.equalsIgnoreCase(b)) {
            return a;
        }
        return a + " + " + b;
    }
}
