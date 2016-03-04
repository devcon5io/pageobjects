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

import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Optional;

import io.devcon5.pageobjects.ElementGroup;
import io.devcon5.pageobjects.measure.ResponseTimeCollector;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;

/**
 * Utility class to enhance a Page instance with transaction support.
 */
public final class TransactionHelper {

    private TransactionHelper() {

    }

    /**
     * Adds transaction support to the page. The transaction support captures execution time of methods annotated with
     * {@link io.devcon5.pageobjects.tx.Transaction}
     *
     * @param elementGroup
     *         the element group, i.e. a page
     * @param <T>
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T extends ElementGroup> T addTransactionSupport(T elementGroup) {
        final Class<T> type = (Class<T>) elementGroup.getClass();
        return (T) Enhancer.create(elementGroup.getClass(), (MethodInterceptor) (obj, method, args, proxy) -> {
            /*
            if(type.getMethod("getClass").equals(method)){
                return type;
            }
            */
            //Capture the time only if a response time collector is present, and the method
            //is associated with a transaction
            final Optional<ResponseTimeCollector> rtc = ResponseTimeCollector.current();
            final Optional<String> txName = rtc.flatMap(r -> getTxName(elementGroup, method));
            final Optional<Instant> start = txName.map(n -> Instant.now());
            try {
                Object result = method.invoke(elementGroup, args);
                if (!isCGLibProxy(result) && result instanceof Transactional) {
                    result = addTransactionSupport(elementGroup);
                }
                return result;
            } finally {
                final Optional<Instant> end = txName.map(n -> Instant.now());
                txName.ifPresent(name -> rtc.ifPresent(r -> r.captureTx(name, start.get(), end.get())));
            }
        });
    }

    private static boolean isCGLibProxy(Object result) {
        return result != null
                && result.getClass()
                         .getName()
                         .contains("$$EnhancerByCGLIB$$");
    }

    /**
     * Determines the transaction name of the method. The method must be annotated with {@link Transaction} otherwise
     * the empty optional is returned. The name is derived from the value of the {@link Transaction} annotation or from
     * the mehtod name. If the declaring class denotes a transaction itself, it's name prefixes the method transaction.
     *
     * @param method
     *         the method for which the transaction name should be determined
     *
     * @return the name of the transaction or the empty optional if the method denotes no transaction
     */
    private static Optional<String> getTxName(Object object, final Method method) {

        return Optional.ofNullable(method.getAnnotation(Transaction.class))
                       .map(t -> getClassTxName(object.getClass())
                               .map(ctx -> ctx + '_')
                               .orElse("") + (isEmpty(t.value())
                                              ? method.getName()
                                              : t.value()));
    }

    /**
     * Determines the transaction name for the class. The class must be annoted with {@link Transaction} otherwise an
     * empty optional is returned. The name of the transaction is either the value of the annotation of the simple name
     * of the class itself
     *
     * @param type
     *         the type for which a transaction name should be determined
     *
     * @return the name of the transaction of the empty optional if the class is not transactional
     */
    public static Optional<String> getClassTxName(final Class<?> type) {

        return Optional.ofNullable(type.getAnnotation(Transaction.class))
                       .map(t -> isEmpty(t.value())
                                 ? type.getSimpleName()
                                 : t.value());
    }
}
