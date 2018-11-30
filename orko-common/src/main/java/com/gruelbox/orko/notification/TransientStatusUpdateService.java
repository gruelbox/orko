package com.gruelbox.orko.notification;

import com.google.inject.ImplementedBy;

@ImplementedBy(StatusUpdateServiceImpl.class)
public interface TransientStatusUpdateService extends StatusUpdateService {

}
