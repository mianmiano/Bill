package com.swufe.bill.presenter.Contract;

import com.swufe.bill.base.BaseContract;
import com.swufe.bill.bean.Bill;
import com.swufe.bill.bean.MonthListBean;

public interface MonthListContract extends BaseContract {

    interface View extends BaseContract.BaseView {

        void loadDataSuccess(MonthListBean list);

    }

    interface Presenter extends BaseContract.BasePresenter<View> {

        void getMonthList(String id, String year, String month);

        void deleteBill(Long id);

        void updateBill(Bill bBill);
    }
}