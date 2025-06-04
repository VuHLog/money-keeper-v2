package com.vuhlog.money_keeper.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "users")
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column
    private String id;

    @Column
    private String fullName;

    @Column
    private String username;

    @Column
    private String password;

    @Column
    private String email;

    private String avatarUrl;

    @Column
    private Boolean oAuth2;

    @OneToMany(mappedBy = "user",cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JsonIgnore
    private Set<UserRole> user_roles;

    @OneToMany(mappedBy = "user",cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JsonIgnore
    private Set<DictionaryExpense> dictionaryExpenses;

    @OneToMany(mappedBy = "user",cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    @JsonIgnore
    private Set<DictionaryRevenue> dictionaryRevenues;

    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private Set<DictionaryBucketPayment> dictionaryBucketPayment;

//    @OneToMany(mappedBy = "user")
//    @JsonIgnore
//    private Set<Beneficiary> beneficiaries;
//
//    @OneToMany(mappedBy = "user")
//    @JsonIgnore
//    private Set<TripEvent> tripEvents;
//
//    @OneToMany(mappedBy = "user")
//    @JsonIgnore
//    private Set<CollectMoneyWho> collectMoneyWho;

    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private Set<ReportExpenseRevenue> reportExpenseRevenues;

    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private Set<ExpenseLimit> expenseLimits;

    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private Set<Notification> notifications;

    @OneToOne(mappedBy = "user")
    @JsonIgnore
    private ForgotPassword forgotPassword;

    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private Set<FinancialGoal> financialGoals;

    @PrePersist
    public void onCreate() {
        if(oAuth2 == null) {
            oAuth2 = false;
        }
    }
}