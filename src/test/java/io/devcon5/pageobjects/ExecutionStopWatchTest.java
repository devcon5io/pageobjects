package io.devcon5.pageobjects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import org.junit.Test;

/**
 *
 */
public class ExecutionStopWatchTest {

    @Test
    public void testAround_runnable() throws Exception {


        //act
        ExecutionStopWatch watch = ExecutionStopWatch.measure(() -> sleep(100));

        //assert
        //5 millis tolerance
        assertTrue(watch.getDuration().plusMillis(5).compareTo(Duration.ofMillis(100)) >= 0);
        assertEquals(Void.TYPE, watch.getResult().get());

    }

    @Test
    public void testMeasure_callable_returningNull() throws Exception {


        //act
        ExecutionStopWatch watch = ExecutionStopWatch.measure(() -> {
            sleep(100);
            return null;
        });

        //assert
        assertTrue(watch.getDuration().plusMillis(5).compareTo(Duration.ofMillis(100)) >= 0);
        assertFalse(watch.getResult().isPresent());
    }

    @Test
    public void testMeasure_callable_returningResult() throws Exception {


        //act
        ExecutionStopWatch watch = ExecutionStopWatch.measure(() -> {
            sleep(100);
            return "out";
        });

        //assert
        assertTrue(watch.getDuration().plusMillis(5).compareTo(Duration.ofMillis(100)) >= 0);
        assertEquals("out", watch.getResult().orElse("FAIL"));
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            //omit
        }
    }

}
