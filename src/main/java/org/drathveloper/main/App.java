package org.drathveloper.main;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class App
{
    public static void main( String[] args )
    {
        final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(new TinderBot4J(), 0, 30, TimeUnit.MINUTES);
    }
}
