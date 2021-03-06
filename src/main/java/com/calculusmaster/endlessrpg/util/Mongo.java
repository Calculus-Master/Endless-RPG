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
    public static final MongoCollection<Document> LootData = EndlessRPGDB.getCollection("LootData");
    public static final MongoCollection<Document> LocationData = EndlessRPGDB.getCollection("LocationData");
    public static final MongoCollection<Document> RealmData = EndlessRPGDB.getCollection("RealmData");
    public static final MongoCollection<Document> QuestData = EndlessRPGDB.getCollection("QuestData");
    public static final MongoCollection<Document> ComponentData = EndlessRPGDB.getCollection("ComponentData");
}