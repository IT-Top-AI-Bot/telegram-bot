package com.aquadev.telegrambot.client;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.observation.ClientRequestObservationContext;
import org.springframework.http.client.observation.DefaultClientRequestObservationConvention;

public class UriClientObservationConvention extends DefaultClientRequestObservationConvention {

    @Override
    public String getContextualName(ClientRequestObservationContext context) {
        HttpRequest request = context.getCarrier();
        if (request == null) {
            return super.getContextualName(context);
        }
        return request.getMethod().name() + " " + request.getURI().getPath();
    }
}
