package io.devcon5.pageobjects;

import static org.slf4j.LoggerFactory.getLogger;

import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Adds transaction support to a page object. Using transactions, response times of accesses to page objects can be
 * recorded
 */
public interface TransactionSupport {

    /**
     * @return the name of the transaction
     */
    default Optional<String> getTxName() {
        final Instant start = Instant.now();
        try {
            Optional<Transaction> typeTx = Optional.ofNullable(this.getClass().getAnnotation(Transaction.class));
            Optional<Method> txMethod = Optional.empty();
            final Set<String> filterNames = new HashSet<>(Arrays.asList("getTxName", "startTx", "stopTx", "getStackTrace", "accept"));
            for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
                if (filterNames.contains(ste.getMethodName())) {
                    continue;
                }
                try {
                    Method m = ClassUtils.getMethod(ste);
                    if (m.getAnnotation(Transaction.class) == null) {
                        continue;
                    }
                    txMethod = Optional.of(m);
                    break;
                } catch (Exception e) {
                }
            }
            return txMethod.map(m -> m.getAnnotation(Transaction.class))
                           .map(t -> Optional.of(t.value()))
                           .orElse(typeTx.map(t -> Optional.of(t.value()))
                                         .orElseGet(Optional::empty));
        } finally {
            getLogger("PERF").debug("time to getTxName = {}", Duration.between(start, Instant.now()));
        }
    }


    default void startTx() {
        getTxName().ifPresent(n -> TXResponseTimeCollector.current().startTx(n));
    }

    default void stopTx() {
        Instant now = Instant.now();
        getTxName().ifPresent(n -> TXResponseTimeCollector.current().stopTx(n, now));
    }
}
