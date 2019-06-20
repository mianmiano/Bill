package com.swufe.bill.bean;

import java.util.UUID;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.BmobUser;

public class Conversation extends BmobObject {
    private BmobUser userId;
    private Integer type;
    private String content;
    private Bill bill;
    private String uuid;
    private String billUuid;

    public static int SEND_TEXT = 1;
    public static int RECEIVE_TEXT = 2;

    public Conversation(){
        uuid = UUID.randomUUID().toString();
    }

    public Conversation(Integer type,String content){
        this.type = type;
        this.content = content;
        uuid = UUID.randomUUID().toString();
    }

    public String getBillUuid() {
        return billUuid;
    }

    public void setBillUuid(String billUuid) {
        this.billUuid = billUuid;
    }

    public BmobUser getUserId() {
        return userId;
    }

    public void setUserId(BmobUser userId) {
        this.userId = userId;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Bill getBill() {
        return bill;
    }

    public void setBill(Bill bill) {
        this.bill = bill;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
