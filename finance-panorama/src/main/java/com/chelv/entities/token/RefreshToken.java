package com.chelv.entities.token;

import java.time.Instant;

import com.chelv.entities.user.UserInfo;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Table(name = "tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String token;
    private Instant expiryDate;

    @OneToOne
    @JoinColumn(name = "id", referencedColumnName = "user_id")
    private UserInfo userInfo;
}
