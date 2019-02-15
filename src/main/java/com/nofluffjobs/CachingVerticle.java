package com.nofluffjobs;

import io.vertx.reactivex.core.AbstractVerticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class CachingVerticle extends AbstractVerticle {

    private static final Logger log = LoggerFactory.getLogger(CachingVerticle.class);
    private final long refreshMilliseconds;
    private final ExpBackoff expBackoff;

    CachingVerticle(long refreshMilliseconds, ExpBackoff expBackoff) {
        this.refreshMilliseconds = refreshMilliseconds;
        this.expBackoff = expBackoff;
    }

    @Override
    public void start() {
        vertx.setPeriodic(refreshMilliseconds, this::updateCache);
    }

    private void updateCache(long counter) {
        vertx.eventBus().<String>rxSend("posting::get", null)
                .retryWhen(expBackoff)
                .doOnSuccess(message -> log.info("Got message: {}", message.body()))
                .doOnSubscribe(x -> log.info("Called"))
                .subscribe();
    }
}
