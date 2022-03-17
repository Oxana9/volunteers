package com.example.volunteers;

public class Model {

    private String userId, task, description, id, date, status;
    private long category, place;
    private String whoHelped;

    public Model() {
    }

//    public Model(String userId, String task, String description, String id, String date) {
//        this(userId, task,description,id,date,0,0);
//    }

    public Model(String userId, String task, String description, String id, String date, long category, long place, String status) {
        this.userId = userId;
        this.task = task;
        this.description = description;
        this.id = id;
        this.date = date;
        this.category = category;
        this.place = place;
        this.status = status;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getCategory() { return category; }

    public void setCategory(int category) {
        this.category = category;
    }

    public long getPlace() {
        return place;
    }

    public void setPlace(long place) {
        this.place = place;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
