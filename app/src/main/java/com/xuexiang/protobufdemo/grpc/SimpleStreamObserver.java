package com.xuexiang.protobufdemo.grpc;

import android.support.annotation.MainThread;

import com.xuexiang.xutil.tip.ToastUtils;

import io.grpc.stub.StreamObserver;

/**
 * 简单的StreamObserver[回到主线程]
 *
 * @author XUE
 * @since 2019/3/19 13:34
 */
public abstract class SimpleStreamObserver<T> implements StreamObserver<T> {

    @Override
    public void onCompleted() {

    }

    @MainThread
    @Override
    public void onNext(T value) {
        //增加注解，回到主线程
        onSuccess(value);
    }

    @Override
    public void onError(Throwable t) {
        t.printStackTrace();
        ToastUtils.toast(t.getMessage());
    }


    /**
     * 请求成功的回调
     *
     * @param value
     */
    protected abstract void onSuccess(T value);


}
