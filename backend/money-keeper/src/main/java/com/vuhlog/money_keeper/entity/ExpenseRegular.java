package com.vuhlog.money_keeper.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.sql.Timestamp;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "expense_regular")
public class ExpenseRegular {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private Long amount;
    private String location;
    private String interpretation;
    private Timestamp expenseDate;
    private Timestamp createdDate;
    private Timestamp modifiedDate;
    private String transferType;
    private Double balance;
    private Double convertedBalance;
    private String tripEvent;
    private String beneficiary;
    private String currency;
    private String currencySymbol;
    private Double exchangeRate;
    private Long convertedAmount;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH, CascadeType.REFRESH})
    @JoinColumn(name = "beneficiary_account_id")
    private DictionaryBucketPayment beneficiaryAccount;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH, CascadeType.REFRESH})
    @JoinColumn(name = "dictionary_bucket_payment_id")
    private DictionaryBucketPayment dictionaryBucketPayment;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.DETACH, CascadeType.REFRESH})
    @JoinColumn(name = "dictionary_expense_id")
    private DictionaryExpense dictionaryExpense;

//    @ManyToOne
//    @JoinColumn(name = "trip_event_id")
//    private TripEvent tripEvent;
//
//    @ManyToOne
//    @JoinColumn(name = "beneficiary_id")
//    private Beneficiary beneficiary;

    @PrePersist
    public void onCreate() {
        this.createdDate = new Timestamp(System.currentTimeMillis());
        this.modifiedDate = new Timestamp(System.currentTimeMillis());
    }

    @PreUpdate
    public void onUpdate() {
        this.modifiedDate = new Timestamp(System.currentTimeMillis());
    }

}
