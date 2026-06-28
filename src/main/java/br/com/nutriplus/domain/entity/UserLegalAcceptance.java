package br.com.nutriplus.domain.entity;

import br.com.nutriplus.domain.enums.LegalDocumentType;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_legal_acceptances")
public class UserLegalAcceptance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 30)
    private LegalDocumentType documentType;

    @Column(name = "document_version", nullable = false, length = 20)
    private String documentVersion;

    @CreationTimestamp
    @Column(name = "accepted_at", nullable = false, updatable = false)
    private LocalDateTime acceptedAt;

    @Column(name = "app_platform", length = 30)
    private String appPlatform;

    @Column(nullable = false, length = 30)
    private String source = "ONBOARDING";

    protected UserLegalAcceptance() {
    }

    public static UserLegalAcceptance record(User user,
                                             LegalDocumentType documentType,
                                             String documentVersion,
                                             String appPlatform,
                                             String source) {
        UserLegalAcceptance acceptance = new UserLegalAcceptance();
        acceptance.user = user;
        acceptance.documentType = documentType;
        acceptance.documentVersion = documentVersion;
        acceptance.appPlatform = appPlatform;
        acceptance.source = source != null ? source : "ONBOARDING";
        return acceptance;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public LegalDocumentType getDocumentType() {
        return documentType;
    }

    public String getDocumentVersion() {
        return documentVersion;
    }

    public LocalDateTime getAcceptedAt() {
        return acceptedAt;
    }

    public String getAppPlatform() {
        return appPlatform;
    }

    public String getSource() {
        return source;
    }
}
