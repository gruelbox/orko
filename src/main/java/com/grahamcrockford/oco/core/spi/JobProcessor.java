package com.grahamcrockford.oco.core.spi;

import java.util.function.Consumer;

public interface JobProcessor<T extends Job> {

  public void start(T job, Consumer<T> onUpdate, Runnable onFinished);

  public void stop(T job);

}
