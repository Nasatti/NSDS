package com.lab.ex6;

import java.time.Duration;
import java.util.Locale;

import akka.actor.AbstractActorWithStash;
import akka.actor.Props;

public class UppercaseServerActor extends AbstractActorWithStash {

    private static final Duration PROCESSING_DELAY = Duration.ofMillis(1000);

    @Override
    public Receive createReceive() {
        return ready();
    }

    private Receive ready() {
        return receiveBuilder()
                .match(TextMsg.class, this::startProcessing)
                .build();
    }

    private Receive busy() {
        return receiveBuilder()
                .match(TextMsg.class, this::putAside)
                .match(ProcessingDoneMsg.class, this::finishProcessing)
                .build();
    }

    void startProcessing(TextMsg msg) {
        System.out.println("SERVER: Processing: " + msg.getText());

        // Remember the current sender: for Ask, it is the temporary reply actor
        ProcessingDoneMsg done = new ProcessingDoneMsg(msg.getText(), sender());

        // Change behavior before scheduling completion of this request
        getContext().become(busy());

        // Simulate a longer operation without blocking the actor's mailbox
        getContext().system().scheduler().scheduleOnce(
                PROCESSING_DELAY,
                self(),
                done,
                getContext().dispatcher(),
                self());
    }

    void putAside(TextMsg msg) {
        System.out.println("SERVER: Stashing: " + msg.getText());
        stash();
    }

    void finishProcessing(ProcessingDoneMsg msg) {
        String uppercase = msg.getText().toUpperCase(Locale.ROOT);
        System.out.println("SERVER: Completed: " + uppercase);

        // Reply to the Ask that initiated the current request
        msg.getRequester().tell(new ReplyMsg(uppercase), self());

        // Resume accepting requests; process stashed messages
        getContext().become(ready());
        unstashAll();
    }

    static Props props() {
        return Props.create(UppercaseServerActor.class);
    }
}
