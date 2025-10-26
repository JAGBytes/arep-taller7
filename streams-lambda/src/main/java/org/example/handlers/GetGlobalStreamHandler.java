package org.example.handlers;

import com.amazonaws.services.lambda.runtime.*;
import com.amazonaws.services.lambda.runtime.events.*;
import com.mongodb.client.MongoCollection;
import org.example.db.MongoProvider;
import org.example.util.ApiResponses;
import org.bson.Document;
import org.bson.types.ObjectId;

import static com.mongodb.client.model.Filters.eq;

public class GetGlobalStreamHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private MongoCollection<Document> coll() {
        var db = System.getenv().getOrDefault("DB_NAME", "arep");
        return MongoProvider.client().getDatabase(db).getCollection("streams");
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context ctx) {
        try {
            // buscar el stream "global"
            var existing = coll().find(eq("name", "global")).first();
            if (existing != null) {
                existing.put("id", ((ObjectId) existing.remove("_id")).toHexString());
                var ca = existing.get("createdAt");
                if (ca instanceof java.util.Date d) existing.put("createdAt", d.toInstant().toString());
                return ApiResponses.ok(existing);
            }

            var doc = new Document("_id", new ObjectId())
                    .append("name", "global")
                    .append("createdAt", new java.util.Date());
            coll().insertOne(doc);

            var resp = new Document("id", ((ObjectId) doc.remove("_id")).toHexString())
                    .append("name", "global")
                    .append("createdAt", ((java.util.Date) doc.remove("createdAt")).toInstant().toString());

            return ApiResponses.created(resp);

        } catch (Exception e) {
            ctx.getLogger().log("GetGlobalStream error: " + e);
            return ApiResponses.err(e.getMessage());
        }
    }
}