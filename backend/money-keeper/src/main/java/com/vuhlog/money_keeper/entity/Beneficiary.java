//package com.vuhlog.money_keeper.entity;
//
//import jakarta.persistence.*;
//import lombok.*;
//
//import java.util.List;
//import java.util.Set;
//
//@Setter
//@Getter
//@AllArgsConstructor
//@NoArgsConstructor
//@Builder
//@Entity
//@Table(name = "beneficiary")
//public class Beneficiary {
//    @Id
//    @GeneratedValue(strategy = GenerationType.UUID)
//    private String id;
//    private String name;
//
//    @ManyToOne
//    @JoinColumn(name = "user_id")
//    private Users user;
//
//    @OneToMany(mappedBy = "beneficiary")
//    private Set<ExpenseRegular> expenseRegulars;
//}
