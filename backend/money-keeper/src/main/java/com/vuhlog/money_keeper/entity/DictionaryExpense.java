package com.vuhlog.money_keeper.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "dictionary_expense")
@Entity
public class DictionaryExpense {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String parentId;

    private String name;

    private boolean regular;

    private boolean systemDefault;

    private String iconUrl;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "user_id")
    private Users user;

    @OneToMany(mappedBy = "dictionaryExpense")
    @JsonIgnore
    private Set<ExpenseRegular> expenseRegulars;
}
