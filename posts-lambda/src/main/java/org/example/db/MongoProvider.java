package org.example.db;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

public final class MongoProvider {
    private MongoProvider(){}
    private static class Holder {
        static final MongoClient CLIENT = MongoClients.create(
                MongoClientSettings.builder()
                        .applyConnectionString(new ConnectionString(System.getenv("MONGODB_URI")))
                        .applyToSslSettings(b -> b.enabled(true))
                        .build());
    }
    public static MongoClient client(){ return Holder.CLIENT; }
}