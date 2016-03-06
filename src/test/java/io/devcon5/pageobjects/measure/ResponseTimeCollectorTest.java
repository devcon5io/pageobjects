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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 */
public class ResponseTimeCollectorTest {

    /**
     * The class under test
     */
    private ResponseTimeCollector subject  = new ResponseTimeCollector();

    private AtomicReference<ResponseTime> rtStartRef = new AtomicReference<>();
    private AtomicReference<ResponseTime> rtEndRef = new AtomicReference<>();

    @Before
    public void setUp() throws Exception {
        ResponseTimes.onMeasureStart(responseTime -> rtStartRef.set(responseTime));
        ResponseTimes.onMeasureEnd(responseTime -> rtEndRef.set(responseTime));
    }

    @After
    public void tearDown() throws Exception {
        ResponseTimeCollector.current().ifPresent(ResponseTimeCollector::stopCollecting);
        ResponseTimes.resetResponseTimeHandlers();
        ResponseTimes.clear();
    }

    @Test
    public void testCurrent() throws Exception {
        Optional<ResponseTimeCollector> result = ResponseTimeCollector.current();
        assertNotNull(result);
        assertFalse(result.isPresent());
    }

    @Test
    public void testStartStopCollecting() throws Exception {
        //prepare

        //act
        subject.startCollecting();
        try {
            Optional<ResponseTimeCollector> result = ResponseTimeCollector.current();
            assertNotNull(result);
            assertTrue(result.isPresent());
            assertEquals(subject, result.get());
        } finally {
            subject.stopCollecting();
        }
        //assert
        Optional<ResponseTimeCollector> result = ResponseTimeCollector.current();
        assertNotNull(result);
        assertFalse(result.isPresent());
    }


    @Test
    public void testCaptureTx_InstantInstant() throws Exception {
        //prepare
        subject.startCollecting();
        Instant now = Instant.now();

        //act
        subject.captureTx("test", now, now.plus(100, ChronoUnit.MILLIS));

        //assert
        ResponseTime rt = rtEndRef.get();
        assertNotNull(rt);
        assertEquals("test", rt.getTransaction());
        assertEquals(now, rt.getStart());
        assertEquals(Duration.ofMillis(100), rt.getDuration());
    }

    @Test
    public void testCaptureTx_InstantDuration() throws Exception {
        //prepare
        subject.startCollecting();
        Instant now = Instant.now();
        Duration dur = Duration.ofMillis(100);

        //act
        subject.captureTx("test", now, dur);

        //assert
        subject.stopCollecting();
        ResponseTime rt = rtEndRef.get();
        assertNotNull(rt);
        assertEquals("test", rt.getTransaction());
        assertEquals(now, rt.getStart());
        assertEquals(Duration.ofMillis(100), rt.getDuration());
    }

    @Test
    public void testStartTx() throws Exception {
        //prepare
        subject.startCollecting();

        //act
        subject.startTx("test");

        //assert
        ResponseTime rt = rtStartRef.get();
        assertNotNull(rt);
        assertEquals("test", rt.getTransaction());
        assertFalse(rt.isFinished());
    }

    @Test
    public void testStopTx() throws Exception {
        //prepare
        subject.startCollecting();
        subject.startTx("test");

        //act
        subject.stopTx("test");

        //assert
        subject.stopCollecting();
        ResponseTime rt = rtEndRef.get();
        assertNotNull(rt);
        assertEquals("test", rt.getTransaction());
    }

    @Test
    public void testStopTx_Instant() throws Exception {
        //prepare
        subject.startCollecting();
        subject.startTx("test");
        Instant end = Instant.now().plus(100, ChronoUnit.MILLIS);

        //act
        subject.stopTx("test", end);

        //assert
        subject.stopCollecting();
        ResponseTime rt = rtEndRef.get();
        assertNotNull(rt);
        assertEquals("test", rt.getTransaction());
        assertTrue(rt.getDuration().compareTo(Duration.ofMillis(95)) > 0);
    }
}
