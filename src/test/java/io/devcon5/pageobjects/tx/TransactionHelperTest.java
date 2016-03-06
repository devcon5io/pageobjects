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

import java.lang.reflect.Method;
import java.util.Optional;
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
public class TransactionHelperTest {

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
    public void testAddTransactionSupport() throws Exception {
        //prepare
        NamedTransaction tx = new NamedTransaction();

        //act
        NamedTransaction etx = TransactionHelper.addTransactionSupport(tx);

        //assert
        assertNotNull(etx);
        //verify if transaction has been recorded
        etx.namedTx();
        ResponseTime rt = rtEndRef.get();
        assertNotNull(rt);
        assertTrue(rt.isFinished());
        assertEquals("CustomName_CustomTx", rt.getTransaction());
    }

    @Test
    public void testAddTransactionSupport_dynamicReturnValueProxying() throws Exception {
        //prepare
        NamedTransaction tx = new NamedTransaction();

        //act
        NamedTransaction etx = TransactionHelper.addTransactionSupport(tx);

        //assert
        assertNotNull(etx);
        //verify if transaction has been recorded, even if an intermediate method returning this is invoked
        etx.someOperation().namedTx();
        ResponseTime rt = rtEndRef.get();
        assertNotNull(rt);
        assertTrue(rt.isFinished());
        assertEquals("CustomName_CustomTx", rt.getTransaction());
    }

    @Test
    public void testGetTxName_unnamedTx() throws Exception {
        //prepare
        UnnamedTransaction tx = new UnnamedTransaction();
        Method m = UnnamedTransaction.class.getMethod("unnamedTx");

        //act
        Optional<String> txName = TransactionHelper.getTxName(tx,m);
        //assert
        assertEquals("UnnamedTransaction_unnamedTx", verifyResult(txName));
    }

    @Test
    public void testGetTxName_namedTx() throws Exception {
        //prepare
        NamedTransaction tx = new NamedTransaction();
        Method m = NamedTransaction.class.getMethod("namedTx");

        //act
        Optional<String> txName = TransactionHelper.getTxName(tx,m);
        //assert
        assertEquals("CustomName_CustomTx", verifyResult(txName));
    }

    @Test
    public void testGetTxNamee_noTx() throws Exception {
        //prepare
        NoTransaction tx = new NoTransaction();
        Method m = NoTransaction.class.getMethod("noTx");

        //act
        Optional<String> txName = TransactionHelper.getTxName(tx,m);
        //assert
        assertNotNull(txName);
        assertFalse(txName.isPresent());
    }

    @Test
    public void testGetClassTxName_unnamedTx_className() throws Exception {
        //prepare

        //act
        Optional<String> txName = TransactionHelper.getClassTxName(UnnamedTransaction.class);
        //assert
        verifyResult(txName);
        assertEquals("UnnamedTransaction", txName.get());
    }

    @Test
    public void testGetClassTxName_namedTx_className() throws Exception {
        //prepare

        //act
        Optional<String> txName = TransactionHelper.getClassTxName(NamedTransaction.class);
        //assert
        verifyResult(txName);
        assertEquals("CustomName", txName.get());
    }

    @Test
    public void testGetClassTxName_noTx() throws Exception {
        //prepare

        //act
        Optional<String> txName = TransactionHelper.getClassTxName(NoTransaction.class);
        //assert
        assertNotNull(txName);
        assertFalse(txName.isPresent());
    }

    private String verifyResult(final Optional<String> txName) {

        assertNotNull(txName);
        assertTrue(txName.isPresent());
        return txName.get();
    }

    //// Test classes
    @Transaction
    public static class UnnamedTransaction implements TransactionSupport {

        @Transaction
        public void unnamedTx(){}



    }

    @Transaction("CustomName")
    public static class NamedTransaction implements TransactionSupport {
        @Transaction("CustomTx")
        public void namedTx(){}
        public void noTx(){}

        public NamedTransaction someOperation(){
            return this;
        }
    }

    public static class NoTransaction {
        public void noTx(){}
    }
}
