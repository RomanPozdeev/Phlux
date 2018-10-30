package phlux;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * PhluxViewAdapter incorporates common view logic.
 * This should be a single place where view state is stored.
 */
public final class PhluxViewAdapter<S extends ViewState> {

    private final PhluxView<S> view;
    private final StateCallback<S> callback = new StateCallback<S>() {
        @Override
        public void call(S state) {
            view.update(state);
        }
    };
    private final Map<String, Object> updated = new HashMap<>();

    private boolean started;
    private Scope<S> scope;

    public PhluxViewAdapter(@NonNull PhluxView<S> view) {
        this.view = view;
    }

    public void startObserveState() {
        if (!started) {
            view.update(scope().state());
            started = true;
        }
    }

    public void stopObserveState() {
        scope.unregister(callback);
        started = false;
    }

    public void onRestore(@NonNull Bundle bundle) {
        if (scope != null) {
            throw new IllegalStateException("onRestore() must be called before scope() and before onStart()");
        }

        scope = new Scope<>(bundle);
        scope.register(callback);
    }

    public Scope<S> scope() {
        if (scope == null) {
            scope = new Scope<>(view.initial());
            scope.register(callback);
            view.onScopeCreated(scope);
        }
        return scope;
    }

    public <T> void part(@NonNull String name, @Nullable T newValue, @NonNull PhluxView.FieldUpdater<T> updater) {
        if (!updated.containsKey(name) || updated.get(name) != newValue) {
            updater.call(newValue);
            updated.put(name, newValue);
        }
    }

    public void resetParts() {
        updated.clear();
    }
}
