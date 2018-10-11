package com.grahamcrockford.oco.notification;

import com.google.inject.ImplementedBy;

@ImplementedBy(StatusUpdateServiceImpl.class)
public interface TransientStatusUpdateService extends StatusUpdateService {

}
