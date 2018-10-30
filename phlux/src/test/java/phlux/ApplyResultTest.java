package phlux;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ApplyResultTest {
    @Test
    public void applyResultShouldKeepValues() {
        ApplyResult<String> result = new ApplyResult<>("PREV", "NOW");
        assertThat(result.now).isEqualTo("NOW");
        assertThat(result.prev).isEqualTo("PREV");
    }
}