package br.com.nutriplus.domain.entity;

import br.com.nutriplus.domain.enums.HouseholdMemberRole;
import br.com.nutriplus.domain.enums.HouseholdMemberStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "household_members")
public class HouseholdMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "household_id", nullable = false)
    private Household household;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private HouseholdMemberRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private HouseholdMemberStatus status = HouseholdMemberStatus.ACTIVE;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected HouseholdMember() {
    }

    private HouseholdMember(Builder builder) {
        this.id = builder.id;
        this.household = builder.household;
        this.user = builder.user;
        this.role = builder.role;
        this.status = builder.status != null ? builder.status : HouseholdMemberStatus.ACTIVE;
        this.joinedAt = builder.joinedAt;
        this.createdAt = builder.createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public Household getHousehold() {
        return household;
    }

    public User getUser() {
        return user;
    }

    public HouseholdMemberRole getRole() {
        return role;
    }

    public HouseholdMemberStatus getStatus() {
        return status;
    }

    public void setStatus(HouseholdMemberStatus status) {
        this.status = status;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }

    public static class Builder {
        private Long id;
        private Household household;
        private User user;
        private HouseholdMemberRole role;
        private HouseholdMemberStatus status;
        private LocalDateTime joinedAt;
        private LocalDateTime createdAt;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder household(Household household) {
            this.household = household;
            return this;
        }

        public Builder user(User user) {
            this.user = user;
            return this;
        }

        public Builder role(HouseholdMemberRole role) {
            this.role = role;
            return this;
        }

        public Builder status(HouseholdMemberStatus status) {
            this.status = status;
            return this;
        }

        public Builder joinedAt(LocalDateTime joinedAt) {
            this.joinedAt = joinedAt;
            return this;
        }

        public HouseholdMember build() {
            return new HouseholdMember(this);
        }
    }
}
