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

package io.devcon5.pageobjects.tx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.atomic.AtomicReference;

import io.devcon5.pageobjects.measure.ResponseTime;
import io.devcon5.pageobjects.measure.ResponseTimeCollector;
import io.devcon5.pageobjects.measure.ResponseTimes;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 */
public class TransactionalTest {

    /**
     * The class under test
     */
    private Transactional subject = new Transactional() {
    };

    private ResponseTimeCollector rtc = new ResponseTimeCollector();

    private AtomicReference<ResponseTime> rtStartRef = new AtomicReference<>();
    private AtomicReference<ResponseTime> rtEndRef = new AtomicReference<>();

    @Before
    public void setUp() throws Exception {
        ResponseTimes.onMeasureStart(responseTime -> rtStartRef.set(responseTime));
        ResponseTimes.onMeasureEnd(responseTime -> rtEndRef.set(responseTime));
        rtc.startCollecting();
    }

    @After
    public void tearDown() throws Exception {
        rtc.stopCollecting();
        ResponseTimes.resetResponseTimeHandlers();
        ResponseTimes.clear();
    }

    @Test
    public void testTxBegin() throws Exception {

        //prepare

        //act
        subject.txBegin("myTx");

        //assert
        ResponseTime rt = rtStartRef.get();
        assertNotNull(rt);
        assertFalse(rt.isFinished());
        assertEquals("myTx", rt.getTransaction());
    }

    @Test
    public void testTxEnd() throws Exception {

        //prepare
        subject.txBegin("myTx");

        //act
        subject.txEnd("myTx");

        //assert
        ResponseTime rt = rtEndRef.get();
        assertNotNull(rt);
        assertTrue(rt.isFinished());
        assertEquals("myTx", rt.getTransaction());

    }

    @Test(expected = IllegalStateException.class)
    public void testTxEnd_uninitializedTx_exception() throws Exception {

        //act
        subject.txEnd("myTx");


    }

}
