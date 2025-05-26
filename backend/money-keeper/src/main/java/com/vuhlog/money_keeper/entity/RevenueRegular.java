package com.vuhlog.money_keeper.entity;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "revenue_regular")
public class RevenueRegular {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private Long amount;
    private String location;
    private String interpretation;
    private Timestamp revenueDate;
    private Timestamp createdDate;
    private Timestamp modifiedDate;
    private String transferType;
    private Double balance;
    private Double convertedBalance;
    private String expenseRegularId;
    private String tripEvent;
    private String collectMoneyWho;
    private String currency;
    private Double exchangeRate;
    private Long convertedAmount;
    private String currencySymbol;


    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "sender_account_id")
    private DictionaryBucketPayment senderAccount;


    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "dictionary_bucket_payment_id")
    private DictionaryBucketPayment dictionaryBucketPayment;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @JoinColumn(name = "dictionary_revenue_id")
    private DictionaryRevenue dictionaryRevenue;

//    @ManyToOne
//    @JoinColumn(name = "trip_event_id")
//    private TripEvent tripEvent;
//
//    @ManyToOne
//    @JoinColumn(name = "collect_money_who_id")
//    private CollectMoneyWho collectMoneyWho;

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
