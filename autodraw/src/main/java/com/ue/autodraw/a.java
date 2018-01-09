package com.ue.autodraw;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

/**
 * Created by hawk on 2018/1/9.
 */

public class a {
    private void a() {
        Observable
                .create(new ObservableOnSubscribe<Object>() {
                    @Override
                    public void subscribe(ObservableEmitter<Object> e) throws Exception {

                    }
                })
                .subscribe();
    }
}
