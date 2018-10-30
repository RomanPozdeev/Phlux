package phlux;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

/**
 * An utility class that represents a reference to a value that should not be parceled.
 */
public final class Transient<T> implements Parcelable {

    public static final Creator<Transient> CREATOR = new Creator<Transient>() {
        @Override
        public Transient createFromParcel(Parcel in) {
            return new Transient(in, true);
        }

        @Override
        public Transient[] newArray(int size) {
            return new Transient[size];
        }
    };

    private static final Transient EMPTY = new Transient<>();

    @Nullable
    public final T value;
    private final boolean restored;

    private Transient() {
        restored = false;
        value = null;
    }

    @SuppressWarnings("WeakerAccess")
    protected Transient(Parcel parcel, boolean fromParcel) {
        this.restored = true;
        this.value = null;
    }

    private Transient(@Nullable T value) {
        restored = false;
        this.value = value;
    }

    @SuppressWarnings({"unchecked", "WeakerAccess"})
    public static <T> Transient<T> empty() {
        return (Transient<T>) EMPTY;
    }

    public static <T> Transient<T> of(T value) {
        return value == null ? Transient.empty() : new Transient<>(value);
    }

    @Nullable
    public T get() {
        return value;
    }

    public boolean isRestored() {
        return restored;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Transient<?> that = (Transient<?>) o;

        if (restored != that.restored) {
            return false;
        }
        return value != null ? value.equals(that.value) : that.value == null;
    }

    @Override
    public int hashCode() {
        int result = (restored ? 1 : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Transient{" +
                "restored=" + restored +
                ", value=" + value +
                '}';
    }
}
