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

package io.devcon5.pageobjects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openqa.selenium.WebDriver;

/**
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class SeleniumControlBuilderTest {

    @Mock
    private WebDriver webDriver;

    @Test
    public void testBuilder() throws Exception {
        //prepare

        //act
        SeleniumControl.SeleniumContextBuilder builder = SeleniumControl.builder();

        //assert
        assertNotNull(builder);
    }

    @Test
    public void testBuild() throws Throwable {
        //prepare

        //act
        SeleniumControl ctx = SeleniumControl.builder()
                                             .baseUrl("myBaseUrl")
                                             .driver(() -> webDriver)
                                             .build();
        //assert
        ctx.before();
        try {
            assertNotNull(ctx);
            assertEquals("myBaseUrl", ctx.getBaseUrl());
            assertEquals(webDriver, ctx.getDriver().get());
        } finally {
            ctx.after();
        }

    }
}
