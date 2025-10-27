package org.example.handlers;

import com.amazonaws.services.lambda.runtime.*;
import com.amazonaws.services.lambda.runtime.events.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import org.example.db.MongoProvider;
import org.example.util.ApiResponses;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;

import static com.mongodb.client.model.Filters.eq;

public class CreatePostHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final ObjectMapper M = new ObjectMapper();

    private String dbName() { return System.getenv().getOrDefault("DB_NAME","arep"); }
    private MongoCollection<Document> streams() { return MongoProvider.client().getDatabase(dbName()).getCollection("streams"); }
    private MongoCollection<Document> posts()   { return MongoProvider.client().getDatabase(dbName()).getCollection("posts"); }

    @Override public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context ctx) {
        try {
            String authHdr = null;
            if (event.getHeaders() != null) {
                var h = event.getHeaders();
                authHdr = h.getOrDefault("Authorization",
                        h.getOrDefault("authorization",
                                h.getOrDefault("AUTHORIZATION", null)));
            }
            if (authHdr == null || authHdr.isBlank()) {
                return ApiResponses.bad("Missing Authorization header");
            }
            String content = null, stream = "global"; String userId = null;

            if ((content==null || userId==null) && event.getBody()!=null && !event.getBody().isBlank()) {
                JsonNode json = M.readTree(event.getBody());
                if (content==null) content = json.path("content").asText(null);
                if (userId==null && json.has("userId")) userId = json.get("userId").asText(null);
            }

            if (content==null || content.isBlank()) return ApiResponses.bad("'content' is required");
            if (content.length()>140)              return ApiResponses.bad("'content' must be <= 140 chars");
            if (userId==null)                      return ApiResponses.bad("'userId' is required");

            var baseUrl = System.getenv().getOrDefault("USERS_BASE_URL", "https://o8dquugs9e.execute-api.us-east-1.amazonaws.com/beta");
            var req = java.net.http.HttpRequest.newBuilder(
                            java.net.URI.create(baseUrl + "/users/" + userId))
                    .timeout(java.time.Duration.ofSeconds(6))
                    .header("Authorization", authHdr)
                    .GET()
                    .build();
            var res = java.net.http.HttpClient.newHttpClient()
                    .send(req, java.net.http.HttpResponse.BodyHandlers.discarding());

            if (res.statusCode() == 404) return ApiResponses.notFound("User not found: " + userId);
            if (res.statusCode() >= 300) return ApiResponses.err("User service error: " + res.statusCode());

            var streamBase = System.getenv().getOrDefault("STREAMS_BASE_URL",
                    "https://o8dquugs9e.execute-api.us-east-1.amazonaws.com/beta");
            var streamReq = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(streamBase + "/streams/global"))
                    .timeout(java.time.Duration.ofSeconds(6))
                    .header("Authorization", authHdr)
                    .GET()
                    .build();
            var client = HttpClient.newHttpClient();

            var streamRes = client.send(streamReq, HttpResponse.BodyHandlers.ofString());

            if (streamRes.statusCode() == 404)
                return ApiResponses.notFound("Stream 'global' not found");
            if (streamRes.statusCode() >= 300)
                return ApiResponses.err("Stream service error: " + streamRes.statusCode());

            JsonNode streamJson = M.readTree(streamRes.body());
            String streamIdStr = streamJson.path("id").asText(null);
            if (streamIdStr == null || streamIdStr.isBlank())
                return ApiResponses.err("Invalid response from Stream service");

            var streamId = new ObjectId(streamIdStr);

            var doc = new Document("_id", new ObjectId())
                    .append("streamId", streamId)
                    .append("userId", userId)
                    .append("content", content)
                    .append("createdAt", new java.util.Date());

            posts().insertOne(doc);

            var resp = new Document("id", ((ObjectId)doc.remove("_id")).toHexString())
                    .append("streamId", streamId.toHexString())
                    .append("userId", userId)
                    .append("content", content)
                    .append("createdAt", ((java.util.Date)doc.remove("createdAt")).toInstant().toString());

            return ApiResponses.created(resp);

        } catch (NumberFormatException bad) {
            return ApiResponses.bad("'userId' must be a number");
        } catch (Exception e) {
            ctx.getLogger().log("CreatePost error: " + e);
            return ApiResponses.err(e.getMessage());
        }
    }
}
