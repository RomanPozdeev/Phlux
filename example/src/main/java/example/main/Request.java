package example.main;

import android.util.Log;

import com.google.auto.value.AutoValue;

import base.App;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import phlux.Background;
import phlux.BackgroundCallback;
import phlux.Cancellable;
import phlux.Transient;

@AutoValue
public abstract class Request implements Background<MainState> {

    public static Request create(String name) {
        return new AutoValue_Request(name);
    }

    public abstract String name();

    @Override
    public Cancellable execute(BackgroundCallback<MainState> callback) {
        Log.d("MainActivity", "execute");
        String firstName = name().split("\\s+")[0];
        String lastName = name().split("\\s+")[1];
        Disposable subscribe = App.getServerAPI()
                .getItems(firstName, lastName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> callback.apply(state ->
                        state.toBuilder()
                                .loading(true)
                                .build()))
                .doFinally(() -> callback.apply(state ->
                        state.toBuilder()
                                .loading(false)
                                .build()))
                .subscribe(
                        response ->
                                callback.apply(state -> state.toBuilder()
                                        .items(Transient.of(response.items))
                                        .build()),
                        throwable ->
                                callback.apply(state -> state.toBuilder()
                                        .error(throwable.toString())
                                        .build()));

        return subscribe::dispose;
    }
}
