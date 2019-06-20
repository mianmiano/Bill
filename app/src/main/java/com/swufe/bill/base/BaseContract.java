package com.swufe.bill.base;

public interface BaseContract {
    interface BasePresenter<T> {

        void attachView(T view);

        void detachView();
    }

    interface BaseView {

        void onSuccess();

        void onFailure(Throwable e);
    }
}
