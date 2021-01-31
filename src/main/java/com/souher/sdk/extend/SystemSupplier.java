package com.souher.sdk.extend;

@FunctionalInterface
public interface SystemSupplier<T>
{
    T get() throws Exception;
}
