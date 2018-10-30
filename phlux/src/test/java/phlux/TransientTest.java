package phlux;

import android.os.Parcel;

import org.junit.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

public class TransientTest {

    @Test
    public void emptyShouldReturnConstant() {
        Transient<String> emptyString = Transient.empty();
        Transient<Integer> emptyInteger = Transient.empty();
        assertThat(emptyInteger).isSameAs(emptyString);
    }

    @Test
    public void ofNullShouldReturnEmpty() {
        assertThat(Transient.of(null)).isSameAs(Transient.empty());
    }

    @Test
    public void ofNonNullShouldReturnSameNonNull() {
        assertThat(Transient.of("test").get()).isEqualTo("test");
    }

    @Test
    public void ofShouldNotBeRestored() {
        assertThat(Transient.of("test").isRestored()).isFalse();
    }

    @Test
    public void creatorShouldReturnRestored() {
        Transient fromParcel = Transient.CREATOR.createFromParcel(null);
        assertThat(fromParcel.isRestored()).isTrue();
    }

    @Test
    public void creatorShouldReturnRestoredNullValue() {
        Transient fromParcel = Transient.CREATOR.createFromParcel(null);
        assertThat(fromParcel.get()).isNull();
    }

    @Test
    public void creatorCanReturnArrayOfTransient() {
        assertThat(Transient.CREATOR.newArray(10)).hasSize(10);
        assertThat(Transient.CREATOR.newArray(20)).hasSize(20);
    }

    @Test
    public void writeToParcel() {
        Parcel mock = Mockito.mock(Parcel.class);
        Transient.of("test").writeToParcel(mock, 0);
        Mockito.verifyZeroInteractions(mock);
    }

    @Test
    public void describeContentsShouldReturnZero() {
        assertThat(Transient.of("test").describeContents()).isEqualTo(0);
        assertThat(Transient.of(123).describeContents()).isEqualTo(0);
    }

    @Test
    public void equalsShouldWorkForEqualObject() {
        Transient<String> test = Transient.of("test");
        Transient<String> test2 = Transient.of("test");
        assertThat(test).isEqualTo(test);
        assertThat(test).isEqualTo(test2);
        assertThat(test2).isEqualTo(test);
    }

    @Test
    public void equalsShouldFailForRestoredAndNotRestoredObject() {
        Transient test = Transient.CREATOR.createFromParcel(null);
        Transient<String> test2 = Transient.of("test");
        assertThat(test).isNotEqualTo(test2);
        assertThat(test2).isNotEqualTo(test);
    }

    @Test
    public void equalsShouldFailForDifferentClassect() {
        Transient<String> test2 = Transient.of("test");
        assertThat(test2).isNotEqualTo("test");
        assertThat(test2).isNotEqualTo(null);
    }

    @Test
    public void hashCodeContracts() {
        Transient<String> test = Transient.of("test");
        Transient<String> test2 = Transient.of("test2");
        assertThat(test.hashCode()).isEqualTo(test.hashCode());
        assertThat(test.hashCode()).isNotEqualTo(test2.hashCode());
    }

    @Test
    public void toStringTest() {
        Transient<String> test = Transient.of("test");
        assertThat(test.toString()).isEqualTo("Transient{restored=false, value=test}");
        Transient fromParcel = Transient.CREATOR.createFromParcel(null);
        assertThat(fromParcel.toString()).isEqualTo("Transient{restored=true, value=null}");
    }
}