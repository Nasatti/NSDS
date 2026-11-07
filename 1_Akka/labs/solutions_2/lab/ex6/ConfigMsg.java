package com.lab.ex6;

import akka.actor.ActorRef;

public class ConfigMsg {

    private final ActorRef server;

    public ConfigMsg(ActorRef server) {
        this.server = server;
    }

    public ActorRef getServerRef() {
        return server;
    }
}
