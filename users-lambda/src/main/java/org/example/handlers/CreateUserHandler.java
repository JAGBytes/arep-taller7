package org.example.handlers;


import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.example.db.MongoProvider;
import org.example.util.ApiResponses;

public class CreateUserHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final ObjectMapper M = new ObjectMapper();

    private MongoCollection<Document> coll() {
        String db = System.getenv().getOrDefault("DB_NAME", "arep");
        String col = System.getenv().getOrDefault("COLLECTION_NAME", "users");
        MongoClient client = MongoProvider.client();
        return client.getDatabase(db).getCollection(col);
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context ctx) {
        try {
            if (event.getBody() == null || event.getBody().isBlank()) {
                return ApiResponses.bad("Body is required");
            }
            // Parsear el JSON a Document
            Document doc = Document.parse(event.getBody());

            // Generar _id si no viene
            if (!doc.containsKey("_id")) {
                doc.put("_id", new ObjectId());
            }

            coll().insertOne(doc);

            // Responder con { id: "<hex>", ...resto }
            String id = ((ObjectId) doc.get("_id")).toHexString();
            doc.remove("_id");
            doc.put("id", id);

            return ApiResponses.created(doc);
        } catch (Exception e) {
            ctx.getLogger().log("CreateUser error: " + e);
            return ApiResponses.err(e.getMessage());
        }
    }
}