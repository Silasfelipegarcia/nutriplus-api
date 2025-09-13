package br.com.nutriplus.domain.model;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class UserProfile {
    private final UUID id;
    private final String name;
    private final String sex;
    private final Integer age;
    private final Integer heightCm;
    private final Double weightKg;
    private final Integer trainingDaysPerWeek;
    private final List<String> dislikes;
    private final List<String> style;

    private UserProfile(Builder builder) {
        this.id = Objects.requireNonNull(builder.id, "id is required");
        this.name = Objects.requireNonNull(builder.name, "name is required");
        this.sex = Objects.requireNonNull(builder.sex, "sex is required");
        this.age = Objects.requireNonNull(builder.age, "age is required");
        this.heightCm = Objects.requireNonNull(builder.heightCm, "heightCm is required");
        this.weightKg = Objects.requireNonNull(builder.weightKg, "weightKg is required");
        this.trainingDaysPerWeek = Objects.requireNonNull(builder.trainingDaysPerWeek, "trainingDaysPerWeek is required");
        this.dislikes = builder.dislikes;
        this.style = builder.style;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSex() {
        return sex;
    }

    public Integer getAge() {
        return age;
    }

    public Integer getHeightCm() {
        return heightCm;
    }

    public Double getWeightKg() {
        return weightKg;
    }

    public Integer getTrainingDaysPerWeek() {
        return trainingDaysPerWeek;
    }

    public List<String> getDislikes() {
        return dislikes;
    }

    public List<String> getStyle() {
        return style;
    }

    public static class Builder {
        private UUID id;
        private String name;
        private String sex;
        private Integer age;
        private Integer heightCm;
        private Double weightKg;
        private Integer trainingDaysPerWeek;
        private List<String> dislikes;
        private List<String> style;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder sex(String sex) {
            this.sex = sex;
            return this;
        }

        public Builder age(Integer age) {
            this.age = age;
            return this;
        }

        public Builder heightCm(Integer heightCm) {
            this.heightCm = heightCm;
            return this;
        }

        public Builder weightKg(Double weightKg) {
            this.weightKg = weightKg;
            return this;
        }

        public Builder trainingDaysPerWeek(Integer trainingDaysPerWeek) {
            this.trainingDaysPerWeek = trainingDaysPerWeek;
            return this;
        }

        public Builder dislikes(List<String> dislikes) {
            this.dislikes = dislikes;
            return this;
        }

        public Builder style(List<String> style) {
            this.style = style;
            return this;
        }

        public UserProfile build() {
            return new UserProfile(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
