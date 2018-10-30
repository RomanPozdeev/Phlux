package phlux;

import android.os.Bundle;
import android.os.Parcel;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class ScopeTest {
    private final TestState testState = new TestState();

    @Test
    public void state() {
        Scope<TestState> testScope = new Scope<>(testState);
        assertThat(testScope.state()).isEqualTo(testState);

        testScope.background(1, new Background<TestState>() {
            @Override
            public Cancellable execute(BackgroundCallback<TestState> callback) {
                return null;
            }

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {

            }
        });

        Scope<TestState> testScope2 = new Scope<>(testState);
    }

    @Test
    public void save() {
        Scope<TestState> testScope = new Scope<>(testState);
        Bundle bundle = testScope.save();
        assertThat(bundle.containsKey("key")).isTrue();
        assertThat(bundle.containsKey("scope")).isTrue();
        assertThat(bundle.containsKey("values")).isFalse();
    }

    public final class TestState implements ViewState {

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {

        }
    }
}