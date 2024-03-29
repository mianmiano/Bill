package com.swufe.bill.bean;

import com.swufe.bill.DateUtil;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobDate;

public class Bill extends BmobObject implements Serializable {
    private BmobUser userId;
    private Integer type;
    private String category;
    private String remark;
    private Number amount;
    private String date;
    private String year2month;
    private String objectedId;
    private BmobDate crDate;
    private String uuid;

    public static int RECORD_TYPE_EXPENSE = 1;
    public static int RECORD_TYPE_INCOME = 2;

    public Bill(){
        date = DateUtil.getFormattedDate();
        uuid = UUID.randomUUID().toString();
    }

    public Bill(Integer type,String category,String remark,Number amount){
        this.type = type;
        this.category = category;
        this.remark = remark;
        this.amount = amount;
        date = DateUtil.getFormattedDate();
        uuid = UUID.randomUUID().toString();
    }

    public Bill(Integer type, String category, String remark, Number amount, String date,String uuid,String objectedId){
        this.type = type;
        this.category = category;
        this.remark = remark;
        this.amount = amount;
        this.date = date;
        this.uuid = uuid;
        this.objectedId = objectedId;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getObjectedId() {
        return objectedId;
    }

    public void setObjectedId(String objectedId) {
        this.objectedId = objectedId;
    }

    public Number getAmount() {
        return amount;
    }

    public void setAmount(Number amount) {
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public BmobDate getCrDate() {
        return crDate;
    }

    public void setCrDate(BmobDate crDate) {
        this.crDate = crDate;
    }

    public String getYear2month() {
        return year2month;
    }

//    public void setYear2month(String year2month){
//        this.year2month = year2month;
//    }
    public void setYear2month(String date) throws ParseException {
        DateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
        Date d = format1.parse(date);

        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy");
        SimpleDateFormat sdf2 = new SimpleDateFormat("MM");
        String year = sdf1.format(d);
        String month = sdf2.format(d);
        this.year2month = year+"-"+month;
    }
}
