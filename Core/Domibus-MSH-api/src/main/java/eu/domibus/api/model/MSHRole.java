package eu.domibus.api.model;


public enum MSHRole {
    SENDING,
    RECEIVING,
  	/*
     * TODO: to remove once the database migration for the self-sending is done.
     */
  	TEMPORARY_FIX_ROLE;
}
