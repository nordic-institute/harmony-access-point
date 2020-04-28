package eu.domibus.plugin.fs.worker;

import eu.domibus.ext.domain.DomainDTO;
import eu.domibus.ext.quartz.DomibusQuartzJobExtBean;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.plugin.fs.custom.Domain2Entity;
import eu.domibus.plugin.fs.custom.Domain3Entity;
import eu.domibus.plugin.fs.custom.FSPluginEntityDao;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Quartz based worker responsible for the periodical execution of the FSPurgeSentService.
 *
 * @author FERNANDES Henrique, GONCALVES Bruno
 */
@DisallowConcurrentExecution // Only one FSPurgeSentWorker runs at any time on the same node
public class Domain3Worker extends DomibusQuartzJobExtBean {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(Domain3Worker.class);

    @Autowired
    protected FSPluginEntityDao fsPluginEntityDao;

    @Override
    protected void executeJob(JobExecutionContext context, DomainDTO domain) {
        LOG.info("--------------Executing Domain3 job");

        if (!domain.getCode().equalsIgnoreCase("domibus_domain_3")) {
            LOG.debug("Job is not allowed to run on domain [{}]", domain.getCode());
            return;
        }

        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
        List<Domain3Entity> allFSPluginEntities = fsPluginEntityDao.findAllForDomain3();

        LOG.info("--------------Finished executing Domain3 job found [{}]", allFSPluginEntities.size());
    }

}
