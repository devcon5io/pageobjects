package io.devcon5.pageobjects;

import static org.slf4j.LoggerFactory.getLogger;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.slf4j.Logger;

import io.inkstand.scribble.rules.ExternalResource;

/**
 *
 */
public class TXResponseTimeCollector extends ExternalResource {

    private static final Logger LOG = getLogger(TXResponseTimeCollector.class);

    private static final ThreadLocal<TXResponseTimeCollector> CURRENT = new ThreadLocal<>();

    private Map<String, TXResponseTime> responseTimes = new ConcurrentHashMap<>();

    @Override
    protected void beforeClass() throws Throwable {
        before();
    }

    @Override
    protected void before() throws Throwable {
        CURRENT.set(this);
    }

    @Override
    protected void afterClass() {
        after();
    }

    @Override
    protected void after() {
        CURRENT.set(null);
        if (!responseTimes.isEmpty()) {

            LOG.warn("Some Transactions have not been completed:\n{}", responseTimes.values()
                                                                                    .stream()
                                                                                    .map(TXResponseTime::toString)
                                                                                    .collect(Collectors.joining("\n")));
            responseTimes.clear();
        }
    }

    public void startTx(String tx) {
        final Instant now = Instant.now();
        LOG.debug("TX Start {} at {}", tx, now);
        responseTimes.put(tx, TXResponseTimes.getInstance().startTx(tx, now));
    }

    public void stopTx(String tx) {
        final Instant now = Instant.now();
        stopTx(tx, now);
    }

    public void stopTx(String tx, Instant now) {
        if (!responseTimes.containsKey(tx)) {
            throw new IllegalStateException("Transaction " + tx + " not started");
        }
        LOG.debug("TX End {} at {}", tx, now);
        TXResponseTimes.getInstance().stopTx(responseTimes.remove(tx).finish(now));
    }

    public static TXResponseTimeCollector current(){
        return CURRENT.get();
    }
}
