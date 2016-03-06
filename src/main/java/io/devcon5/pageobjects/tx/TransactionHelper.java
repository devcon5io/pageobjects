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
import java.util.Optional;

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
     * @param <T>
     *
     * @param transactionSupport
     *         the transactionSupport element to be enhanced.
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T extends TransactionSupport> T addTransactionSupport(TransactionSupport transactionSupport) {
        return (T) Enhancer.create(transactionSupport.getClass(), (MethodInterceptor) (obj, method, args, proxy) -> {
            final Optional<String> txName = getTxName(transactionSupport, method);
            try {
                txName.ifPresent(transactionSupport::txBegin);
                Object result = method.invoke(transactionSupport, args);
                //dynamically enhance return values, if they are transactionSupport and not yet enhanced
                //this is required, i.e. if method return 'this' or create new objects which will
                //not be enhanced
                if (!isCGLibProxy(result) && result instanceof TransactionSupport) {
                    result = addTransactionSupport(transactionSupport);
                }
                return result;
            } finally {
                txName.ifPresent(transactionSupport::txEnd);
            }
        });
    }

    /**
     * Determines whether an instance is a CGLib Proxy.
     * @param object
     *  the object to check
     * @return
     *  true if the object is a CGLib proxy
     */
    private static boolean isCGLibProxy(Object object) {
        return object != null
                && object.getClass()
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
    public static Optional<String> getTxName(Object object, final Method method) {

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
