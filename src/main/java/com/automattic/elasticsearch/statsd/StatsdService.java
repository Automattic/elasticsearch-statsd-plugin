package com.automattic.elasticsearch.statsd;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.automattic.elasticsearch.plugin.StatsdPlugin;
import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.SpecialPermission;
import org.elasticsearch.action.admin.indices.stats.CommonStatsFlags;
import org.elasticsearch.client.Client;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.component.AbstractLifecycleComponent;
import org.elasticsearch.common.component.Lifecycle;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.util.concurrent.EsExecutors;
import org.elasticsearch.indices.IndicesService;
import org.elasticsearch.node.NodeService;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

public class StatsdService extends AbstractLifecycleComponent {

    private final Client client;
    private final ClusterService clusterService;
    private final IndicesService indicesService;
    private final NodeService nodeService;
    private final String statsdHost;
    private final Integer statsdPort;
    private final TimeValue statsdRefreshInternal;
    private final String statsdPrefix;
    private final String statsdNodeName;
    private final boolean statsdReportNodeIndices;
    private final boolean statsdReportIndices;
    private final boolean statsdReportShards;
    private final boolean statsdReportFsDetails;
    private final boolean statsdSendHttpStats;
    private final StatsDClient statsdClient;

    private final Thread statsdReporterThread;
    private final AtomicBoolean closed = new AtomicBoolean(false);

    protected static final Logger logger = LogManager.getLogger(StatsdService.class);

    @Inject
    public StatsdService(Settings settings, Client client, ClusterService clusterService, IndicesService indicesService, NodeService nodeService) {
        super(settings);
        this.client = client;
        this.clusterService = clusterService;
        this.indicesService = indicesService;
        this.nodeService = nodeService;
        this.statsdRefreshInternal = StatsdPlugin.EVERY_S.get(settings);
        this.statsdHost = StatsdPlugin.HOST_S.get(settings);
        this.statsdPort = StatsdPlugin.PORT_S.get(settings);
        this.statsdPrefix = Stream.of(StatsdPlugin.PREFIX_S.get(settings), "elasticsearch" + "." +
                settings.get("cluster.name")).filter(s -> s.length() > 0).findFirst().get();
        this.statsdNodeName = StatsdPlugin.NODE_NAME_S.get(settings);
        this.statsdReportNodeIndices = StatsdPlugin.REPORT_NODE_INDICES_S.get(settings);
        this.statsdReportIndices = StatsdPlugin.REPORT_INDICES_S.get(settings);
        this.statsdReportShards = StatsdPlugin.REPORT_SHARDS_S.get(settings);
        this.statsdReportFsDetails = StatsdPlugin.REPORT_FS_DETAILS_S.get(settings);
        this.statsdSendHttpStats = !StatsdPlugin.TEST_MODE_S.get(settings);

        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            // unprivileged code such as scripts do not have SpecialPermission
            sm.checkPermission(new SpecialPermission());
        }
        this.statsdClient = AccessController.doPrivileged((PrivilegedAction<StatsDClient>) () ->
                new NonBlockingStatsDClient(StatsdService.this.statsdPrefix, StatsdService.this.statsdHost,
                        StatsdService.this.statsdPort));

        this.statsdReporterThread = EsExecutors
                .daemonThreadFactory(settings, "statsd_reporter")
                .newThread(new StatsdReporterThread());
    }

    @Override
    protected void doStart() throws ElasticsearchException {
        if (this.statsdHost != null && this.statsdHost.length() > 0) {
            this.statsdReporterThread.start();
            this.logger.info(
                    "StatsD reporting triggered every [{}] to host [{}:{}] with metric prefix [{}]",
                    this.statsdRefreshInternal, this.statsdHost, this.statsdPort, this.statsdPrefix
            );
        } else {
            this.logger.error(
                    "StatsD reporting disabled, no StatsD host configured"
            );
        }
    }

    @Override
    protected void doStop() throws ElasticsearchException {
        doClose();
    }

    @Override
    protected void doClose() throws ElasticsearchException {
        if(this.closed.compareAndSet(false, true)) {
            this.statsdReporterThread.interrupt();
            this.logger.info("StatsD reporter stopped");
        }
    }

    public class StatsdReporterThread implements Runnable {

        @Override
        public void run() {
            try {
                while (!StatsdService.this.closed.get()) {
                    long startTime = System.currentTimeMillis();

                    ClusterState state;
                    try {
                        state = StatsdService.this.clusterService.state();
                    } catch (AssertionError e) {
                        logger.info("Cluster state not set yet. Looping...");
                        Thread.sleep(10);
                        continue;
                    }
                    boolean isClusterStarted = StatsdService.this.clusterService
                            .lifecycleState()
                            .equals(Lifecycle.State.STARTED);


                    if(isClusterStarted) {
                        DiscoveryNode node = StatsdService.this.clusterService.localNode();

                        if (node != null && state != null) {
                            String statsdNodeName = StatsdService.this.statsdNodeName;
                            if (Strings.isNullOrEmpty(statsdNodeName)) {
                                statsdNodeName = node.getName();
                            }

                            // Report node stats -- runs for all nodes
                            try {
                                StatsdReporter nodeStatsReporter = new StatsdReporterNodeStats(
                                        StatsdService.this.nodeService.stats(
                                                new CommonStatsFlags().clear(),     // indices
                                                true,                               // os
                                                true,                               // process
                                                true,                               // jvm
                                                true,                               // threadPool
                                                true,                               // fs
                                                true,                               // transport
                                                statsdSendHttpStats,                // http
                                                true,                               // circuitBreaker
                                                false,                              // script,
                                                false,                              // discoveryStats
                                                false,                              // ingest
                                                false                               // adaptiveSelection
                                        ),
                                        statsdNodeName,
                                        StatsdService.this.statsdReportFsDetails
                                );
                                nodeStatsReporter
                                        .setStatsDClient(StatsdService.this.statsdClient)
                                        .run();
                            } catch (Exception e) {
                                StatsdService.this.logger.error("Unable to send node stats", e);
                            }

                            // Maybe report index stats per node
                            if (StatsdService.this.statsdReportNodeIndices && node.isDataNode()) {
                                try {
                                    StatsdReporter nodeIndicesStatsReporter = new StatsdReporterNodeIndicesStats(
                                            StatsdService.this.indicesService.stats(
                                                    false // includePrevious
                                            ),
                                            statsdNodeName
                                    );
                                    nodeIndicesStatsReporter
                                            .setStatsDClient(StatsdService.this.statsdClient)
                                            .run();
                                } catch (Exception e) {
                                    StatsdService.this.logger.error("Unable to send node indices stats", e);
                                }
                            }

                            // Master node is the only one allowed to send cluster wide sums / stats
                            if (state.nodes().isLocalNodeElectedMaster()) {
                                try {
                                    StatsdReporter indicesReporter = new StatsdReporterIndices(
                                            StatsdService.this.client
                                                    .admin()        // AdminClient
                                                    .indices()      // IndicesAdminClient
                                                    .prepareStats() // IndicesStatsRequestBuilder
                                                    .all()          // IndicesStatsRequestBuilder
                                                    .get(),         // IndicesStatsResponse
                                            StatsdService.this.statsdReportIndices,
                                            StatsdService.this.statsdReportShards
                                    );
                                    indicesReporter
                                            .setStatsDClient(StatsdService.this.statsdClient)
                                            .run();
                                } catch (Exception e) {
                                    StatsdService.this.logger.error("Unable to send cluster wide stats", e);
                                }
                            }
                        }
                    }

                    long elapsedTime = System.currentTimeMillis() - startTime;

                    try {
                        Thread.sleep(StatsdService.this.statsdRefreshInternal.millis() - elapsedTime);
                    } catch (InterruptedException e1) {
                        continue;
                    }
                }
            } catch (Exception e) {
                StatsdService.this.logger.error("Exception thrown from the event loop of StatsdReporterThread", e);
            }

            StatsdService.this.logger.error("Exiting StatsdReporterThread");
        }
    }
}
