package com.vuhlog.money_keeper.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NearestTransaction {
    private String id;
    private Double balance;
    private Double amount;
    private String type;
}
