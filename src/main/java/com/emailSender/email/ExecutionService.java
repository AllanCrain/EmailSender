package com.emailSender.email;

import com.emailSender.Either;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ExecutionService {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public Either<Exception, String> execute(Callable<Either<Exception, String>> callable, long timeOut, TimeUnit unit) {
        try {
            return executorService.submit(callable).get(timeOut, unit);
        }
        catch (Exception e) {
            return Either.left(e);
        }
    }
}
