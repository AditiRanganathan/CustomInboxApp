package com.example.custominboxapp;

public class CustomInboxMessage {
    private long ttl;
    private String title;
    private String message;
    private boolean isRead;

    public CustomInboxMessage(String title, String message, boolean isRead, long ttl) {
        this.title = title;
        this.message = message;
        this.isRead = isRead;
        this.ttl = ttl;
    }

//    public String getMessageId() {
//        return messageId;
//    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public boolean isRead() {
        return isRead;
    }

    public long getTtl() {
        return ttl;
    }

}
