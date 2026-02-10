package com.agrowmart.dto.auth.shop;

public class WorkingHourDTO {

    private String day;   // MONDAY
    private String open;  // 09:00
    private String close; // 18:00

    public WorkingHourDTO() {}

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getOpen() {
        return open;
    }

    public void setOpen(String open) {
        this.open = open;
    }

    public String getClose() {
        return close;
    }

    public void setClose(String close) {
        this.close = close;
    }
}