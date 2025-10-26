package org.example.handlers;

import com.amazonaws.services.lambda.runtime.*;
import com.amazonaws.services.lambda.runtime.events.*;
import com.mongodb.client.MongoCollection;
import org.example.db.MongoProvider;
import org.example.util.ApiResponses;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Sorts.descending;

public class ListPostsHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private String dbName() { return System.getenv().getOrDefault("DB_NAME","arep"); }
    private MongoCollection<Document> streams() { return MongoProvider.client().getDatabase(dbName()).getCollection("streams"); }
    private MongoCollection<Document> posts()   { return MongoProvider.client().getDatabase(dbName()).getCollection("posts"); }

    @Override public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context ctx) {
        try {
            String stream = "global";
            int limit = 20;

            var q = event.getQueryStringParameters();
            if (q!=null) {
                if (q.get("stream")!=null) stream = q.get("stream");
                if (q.get("limit")!=null)  try { limit = Math.min(100, Math.max(1, Integer.parseInt(q.get("limit")))); } catch (Exception ignore) {}
            }

            var s = streams().find(eq("name", stream)).first();
            if (s==null) return ApiResponses.ok(new ArrayList<>()); // sin stream => lista vacía
            var streamId = (ObjectId) s.get("_id");

            var list = new ArrayList<Document>();
            for (var doc : posts().find(eq("streamId", streamId)).sort(descending("createdAt")).limit(limit)) {
                var id      = ((ObjectId)doc.remove("_id")).toHexString();
                var created = ((java.util.Date)doc.remove("createdAt")).toInstant().toString();
                list.add(new Document("id", id)
                        .append("streamId", streamId.toHexString())
                        .append("userId", doc.get("userId"))
                        .append("content", doc.getString("content"))
                        .append("createdAt", created));
            }
            return ApiResponses.ok(list);

        } catch (Exception e) {
            ctx.getLogger().log("ListPosts error: " + e);
            return ApiResponses.err(e.getMessage());
        }
    }
}