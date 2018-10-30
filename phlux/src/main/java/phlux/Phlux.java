package phlux;

import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static phlux.Util.with;
import static phlux.Util.without;

/**
 * This singleton should be a single place where mutable state of the entire application is stored.
 * Don't use the class directly - {@link phlux.Scope} is a more convenient and type-safe way.
 */
final class Phlux {

    private static final int STM_MAX_TRY = 99;
    private final AtomicReference<Map<String, Scope>> root = new AtomicReference<>(Collections.emptyMap());
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    static Phlux getInstance() {
        return InstanceHolder.instance;
    }

    @Override
    public String toString() {
        return "Phlux{" +
                "root=" + root +
                '}';
    }

    void create(String key, ViewState initialState) {
        ScopeTransactionResult result = swapScope(key, scope -> new Scope(initialState));
        if (result.now != null) {
            for (Map.Entry<Integer, Background> entry : result.now.background.entrySet()) {
                //noinspection unchecked
                execute(key, entry.getKey(), entry.getValue());
            }
        }
    }

    void restore(String key, Parcelable scope) {
        ScopeTransactionResult result = swapScope(key, scope1 -> scope1 == null ?
                (Scope) scope : scope1);
        if (result.prev == null && result.now != null) {
            for (Map.Entry<Integer, Background> entry : result.now.background.entrySet()) {
                //noinspection unchecked
                execute(key, entry.getKey(), entry.getValue());
            }
        }
    }

    Parcelable get(String key) {
        return root.get().get(key);
    }

    ViewState state(String key) {
        Scope scope = root.get().get(key);
        return scope == null ? null : scope.state;
    }

    void remove(String key) {
        ScopeTransactionResult result = swapScope(key, scope -> null);
        if (result.prev != null) {
            for (Cancellable cancellable : result.prev.cancellable.values()) {
                cancellable.cancel();
            }
        }
    }

    @Nullable
    <S extends ViewState> ApplyResult<S> apply(String key, Function<S> function) {
        ScopeTransactionResult result = swapScope(key, scope -> {
            //noinspection unchecked
            return scope == null ? null : scope.withState(function.call((S) scope.state));
        });
        if (result.now != null) {
            callback(key, result.now);
        }
        //noinspection unchecked,ConstantConditions
        return result.prev == null
                ? null
                : new ApplyResult(result.prev.state, result.now.state);
    }

    void background(String key, int id, Background task) {
        ScopeTransactionResult result = swapScope(key, scope -> scope == null ? null : scope
                .withBackground(with(scope.background, id, task))
                .withCancellable(without(scope.cancellable, id)));
        if (result.prev != null && result.prev.cancellable.containsKey(id)) {
            result.prev.cancellable.get(id).cancel();
        }

        @SuppressWarnings("unchecked")
        Cancellable cancellable = execute(key, id, task);

        ScopeTransactionResult result2 = swapScope(key, scope -> scope == null ? null :
                scope.background.get(id) == task ?
                        scope.withCancellable(with(scope.cancellable, id, cancellable)) : scope);
        if (result2.now == result2.prev) {
            cancellable.cancel();
        }
    }

    void drop(String key, int id) {
        ScopeTransactionResult result = swapScope(key, scope -> scope == null ? null : scope
                .withBackground(without(scope.background, id))
                .withCancellable(without(scope.cancellable, id)));
        if (result.prev != null && result.prev.cancellable.containsKey(id)) {
            result.prev.cancellable.get(id).cancel();
        }
    }

    void register(String key, StateCallback callback) {
        swapScope(key, scope -> scope == null ? null : scope.withCallbacks(with(scope.callbacks, callback)));
    }

    void unregister(String key, StateCallback callback) {
        swapScope(key, scope -> scope == null ? null : scope.withCallbacks(without(scope.callbacks, callback)));
    }

    @SuppressWarnings("unchecked")
    private void callback(String key, Scope scope) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            for (StateCallback callback : scope.callbacks) {
                callback.call(scope.state);
            }
        } else {
            mainHandler.post(() -> {
                Scope current = root.get().get(key);
                if (current != null && current.state == scope.state) {
                    for (StateCallback callback : current.callbacks) {
                        callback.call(scope.state);
                    }
                }
            });
        }
    }

    private ScopeTransactionResult swapScope(String key, ScopeTransaction transaction) {
        return new ScopeTransactionResult(key, swap(root -> {
            Scope scope = transaction.transact(root.get(key));
            return scope == null ? without(root, key) : with(root, key, scope);
        }));
    }

    private ApplyResult<Map<String, Scope>> swap(Transaction transaction) {
        int counter = 0;
        ApplyResult<Map<String, Scope>> newValue;
        while ((newValue = tryTransaction(root, transaction)) == null) {
            if (++counter > STM_MAX_TRY) {
                throw new IllegalStateException("Are you doing time consuming operations during Phlux apply()?");
            }
        }
        return newValue;
    }

    private ApplyResult<Map<String, Scope>> tryTransaction(AtomicReference<Map<String, Scope>> ref,
            Transaction transaction) {
        Map<String, Scope> original = ref.get();
        Map<String, Scope> newValue = transaction.transact(original);
        return ref.compareAndSet(original, newValue) ? new ApplyResult<>(original, newValue) : null;
    }

    private <S extends ViewState> Cancellable execute(String key, int id, Background<S> entry) {
        return entry.execute(new BackgroundCallback<S>() {
            @Override
            public void apply(Function<S> function) {
                Phlux.this.apply(key, function);
            }

            @Override
            public void dismiss() {
                swapScope(key, scope -> scope == null ? null : scope
                        .withBackground(without(scope.background, id))
                        .withCancellable(without(scope.cancellable, id)));
            }
        });
    }

    private interface Transaction {
        Map<String, Scope> transact(Map<String, Scope> root);
    }

    private interface ScopeTransaction {
        Scope transact(Scope scope);
    }

    private static class InstanceHolder {
        static Phlux instance = new Phlux();
    }

    private static class ScopeTransactionResult extends ApplyResult<Scope> {
        private ScopeTransactionResult(String key, ApplyResult<Map<String, Scope>> result) {
            //noinspection ConstantConditions
            super(result.prev.get(key), result.now.get(key));
        }
    }

    private final static class Scope implements Parcelable {

        public static final Creator<Scope> CREATOR = new Creator<Scope>() {
            @Override
            public Scope createFromParcel(Parcel in) {
                return new Scope(in);
            }

            @Override
            public Scope[] newArray(int size) {
                return new Scope[size];
            }
        };
        final ViewState state;
        final List<StateCallback> callbacks;
        final Map<Integer, Background> background;
        final Map<Integer, Cancellable> cancellable;

        Scope(ViewState state, List<StateCallback> callbacks,
                Map<Integer, Background> background,
                Map<Integer, Cancellable> cancellable) {
            this.state = state;
            this.callbacks = callbacks;
            this.background = background;
            this.cancellable = cancellable;
        }

        Scope(ViewState state) {
            this.state = state;
            this.callbacks = Collections.emptyList();
            this.background = Collections.emptyMap();
            this.cancellable = Collections.emptyMap();
        }

        @SuppressWarnings("WeakerAccess")
        protected Scope(Parcel in) {
            ClassLoader viewStateClassLoader = ViewState.class.getClassLoader();
            this.state = in.readParcelable(viewStateClassLoader);
            this.callbacks = Collections.emptyList();
            this.background = Collections.emptyMap();
            this.cancellable = Collections.emptyMap();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(state, flags);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public String toString() {
            return "Scope{" +
                    "state=" + state +
                    ", callbacks=" + callbacks +
                    ", background=" + background +
                    ", cancellable=" + cancellable +
                    '}';
        }

        Scope withState(ViewState state) {
            return new Scope(state, callbacks, background, cancellable);
        }

        Scope withCallbacks(List<StateCallback> callbacks) {
            return new Scope(state, callbacks, background, cancellable);
        }

        Scope withBackground(Map<Integer, Background> background) {
            return new Scope(state, callbacks, background, cancellable);
        }

        Scope withCancellable(Map<Integer, Cancellable> cancellable) {
            return new Scope(state, callbacks, background, cancellable);
        }
    }
}
