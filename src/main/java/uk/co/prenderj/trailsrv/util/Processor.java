package uk.co.prenderj.trailsrv.util;

/**
 * Generic interface for a method which processes some value.
 * @author Joshua Prendergast
 * @param <T> the value type
 */
public interface Processor<T> {
    public void call(T value) throws RuntimeException;
}
