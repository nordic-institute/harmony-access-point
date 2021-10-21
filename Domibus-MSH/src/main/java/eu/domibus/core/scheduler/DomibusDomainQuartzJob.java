package eu.domibus.core.scheduler;

import eu.domibus.api.multitenancy.Domain;

/**
 * Encapsulates Domibus domain and a Quartz job
 * @since 5.0
 * @author Catalin Enache
 */
public class DomibusDomainQuartzJob {
    private Domain domain;

    private String quartzJob;

    public DomibusDomainQuartzJob(Domain domain, String quartzJob) {
        this.domain = domain;
        this.quartzJob = quartzJob;
    }

    public Domain getDomain() {
        return domain;
    }

    public void setDomain(Domain domain) {
        this.domain = domain;
    }

    public String getQuartzJob() {
        return quartzJob;
    }

    public void setQuartzJob(String quartzJob) {
        this.quartzJob = quartzJob;
    }
}
