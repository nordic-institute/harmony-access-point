/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 * <p>
 * https://github.com/keeps/commons-ip
 */
package eu.domibus.core.earchive.eark;

import org.roda_project.commons_ip2.model.impl.eark.EARKSIP;

public class DomibusEARKSIP extends EARKSIP {
    private String batchId;

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }
}
