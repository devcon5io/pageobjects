/*
 * Copyright 2015-2016 DevCon5 GmbH, info@devcon5.ch
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.devcon5.pageobjects.measure;

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
