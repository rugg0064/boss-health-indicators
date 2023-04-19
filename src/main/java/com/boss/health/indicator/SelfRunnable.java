package com.boss.health.indicator;

@FunctionalInterface
public interface SelfRunnable<T> {
    void run(T self);
}
