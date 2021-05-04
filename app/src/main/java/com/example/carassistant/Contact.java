package com.example.carassistant;

public class Contact {
    private String name;
    private String phone;

    public Contact() {
        // Empty constructor needed
    }

    public Contact(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }
}
