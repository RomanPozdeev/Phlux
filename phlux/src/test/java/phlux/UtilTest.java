package phlux;

import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Assertions.entry;

public class UtilTest {

    @Test
    public void withShouldAddEntryIntoMap() {
        Map<String, String> testMap = Collections.singletonMap("TestKey1", "TestValue1");
        Map<String, String> withResult = Util.with(testMap, "TestKey2", "TestValue2");
        assertThat(withResult).containsOnly(entry("TestKey1", "TestValue1"), entry("TestKey2", "TestValue2"));
    }

    @Test
    public void withShouldReturnImmutableMap() {
        Map<String, String> withResult = Util.with(Collections.emptyMap(), "TestKey2", "TestValue2");
        Throwable thrown = catchThrowable(() ->
                withResult.put("TestKey2", "TestValue2"));
        assertThat(thrown).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    public void withShouldAddItemIntoList() {
        List<String> testList = Collections.singletonList("TestKey1");
        List<String> withResult = Util.with(testList, "TestKey2");
        assertThat(withResult).containsOnly("TestKey1", "TestKey2");
    }

    @Test
    public void withShouldReturnImmutableList() {
        List<String> withResult = Util.with(Collections.emptyList(), "TestKey2");
        Throwable thrown = catchThrowable(() ->
                withResult.add("TestKey2"));
        assertThat(thrown).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    public void withoutShouldRemoveEntryIntoMap() {
        Map<String, String> testMap = Collections.singletonMap("TestKey1", "TestValue1");
        Map<String, String> withoutResult = Util.without(testMap, "TestKey1");
        assertThat(withoutResult).doesNotContainKey("TestKey1");
    }

    @Test
    public void withoutShouldReturnImmutableMap() {
        Map<String, String> withoutResult = Util.without(Collections.emptyMap(), "TestKey2");
        Throwable thrown = catchThrowable(() ->
                withoutResult.put("TestKey2", "TestValue2"));
        assertThat(thrown).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    public void withoutShouldAddItemIntoList() {
        List<String> testList = Collections.singletonList("TestKey1");
        List<String> withoutResult = Util.without(testList, "TestKey1");
        assertThat(withoutResult).doesNotContain("TestKey1");
    }

    @Test
    public void withoutShouldReturnImmutableList() {
        List<String> withoutResult = Util.with(Collections.emptyList(), "TestKey2");
        Throwable thrown = catchThrowable(() ->
                withoutResult.add("TestKey2"));
        assertThat(thrown).isInstanceOf(UnsupportedOperationException.class);
    }
}