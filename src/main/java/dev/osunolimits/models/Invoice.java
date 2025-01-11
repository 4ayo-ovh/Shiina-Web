package dev.osunolimits.models;

import lombok.Data;

@Data
public class Invoice {
    private int user_id;
    private int months;
    private double total;
    private String payment_id;
}
