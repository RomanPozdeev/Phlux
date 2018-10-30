package phlux.base;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;

import phlux.PhluxViewAdapter;
import phlux.Scope;
import phlux.ViewState;

/**
 * This is an *example* of how to adapt Phlux to Views.
 */
public abstract class PhluxCustomView<S extends ViewState> extends View implements phlux.PhluxView<S> {

    private static final String PHLUX_SCOPE = "phlux_scope";
    private static final String SUPER = "super";

    private final PhluxViewAdapter<S> adapter = new PhluxViewAdapter<>(this);

    public PhluxCustomView(Context context) {
        super(context);
    }

    public PhluxCustomView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PhluxCustomView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (isInEditMode()) {
            return;
        }

        adapter.startObserveState();
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Bundle bundle = (Bundle) state;
        super.onRestoreInstanceState(bundle.getParcelable(SUPER));
        Bundle phulxBundle = bundle.getBundle(PHLUX_SCOPE);
        if (phulxBundle != null) {
            adapter.onRestore(phulxBundle);
        } else {
            throw new IllegalStateException("saved phlux scope is null");
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(PHLUX_SCOPE, adapter.scope().save());
        bundle.putParcelable(SUPER, super.onSaveInstanceState());
        return bundle;
    }

    @Override
    protected void onDetachedFromWindow() {
        adapter.stopObserveState();
        super.onDetachedFromWindow();
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
