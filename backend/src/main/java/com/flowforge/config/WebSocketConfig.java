package com.flowforge.config;

import com.flowforge.security.JwtUtils;
import com.flowforge.security.UserDetailsServiceImpl;
import com.flowforge.service.JobAccessService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final JwtUtils jwtUtils;
    private final UserDetailsServiceImpl userDetailsService;
    private final JobAccessService jobAccessService;
    private final String[] allowedOrigins;

    public WebSocketConfig(JwtUtils jwtUtils, UserDetailsServiceImpl userDetailsService,
                           JobAccessService jobAccessService,
                           @Value("${flowforge.cors.allowed-origins:http://localhost:5173}") String[] allowedOrigins) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
        this.jobAccessService = jobAccessService;
        this.allowedOrigins = allowedOrigins;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOrigins(allowedOrigins).withSockJS();
    }

    @Override
    public void configureClientInboundChannel(org.springframework.messaging.simp.config.ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (accessor == null) {
                    return message;
                }
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String authorization = accessor.getFirstNativeHeader("Authorization");
                    if (authorization == null || !authorization.startsWith("Bearer ")) {
                        throw new org.springframework.security.access.AccessDeniedException("Missing WebSocket token");
                    }
                    String token = authorization.substring(7);
                    if (!jwtUtils.validateJwtToken(token)) {
                        throw new org.springframework.security.access.AccessDeniedException("Invalid WebSocket token");
                    }
                    UserDetails userDetails = userDetailsService.loadUserByUsername(jwtUtils.getUserNameFromJwtToken(token));
                    accessor.setUser(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));
                } else if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
                    String destination = accessor.getDestination();
                    if (accessor.getUser() == null || destination == null || !destination.startsWith("/topic/jobs/")) {
                        throw new org.springframework.security.access.AccessDeniedException("WebSocket subscription denied");
                    }
                    try {
                        Long jobId = Long.valueOf(destination.substring("/topic/jobs/".length()));
                        jobAccessService.requireAccess(jobId, accessor.getUser().getName());
                    } catch (NumberFormatException exception) {
                        throw new org.springframework.security.access.AccessDeniedException("Invalid job subscription");
                    }
                }
                return message;
            }
        });
    }
}
