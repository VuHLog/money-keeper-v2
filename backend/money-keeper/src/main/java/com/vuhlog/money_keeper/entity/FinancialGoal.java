package com.vuhlog.money_keeper.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "financial_goal")
public class FinancialGoal {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String name;
    private Long targetAmount;
    private Long currentAmount;
    private LocalDate deadline;
    private String interpretation;
    private String currency;
    private String currencySymbol;
    private Integer status;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users user;

    @ManyToOne
    @JoinColumn(name = "bucket_payment_id")
    private DictionaryBucketPayment bucketPayment;

    @OneToMany(mappedBy = "financialGoal")
    @JsonIgnore
    private Set<ExpenseRegular> expenseRegulars;

    @PrePersist
    public void prePersist() {
        this.createdAt = new Timestamp(System.currentTimeMillis());
        this.updatedAt = new Timestamp(System.currentTimeMillis());
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = new Timestamp(System.currentTimeMillis());
    }
}
