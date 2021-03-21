package com.tradebot.core.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class CommonUtils {


    public static void commonExecutorServiceShutdown(ExecutorService executorService, long millisecondsWait) {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(millisecondsWait, TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException interruptedException) {
            log.warn("Could not shutdown executor service in graceful manner, trying to stop now...", interruptedException);
            executorService.shutdownNow();
        }
    }



}
