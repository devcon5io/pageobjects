package io.devcon5.pageobjects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.Duration;

import io.devcon5.pageobjects.measure.ExecutionStopWatch;
import io.devcon5.pageobjects.measure.MeasuredExecutionResult;
import org.junit.Test;

/**
 *
 */
public class ExecutionStopWatchTest {

    @Test
    public void testRunMeasured_runnable() throws Exception {


        //act
        MeasuredExecutionResult watch = ExecutionStopWatch.runMeasured(() -> sleep(100));

        //assert
        //5 millis tolerance
        assertTrue(watch.getDuration().plusMillis(5).compareTo(Duration.ofMillis(100)) >= 0);
        assertEquals(Void.TYPE, watch.getReturnValue().get());

    }

    @Test
    public void testMeasure_callable_returningNull() throws Exception {


        //act
        MeasuredExecutionResult watch = ExecutionStopWatch.runMeasured(() -> {
            sleep(100);
            return null;
        });

        //assert
        assertTrue(watch.getDuration().plusMillis(5).compareTo(Duration.ofMillis(100)) >= 0);
        assertFalse(watch.getReturnValue().isPresent());
    }

    @Test
    public void testMeasure_callable_returningResult() throws Exception {


        //act
        MeasuredExecutionResult<String> watch = ExecutionStopWatch.runMeasured(() -> {
            sleep(100);
            return "out";
        });

        //assert
        assertTrue(watch.getDuration().plusMillis(5).compareTo(Duration.ofMillis(100)) >= 0);
        assertEquals("out", watch.getReturnValue().orElse("FAIL"));
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            //omit
        }
    }

}
