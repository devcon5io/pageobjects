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
import java.util.concurrent.Callable;

/**
 * A functional stop watch for measuring the time for executing a specific task.
 */
public final class ExecutionStopWatch {

    private ExecutionStopWatch(){
    }

    /**
     * Performs the measured execution returning the result including the time it took to execution
     * @param r
     *  the runnable whose execution should be measured.
     * @return
     *  the measured execution result. The return value is always of type {@link Void}
     */
    public static MeasuredExecutionResult runMeasured(Runnable r){
        return runMeasured(() -> {
            r.run();
            return Void.TYPE;
        });
    }

    /**
     * Performs the measured execution returning the result including the time it took to execution
     * @param callable
     *  the callable to be measured
     * @param <T>
     *    the type of the result
     * @return
     *  the measured execution result which is a wrapper around the return value and includes the
     *  return value or exception.
     */
    public static <T> MeasuredExecutionResult<T> runMeasured(Callable<T> callable) {
        final Instant start = Instant.now();
        MeasuredExecutionResult<T> result;
        try {
            final T returnValue = callable.call();
            final Duration duration = Duration.between(start, Instant.now());
            result = new MeasuredExecutionResult<>(start, duration, returnValue);
        } catch (Exception e) {
            result = new MeasuredExecutionResult<>(start, Duration.between(start, Instant.now()), e);
        }
        return result;
    }

}
