package example.demo;

import android.os.AsyncTask;

import com.google.auto.value.AutoValue;

import phlux.Background;
import phlux.BackgroundCallback;
import phlux.Cancellable;

@AutoValue
public abstract class DemoTask1 implements Background<DemoState> {

    public static DemoTask1 create() {
        return new AutoValue_DemoTask1();
    }

    @Override
    public Cancellable execute(BackgroundCallback<DemoState> callback) {
        AsyncTask<Void, Float, Void> task = new AsyncTask<Void, Float, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                for (float progress = 0; progress <= 100 && !isCancelled(); progress++) {
                    final float finalProgress = progress;
                    callback.apply(state -> DemoState.create(finalProgress));
                    sleep();
                }
                callback.dismiss();
                return null;
            }
        }.execute();
        return () -> task.cancel(true);
    }

    private static void sleep() {
        try {
            Thread.sleep(100);
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
