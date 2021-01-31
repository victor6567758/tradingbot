package com.tradebot.config;

import com.tradebot.util.GeneralConst;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker(GeneralConst.WS_TOPIC);
        registry.setApplicationDestinationPrefixes(GeneralConst.WS_SEND_PREFIX);
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(GeneralConst.WS_WEBSOCKET_ENDPOINT);
        registry.addEndpoint(GeneralConst.WS_SOCKJS_ENDPOINT).withSockJS();
    }
}
