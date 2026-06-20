package br.com.nutriplus.domain.entity;

import br.com.nutriplus.domain.enums.SportType;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_training_activities")
public class UserTrainingActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "sport_type", nullable = false, length = 40)
    private SportType sportType;

    @Column(name = "days_per_week", nullable = false)
    private Integer daysPerWeek;

    @Column(name = "minutes_per_session", nullable = false)
    private Integer minutesPerSession;

    @Column(name = "custom_label", length = 80)
    private String customLabel;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected UserTrainingActivity() {
    }

    public UserTrainingActivity(User user, SportType sportType, int daysPerWeek, int minutesPerSession) {
        this.user = user;
        this.sportType = sportType;
        this.daysPerWeek = daysPerWeek;
        this.minutesPerSession = minutesPerSession;
    }

    public UserTrainingActivity(User user, SportType sportType, int daysPerWeek, int minutesPerSession, String customLabel) {
        this.user = user;
        this.sportType = sportType;
        this.daysPerWeek = daysPerWeek;
        this.minutesPerSession = minutesPerSession;
        this.customLabel = customLabel;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public SportType getSportType() {
        return sportType;
    }

    public void setSportType(SportType sportType) {
        this.sportType = sportType;
    }

    public Integer getDaysPerWeek() {
        return daysPerWeek;
    }

    public void setDaysPerWeek(Integer daysPerWeek) {
        this.daysPerWeek = daysPerWeek;
    }

    public Integer getMinutesPerSession() {
        return minutesPerSession;
    }

    public void setMinutesPerSession(Integer minutesPerSession) {
        this.minutesPerSession = minutesPerSession;
    }

    public String getCustomLabel() {
        return customLabel;
    }

    public void setCustomLabel(String customLabel) {
        this.customLabel = customLabel;
    }
}
