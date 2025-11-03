package com.chelv.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_info")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    @JsonProperty("user_id")
    @NonNull
    private String userId;

    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String email;
    private String profilePicture;
}
