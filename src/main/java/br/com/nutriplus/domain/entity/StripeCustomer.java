package br.com.nutriplus.domain.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "stripe_customers")
public class StripeCustomer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "stripe_customer_id", nullable = false)
    private String stripeCustomerId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected StripeCustomer() {
    }

    public static StripeCustomer of(User user, String stripeCustomerId) {
        StripeCustomer sc = new StripeCustomer();
        sc.user = user;
        sc.stripeCustomerId = stripeCustomerId;
        return sc;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getStripeCustomerId() {
        return stripeCustomerId;
    }
}
