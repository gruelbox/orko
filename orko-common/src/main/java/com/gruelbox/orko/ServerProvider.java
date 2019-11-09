package com.gruelbox.orko;

import org.eclipse.jetty.server.Server;

import com.google.inject.Provider;

import io.dropwizard.lifecycle.ServerLifecycleListener;

class ServerProvider implements ServerLifecycleListener, Provider<Server> {

  private Server server;

  @Override
  public void serverStarted(Server server) {
    this.server = server;
  }

  @Override
  public Server get() {
    return server;
  }

}
