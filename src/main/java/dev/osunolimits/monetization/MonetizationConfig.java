package dev.osunolimits.monetization;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import com.google.gson.Gson;

import dev.osunolimits.main.App;
import dev.osunolimits.monetization.models.MonetizationConfigModel;
import dev.osunolimits.monetization.models.StripeConfigModel;

public class MonetizationConfig {

    public final boolean ENABLED;
    private StripeConfigModel stripeConfigModel;

    public MonetizationConfig() {
        File monetizationFile = new File("data/monetization.json");

        // Ensure the directory exists
        createDirectoryIfNotExists("data/monetization");

        if (!monetizationFile.exists()) {
            ENABLED = false;
            createDefaultMonetizationConfig(monetizationFile);
        } else {
            ENABLED = loadMonetizationConfig(monetizationFile);
        }

        if (ENABLED) {
            boolean exEnabled = App.env.get("EXPUBSUBS").equals("true");
            if (!exEnabled) {
                App.log.error("Monetization is enabled but EXPUBSUBS is not. Please enable EXPUBSUBS in your .env file.");
                System.exit(0);
            }

            File stripeConfigFile = new File("data/monetization/stripe.json");

            // Ensure the directory exists
            createDirectoryIfNotExists("data/monetization");

            if (!stripeConfigFile.exists()) {
                createDefaultStripeConfig(stripeConfigFile);
            } else {
                stripeConfigModel = loadStripeConfig(stripeConfigFile);
                validateStripeConfig(stripeConfigFile);
            }
        }
    }

    // Method to return the StripeConfigModel
    public StripeConfigModel getStripeConfig() {
        return stripeConfigModel;
    }

    private void createDirectoryIfNotExists(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            boolean dirCreated = dir.mkdirs();
            if (dirCreated) {
                App.log.info("Created directory at {}", path);
            } else {
                App.log.error("Failed to create directory at {}", path);
            }
        }
    }

    private void createDefaultMonetizationConfig(File monetizationFile) {
        MonetizationConfigModel model = new MonetizationConfigModel();
        model.setEnabled(false);
        String fileContent = new Gson().toJson(model);

        try (FileWriter writer = new FileWriter(monetizationFile)) {
            writer.write(fileContent);
            App.log.info("Created default monetization configuration at {}", monetizationFile.getPath());
        } catch (IOException e) {
            App.log.error("Failed to create default monetization configuration", e);
        }
    }

    private boolean loadMonetizationConfig(File monetizationFile) {
        try {
            String fileContent = Files.readString(monetizationFile.toPath());
            return new Gson().fromJson(fileContent, MonetizationConfigModel.class).isEnabled();
        } catch (IOException e) {
            App.log.error("Failed to read monetization configuration", e);
            return false;
        }
    }

    private void createDefaultStripeConfig(File stripeConfigFile) {
        StripeConfigModel stripeModel = new StripeConfigModel();
        String fileContent = new Gson().toJson(stripeModel);

        try (FileWriter writer = new FileWriter(stripeConfigFile)) {
            writer.write(fileContent);
            App.log.info("Created default Stripe configuration at {}", stripeConfigFile.getPath());
        } catch (IOException e) {
            App.log.error("Failed to create default Stripe configuration", e);
        }
    }

    private StripeConfigModel loadStripeConfig(File stripeConfigFile) {
        try {
            String fileContent = Files.readString(stripeConfigFile.toPath());
            return new Gson().fromJson(fileContent, StripeConfigModel.class);
        } catch (IOException e) {
            App.log.error("Failed to read Stripe configuration", e);
            return null;  // Return null in case of an error
        }
    }

    private void validateStripeConfig(File stripeConfigFile) {
        if (stripeConfigModel == null) {
            App.log.error("Stripe configuration is invalid or missing.");
            System.exit(0);
        }

        if (stripeConfigModel.getClientPublic().isEmpty() || 
            stripeConfigModel.getClientSecret().isEmpty() || 
            stripeConfigModel.getWebhookSecret().isEmpty()) {

            App.log.error("Stripe configuration is incomplete. Please fill in the required fields in {}", stripeConfigFile.getPath());
            System.exit(0);
        } else {
            App.log.info("Stripe configuration loaded successfully.");
        }
    }
}
