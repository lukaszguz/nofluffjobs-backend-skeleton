package com.nofluffjobs;

import java.util.Random;

public class DefaultJitter implements Jitter {
    private Random random = new Random();

    @Override
    public Double get() {
        return 0.85 + random.nextDouble() % 0.3f;
    }
}