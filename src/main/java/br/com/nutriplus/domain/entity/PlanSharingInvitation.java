package br.com.nutriplus.domain.entity;

import br.com.nutriplus.domain.enums.PlanSharingInvitationStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "plan_sharing_invitations")
public class PlanSharingInvitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "household_id", nullable = false)
    private Household household;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inviter_user_id", nullable = false)
    private User inviter;

    @Column(name = "invitee_email", nullable = false)
    private String inviteeEmail;

    @Column(name = "invitee_name", length = 120)
    private String inviteeName;

    @Column(nullable = false, length = 64, unique = true)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PlanSharingInvitationStatus status = PlanSharingInvitationStatus.PENDING;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accepted_user_id")
    private User acceptedUser;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected PlanSharingInvitation() {
    }

    public boolean isValid() {
        return status == PlanSharingInvitationStatus.PENDING && expiresAt.isAfter(LocalDateTime.now());
    }

    public Long getId() {
        return id;
    }

    public Household getHousehold() {
        return household;
    }

    public User getInviter() {
        return inviter;
    }

    public String getInviteeEmail() {
        return inviteeEmail;
    }

    public String getInviteeName() {
        return inviteeName;
    }

    public String getToken() {
        return token;
    }

    public PlanSharingInvitationStatus getStatus() {
        return status;
    }

    public void setStatus(PlanSharingInvitationStatus status) {
        this.status = status;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public User getAcceptedUser() {
        return acceptedUser;
    }

    public void setAcceptedUser(User acceptedUser) {
        this.acceptedUser = acceptedUser;
    }

    public LocalDateTime getAcceptedAt() {
        return acceptedAt;
    }

    public void setAcceptedAt(LocalDateTime acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public static PlanSharingInvitation create(Household household,
                                               User inviter,
                                               String inviteeEmail,
                                               String inviteeName,
                                               String token,
                                               LocalDateTime expiresAt) {
        PlanSharingInvitation invitation = new PlanSharingInvitation();
        invitation.household = household;
        invitation.inviter = inviter;
        invitation.inviteeEmail = inviteeEmail.trim().toLowerCase();
        invitation.inviteeName = inviteeName;
        invitation.token = token;
        invitation.expiresAt = expiresAt;
        invitation.status = PlanSharingInvitationStatus.PENDING;
        return invitation;
    }
}
