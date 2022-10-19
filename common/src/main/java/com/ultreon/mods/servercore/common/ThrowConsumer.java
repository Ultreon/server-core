package com.ultreon.mods.servercore.common;

/**
 * Consumer that con throw any exception.
 *
 * @param <T> the value to consume.
 * @param <E> the exception / error being thrown.
 */
public interface ThrowConsumer<T, E extends Throwable> {
    /**
     * Accept a value.
     *
     * @param value the value to accept.
     * @throws E the exception / error being thrown.
     */
    void accept(T value) throws E;
}
