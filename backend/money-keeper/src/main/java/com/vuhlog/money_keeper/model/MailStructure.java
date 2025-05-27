package com.vuhlog.money_keeper.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MailStructure {
    private String subject;
    private String message;
}
