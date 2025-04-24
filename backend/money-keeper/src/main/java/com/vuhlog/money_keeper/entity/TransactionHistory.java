package com.vuhlog.money_keeper.entity;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "transaction_history")
public class TransactionHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String transactionId;
    private String transactionType;
    private long oldAmount;
    private long newAmount;
    private String oldCategoryId;
    private String newCategoryId;
    private Timestamp updatedAt;
    private String beneficiaryAccountId;
    private String senderAccountId;

    @ManyToOne
    @JoinColumn(name = "bucket_payment_id")
    private DictionaryBucketPayment bucketPayment;

    @PrePersist
    public void prePersist() {
        this.updatedAt = new Timestamp(System.currentTimeMillis());
    }
}
