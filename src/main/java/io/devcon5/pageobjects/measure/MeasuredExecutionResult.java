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

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * The result of a time measured execution. As this measurement holds a reference to the return value of
 * a measured execution (or the throwable that occured during the execution) you should not keep it in memory
 * for recording execution times as this will fill up memory.
 */
public class MeasuredExecutionResult<RESULTTYPE> extends TimeMeasure {

    private final Optional<RESULTTYPE> result;
    private final Optional<Throwable> throwable;

    public MeasuredExecutionResult(final Instant start, final Duration duration, RESULTTYPE result) {

        super(start, duration);
        this.result = Optional.ofNullable(result);
        this.throwable = Optional.empty();
    }

    public MeasuredExecutionResult(final Instant start, final Duration duration, Throwable throwable) {

        super(start, duration);
        this.result = Optional.empty();
        this.throwable = Optional.of(throwable);
    }

    /**
     * The result of the execution. In case the operation had no result - because it was of return type void -
     * this method returns {@link Void}.
     * @return
     *  the result of the execution.
     */
    public Optional<RESULTTYPE> getReturnValue() {

        return result;
    }

    public Optional<Throwable> getException(){
        return throwable;
    }

    /**
     * Indicator whether the execution was successful.
     * @return
     *  true if the execution did not throw an exception.
     */
    public boolean wasSuccessful(){
        return !throwable.isPresent();
    }
}
