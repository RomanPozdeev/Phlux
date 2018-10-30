package phlux.base;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

import phlux.PhluxView;
import phlux.PhluxViewAdapter;
import phlux.Scope;
import phlux.ViewState;

/**
 * This is an *example* of how to adapt Phlux to Activities.
 */
public abstract class PhluxFragment<S extends ViewState> extends Fragment implements PhluxView<S> {

    private static final String PHLUX_SCOPE = "phlux_scope";
    private final PhluxViewAdapter<S> adapter = new PhluxViewAdapter<>(this);

    private boolean isStateSaved;

    @CallSuper
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter.resetParts();
    }

    @CallSuper
    @Override
    public void onStart() {
        super.onStart();
        adapter.startObserveState();
        isStateSaved = false;
    }

    @CallSuper
    @Override
    public void onResume() {
        super.onResume();
        adapter.startObserveState();
        isStateSaved = false;
    }

    @CallSuper
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        Bundle save = adapter.scope().save();
        outState.putParcelable(PHLUX_SCOPE, save);
        isStateSaved = true;
        adapter.stopObserveState();
        super.onSaveInstanceState(outState);
    }

    @CallSuper
    @Override
    public void onStop() {
        adapter.stopObserveState();
        super.onStop();
    }

    @CallSuper
    @Override
    public void onDestroyView() {
        adapter.stopObserveState();
        super.onDestroyView();
    }

    @CallSuper
    @Override
    public void onDestroy() {
        if (requireActivity().isFinishing()) {
            adapter.scope().remove();
            return;
        }

        // http://stackoverflow.com/questions/34649126/fragment-back-stack-and-isremoving
        if (isStateSaved) {
            isStateSaved = false;
            return;
        }

        boolean anyParentIsRemoving = false;
        Fragment parent = getParentFragment();
        while (!anyParentIsRemoving && parent != null) {
            anyParentIsRemoving = parent.isRemoving();
            parent = parent.getParentFragment();
        }

        if (isRemoving() || anyParentIsRemoving) {
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
    public void onScopeCreated(Scope<S> scope) {
    }

    @Override
    public <T> void part(String name, T newValue, FieldUpdater<T> updater) {
        adapter.part(name, newValue, updater);
    }

    @Override
    public void resetParts() {
        adapter.resetParts();
    }
}
