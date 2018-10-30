package phlux;

public interface Cancellable {

    Cancellable NOOP = () -> {
    };

    void cancel();
}
