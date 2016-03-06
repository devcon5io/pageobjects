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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ResponseTimeRecordingTest {

    /**
     * The class under test
     */
    @InjectMocks
    private ResponseTimeRecording subject;

    @After
    public void tearDown() throws Exception {
        ResponseTimes.clear();
    }

    @Test
    public void testClearGlobalTable_default() throws Exception {
        //prepare
        ResponseTimes.collect(new ResponseTime("test", Instant.now(), Duration.ofMillis(100)));

        //act
        subject.after();

        //assert
        assertTrue(ResponseTimes.getResponseTimes().isEmpty());
    }

    @Test
    public void testClearGlobalTable_false() throws Exception {
        //prepare
        ResponseTimes.collect(new ResponseTime("test", Instant.now(), Duration.ofMillis(100)));

        //act
        subject.clearGlobalTable(false);
        subject.after();

        //assert
        assertFalse(ResponseTimes.getResponseTimes().isEmpty());
    }

    @Test
    public void testBeforeAfterClass() throws Throwable {
        try {
            subject.beforeClass();
            Optional<ResponseTimeCollector> rtc = ResponseTimeCollector.current();
            assertTrue(rtc.isPresent());

        } finally {
            subject.afterClass();
            Optional<ResponseTimeCollector> rtc = ResponseTimeCollector.current();
            assertFalse(rtc.isPresent());
        }
    }

    @Test
    public void testBeforeAfter() throws Throwable {
        try {
            subject.before();
            Optional<ResponseTimeCollector> rtc = ResponseTimeCollector.current();
            assertTrue(rtc.isPresent());

        } finally {
            subject.after();
            Optional<ResponseTimeCollector> rtc = ResponseTimeCollector.current();
            assertFalse(rtc.isPresent());
        }
    }

}
