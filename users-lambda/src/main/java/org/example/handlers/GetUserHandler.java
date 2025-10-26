package org.example.handlers;


import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.example.db.MongoProvider;
import org.example.util.ApiResponses;

import static com.mongodb.client.model.Filters.eq;

public class GetUserHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private MongoCollection<Document> coll() {
        String db = System.getenv().getOrDefault("DB_NAME", "arep");
        String col = System.getenv().getOrDefault("COLLECTION_NAME", "users");
        MongoClient client = MongoProvider.client();
        return client.getDatabase(db).getCollection(col);
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context ctx) {
        try {
            String id = event.getPathParameters() != null ? event.getPathParameters().get("id") : null;
            if (id == null || id.isBlank()) return ApiResponses.bad("Path param 'id' is required");

            Document found = coll().find(eq("_id", new ObjectId(id))).first();
            if (found == null) return ApiResponses.ok(new Document("message", "User not found"));

            // Convertir _id -> id
            String hex = ((ObjectId) found.get("_id")).toHexString();
            found.remove("_id");
            found.put("id", hex);

            return ApiResponses.ok(found);
        } catch (IllegalArgumentException badId) {
            // id no es un ObjectId válido
            return ApiResponses.bad("Invalid id format");
        } catch (Exception e) {
            ctx.getLogger().log("GetUser error: " + e);
            return ApiResponses.err(e.getMessage());
        }
    }
}