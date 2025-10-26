package org.example.util;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public final class ApiResponses {
    private static final ObjectMapper M = new ObjectMapper();
    private static final Map<String,String> H = Map.of("Content-Type","application/json");
    private ApiResponses(){}

    public static APIGatewayProxyResponseEvent ok(Object body){ return build(200, body); }
    public static APIGatewayProxyResponseEvent created(Object body){ return build(201, body); }
    public static APIGatewayProxyResponseEvent bad(String m){ return build(400, Map.of("message", m)); }
    public static APIGatewayProxyResponseEvent err(String m){ return build(500, Map.of("message", m)); }

    private static APIGatewayProxyResponseEvent build(int code, Object body){
        try {
            return new APIGatewayProxyResponseEvent().withStatusCode(code).withHeaders(H).withBody(M.writeValueAsString(body));
        } catch (Exception e) {
            return new APIGatewayProxyResponseEvent().withStatusCode(500).withHeaders(H).withBody("{\"message\":\"serialization error\"}");
        }
    }
}