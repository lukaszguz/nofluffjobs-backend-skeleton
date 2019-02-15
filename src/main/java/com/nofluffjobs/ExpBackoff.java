package com.nofluffjobs;

import io.reactivex.Flowable;
import io.reactivex.functions.Function;

import java.util.concurrent.TimeUnit;

class ExpBackoff implements Function<Flowable<Throwable>, Flowable<Long>> {
    private final Jitter jitter;
    private final Long delay;
    private final TimeUnit unit;
    private final Integer retries;

    ExpBackoff(Jitter jitter, Long delay, TimeUnit unit, Integer retries) {
        this.jitter = jitter;
        this.delay = delay;
        this.unit = unit;
        this.retries = retries;
    }

    @Override
    public Flowable<Long> apply(Flowable<Throwable> flowable) {
        return flowable.zipWith(Flowable.range(1, retries), (throwable, retryCount) -> retryCount)
                .flatMap(attemptNumber -> Flowable.timer(getNewInterval(attemptNumber), unit));
    }

    private Long getNewInterval(Integer retryCount) {
        long newInterval = (long) (delay * Math.pow(retryCount.doubleValue(), 2.0) * jitter.get());
        if (newInterval < 0) {
            newInterval = Long.MAX_VALUE;
        }
        return newInterval;
    }
}