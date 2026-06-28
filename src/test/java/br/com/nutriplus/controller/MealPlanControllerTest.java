package br.com.nutriplus.controller;

import br.com.nutriplus.domain.enums.MealPlanGenerationStatus;
import br.com.nutriplus.domain.enums.PlanRegenerationReason;
import br.com.nutriplus.dto.request.MealPlanGenerateRequest;
import br.com.nutriplus.dto.response.MealPlanGenerationStatusResponse;
import br.com.nutriplus.dto.response.MealPlanResponse;
import br.com.nutriplus.dto.response.PlanRegenerationEligibilityResponse;
import br.com.nutriplus.exception.ResourceNotFoundException;
import br.com.nutriplus.infrastructure.web.ApiExceptionHandler;
import br.com.nutriplus.service.MealPlanService;
import br.com.nutriplus.support.WebMvcTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MealPlanController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(ApiExceptionHandler.class)
class MealPlanControllerTest extends WebMvcTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MealPlanService mealPlanService;

    @Test
    void generateReturnsAccepted() throws Exception {
        when(mealPlanService.enqueueGeneration(org.mockito.ArgumentMatchers.any(MealPlanGenerateRequest.class)))
                .thenReturn(
                new MealPlanGenerationStatusResponse(
                        1L, MealPlanGenerationStatus.PENDING, null, null, "Analisando…", 1, 5)
        );

        mockMvc.perform(post("/meal-plans/generate")
                        .contentType("application/json")
                        .content("{\"reason\":\"FIRST_PLAN\"}"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.jobId").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void generationStatusReturnsOk() throws Exception {
        when(mealPlanService.getGenerationStatus()).thenReturn(
                new MealPlanGenerationStatusResponse(
                        2L, MealPlanGenerationStatus.RUNNING, null, null, "Montando refeições…", 2, 5)
        );

        mockMvc.perform(get("/meal-plans/generation-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RUNNING"));
    }

    @Test
    void latestReturnsOkWhenPlanExists() throws Exception {
        when(mealPlanService.getLatest()).thenReturn(
                new MealPlanResponse(
                        10L,
                        LocalDate.now(),
                        new BigDecimal("1900"),
                        new BigDecimal("150"),
                        new BigDecimal("190"),
                        new BigDecimal("63"),
                        "Disclaimer",
                        List.of(),
                        LocalDateTime.now(),
                        "APPROVED",
                        null,
                        null,
                        null,
                        null,
                        null,
                        "AI"
                )
        );

        mockMvc.perform(get("/meal-plans/latest"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10));
    }

    @Test
    void latestReturns404WhenNoPlan() throws Exception {
        when(mealPlanService.getLatest())
                .thenThrow(new ResourceNotFoundException("Nenhum plano alimentar encontrado"));

        mockMvc.perform(get("/meal-plans/latest"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Nenhum plano alimentar encontrado"));
    }
}
