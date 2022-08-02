package me.bananababoo.tickets.TicketStuff;

import me.bananababoo.tickets.Database.MongodbServer;
import org.bson.types.ObjectId;

import java.util.Date;

public class Ticket {

    private final ObjectId _id;
    private final String playername;
    private final Integer id;
    private final Category category;
    private Status status;
    private String description;
    private String name;


    public Ticket(String playername, Category category, String name, String description){
        this._id = new ObjectId();
        this.playername = playername;
        this.category = category;
        this.description = description;
        this.name = name;
        this.status = Status.NORMAL;
        this.id = MongodbServer.getCurrentId();
    }
    public String toString(){
        return this.name;
    }

    public String name(){
        return this.name;
    }

    public Date dateCreated(){
        return this._id.getDate();
    }

    public Category category(){
        return this.category;
    }

    public Status status() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String playerName(){
       return this.playername;
    }

    public Date DateCreated(){
        return this._id.getDate();
    }

    public String description(){
        return this.description;
    }

    public ObjectId _ID(){
        return this._id;
    }
    public Integer ID(){
        return this.id;
    }

}
