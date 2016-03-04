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

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;

/**
 *
 */
public class TimeMeasureTest {

    /**
     * The class under test
     */
    @InjectMocks
    private TimeMeasure subject;
    private Duration duration;
    private Instant timestamp;

    @Before
    public void setUp() throws Exception {
        this.timestamp = Instant.now();
        this.duration = Duration.ofMillis(1500);
        subject = new TimeMeasure(timestamp, duration);
    }

    @Test
    public void testGetStart() throws Exception {
        assertEquals(timestamp, subject.getStart());
    }

    @Test
    public void testGetDuration() throws Exception {
        assertEquals(duration, subject.getDuration());
    }

    @Test
    public void testIsFinished() throws Exception {
        assertTrue(subject.isFinished());
    }

    @Test
    public void testIsFinished_false() {
        assertFalse(new TimeMeasure(timestamp).isFinished());
    }
}
