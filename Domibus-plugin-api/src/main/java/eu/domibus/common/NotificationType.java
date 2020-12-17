
package eu.domibus.common;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

/**
 * The types of notifications that can be submitted to a backoffice application.
 *
 * @author Christian Koch, Stefan Mueller
 * @author Cosmin Baciu
 */
public enum NotificationType {
    MESSAGE_RECEIVED,
    MESSAGE_FRAGMENT_RECEIVED,
    MESSAGE_SEND_FAILURE,
    MESSAGE_FRAGMENT_SEND_FAILURE,
    MESSAGE_RECEIVED_FAILURE,
    MESSAGE_FRAGMENT_RECEIVED_FAILURE,
    MESSAGE_SEND_SUCCESS,
    MESSAGE_FRAGMENT_SEND_SUCCESS,
    MESSAGE_STATUS_CHANGE,
    MESSAGE_FRAGMENT_STATUS_CHANGE;

    public static final List<NotificationType> DEFAULT_PULL_NOTIFICATIONS = unmodifiableList(asList(
            MESSAGE_RECEIVED,
            MESSAGE_SEND_FAILURE,
            MESSAGE_RECEIVED_FAILURE));
    public static final List<NotificationType> DEFAULT_PUSH_NOTIFICATIONS = unmodifiableList(asList(
            MESSAGE_RECEIVED,
            MESSAGE_SEND_FAILURE,
            MESSAGE_RECEIVED_FAILURE,
            MESSAGE_SEND_SUCCESS,
            MESSAGE_STATUS_CHANGE));
}
