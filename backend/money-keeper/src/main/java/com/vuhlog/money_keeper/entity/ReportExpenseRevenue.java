package com.vuhlog.money_keeper.entity;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "report_expense_revenue")
public class ReportExpenseRevenue {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private int month;
    private int year;
    private String type;
    private long totalExpense;
    private long totalRevenue;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private String bucketPaymentId;
    private String categoryId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users user;

    @PrePersist
    protected void onCreate() {
        this.createdAt = new Timestamp(System.currentTimeMillis());
        this.updatedAt = new Timestamp(System.currentTimeMillis());
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = new Timestamp(System.currentTimeMillis());
    }
}
