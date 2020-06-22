package com.example.chattingdemo.contacts;

public class ContactHandler {

    private String photo;
    private String message;
    private int online_status;
    private int read_status;
    private String row_id;
    private String bucket_id;
    private String seller_key;
    private String name;

    public ContactHandler(String name, String photo, String message, int online_status, int read_status, String row, String id, String sid) {
        this.name = name;
        this.photo = photo;
        this.message = message;
        this.online_status = online_status;
        this.read_status = read_status;
        this.bucket_id = id;
        this.seller_key = sid;
        this.row_id = row;
    }

    public String getPhoto() {
        return photo;
    }

    public String getMessage() {
        return message;
    }

    public int getOnlineStatus() {
        return online_status;
    }

    public int getReadStatus() {
        return read_status;
    }

    public String getRowId() {
        return row_id;
    }

    public String getBucketId() {
        return bucket_id;
    }

    public String getSellerId() {
        return seller_key;
    }

    public String getName() {
        return name;
    }
}
