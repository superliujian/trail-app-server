package uk.co.prenderj.trailsrv.util;

import java.util.concurrent.TimeUnit;

/**
 * Tracks a span of time using the internal clock.
 * @author Joshua Prendergast
 */
public class SimpleTimer {
    private long span;
    private long end;
    private long pauseStart;
    private boolean paused;

    public SimpleTimer(long value, TimeUnit unit) {
        setTimeRemaining(value, unit);
    }

    public SimpleTimer(long millis) {
        this(millis, TimeUnit.MILLISECONDS);
    }

    /**
     * Removes all accrued progress and starts from the beginning. Equivalent to
     * <code>setTimeRemaining(getSpan())</code>.
     */
    public void reset() {
        setTimeRemaining(getSpan());
    }

    public void setPaused(boolean paused) {
        if (this.paused != paused) {
            long now = System.nanoTime();
            if (paused) {
                pauseStart = now;
            } else {
                end = now + (end - pauseStart);
            }
            this.paused = paused;
        }
    }

    /**
     * Gets the remaining time in milliseconds. This value can be negative.
     *
     * @return the remaining time in milliseconds
     */
    public long getTimeRemaining() {
        return TimeUnit.MILLISECONDS.convert(paused ? end - pauseStart : end - System.nanoTime(), TimeUnit.NANOSECONDS);
    }

    /**
     * Gets the remaining time in the given unit. This value can be negative.
     *
     * @param unit the desired unit
     * @return the remaining time in the given unit
     */
    public long getTimeRemaining(TimeUnit unit) {
        return unit.convert(getTimeRemaining(), TimeUnit.MILLISECONDS);
    }

    public void setTimeRemaining(long millis) {
        setTimeRemaining(millis, TimeUnit.MILLISECONDS);
    }

    public void setTimeRemaining(long value, TimeUnit unit) {
        long now = System.nanoTime();
        span = unit.toNanos(value);
        end = now + span;

        if (paused) {
            pauseStart = now;
        }
    }

    /**
     * Checks if the task has completed.
     *
     * @return true if the task is complete
     */
    public boolean isComplete() {
        return getTimeRemaining() <= 0;
    }

    /**
     * Gets the progress as a float from 0.0 - 1.0.
     *
     * @return a float from 0.0 - 1.0.
     */
    public float getProgress() {
        long remaining = getTimeRemaining(TimeUnit.NANOSECONDS);
        return Math.min(1.0f, (float) (span - remaining) / (float) span);
    }

    /**
     * Gets the initial delay in milliseconds.
     *
     * @return the initial delay in milliseconds
     */
    public long getSpan() {
        return getSpan(TimeUnit.MILLISECONDS);
    }

    /**
     * Gets the initial delay in the given time unit.
     *
     * @param unit the output unit
     * @return the initial delay in the given time unit
     */
    public long getSpan(TimeUnit unit) {
        return unit.convert(span, TimeUnit.NANOSECONDS);
    }

    public void setSpan(long span) {
        this.span = span;
    }

    /**
     * Gets a completed timer. This timer is mutable.
     *
     * @return a completed timer
     */
    public static SimpleTimer getCompletedTimer() {
        return new SimpleTimer(0);
    }

    /**
     * Creates a new completed timer with the given time as its span. Call {@link #reset()}
     * for simple tasks which never change the span.
     *
     * @param millis the span in milliseconds
     * @return a completed timer with the given span
     */
    public static SimpleTimer getCompletedTimer(long millis) {
        SimpleTimer rv = new SimpleTimer(0);
        rv.setSpan(TimeUnit.NANOSECONDS.convert(millis, TimeUnit.MILLISECONDS));
        return rv;
    }
}