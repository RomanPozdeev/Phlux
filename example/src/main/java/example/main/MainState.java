package example.main;

import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;

import base.ServerAPI;
import phlux.Transient;
import phlux.ViewState;

@AutoValue
public abstract class MainState implements ViewState {

    static final String NAME_1 = "Chuck Norris";
    static final String NAME_2 = "Jackie Chan";
    static final String DEFAULT_NAME = NAME_1;

    public static MainState initial() {
        return builder()
                .name(DEFAULT_NAME)
                .items(Transient.empty())
                .loading(false)
                .error(null)
                .build();
    }

    public static Builder builder() {
        return new $AutoValue_MainState.Builder();
    }

    public abstract boolean loading();

    public abstract String name();

    public abstract Transient<ServerAPI.Item[]> items();

    @Nullable
    public abstract String error();

    public abstract Builder toBuilder();

    @AutoValue.Builder
    public abstract static class Builder {
        public abstract Builder name(String name);

        public abstract Builder items(Transient<ServerAPI.Item[]> items);

        public abstract Builder error(String error);

        public abstract Builder loading(boolean loading);

        public abstract MainState build();
    }
}
