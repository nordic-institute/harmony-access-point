package eu.domibus.core.message;

import eu.domibus.api.model.PartInfo;
import eu.domibus.api.model.UserMessage;
import eu.domibus.core.dao.BasicDao;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import eu.domibus.logging.DomibusMessageCode;
import eu.domibus.logging.MDCKey;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.jpa.QueryHints;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author François Gautier
 * @since 5.0
 */
@Repository
public class PartInfoDao extends BasicDao<PartInfo> {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(PartInfoDao.class);

    public PartInfoDao() {
        super(PartInfo.class);
    }

    public List<PartInfo> findPartInfoByUserMessageEntityId(final Long userMessageEntityId) {
        final Query query = this.em.createNamedQuery("PartInfo.findPartInfos");
        query.setParameter("ENTITY_ID", userMessageEntityId);
        query.setHint(QueryHints.HINT_PASS_DISTINCT_THROUGH, false);
        return query.getResultList();
    }

    @Transactional(propagation = Propagation.MANDATORY)
    @MDCKey(DomibusLogger.MDC_MESSAGE_ID)
    public void clearPayloadData(final Long userMessageEntityId) {
        LOG.debug("Start clearing payloadData");

        List<PartInfo> partInfos = findPartInfoByUserMessageEntityId(userMessageEntityId);

        if (CollectionUtils.isEmpty(partInfos)) {
            LOG.debug("No payloads to clear");
            return;
        }
        clearDatabasePayloads(partInfos);
        clearFileSystemPayloads(partInfos);

        LOG.businessInfo(DomibusMessageCode.BUS_MESSAGE_PAYLOAD_DATA_CLEARED);
    }

    public List<String> findFileSystemPayloadFilenames(List<String> userMessageEntityIds) {
        TypedQuery<String> query = em.createNamedQuery("PartInfo.findFilenames", String.class);
        query.setParameter("MESSAGEIDS", userMessageEntityIds);
        return query.getResultList();
    }

    public void clearFileSystemPayloads(final List<PartInfo> partInfos) {
        List<PartInfo> fileSystemPayloads = getFileSystemPayloads(partInfos);
        if (CollectionUtils.isEmpty(fileSystemPayloads)) {
            LOG.debug("No file system payloads to clear");
            return;
        }

        for (PartInfo result : fileSystemPayloads) {
            deletePayloadFile(result.getFileName());
        }
    }

    protected List<PartInfo> getFileSystemPayloads(final List<PartInfo> partInfos) {
        Predicate<PartInfo> filenamePresentPredicate = getFilenameEmptyPredicate().negate();
        return getPayloads(partInfos, filenamePresentPredicate);
    }

    protected void clearDatabasePayloads(final List<PartInfo> partInfos) {
        List<PartInfo> databasePayloads = getDatabasePayloads(partInfos);
        if (CollectionUtils.isEmpty(databasePayloads)) {
            LOG.debug("No database payloads to clear");
            return;
        }

        final Query emptyQuery = em.createNamedQuery("PartInfo.emptyPayloads");
        emptyQuery.setParameter("PARTINFOS", databasePayloads);
        emptyQuery.executeUpdate();
    }

    public void deletePayloadFile(String filename) {
        if(StringUtils.isAllBlank(filename)) {
            LOG.warn("Empty filename used to delete payload on filesystem!");
            return;
        }

        try {
            Path path = Paths.get(filename);
            if(path == null) {
                LOG.warn("Trying to delete an empty path, filename [{}]", filename);
                return;
            }
            Files.delete(path);
        } catch (IOException e) {
            LOG.debug("Problem deleting payload data files", e);
        }
    }

    protected List<PartInfo> getDatabasePayloads(final List<PartInfo> partInfos) {
        Predicate<PartInfo> filenameEmptyPredicate = getFilenameEmptyPredicate();
        return getPayloads(partInfos, filenameEmptyPredicate);
    }

    protected List<PartInfo> getPayloads(final List<PartInfo> partInfos, Predicate<PartInfo> partInfoPredicate) {
        return partInfos.stream().filter(partInfoPredicate).collect(Collectors.toList());
    }

    protected Predicate<PartInfo> getFilenameEmptyPredicate() {
        return partInfo -> StringUtils.isBlank(partInfo.getFileName());
    }

    public void deletePayloadFiles(List<String> filenames) {
        if(CollectionUtils.isEmpty(filenames)) {
            LOG.debug("No payload data file to delete from the filesystem");
            return;
        }

        LOG.debug("Thre are [{}] payloads on filesystem to delete: [{}] ", filenames.size() );
        for(String filename : filenames) {
            LOG.debug("Deleting payload data file: [{}]", filename);
            deletePayloadFile(filename);
        }
    }

}
