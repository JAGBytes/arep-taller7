package org.example.model;

import org.bson.types.ObjectId;
import java.util.Date;

public class StreamDoc {
    private ObjectId id;
    private String name;
    private Date createdAt;

    public ObjectId getId(){return id;}
    public void setId(ObjectId id){this.id=id;}
    public String getName(){return name;}
    public void setName(String n){this.name=n;}
    public Date getCreatedAt(){return createdAt;}
    public void setCreatedAt(Date d){this.createdAt=d;}
}