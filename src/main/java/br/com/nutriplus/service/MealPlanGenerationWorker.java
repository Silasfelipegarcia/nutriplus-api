package br.com.nutriplus.service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class MealPlanGenerationWorker {

    private static final Logger log = LoggerFactory.getLogger(MealPlanGenerationWorker.class);

    private final MealPlanGenerationProcessor processor;
    private final MeterRegistry meterRegistry;

    public MealPlanGenerationWorker(MealPlanGenerationProcessor processor, MeterRegistry meterRegistry) {
        this.processor = processor;
        this.meterRegistry = meterRegistry;
    }

    @Async("mealPlanExecutor")
    public void processAsync(Long jobId, Long userId) {
        Timer.Sample sample = Timer.start(meterRegistry);
        long start = System.currentTimeMillis();

        try {
            processor.run(jobId, userId, start);
        } catch (Exception e) {
            log.error("Meal plan generation failed jobId={} userId={}", jobId, userId, e);
            processor.markFailed(jobId, e.getMessage() != null ? e.getMessage() : "Erro desconhecido");
        } finally {
            sample.stop(Timer.builder("nutriplus.meal_plan.generation.duration").register(meterRegistry));
        }
    }
}
