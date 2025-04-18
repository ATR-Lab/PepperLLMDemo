package com.aldebaran.qi.sdk.object.holder;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiThreadPool;
import com.aldebaran.qi.sdk.object.autonomousabilities.AutonomousAbilityHolder;
import com.aldebaran.qi.sdk.util.FutureUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class Holder {

    private Async asyncHolder;

    public Holder(List<Callable<AutonomousAbilityHolder>> callables) {
        this.asyncHolder = new Async(callables);
    }

    public void hold() {
        FutureUtils.get(asyncHolder.hold());
    }

    public void release() {
        FutureUtils.get(asyncHolder.release());
    }

    public Async async() {
        return asyncHolder;
    }

    public class Async {

        private List<Callable<AutonomousAbilityHolder>> callables;
        private List<Future<AutonomousAbilityHolder>> futures;

        public Async(List<Callable<AutonomousAbilityHolder>> callables) {
            this.callables = callables;
            this.futures = new ArrayList<>();
        }

        public Future<Void> hold() {
            futures = new ArrayList<>();

            for (Callable<AutonomousAbilityHolder> callable : callables) {
                futures.add(QiThreadPool.execute(callable));
            }

            if (futures.isEmpty()) {
                return Future.of(null);
            } else {
                return Future.waitAll(futures.toArray(new Future[futures.size()]));
            }
        }

        public Future<Void> release() {
            Future<Void>[] releaseFutures = new Future[this.futures.size()];

            for (int i = 0; i < this.futures.size(); i++) {
                releaseFutures[i] = this.futures.get(i).andThenCompose(holder -> holder.async().release());
            }
            return Future.waitAll(releaseFutures);
        }
    }
}
