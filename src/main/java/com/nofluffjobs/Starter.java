package com.nofluffjobs;

import io.vertx.core.Vertx;

import static java.util.concurrent.TimeUnit.SECONDS;

public class Starter {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new PostingVerticle("nfj_skeleton_db.json"));
        vertx.deployVerticle(
                new CachingVerticle(12000, new ExpBackoff(new DefaultJitter(), 1L, SECONDS, 3))
        );
    }
}
