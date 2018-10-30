package phlux;

import android.support.annotation.Nullable;

class ApplyResult<T> {

    @Nullable
    final T prev;
    @Nullable
    final T now;

    ApplyResult(@Nullable T prev, @Nullable T now) {
        this.prev = prev;
        this.now = now;
    }
}
