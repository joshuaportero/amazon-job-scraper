package me.joshuaportero.ajs.notifications;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;

@Slf4j
public class DiscordWebhook {
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient client;
    private final String webhookUrl;

    public DiscordWebhook(String webhookUrl) {
        this.client = new OkHttpClient();
        this.webhookUrl = webhookUrl;
    }

    public void sendNotification(String message) {
        RequestBody body = RequestBody.create(message, JSON);

        Request request = new Request.Builder()
                .url(webhookUrl)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        try {
            log.info("Sending notification...");
            long startTime = System.currentTimeMillis();
            Response response = client.newCall(request).execute();
            long endTime = System.currentTimeMillis();
            if (response.isSuccessful()) {
                log.info("Notification sent successfully! (" + (endTime - startTime) + "ms)");
            } else {
                log.error("Failed to send notification: " + response.code() + ", " + response.message() + ".");
            }
        } catch (IOException e) {
            log.error("Failed to send notification: " + e.getMessage());
        }
    }
}
