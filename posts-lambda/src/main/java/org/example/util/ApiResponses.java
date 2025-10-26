package org.example.util;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

public final class ApiResponses {
    private static final ObjectMapper M = new ObjectMapper();
    private static final Map<String,String> H = Map.of("Content-Type","application/json");
    private ApiResponses(){}

    public static APIGatewayProxyResponseEvent ok(Object b){return b(200,b);}
    public static APIGatewayProxyResponseEvent created(Object b){return b(201,b);}
    public static APIGatewayProxyResponseEvent bad(String m){return b(400, Map.of("message", m));}
    public static APIGatewayProxyResponseEvent notFound(String m){return b(404, Map.of("message", m));}
    public static APIGatewayProxyResponseEvent err(String m){return b(500, Map.of("message", m));}

    private static APIGatewayProxyResponseEvent b(int c, Object o){
        try { return new APIGatewayProxyResponseEvent().withStatusCode(c).withHeaders(H).withBody(M.writeValueAsString(o)); }
        catch(Exception e){ return new APIGatewayProxyResponseEvent().withStatusCode(500).withHeaders(H).withBody("{\"message\":\"serialization error\"}"); }
    }
}