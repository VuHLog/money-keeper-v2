package com.vuhlog.money_keeper.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "trip_event")
public class TripEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String name;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users user;

    @OneToMany(mappedBy = "tripEvent")
    private Set<ExpenseRegular> expenseRegulars;

    @OneToMany(mappedBy = "tripEvent")
    private Set<RevenueRegular> revenueRegulars;
}
