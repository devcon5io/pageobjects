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
import java.time.Instant;
import java.util.Optional;

import org.junit.Test;

/**
 *
 */
public class MeasuredExecutionResultTest {

    private Instant timestamp = Instant.now();
    private Duration duration = Duration.ofMillis(1500);

    @Test
    public void testGetReturnValue() throws Exception {
        //prepare
        Object returnValue = new Object();
        MeasuredExecutionResult<Object> result = new MeasuredExecutionResult<Object>(timestamp, duration, returnValue);

        //act
        Optional<Object> actual = result.getReturnValue();
        Optional<Throwable> exception = result.getException();

        //assert
        assertEquals(returnValue, actual.get());
        assertFalse(exception.isPresent());
        assertTrue(result.wasSuccessful());
    }

    @Test
    public void testGetException() throws Exception {
        Throwable t = new Throwable();
        MeasuredExecutionResult<Object> result = new MeasuredExecutionResult<Object>(timestamp, duration, t);

        //act
        Optional<Object> actual = result.getReturnValue();
        Optional<Throwable> exception = result.getException();

        //assert
        assertFalse(actual.isPresent());
        assertEquals(t, exception.get());
        assertFalse(result.wasSuccessful());
    }

}
