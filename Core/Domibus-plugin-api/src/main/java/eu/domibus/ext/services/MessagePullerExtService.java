
package eu.domibus.ext.services;


/**
 * Pull requests are initiated through this interface
 *
 * @author Ion Perpegel
 * @since 5.0
 */
public interface MessagePullerExtService {

    /**
     * Make Domibus initiate a pull request.
     *
     * @param mpc the mpc to be used in the pull request
     *
     */
    void initiatePull(String mpc) ;
}
