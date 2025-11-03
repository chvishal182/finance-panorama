package com.chelv.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "expense")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Expense {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "external_id")
    private String externalId;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "merchant")
    private String merchant;

    @Column(name = "currency")
    private String currency;

    @Column(name = "created_at", nullable = false, updatable = false) 
    private Timestamp createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.externalId == null) {
            this.externalId = UUID.randomUUID().toString();
        }
        if (this.createdAt == null) {
            this.createdAt = Timestamp.from(Instant.now());
        }
    }
}
