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
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.After;
import org.junit.Test;

/**
 *
 */
public class ResponseTimesTest {

    @After
    public void tearDown() throws Exception {
        ResponseTimes.clear();
        ResponseTimes.resetResponseTimeHandlers();
    }

    @Test
    public void testClear() throws Exception {
        //prepare
        ResponseTimes.collect(new ResponseTime("test", Instant.now(), Duration.ofMillis(100)));
        assumeFalse(ResponseTimes.getResponseTimes().isEmpty());

        //act
        ResponseTimes.clear();

        //assert
        assertTrue(ResponseTimes.getResponseTimes().isEmpty());

    }

    @Test
    public void testResetResponseTimeHandlers() throws Exception {
        //prepare
        ResponseTimes.onMeasureStart(responseTime -> {});
        ResponseTimes.onMeasureEnd(responseTime -> {});
        ResponseTimes.collect(new ResponseTime("test", Instant.now(), Duration.ofMillis(100)));
        assumeTrue(ResponseTimes.getResponseTimes().isEmpty());

        //act
        ResponseTimes.resetResponseTimeHandlers();

        //assert
        ResponseTimes.collect(new ResponseTime("test", Instant.now(), Duration.ofMillis(100)));
        assertFalse(ResponseTimes.getResponseTimes().isEmpty());
    }

    @Test
    public void testOnMeasureStart() throws Exception {
        //prepare
        AtomicReference<ResponseTime> rtRef = new AtomicReference<>();

        //act
        ResponseTimes.onMeasureStart(rtRef::set);

        //assert
        ResponseTimes.startTx("test", Instant.now());
        assertNotNull(rtRef.get());

    }

    @Test
    public void testOnMeasureEnd() throws Exception {
        //prepare
        AtomicReference<ResponseTime> rtRef = new AtomicReference<>();

        //act
        ResponseTimes.onMeasureEnd(rtRef::set);

        //assert
        ResponseTimes.stopTx(ResponseTimes.startTx("test", Instant.now()));
        assertNotNull(rtRef.get());
    }

    @Test
    public void testStartTx_Instant() throws Exception {
        //prepare
        Instant now = Instant.now();

        //act
        ResponseTime rt = ResponseTimes.startTx("test", now);

        //assert
        assertNotNull(rt);
        assertEquals("test", rt.getTransaction());
        assertEquals(now, rt.getStart());
    }

    @Test
    public void testStartTx() throws Exception {
        //prepare

        //act
        ResponseTime rt = ResponseTimes.startTx("test");

        //assert
        assertNotNull(rt);
        assertEquals("test", rt.getTransaction());
    }

    @Test
    public void testStopTx_finishedResponseTime() throws Exception {
        //prepare
        ResponseTime rt = new ResponseTime("test", Instant.now(), Duration.ofMillis(100));

        //act
        ResponseTime actual = ResponseTimes.stopTx(rt);

        //assert
        assertNotNull(actual);
        assertEquals(rt, actual);
    }

    @Test
    public void testStopTx_unfinishedResponseTime() throws Exception {
        //prepare
        ResponseTime rt = new ResponseTime("test", Instant.now());
        assumeFalse(rt.isFinished());

        //act
        ResponseTime actual = ResponseTimes.stopTx(rt);

        //assert
        assertNotNull(actual);
        assertEquals(rt, actual);
        assertTrue(actual.isFinished());
    }

    @Test(expected = AssertionError.class)
    public void testCollect_unfinished_exception() throws Exception {
        //prepare
        ResponseTime rt = new ResponseTime("test", Instant.now());

        //act
        ResponseTimes.collect(rt);

    }

    @Test
    public void testCollect_finished() throws Exception {
        //prepare
        ResponseTime rt = new ResponseTime("test", Instant.now(), Duration.ofMillis(100));

        //act
        ResponseTime actual = ResponseTimes.collect(rt);

        //assert
        assertNotNull(actual);
        assertEquals(rt, actual);
        assertFalse(ResponseTimes.getResponseTimes().isEmpty());
    }

    @Test
    public void testCollect_txNameAndMeasure() throws Exception {
        //prepare
        Instant now = Instant.now();
        Duration dur = Duration.ofMillis(100);
        TimeMeasure tm = new TimeMeasure(now, dur);

        //act
        ResponseTime rt = ResponseTimes.collect("test", tm);

        //assert
        assertNotNull(rt);
        assertEquals(now,rt.getStart());
        assertEquals(dur,rt.getDuration());
        assertFalse(ResponseTimes.getResponseTimes().isEmpty());
    }

    @Test(expected = AssertionError.class)
    public void testCollect_txNameAndMeasure_unfinished_exception() throws Exception {
        //prepare
        TimeMeasure tm = new TimeMeasure(Instant.now());

        //act
        ResponseTimes.collect("test", tm);
    }

    @Test
    public void testGetResponseTimes() throws Exception {
        //prepare
        ResponseTimes.collect("tx1", new TimeMeasure(Instant.now(), Duration.ZERO));
        ResponseTimes.collect("tx1", new TimeMeasure(Instant.now(), Duration.ZERO));
        ResponseTimes.collect("tx2", new TimeMeasure(Instant.now(), Duration.ZERO));

        //act
        Map<String, List<ResponseTime>> rts = ResponseTimes.getResponseTimes();

        //assert
        assertNotNull(rts);
        assertEquals(2, rts.size());
        assertTrue(rts.containsKey("tx1"));
        assertTrue(rts.containsKey("tx2"));
        assertEquals(2, rts.get("tx1").size());
        assertEquals(1, rts.get("tx2").size());
    }
}
