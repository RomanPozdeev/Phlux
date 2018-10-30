package example.demo;

import com.google.auto.value.AutoValue;

import phlux.ViewState;

@AutoValue
public abstract class DemoState implements ViewState {

    public abstract float progress();

    public static DemoState create(float progress) {
        return new AutoValue_DemoState(progress);
    }

    public static DemoState create() {
        return new AutoValue_DemoState(0);
    }
}
