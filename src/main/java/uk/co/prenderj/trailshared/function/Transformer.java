package uk.co.prenderj.trailshared.function;

/**
 * Generic interface for a method which converts some value
 * into another.
 * @author Joshua Prendergast
 * @param <T> the value type
 * @param <R> the return type
 */
public interface Transformer<T, R> {
    public R call(T value) throws RuntimeException;
}
