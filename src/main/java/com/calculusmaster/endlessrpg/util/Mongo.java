package com.calculusmaster.endlessrpg.util;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class Mongo
{
    //Connection Strings & Clients
    private static final ConnectionString CONNECT_MAIN = new ConnectionString(PrivateInfo.MAIN_USER);
    private static final MongoClient CLIENT_MAIN = MongoClients.create(MongoClientSettings.builder().applyConnectionString(CONNECT_MAIN).retryReads(true).retryWrites(true).build());

    //Databases
    private static final MongoDatabase EndlessRPGDB = CLIENT_MAIN.getDatabase("EndlessRPG");

    //Database Collections
    public static final MongoCollection<Document> PlayerData = EndlessRPGDB.getCollection("PlayerData");
    public static final MongoCollection<Document> ServerData = EndlessRPGDB.getCollection("ServerData");
    public static final MongoCollection<Document> CharacterData = EndlessRPGDB.getCollection("CharacterData");
}