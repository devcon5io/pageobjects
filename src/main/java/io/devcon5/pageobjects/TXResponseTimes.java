package io.devcon5.pageobjects;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 */
public class TXResponseTimes {

    private static final TXResponseTimes INSTANCE = new TXResponseTimes();

    private final Map<UUID, TXResponseTime> responseTimes = new ConcurrentHashMap<>();

    public static TXResponseTimes getInstance() {
        return INSTANCE;
    }

    public void clear() {
        responseTimes.clear();
    }

    public TXResponseTime startTx(String transaction) {
        return startTx(transaction, Instant.now());
    }

    public TXResponseTime startTx(String transaction, Instant start) {
        final TXResponseTime trt = new TXResponseTime(transaction, start);
        responseTimes.put(trt.getUuid(), trt);
        return trt;
    }

    public TXResponseTime stopTx(TXResponseTime finish) {
        responseTimes.put(finish.getUuid(), finish);
        return finish;
    }

    public Map<String, List<TXResponseTime>> getResponseTimes() {
        final Map<String, List<TXResponseTime>> result = new HashMap<>();
        responseTimes.values().stream().forEach(
                trt -> {
                    if (!result.containsKey(trt.getTransaction())) {
                        result.put(trt.getTransaction(), new ArrayList<>());
                    }
                    result.get(trt.getTransaction()).add(trt);
                }
        );
        return result;
    }

}
