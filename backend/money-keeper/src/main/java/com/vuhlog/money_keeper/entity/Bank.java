package com.vuhlog.money_keeper.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "banks")
@Entity
public class Bank {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String bin;

    @Column(nullable = false, name = "shortName")
    private String shortName;

    private String logo;

    @Column(columnDefinition = "TINYINT(1) DEFAULT 0")
    private boolean transferSupported;

    @Column(columnDefinition = "TINYINT(1) DEFAULT 0")
    private boolean lookupSupported;

    @Column(columnDefinition = "TINYINT(1) DEFAULT 0")
    private boolean support;

    @Column(columnDefinition = "TINYINT(1) DEFAULT 0")
    private boolean isTransfer;

    private String swiftCode;
}