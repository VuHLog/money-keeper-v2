package com.vuhlog.money_keeper.entity;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "notification")
@Entity
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String title;
    private String content;
    private Timestamp createdAt;
    private String type;
    private int readStatus;
    private String iconUrl;
    private String href;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users user;

    @PrePersist
    public void prePersist() {
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }
}
