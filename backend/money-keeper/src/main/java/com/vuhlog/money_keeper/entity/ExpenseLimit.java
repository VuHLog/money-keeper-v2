package com.vuhlog.money_keeper.entity;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "expense_limit")
@Entity
public class ExpenseLimit {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private Long amount;
    private String name;
    @Lob
    @Column(name = "categories_id", columnDefinition = "LONGTEXT")
    private String categoriesId;
    @Lob
    @Column(name = "bucket_payment_ids", columnDefinition = "LONGTEXT")
    private String bucketPaymentIds;
    private String repeatTime;
    private Timestamp startDate;
    private Timestamp endDate;
    private Timestamp startDateLimit;
    private Timestamp endDateLimit;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users user;
}
