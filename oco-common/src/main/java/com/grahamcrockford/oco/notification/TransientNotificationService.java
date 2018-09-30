package com.grahamcrockford.oco.notification;

import com.google.inject.ImplementedBy;

@ImplementedBy(TransientNotificationServiceImpl.class)
public interface TransientNotificationService extends NotificationService {

}
