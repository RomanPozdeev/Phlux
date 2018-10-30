package phlux.base;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import phlux.PhluxView;
import phlux.PhluxViewAdapter;
import phlux.Scope;
import phlux.ViewState;

/**
 * This is an *example* of how to adapt Phlux to Activities.
 */
public abstract class PhluxActivity<S extends ViewState> extends AppCompatActivity implements PhluxView<S> {

    private static final String PHLUX_SCOPE = "phlux_scope";

    private final PhluxViewAdapter<S> adapter = new PhluxViewAdapter<>(this);

    @CallSuper
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            Bundle bundle = savedInstanceState.getBundle(PHLUX_SCOPE);
            if (bundle != null) {
                adapter.onRestore(bundle);
            } else {
                throw new IllegalStateException("saved phlux scope is null");
            }
        }
    }

    @CallSuper
    @Override
    protected void onStart() {
        super.onStart();
        adapter.startObserveState();
    }

    @CallSuper
    @Override
    protected void onResume() {
        super.onResume();
        adapter.startObserveState();
    }

    @CallSuper
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(PHLUX_SCOPE, adapter.scope().save());
        adapter.stopObserveState();
        super.onSaveInstanceState(outState);
    }

    @CallSuper
    @Override
    protected void onStop() {
        adapter.stopObserveState();
        super.onStop();
    }

    @CallSuper
    @Override
    protected void onDestroy() {
        if (isFinishing()) {
            adapter.scope().remove();
        }
        super.onDestroy();
    }

    @Override
    public Scope<S> scope() {
        return adapter.scope();
    }

    @Override
    public S state() {
        return adapter.scope().state();
    }

    @Override
    public <T> void part(String name, T newValue, FieldUpdater<T> updater) {
        adapter.part(name, newValue, updater);
    }

    @Override
    public void resetParts() {
        adapter.resetParts();
    }

    @Override
    public void onScopeCreated(Scope<S> scope) {
    }
}
