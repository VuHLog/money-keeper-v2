package com.vuhlog.money_keeper.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class ForgotPassword {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private Integer otp;

    private Date expirationTime;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;
}
