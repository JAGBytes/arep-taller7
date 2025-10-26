package org.example.db;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

public final class MongoProvider {
    private MongoProvider() {}

    private static class Holder {
        static final MongoClient CLIENT = create();
        private static MongoClient create() {
            String uri = System.getenv("MONGODB_URI");
            if (uri == null || uri.isBlank()) {
                throw new IllegalStateException("MONGODB_URI env var is required");
            }
            return MongoClients.create(
                    MongoClientSettings.builder()
                            .applyConnectionString(new ConnectionString(uri))
                            .applyToSslSettings(b -> b.enabled(true))
                            .build()
            );
        }
    }

    public static MongoClient client() {
        return Holder.CLIENT;
    }
}