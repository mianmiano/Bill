package com.swufe.bill.bean;

import java.util.List;

public class MonthListBean {
    private String t_income;
    private String t_outcome;
    private String t_total;
    private List<DaylistBean> daylist;

    public String getT_income() {
        return t_income;
    }

    public void setT_income(String t_income) {
        this.t_income = t_income;
    }

    public String getT_outcome() {
        return t_outcome;
    }

    public void setT_outcome(String t_outcome) {
        this.t_outcome = t_outcome;
    }

    public String getT_total() {
        return t_total;
    }

    public void setT_total(String t_total) {
        this.t_total = t_total;
    }

    public List<DaylistBean> getDaylist() {
        return daylist;
    }

    public void setDaylist(List<DaylistBean> daylist) {
        this.daylist = daylist;
    }

    public static class DaylistBean {

        private String time;
        private String money;
        private List<Bill> list;

        public DaylistBean(){}

        public DaylistBean(String time, String money, List<Bill> list) {
            this.time = time;
            this.money = money;
            this.list = list;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getMoney() {
            return money;
        }

        public void setMoney(String money) {
            this.money = money;
        }

        public List<Bill> getList() {
            return list;
        }

        public void setList(List<Bill> list) {
            this.list = list;
        }
    }
}
