package dev.sheldan.sissi.module.debra.listener;

import dev.sheldan.abstracto.core.listener.AsyncStartupListener;
import dev.sheldan.abstracto.core.service.ConfigService;
import dev.sheldan.sissi.module.debra.config.DebraProperties;
import dev.sheldan.sissi.module.debra.model.Donation;
import dev.sheldan.sissi.module.debra.service.DonationService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static dev.sheldan.sissi.module.debra.config.DebraFeatureConfig.DEBRA_DONATION_NOTIFICATION_DELAY_CONFIG_KEY;
import static dev.sheldan.sissi.module.debra.config.DebraFeatureConfig.DEBRA_DONATION_NOTIFICATION_SERVER_ID_ENV_NAME;

@Component
@Slf4j
public class WebsocketListener extends WebSocketListener implements AsyncStartupListener {

    @Autowired
    private DonationService donationService;

    @Autowired
    private DebraProperties debraProperties;

    @Autowired
    private ConfigService configService;

    private WebSocket webSocketObj;
    private OkHttpClient clientObj;

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        log.info("Connected to donation websocket.");
        super.onOpen(webSocket, response);
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        CompletableFuture.runAsync(() -> {
            log.info("Handling received message on websocket.");
            try {
                Long targetServerId = Long.parseLong(System.getenv(DEBRA_DONATION_NOTIFICATION_SERVER_ID_ENV_NAME));
                Long delayMillis = configService.getLongValueOrConfigDefault(DEBRA_DONATION_NOTIFICATION_DELAY_CONFIG_KEY, targetServerId);
                log.info("Waiting {} milli seconds to send notification.", delayMillis);
                Thread.sleep(delayMillis);
                log.info("Loading new donation amount and sending notification.");
                Donation donation = donationService.parseDonationFromMessage(text);
                donationService.sendDonationNotification(donation).thenAccept(unused -> {
                    log.info("Successfully notified about donation.");
                }).exceptionally(throwable -> {
                    log.error("Failed to notify about donation.", throwable);
                    return null;
                });
            } catch (Exception exception) {
                log.error("Failed to handle websocket message.", exception);
            }
        });
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, @Nullable Response response) {
        log.warn("Websocket connection failed...", t);
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        log.info("Closing websocket connection. It was closed with code {} and reason {}.", code, reason);
    }

    @Override
    public void execute() {
        if(clientObj != null) {
            clientObj.connectionPool().evictAll();
            clientObj.dispatcher().executorService().shutdownNow();
        }
        clientObj = new OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .retryOnConnectionFailure(true)
                .build();
        startConnection(clientObj);
        clientObj.dispatcher().executorService().shutdown();
    }

    private void startConnection(OkHttpClient client) {
        log.info("Starting websocket connection.");
        Request request = new Request.Builder()
                .url(debraProperties.getWebsocketURL())
                .build();
        this.webSocketObj = client.newWebSocket(request, this);
    }
}
