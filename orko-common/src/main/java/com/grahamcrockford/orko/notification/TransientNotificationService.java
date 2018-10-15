package com.grahamcrockford.orko.notification;

import com.google.inject.ImplementedBy;

@ImplementedBy(NotificationServiceImpl.class)
public interface TransientNotificationService extends NotificationService {

}
