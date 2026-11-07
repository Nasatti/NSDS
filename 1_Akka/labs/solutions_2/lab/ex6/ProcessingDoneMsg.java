package com.lab.ex6;

import akka.actor.ActorRef;

// Internal message sent to the server when the simulated processing finishes
public class ProcessingDoneMsg {

    private final String text;
    private final ActorRef requester;

    public ProcessingDoneMsg(String text, ActorRef requester) {
        this.text = text;
        this.requester = requester;
    }

    public String getText() {
        return text;
    }

    public ActorRef getRequester() {
        return requester;
    }
}
