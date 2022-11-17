package eu.domibus.core.cache.distributed;

import com.hazelcast.config.*;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.spring.cache.HazelcastCacheManager;
import eu.domibus.api.cache.CacheConstants;
import eu.domibus.api.cluster.ClusterDeploymentCondition;
import eu.domibus.api.property.DomibusPropertyProvider;
import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static eu.domibus.api.property.DomibusPropertyMetadataManagerSPI.*;


/**
 * @author Cosmin Baciu
 * @since 5.1
 */
@Conditional(ClusterDeploymentCondition.class)
@Configuration
public class DomibusDistributedCacheConfiguration {

    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(DomibusDistributedCacheConfiguration.class);

    public static final String DOMIBUS_CLUSTER = "domibusDistributedCacheCluster";

    private DomibusPropertyProvider domibusPropertyProvider;

    public DomibusDistributedCacheConfiguration(DomibusPropertyProvider domibusPropertyProvider) {
        this.domibusPropertyProvider = domibusPropertyProvider;
    }

    public static final String HAZELCAST_REST_API_GROUPS_ENABLED = "HEALTH_CHECK,CLUSTER_READ,CLUSTER_WRITE,HOT_RESTART,WAN,DATA,CP";

    @Bean(name = CacheConstants.DISTRIBUTED_CACHE_MANAGER)
    public org.springframework.cache.CacheManager distributedCacheManager() {
        LOG.info("Creating the distributed cache bean");

        final boolean restApiEnabled = BooleanUtils.toBoolean(domibusPropertyProvider.getBooleanProperty(DOMIBUS_DISTRIBUTED_CACHE_REST_API_ENABLED));
        final List<RestEndpointGroup> restEndpointGroups = getConfiguredRestApiGroups(restApiEnabled);

        RestApiConfig restApiConfig = new RestApiConfig()
                .setEnabled(restApiEnabled)
                .enableGroups(restEndpointGroups.toArray(new RestEndpointGroup[0]));

        Config config = new Config();
        config.setClusterName(DOMIBUS_CLUSTER);
        config.setProperty("hazelcast.logging.type", "slf4j");

        config.addMapConfig(mapDefaultConfig());

        NetworkConfig networkConfig = new NetworkConfig();
        networkConfig.setRestApiConfig(restApiConfig);

        final Integer hazelcastPort = domibusPropertyProvider.getIntegerProperty(DOMIBUS_DISTRIBUTED_CACHE_PORT);
        final boolean portAutoincrement = BooleanUtils.toBoolean(domibusPropertyProvider.getBooleanProperty(DOMIBUS_DISTRIBUTED_CACHE_PORT_AUTOINCREMENT));
        final Integer portAutoincrementCount = domibusPropertyProvider.getIntegerProperty(DOMIBUS_DISTRIBUTED_CACHE_PORT_COUNT);

        LOG.info("Using distributed cache port [{}], port autoincrement [{}], port autoincrement count [{}]", hazelcastPort, portAutoincrement, portAutoincrementCount);
        networkConfig.setPort(hazelcastPort).setPortAutoIncrement(portAutoincrement).setPortCount(portAutoincrementCount);
        config.setNetworkConfig(networkConfig);

        //join config
        JoinConfig joinConfig = new JoinConfig();
        networkConfig.setJoin(joinConfig);
        MulticastConfig multicastConfig = new MulticastConfig();
        multicastConfig.setEnabled(false);
        joinConfig.setMulticastConfig(multicastConfig);

        //set cluster members
        TcpIpConfig tcpIpConfig = new TcpIpConfig();
        joinConfig.setTcpIpConfig(tcpIpConfig);
        tcpIpConfig.setEnabled(true);
        final ArrayList<String> members = new ArrayList<>();
        List<String> clusterMembers = domibusPropertyProvider.getCommaSeparatedPropertyValues(DOMIBUS_DISTRIBUTED_CACHE_MEMBERS);
        LOG.info("Distributed cache cluster members are [{}]", clusterMembers);
        members.addAll(clusterMembers);
        tcpIpConfig.setMembers(members);

        HazelcastInstance instance = Hazelcast.newHazelcastInstance(config);
        LOG.info("Started distributed cache");
        instance.getCluster().addMembershipListener(new HazelcastClusterMembershipListener());
        instance.addDistributedObjectListener(new HazelcastDistributedObjectListener());

        final HazelcastCacheManager hazelcastCacheManager = new HazelcastCacheManager(instance);

        LOG.info("Finished creating the distributed cache bean");
        return hazelcastCacheManager;
    }

    protected List<RestEndpointGroup> getConfiguredRestApiGroups(boolean restApiEnabled) {
        if (!restApiEnabled) {
            LOG.info("Distributed cache REST API is disabled");
            return new ArrayList<>();
        }
        final List<String> restApiGroups = domibusPropertyProvider.getCommaSeparatedPropertyValues(DOMIBUS_DISTRIBUTED_CACHE_REST_API_GROUPS);
        LOG.info("Distributed cache REST API configured groups are [{}]", restApiGroups);

        return restApiGroups.stream().
                map(memberValue -> RestEndpointGroup.valueOf(memberValue))
                .collect(Collectors.toList());
    }

    /**
     * Default configuration for all maps
     */
    private MapConfig mapDefaultConfig() {
        MapConfig mapConfig = new MapConfig("*");
        mapConfig.setBackupCount(0);

        final Integer defaultTtl = domibusPropertyProvider.getIntegerProperty(DOMIBUS_DISTRIBUTED_CACHE_DEFAULT_TTL);
        LOG.info("Setting default TTL for distributed cache to [{}]", defaultTtl);
        mapConfig.setTimeToLiveSeconds(defaultTtl);

        final Integer maxIdle = domibusPropertyProvider.getIntegerProperty(DOMIBUS_DISTRIBUTED_CACHE_MAX_IDLE);
        LOG.info("Setting default max idle for distributed cache to [{}]", maxIdle);
        mapConfig.setMaxIdleSeconds(maxIdle);
        return mapConfig;
    }
}
