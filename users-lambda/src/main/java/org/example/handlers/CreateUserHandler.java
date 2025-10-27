package org.example.handlers;


import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.example.db.MongoProvider;
import org.example.util.ApiResponses;

import static com.mongodb.client.model.Filters.eq;

public class CreateUserHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final ObjectMapper M = new ObjectMapper();

    private MongoCollection<Document> coll() {
        String db = System.getenv().getOrDefault("DB_NAME", "arep");
        String col = System.getenv().getOrDefault("COLLECTION_NAME", "users");
        MongoClient client = MongoProvider.client();
        MongoCollection<Document> c = client.getDatabase(db).getCollection(col);
        ensureIndexes(c);
        return c;
    }

    private void ensureIndexes(MongoCollection<Document> c){
        try {
            c.createIndex(Indexes.ascending("sub"), new IndexOptions().unique(true));
        } catch (Exception ignore) {}
    }

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent event, Context ctx) {
        try {
            if (event.getBody() == null || event.getBody().isBlank()) {
                return ApiResponses.bad("Body is required");
            }
            // Parsear el JSON a Document
            Document doc = Document.parse(event.getBody());
            String sub = doc.getString("sub");

            if(sub == null || sub.isBlank()){
                return ApiResponses.bad("Sub Id is required");
            }

            Document existing = coll().find(eq("sub",sub)).first();
            if(existing != null){
                return ApiResponses.conflict("User already exist!");
            }

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