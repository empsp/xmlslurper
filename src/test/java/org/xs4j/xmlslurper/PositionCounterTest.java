package org.xs4j.xmlslurper;

import org.junit.After;
import org.junit.Test;
import org.xs4j.xmlslurper.PositionCounter;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by mturski on 12/22/2016.
 */
public class PositionCounterTest {
    private final PositionCounter counter = new PositionCounter();

    @Test
    public void givenPositiveDepthCounterStartsFromOne() throws Exception {
        assertPosition(1, 1L);
        assertPosition(2, 1L);
        assertPosition(3, 1L);
        assertPosition(4, 1L);
        assertPosition(5, 1L);
    }

    @Test
    public void givenPositiveAndSameDepthCounterCountsThatDepth() throws Exception {
        assertPosition(1, 1L);
        assertPosition(2, 1L);
        assertPosition(2, 2L);
        assertPosition(2, 3L);
    }

    @Test
    public void givenPositiveAndNegativeDepthsCounterCountsThoseDepths() throws Exception {
        assertPosition(1, 1L);
        assertPosition(2, 1L);
        assertPosition(3, 1L);
        assertPosition(3, 2L);
        assertPosition(2, 2L);
        assertPosition(2, 3L);
    }

    public void assertPosition(int depth, long expectedPosition) {
        assertThat(counter.getNodePosition(depth), is(expectedPosition));
    }

    @After
    public void teardown() {
        counter.reset();
    }
}
