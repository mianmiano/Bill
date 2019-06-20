package com.swufe.bill.presenter;

import com.swufe.bill.base.RxPresenter;
import com.swufe.bill.bean.Bill;
import com.swufe.bill.presenter.Contract.MonthListContract;

public class MonthListPresenter extends RxPresenter<MonthListContract.View> implements MonthListContract.Presenter{

    private String TAG="MonthListPresenter";

    @Override
    public void getMonthList(String id, String year, String month) {
//        GlobalUtil.getInstance().getBBillByUserIdWithYM(id, year, month)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new BaseObserver<List<Bill>>() {
//                    @Override
//                    protected void onSuccees(List<Bill> bBills) throws Exception {
//                        mView.loadDataSuccess(BillUtils.packageDetailList(bBills));
//                    }
//
//                    @Override
//                    protected void onFailure(Throwable e, boolean isNetWorkError) throws Exception {
//                        mView.onFailure(e);
//                    }
//                });
    }

    @Override
    public void deleteBill(Long id) {
//        LocalRepository.getInstance().deleteBBillById(id)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new BaseObserver<Long>() {
//                    @Override
//                    protected void onSuccees(Long l) throws Exception {
//                        mView.onSuccess();
//                    }
//
//                    @Override
//                    protected void onFailure(Throwable e, boolean isNetWorkError) throws Exception {
//                        mView.onFailure(e);
//                    }
//                });
    }

    @Override
    public void updateBill(Bill bBill) {
//        LocalRepository.getInstance()
//                .updateBBill(bBill)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new BaseObserver<BBill>() {
//                    @Override
//                    protected void onSuccees(BBill bBill) throws Exception {
//                        mView.onSuccess();
//                    }
//
//                    @Override
//                    protected void onFailure(Throwable e, boolean isNetWorkError) throws Exception {
//                        mView.onFailure(e);
//                    }
//                });
    }
}

