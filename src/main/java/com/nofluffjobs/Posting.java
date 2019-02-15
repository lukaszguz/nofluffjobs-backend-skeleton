package com.nofluffjobs;

public class Posting {

    private String title;
    private String company;
    private String city;
    private String street;
    private String postalCode;
    private Integer salaryMin;
    private Integer salaryMax;

    public Posting(String title, String company, String city, String street, String postalCode, Integer salaryMin, Integer salaryMax) {
        this.title = title;
        this.company = company;
        this.city = city;
        this.street = street;
        this.postalCode = postalCode;
        this.salaryMin = salaryMin;
        this.salaryMax = salaryMax;
    }
}