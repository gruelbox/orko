package com.gruelbox.orko.notification;

import com.google.inject.ImplementedBy;
import com.gruelbox.orko.jobrun.spi.StatusUpdateService;

@ImplementedBy(StatusUpdateServiceImpl.class)
public interface TransientStatusUpdateService extends StatusUpdateService {

}
