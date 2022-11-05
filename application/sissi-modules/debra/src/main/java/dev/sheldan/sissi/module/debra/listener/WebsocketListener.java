package dev.sheldan.sissi.module.debra.listener;

import dev.sheldan.abstracto.core.listener.AsyncStartupListener;
import dev.sheldan.sissi.module.debra.config.DebraProperties;
import dev.sheldan.sissi.module.debra.model.Donation;
import dev.sheldan.sissi.module.debra.service.DonationService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class WebsocketListener extends WebSocketListener implements AsyncStartupListener {

    @Autowired
    private DonationService donationService;

    @Autowired
    private DebraProperties debraProperties;

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        log.info("Connected to donation websocket.");
        super.onOpen(webSocket, response);
    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        log.info("Handling received message on websocket.");
        try {
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
    }

    @Override
    public void execute() {
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .retryOnConnectionFailure(true)
                .build();
        Request request = new Request.Builder()
                .url(debraProperties.getWebsocketURL())
                .build();
        client.newWebSocket(request, this);
        client.dispatcher().executorService().shutdown();
    }
}
