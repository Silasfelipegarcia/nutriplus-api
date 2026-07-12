package br.com.nutriplus.infrastructure.dev;

import br.com.nutriplus.domain.enums.AgentPersona;
import br.com.nutriplus.domain.enums.DietaryPreference;
import br.com.nutriplus.domain.enums.Sex;
import br.com.nutriplus.domain.enums.SubscriptionPlan;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Catálogo de usuários de teste funcional — planos, personas, ciclo de 15 dias e agentes secundários.
 */
public enum DevTestUserSpec {

    LEGACY_TESTE("teste@nutriplus.local", "Usuário Teste", "Luna · perfil completo · sem plano",
            SubscriptionPlan.FREE, AgentPersona.LUNA, true, false, 0, 0,
            null, null, false, false, false, false, 30, Sex.MALE,
            DietaryPreference.OMNIVORE, "frango, arroz, salada, banana",
            "Prefiro café reforçado e almoço prático"),
    LEGACY_TESTE2("teste2@nutriplus.local", "Usuário Teste 2", "Sem perfil · fluxo de onboarding",
            SubscriptionPlan.FREE, AgentPersona.LUNA, false, false, 0, 0,
            null, null, false, false, false, false, 30, Sex.MALE,
            DietaryPreference.OMNIVORE, "", ""),
    LEGACY_ADMIN("admin@nutriplus.local", "Admin Nutri+", "Console administrativo",
            SubscriptionPlan.FREE, AgentPersona.LUNA, false, false, 0, 0,
            null, null, false, false, false, true, 30, Sex.MALE,
            DietaryPreference.OMNIVORE, "", ""),

    PERSONA_LUNA("persona.luna@nutriplus.local", "Teste Luna", "Persona Luna · Essencial mensal",
            SubscriptionPlan.ESSENTIAL_MONTHLY, AgentPersona.LUNA, true, false, 0, 0,
            paidUntil(30), null, true, false, false, false, 30, Sex.FEMALE,
            DietaryPreference.OMNIVORE, "frango, arroz, salada",
            "Assistente Luna — tom acolhedor"),
    PERSONA_BRUNO("persona.bruno@nutriplus.local", "Teste Bruno", "Persona Bruno · Essencial mensal",
            SubscriptionPlan.ESSENTIAL_MONTHLY, AgentPersona.BRUNO, true, false, 0, 0,
            paidUntil(30), null, true, false, false, false, 32, Sex.MALE,
            DietaryPreference.OMNIVORE, "carne, batata doce, ovos",
            "Assistente Bruno — tom objetivo"),

    PLAN_ESSENTIAL_MONTHLY("plano.essencial@nutriplus.local", "Essencial Mensal", "Plano Essencial mensal ativo",
            SubscriptionPlan.ESSENTIAL_MONTHLY, AgentPersona.LUNA, true, false, 0, 0,
            paidUntil(30), null, true, false, false, false, 28, Sex.MALE,
            DietaryPreference.OMNIVORE, "frango, arroz", ""),
    PLAN_ESSENTIAL_YEARLY("plano.essencial.anual@nutriplus.local", "Essencial Anual", "Plano Essencial anual ativo",
            SubscriptionPlan.ESSENTIAL_YEARLY, AgentPersona.BRUNO, true, false, 0, 0,
            paidUntil(365), null, true, false, false, false, 35, Sex.FEMALE,
            DietaryPreference.OMNIVORE, "peixe, legumes", ""),
    PLAN_ATHLETE_MONTHLY("plano.atleta@nutriplus.local", "Atleta Mensal", "Plano Atleta mensal · modo atleta ativo",
            SubscriptionPlan.ATHLETE_MONTHLY, AgentPersona.BRUNO, true, false, 0, 0,
            paidUntil(30), null, true, true, false, false, 27, Sex.MALE,
            DietaryPreference.OMNIVORE, "frango, arroz, whey", "Treino 3x/semana"),
    PLAN_ATHLETE_YEARLY("plano.atleta.anual@nutriplus.local", "Atleta Anual", "Plano Atleta anual · modo atleta",
            SubscriptionPlan.ATHLETE_YEARLY, AgentPersona.LUNA, true, false, 0, 0,
            paidUntil(365), null, true, true, false, false, 29, Sex.FEMALE,
            DietaryPreference.OMNIVORE, "carne, batata doce", ""),
    PLAN_TEST_MONTHLY("plano.teste@nutriplus.local", "Teste Cobrança", "Plano TEST_MONTHLY (R$ 1,00)",
            SubscriptionPlan.TEST_MONTHLY, AgentPersona.LUNA, true, false, 0, 0,
            paidUntil(30), null, true, false, false, false, 30, Sex.MALE,
            DietaryPreference.OMNIVORE, "frango, arroz", ""),
    PLAN_TRIAL("trial@nutriplus.local", "Trial 7 dias", "Trial ativo · acesso completo por 7 dias",
            SubscriptionPlan.ESSENTIAL_MONTHLY, AgentPersona.BRUNO, true, false, 0, 0,
            null, trialUntil(7), true, false, false, false, 31, Sex.MALE,
            DietaryPreference.OMNIVORE, "frango, arroz", ""),
    PLAN_EXPIRED("plano.expirado@nutriplus.local", "Assinatura Expirada", "Essencial expirado · voltou ao FREE efetivo",
            SubscriptionPlan.ESSENTIAL_MONTHLY, AgentPersona.LUNA, true, false, 0, 0,
            expiredDaysAgo(5), null, false, false, false, false, 33, Sex.FEMALE,
            DietaryPreference.OMNIVORE, "frango, arroz", ""),

    AGENT_HELENA("helena@nutriplus.local", "Helena 68 anos", "Idoso 68a · revisão Helena (geriatria)",
            SubscriptionPlan.ESSENTIAL_MONTHLY, AgentPersona.LUNA, true, false, 0, 0,
            paidUntil(30), null, true, false, false, false, 68, Sex.FEMALE,
            DietaryPreference.OMNIVORE, "sopa, frutas, peixe",
            "Dificuldade para mastigar alimentos duros"),
    AGENT_FLORA("flora@nutriplus.local", "Flora Vegetariana", "Vegetariana · revisão Flora (dieta restrita)",
            SubscriptionPlan.FREE, AgentPersona.BRUNO, true, false, 0, 0,
            null, null, false, false, false, false, 30, Sex.FEMALE,
            DietaryPreference.VEGETARIAN, "tofu, lentilha, quinoa, legumes",
            "Não como carne nem frango"),

    CYCLE_LOCKED("plano.bloqueado@nutriplus.local", "Plano Bloqueado", "Plano há 3 dias · bloqueio +12d · correção única disponível",
            SubscriptionPlan.FREE, AgentPersona.LUNA, true, true, 3, 0,
            null, null, false, false, false, false, 30, Sex.MALE,
            DietaryPreference.OMNIVORE, "frango, arroz", "") {
        @Override
        public LocalDate planRegenLockedUntil() {
            return LocalDate.now().plusDays(12);
        }
    },
    CYCLE_CORRECTION_USED("correcao.usada@nutriplus.local", "Correção Usada", "Correção única já usada · bloqueio +8d",
            SubscriptionPlan.FREE, AgentPersona.BRUNO, true, true, 7, 0,
            null, null, false, false, true, false, 30, Sex.MALE,
            DietaryPreference.OMNIVORE, "frango, arroz", "") {
        @Override
        public LocalDate planRegenLockedUntil() {
            return LocalDate.now().plusDays(8);
        }
    },
    CYCLE_REVIEW_DUE("ciclo.vencido@nutriplus.local", "Ciclo 15d Vencido", "Perfil há 20 dias · reavaliação Evolução disponível · lock expirado",
            SubscriptionPlan.ESSENTIAL_MONTHLY, AgentPersona.LUNA, true, true, 18, 20,
            paidUntil(30), null, true, false, false, false, 30, Sex.FEMALE,
            DietaryPreference.OMNIVORE, "frango, arroz", "") {
        @Override
        public LocalDate planRegenLockedUntil() {
            return LocalDate.now().minusDays(2);
        }
    },
    ATHLETE_REGEN("atleta.regen@nutriplus.local", "Atleta Regen", "Modo atleta · regen ATHLETE_SWITCH disponível",
            SubscriptionPlan.ATHLETE_MONTHLY, AgentPersona.BRUNO, true, true, 10, 0,
            paidUntil(30), null, true, true, false, false, 26, Sex.MALE,
            DietaryPreference.OMNIVORE, "frango, arroz, whey", "Acabou de ativar modo atleta") {
        @Override
        public LocalDate planRegenLockedUntil() {
            return LocalDate.now().plusDays(5);
        }

        @Override
        public boolean athleteRegenEligible() {
            return true;
        }
    };

    private final String email;
    private final String displayName;
    private final String scenarioLabel;
    private final SubscriptionPlan subscriptionPlan;
    private final AgentPersona agentPersona;
    private final boolean withProfile;
    private final boolean withStubPlan;
    private final int planAgeDays;
    private final int profileAgeDays;
    private final Instant planValidUntil;
    private final Instant trialUntil;
    private final boolean autoRenew;
    private final boolean athleteMode;
    private final boolean oneTimeCorrectionUsed;
    private final boolean adminRole;
    private final int age;
    private final Sex sex;
    private final DietaryPreference dietaryPreference;
    private final String foodLikes;
    private final String mealNotes;

    DevTestUserSpec(String email, String displayName, String scenarioLabel,
                    SubscriptionPlan subscriptionPlan, AgentPersona agentPersona,
                    boolean withProfile, boolean withStubPlan, int planAgeDays, int profileAgeDays,
                    Instant planValidUntil, Instant trialUntil, boolean autoRenew,
                    boolean athleteMode, boolean oneTimeCorrectionUsed, boolean adminRole,
                    int age, Sex sex, DietaryPreference dietaryPreference,
                    String foodLikes, String mealNotes) {
        this.email = email;
        this.displayName = displayName;
        this.scenarioLabel = scenarioLabel;
        this.subscriptionPlan = subscriptionPlan;
        this.agentPersona = agentPersona;
        this.withProfile = withProfile;
        this.withStubPlan = withStubPlan;
        this.planAgeDays = planAgeDays;
        this.profileAgeDays = profileAgeDays;
        this.planValidUntil = planValidUntil;
        this.trialUntil = trialUntil;
        this.autoRenew = autoRenew;
        this.athleteMode = athleteMode;
        this.oneTimeCorrectionUsed = oneTimeCorrectionUsed;
        this.adminRole = adminRole;
        this.age = age;
        this.sex = sex;
        this.dietaryPreference = dietaryPreference;
        this.foodLikes = foodLikes;
        this.mealNotes = mealNotes;
    }

    public String email() { return email; }
    public String displayName() { return displayName; }
    public String scenarioLabel() { return scenarioLabel; }
    public SubscriptionPlan subscriptionPlan() { return subscriptionPlan; }
    public AgentPersona agentPersona() { return agentPersona; }
    public boolean withProfile() { return withProfile; }
    public boolean withStubPlan() { return withStubPlan; }
    public int planAgeDays() { return planAgeDays; }
    public int profileAgeDays() { return profileAgeDays; }
    public Instant planValidUntil() { return planValidUntil; }
    public Instant trialUntil() { return trialUntil; }
    public boolean autoRenew() { return autoRenew; }
    public boolean athleteMode() { return athleteMode; }
    public boolean oneTimeCorrectionUsed() { return oneTimeCorrectionUsed; }
    public boolean adminRole() { return adminRole; }
    public int age() { return age; }
    public Sex sex() { return sex; }
    public DietaryPreference dietaryPreference() { return dietaryPreference; }
    public String foodLikes() { return foodLikes; }
    public String mealNotes() { return mealNotes; }

    /** E-mail isolado para testes de integração (Testcontainers). */
    public String integrationTestEmail() {
        return name().toLowerCase() + "@func.nutriplus.test";
    }

    public LocalDate planRegenLockedUntil() {
        return withStubPlan ? LocalDate.now().plusDays(12) : null;
    }

    public boolean athleteRegenEligible() {
        return false;
    }

    static Instant paidUntil(int days) {
        return Instant.now().plus(days, ChronoUnit.DAYS);
    }

    static Instant trialUntil(int days) {
        return Instant.now().plus(days, ChronoUnit.DAYS);
    }

    static Instant expiredDaysAgo(int days) {
        return Instant.now().minus(days, ChronoUnit.DAYS);
    }
}
