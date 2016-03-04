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
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

/**
 *
 */
public class ResponseTimeTest {

    private UUID uuid = UUID.randomUUID();
    private String txname = "transaction";
    private Instant timestamp = Instant.now();
    private Duration duration = Duration.ofMillis(150);


    /**
     * The class under test
     */
    private ResponseTime subject;

    @Before
    public void setUp() throws Exception {
        subject = new ResponseTime(txname, timestamp);
    }

    @Test(expected = IllegalStateException.class)
    public void testFinish_alreadyFinished() throws Exception {
        new ResponseTime(uuid, txname, timestamp, duration).finish();
    }

    @Test
    public void testFinish_finished() throws Exception {
        //prepare
        UUID uuid = subject.getUuid();

        //act
        ResponseTime rt = subject.finish();

        //assert
        assertTrue(rt.isFinished());
        assertEquals(uuid, rt.getUuid());
        assertEquals(timestamp, rt.getStart());
    }

    @Test
    public void testFinish_timestamp_finished() throws Exception {
        //prepare
        UUID uuid = subject.getUuid();
        Instant now = timestamp.plus(150, ChronoUnit.MILLIS);

        //act
        ResponseTime rt = subject.finish(now);

        //assert
        assertTrue(rt.isFinished());
        assertEquals(uuid, rt.getUuid());
        assertEquals(timestamp, rt.getStart());
        assertEquals(duration, rt.getDuration());
    }

    @Test
    public void testGetUuid() throws Exception {
        //prepare
        ResponseTime rt = new ResponseTime(uuid, txname, timestamp, duration);

        //act
        UUID actual = rt.getUuid();
        //assert
        assertEquals(uuid, actual);
    }

    @Test
    public void testGetTransaction() throws Exception {
        //prepare

        //act
        String actual = subject.getTransaction();

        //assert
        assertEquals(txname, actual);
    }
}
