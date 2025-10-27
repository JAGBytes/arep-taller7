package org.example.util;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

public final class ApiResponses {
    private static final ObjectMapper M = new ObjectMapper();
    private static final String ORIGIN = System.getenv().getOrDefault(
            "ALLOWED_ORIGIN",
            "https://fronttaller7.duckdns.org"
    );
    private ApiResponses(){}
    public static APIGatewayProxyResponseEvent ok(Object b){return b(200,b);}
    public static APIGatewayProxyResponseEvent created(Object b){return b(201,b);}
    public static APIGatewayProxyResponseEvent bad(String m){return b(400, Map.of("message", m));}
    public static APIGatewayProxyResponseEvent notFound(String m){return b(404, Map.of("message", m));}
    public static APIGatewayProxyResponseEvent err(String m){return b(500, Map.of("message", m));}
    private static APIGatewayProxyResponseEvent b(int c, Object o){
        try { return new APIGatewayProxyResponseEvent().withStatusCode(c).withHeaders(baseHeaders()).withBody(M.writeValueAsString(o)); }
        catch(Exception e){ return new APIGatewayProxyResponseEvent().withStatusCode(500).withHeaders(baseHeaders()).withBody("{\"message\":\"serialization error\"}"); }
    }
    private static Map<String,String> baseHeaders() {
        Map<String,String> h = new HashMap<>();
        h.put("Content-Type", "application/json");
        h.put("Access-Control-Allow-Origin", ORIGIN);
        h.put("Access-Control-Allow-Credentials", "true");
        h.put("Access-Control-Allow-Headers", "Authorization,Content-Type");
        h.put("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
        h.put("Vary", "Origin");
        return h;
    }
}
