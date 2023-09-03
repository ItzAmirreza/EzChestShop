package me.deadlight.ezchestshop.utils;

import me.deadlight.ezchestshop.data.Config;
import org.bukkit.configuration.ConfigurationSection;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class WebhookSender {

    private static final ScheduledExecutorService fastScheduler = Executors.newScheduledThreadPool(1);
    private static final ScheduledExecutorService slowScheduler = Executors.newScheduledThreadPool(1);
    private static final LinkedBlockingQueue<JSONObject> fastQueue = new LinkedBlockingQueue<>();
    private static final LinkedBlockingQueue<JSONObject> slowQueue = new LinkedBlockingQueue<>();
    private static final AtomicInteger fastQueueCounter = new AtomicInteger(0);

    static {
        fastScheduler.scheduleAtFixedRate(() -> {
            try {
                for (int i = 0; i < 5; i++) {
                    JSONObject message = fastQueue.poll();
                    if (message != null) {
                        sendDiscordWebhookInternal(message);
                        fastQueueCounter.incrementAndGet();
                    }
                }
            } catch (Exception e) {
                System.err.println("Error while sending queued webhooks: " + e.getMessage());
            }
        }, 0, 1, TimeUnit.SECONDS);

        slowScheduler.scheduleAtFixedRate(() -> {
            try {
                if (fastQueueCounter.get() >= 25) {
                    JSONObject message = slowQueue.poll();
                    if (message != null) {
                        sendDiscordWebhookInternal(message);
                    }
                }
                fastQueueCounter.set(0);  // Reset the counter every minute
            } catch (Exception e) {
                System.err.println("Error while sending queued webhooks: " + e.getMessage());
            }
        }, 0, 1, TimeUnit.MINUTES);
    }

    public static void sendDiscordWebhook(JSONObject messageJson) {
        if (fastQueueCounter.get() < 25) {
            fastQueue.offer(messageJson);
        } else {
            slowQueue.offer(messageJson);
        }
    }

    private static void sendDiscordWebhookInternal(JSONObject messageJson) {
        try {
            URL url = new URL(Config.discordWebhookUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            connection.setDoOutput(true);

            try (OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), "UTF-8")) {
                writer.write(messageJson.toString());
            }

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                System.err.println("Failed to send webhook message: " + responseCode);
            }

            connection.disconnect();
        } catch (IOException e) {
            System.err.println("Error sending webhook message: " + e.getMessage());
        }
    }

    public static void sendDiscordNewTransactionAlert(
            String buyer,
            String seller,
            String item_name,
            String price,
            String currency,
            String shop_location,
            String time,
            String count,
            String owner
    ) { //Needed for embeds: %BUYER%, %SELLER%, %ITEM_NAME%, %PRICE%, %CURRENCY%, %SHOP_LOCATION%, %TIME%, %COUNT%

        if (!Config.isDiscordNotificationEnabled) {
            return;
        }
        if (!Config.isBuySellWebhookEnabled) {
            return;
        }

        ConfigurationSection webhookSection = Config.buySellWebhookTemplate;
        String jsonString = configurationSectionToJsonString(webhookSection);

        // Replace the placeholders in the JSON string
        jsonString = jsonString.replace("%BUYER%", buyer).replace("%SELLER%", seller).replace("%ITEM_NAME%", item_name).replace("%PRICE%", price).replace("%CURRENCY%", currency).replace("%SHOP_LOCATION%", shop_location).replace("%TIME%", time).replace("%COUNT%", count).replace("%OWNER%", owner);

        // Parse the JSON string into a JSONObject
        JSONParser parser = new JSONParser();
        JSONObject webhookData;
        try {
            webhookData = (JSONObject) parser.parse(jsonString);
        } catch (ParseException e) {
            System.err.println("Error parsing webhook data from config.yml: " + e.getMessage());
            return;
        }

        sendDiscordWebhook(webhookData);

    }

    public static void sendDiscordNewShopAlert( //placeholders: %OWNER%, %BUYING_PRICE%, %SELLING_PRICE%, %ITEM_NAME%, %MATERIAL%, %TIME%, %SHOP_LOCATION%
            String owner,
            String buying_price,
            String selling_price,
            String item_name,
            String material,
            String time,
            String shop_location
    ) {

        if (!Config.isDiscordNotificationEnabled) {
            return;
        }
        if (!Config.isNewShopWebhookEnabled) {
            return;
        }

        ConfigurationSection webhookSection = Config.newShopWebhookTemplate;
        String jsonString = configurationSectionToJsonString(webhookSection);

        // Replace the placeholders in the JSON string
        jsonString = jsonString.replace("%OWNER%", owner).replace("%BUYING_PRICE%", buying_price).replace("%SELLING_PRICE%", selling_price).replace("%ITEM_NAME%", item_name).replace("%MATERIAL%", material).replace("%TIME%", time).replace("%SHOP_LOCATION%", shop_location);

        // Parse the JSON string into a JSONObject
        JSONParser parser = new JSONParser();
        JSONObject webhookData;
        try {
            webhookData = (JSONObject) parser.parse(jsonString);
        } catch (ParseException e) {
            System.err.println("Error parsing webhook data from config.yml: " + e.getMessage());
            return;
        }

        sendDiscordWebhook(webhookData);

    }

    public static String configurationSectionToJsonString(ConfigurationSection section) {
        JSONObject jsonObject = new JSONObject();
        for (String key : section.getKeys(true)) {
            Object value = section.get(key);
            jsonObject.put(key, value);
        }
        return jsonObject.toJSONString();
    }

}
