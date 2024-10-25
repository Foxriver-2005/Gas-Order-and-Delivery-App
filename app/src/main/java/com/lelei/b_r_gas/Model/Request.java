package com.lelei.b_r_gas.Model;

import java.util.List;

public class Request {

    private String id;
    private String name;
    private String phone;
    private String address;
    private String total;
    private String status;
    private String time;
    private String date;
    private String reason;
    private String paymentMethod;
    private String picked;
    private String pickedBy;
    private String mail;
    private String latLng;
    private List<ItemOrder> itemOrders; //list of sweets

    public Request(){

    }

    public Request(String id, String name, String phone, String address, String total, String status, String time, String date, String reason, String paymentMethod, String picked, String pickedBy, String mail, String latLng, List<ItemOrder> itemOrders) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.total = total;
        this.status = status;
        this.time = time;
        this.date = date;
        this.reason = reason;
        this.paymentMethod = paymentMethod;
        this.picked = picked;
        this.pickedBy = pickedBy;
        this.mail = mail;
        this.latLng = latLng;
        this.itemOrders = itemOrders;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPicked() {
        return picked;
    }

    public void setPicked(String picked) {
        this.picked = picked;
    }

    public String getPickedBy() {
        return pickedBy;
    }

    public void setPickedBy(String pickedBy) {
        this.pickedBy = pickedBy;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getLatLng() {
        return latLng;
    }

    public void setLatLng(String latLng) {
        this.latLng = latLng;
    }

    public List<ItemOrder> getSweetOrders() {
        return itemOrders;
    }

    public void setSweetOrders(List<ItemOrder> itemOrders) {
        this.itemOrders = itemOrders;
    }
}
