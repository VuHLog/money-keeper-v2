package com.vuhlog.money_keeper.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "dictionary_bucket_payment")
public class DictionaryBucketPayment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private Long balance; // so du ban dau
    private Long creditLimit; // han muc tin dung
    private String accountName;
    private String accountType;
    private String interpretation; // dien giai
    private boolean haveUse;
    private String iconUrl;

    @ManyToOne
    @JoinColumn(name = "bank_id")
    private Bank bank;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users user;

    @OneToMany(mappedBy = "dictionaryBucketPayment")
    @JsonIgnore
    private Set<ExpenseRegular> expenseRegulars;

    @OneToMany(mappedBy = "beneficiaryAccount")
    @JsonIgnore
    private Set<ExpenseRegular> expenseRegularsForBeneficiaryAccount;

    @OneToMany(mappedBy = "senderAccount")
    @JsonIgnore
    private Set<RevenueRegular> revenueRegularsForSenderAccount;

    @OneToMany(mappedBy = "dictionaryBucketPayment")
    @JsonIgnore
    private Set<RevenueRegular> revenueRegulars;

    @OneToMany(mappedBy = "bucketPayment")
    @JsonIgnore
    private Set<TransactionHistory> transactionHistories;

    @PrePersist
    public void prePersist() {
        this.haveUse = true;
    }
}
