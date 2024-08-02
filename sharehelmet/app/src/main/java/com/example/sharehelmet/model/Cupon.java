package com.example.sharehelmet.model;

public class Cupon {
    private String code;
    private String name;
    private int value;

    //일단 쿠폰 유효기간은 설정하지 않습니다.
    private String startDate;
    private String endDate;

    public Cupon(){}
    public Cupon(String CODES, String NAME, int VALUE){
        this.code = CODES;
        this.name = NAME;
        this.value = VALUE;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

}
