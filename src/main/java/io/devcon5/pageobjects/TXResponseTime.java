package io.devcon5.pageobjects;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 *
 */
public class TXResponseTime {

    private final UUID uuid;
    private final String transaction;
    private final Instant start;
    private final Duration duration;


    public TXResponseTime(String transaction, Instant start) {
        this(UUID.randomUUID(), transaction, start, Duration.ZERO);
    }

    TXResponseTime(UUID uuid, String transaction, Instant start, Duration duration) {
        this.uuid = uuid;
        this.transaction = transaction;
        this.start = start;
        this.duration = duration;
    }

    public TXResponseTime finish(){
        return finish(Instant.now());
    }

    public TXResponseTime finish(Instant end){
        if(duration == Duration.ZERO) {
            return new TXResponseTime(uuid, transaction, start, Duration.between(start, end));
        } else {
            throw new IllegalStateException("Transaction already finished");
        }
    }

    public boolean isFinished(){
        return duration != Duration.ZERO;
    }

    public Duration getResponseTime(){
        return duration;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getTransaction() {
        return transaction;
    }

    public Instant getStart() {
        return start;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TXResponseTime{");
        sb.append("transaction='").append(transaction).append('\'');
        sb.append(", start=").append(start);
        sb.append(", duration=").append(duration);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TXResponseTime that = (TXResponseTime) o;

        return uuid.equals(that.uuid);

    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }
}
