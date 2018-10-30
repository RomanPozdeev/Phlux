package phlux;

import android.os.Bundle;

import java.util.UUID;

/**
 * Represents an easy and type-safe access to {@link Phlux}.
 * <p>
 * {@link Scope} is an interface to access internal state of a View,
 * including its background tasks and callbacks.
 */
public final class Scope<S extends ViewState> {

    private final String key;
    private final Phlux phlux = Phlux.getInstance();

    /**
     * Constructs a new scope from a given initial state.
     */
    @SuppressWarnings("WeakerAccess")
    public Scope(S state) {
        this.key = UUID.randomUUID().toString();
        phlux.create(key, state);
    }

    /**
     * Restores a scope from a given {@link Bundle}.
     */
    @SuppressWarnings("WeakerAccess")
    public Scope(Bundle bundle) {
        this.key = bundle.getString("key");
        phlux.restore(key, bundle.getParcelable("scope"));
    }

    /**
     * Returns the current scope's state.
     */
    @SuppressWarnings("unchecked")
    public S state() {
        return (S) Phlux.getInstance().state(key);
    }

    /**
     * Saves the scope into {@link Bundle}.
     */
    public Bundle save() {
        Bundle bundle = new Bundle();
        bundle.putString("key", key);
        bundle.putParcelable("scope", Phlux.getInstance().get(key));
        return bundle;
    }

    /**
     * Applies a function to the scope's state.
     */
    public ApplyResult<S> apply(Function<S> function) {
        return phlux.apply(key, function);
    }

    /**
     * Executes a background background.
     */
    public void background(int id, Background<S> background) {
        phlux.background(key, id, background);
    }

    /**
     * Drops a background task.
     * The task will be executed without interruption as usual, but it's application function will not be called.
     */
    public void drop(int id) {
        phlux.drop(key, id);
    }

    /**
     * Finally dispose the scope. Do this on Activity.onDestroy when isFinishing() is true.
     * If the scope is controlled by a Fragment then you need to manually control it's existence.
     */
    public void remove() {
        phlux.remove(key);
    }

    /**
     * Registers a callback for state updates.
     * Once registered the callback will be fired immediately.
     */
    void register(StateCallback<S> callback) {
        phlux.register(key, callback);
    }

    /**
     * Unregisters a given state callback.
     */
    void unregister(StateCallback<S> callback) {
        phlux.unregister(key, callback);
    }
}
