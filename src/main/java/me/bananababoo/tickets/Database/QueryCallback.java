package me.bananababoo.tickets.Database;

import com.mongodb.client.FindIterable;
import org.bson.Document;

public interface QueryCallback {

    void onQueryFinished(FindIterable<Document> docs);

}
