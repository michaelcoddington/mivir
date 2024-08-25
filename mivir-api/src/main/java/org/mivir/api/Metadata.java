package org.mivir.api;

public abstract class Metadata<T> {

    public abstract T getValue();
    public abstract void setValue(T value);

}