package dev.osunolimits.monetization.models;

import lombok.Data;

@Data
public class StripeConfigModel {
    private String clientSecret = "";
    private String clientPublic = "";
    private String webhookSecret = "";
}
