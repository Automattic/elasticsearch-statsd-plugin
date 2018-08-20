# Elasticsearch StatsD Plugin
This plugin creates a little push service, which regularly updates a StatsD host with indices stats and nodes stats.
Index stats that apply across the entire cluster is only pushed from the elected master which node level stats are pushed from every node.

The data sent to the StatsD server tries to be roughly equivalent to the [Indices Stats API](https://www.elastic.co/guide/en/elasticsearch/reference/current/indices-stats.html) and [Nodes Stats Api](https://www.elastic.co/guide/en/elasticsearch/reference/current/cluster-nodes-stats.html).

## Versions

![Travis](https://api.travis-ci.org/Automattic/elasticsearch-statsd-plugin.png)

| Elasticsearch  | Plugin         | Release date |
| -------------- | -------------- | ------------ |
| 6.3.1          | 6.3.1.0        | Jul 05, 2018 |
| 6.2.4          | 6.2.4.0        | Apr 26, 2018 |
| 6.2.3          | 6.2.3.0        | Apr 26, 2018 |
| 6.2.2          | 6.2.2.0        | Apr 26, 2018 |
| 6.2.1          | 6.2.1.0        | Feb 9,  2018 |
| 6.2.0          | 6.2.0.0        | Feb 9,  2018 |
| 6.1.3          | 6.1.3.0        | Jan 31, 2018 |
| 6.1.2          | 6.1.2.0        | Jan 31, 2018 |
| 6.1.1          | 6.1.1.1        | Jan 16, 2018 |
| 5.6.9          | 5.6.9.0        | Jun 19, 2018 |
| 5.6.5          | 5.6.5.0        | Dec 19, 2017 |
| 5.6.4          | 5.6.4.0        | Nov 13, 2017 |
| 5.6.3          | 5.6.3.0        | Oct 12, 2017 |
| 5.6.2          | 5.6.2.0        | Oct 12, 2017 |
| 5.6.1          | 5.6.1.0        | Oct 12, 2017 |
| 5.6.0          | 5.6.0.0        | Sep 14, 2017 |
| 5.5.2          | 5.5.2.0        | Aug 22, 2017 |
| 5.5.1          | 5.5.1.0        | Jul 25, 2017 |
| 5.5.0          | 5.5.0.0        | Jul 12, 2017 |
| 5.4.3          | 5.4.3.0        | Sep 15, 2017 |
| 5.4.2          | 5.4.2.0        | Jun 23, 2017 |
| 5.4.1          | 5.4.1.0        | Jun 8,  2017 |
| 5.4.0          | 5.4.0.0        | May 6,  2017 |
| 5.3.3          | 5.3.3.0        | Jun 10, 2017 |
| 5.3.2          | 5.3.2.0        | May 6,  2017 |
| 5.3.1          | 5.3.1.0        | May 6,  2017 |
| 5.3.0          | 5.3.0.1        | May 6,  2017 |
| 5.2.2          | 5.2.2.1        | May 6,  2016 |
| 5.2.1          | 5.2.1.1        | May 6,  2016 |
| 5.2.0          | 5.2.0.1        | May 6,  2016 |
| 5.1.2          | 5.1.2.1        | May 6,  2016 |
| 5.1.1          | 5.1.1.1        | May 6,  2016 |
| 5.0.2          | 5.0.2.1        | May 6,  2016 |
| 5.0.1          | 5.0.1.1        | May 6,  2016 |
| 5.0.0          | 5.0.0.1        | May 6,  2016 |
| 2.4.6          | 2.4.6.0        | Jul 26, 2017 |
| 2.4.5          | 2.4.5.0        | May 7,  2017 |
| 2.4.4          | 2.4.4.1        | May 7,  2017 |
| 2.4.3          | 2.4.3.1        | May 7,  2017 |
| 2.4.2          | 2.4.2.1        | May 7,  2017 |
| 2.4.1          | 2.4.1.1        | May 7,  2017 |
| 2.4.0          | 2.4.0.1        | May 7,  2017 |
| 2.3.5          | 2.3.5.1        | May 6,  2017 |
| 2.3.4          | 2.3.4.1        | May 6,  2017 |
| 2.3.3          | 2.3.3.1        | May 6,  2017 |
| 2.3.2          | 2.3.2.1        | May 6,  2017 |
| 2.3.1          | 2.3.1.1        | May 6,  2017 |
| 2.3.0          | 2.3.0.2        | May 6,  2017 |
| 2.2.2          | 2.2.2.0        | Mar 31, 2016 |
| 2.2.1          | 2.2.1.0        | Mar 23, 2016 |
| 2.2.0          | 2.2.0.0        | Feb 15, 2016 |
| 2.1.2          | 2.1.2.0        | Feb 15, 2016 |
| 2.1.1          | 2.1.1.0        | Feb 15, 2016 |
| 2.1.0          | 2.1.0.0        | Feb 15, 2016 |
| 2.0.2          | 2.0.2.0        | Feb 12, 2016 |
| 2.0.1          | 2.0.1.0        | Feb 12, 2016 |
| 2.0.0          | 2.0.0.0        | Feb 12, 2016 |
| 1.5.x to 1.7.x | 0.4.0          | Feb 3,  2016 |
| < 1.5.x        | 0.3.3          | Aug 20, 2014 |


## Installation Elasticsearch 6.x

The plugin artifacts are published to Maven Central and Github. To install a prepackaged plugin for ES 6.x+ use the following command:

From Github:

```
./bin/elasticsearch-plugin install https://github.com/Automattic/elasticsearch-statsd-plugin/releases/download/6.3.1.0/elasticsearch-statsd-6.3.1.0.zip
```

From Maven Central:
```
./bin/elasticsearch-plugin install http://repo1.maven.org/maven2/com/automattic/elasticsearch-statsd/6.3.1.0/elasticsearch-statsd-6.3.1.0.zip
```

Change the version to match your ES version. For ES `x.y.z` the version is `x.y.z.0`

You can also build your own by doing the following:

```
git clone http://github.com/Automattic/elasticsearch-statsd-plugin.git
cd elasticsearch-statsd-plugin
mvn package -Djava.security.policy=src/test/resources/plugin-security-test.policy
```

Once we have the artifact, install it with the following command:

```
bin/elasticsearch-plugin install file:///absolute/path/to/current/dir/target/releases/elasticsearch-statsd-6.3.1.0.zip
```

## Installation Elasticsearch 5.x

The plugin artifacts are published to Maven Central and Github. To install a prepackaged plugin for ES 5.x+ use the following command:

From Github:

```
./bin/elasticsearch-plugin install https://github.com/Automattic/elasticsearch-statsd-plugin/releases/download/5.6.9.0/elasticsearch-statsd-5.6.9.0.zip
```

From Maven Central:
```
./bin/elasticsearch-plugin install http://repo1.maven.org/maven2/com/automattic/elasticsearch-statsd/5.6.9.0/elasticsearch-statsd-5.6.9.0.zip
```

Change the version to match your ES version. For ES `x.y.z` the version is `x.y.z.0`

You can also build your own by doing the following:

```
git clone http://github.com/Automattic/elasticsearch-statsd-plugin.git
cd elasticsearch-statsd-plugin
mvn package
```

Once we have the artifact, install it with the following command:

```
./bin/elasticsearch-plugin install file:///Users/anandnalya/github/automattic/elasticsearch-statsd-plugin/target/releases/elasticsearch-statsd-5.6.9.0.zip
```

## Installation Elasticsearch 2.x

The plugin artifacts are published to Maven Central. To install a prepackaged plugin for ES 2.x use the following command:

```
bin/plugin install com.automattic/elasticsearch-statsd/2.4.4.0
```

Change the version to match your ES version. For ES `x.y.z` the version is `x.y.z.0`

You can also build your own by doing the following:

```
git clone http://github.com/Automattic/elasticsearch-statsd-plugin.git
cd elasticsearch-statsd-plugin
mvn package -Dtests.security.manager=false
bin/plugin file:///absolute/path/to/current/dir/target/releases/elasticsearch-statsd-2.4.4.0.zip
```


## Installation Elasticsearch 1.x

```
bin/plugin -install statsd -url https://github.com/Automattic/elasticsearch-statsd-plugin/releases/download/v0.4.0/elasticsearch-statsd-0.4.0.zip
```

## Configuration

Configuration is possible via these parameters:

* `metrics.statsd.host`: The statsd host to connect to (default: localhost)
* `metrics.statsd.port`: The port to connect to (default: 8125)
* `metrics.statsd.every`: The interval to push data (default: 1m)
* `metrics.statsd.prefix`: The metric prefix that's sent with metric names (default: elasticsearch.your_cluster_name)
* `metrics.statsd.node_name`: Override the name for node used in the stat keys (default: the ES node name)
* `metrics.statsd.report.node_indices`: If per node index sums should be reported (default: false)
* `metrics.statsd.report.indices`: If index level sums should be reported (default: true)
* `metrics.statsd.report.shards`: If shard level stats should be reported (default: false)
* `metrics.statsd.report.fs_details`: If nodes should break down the FS by device instead of total disk (default: false)

Check your elasticsearch log file for a line like this after adding the configuration parameters below to the configuration file

```
[INFO ][com.automattic.elasticsearch.statsd.StatsdService] [Ludi] StatsD reporting triggered every [8s] to host [192.168.99.100:32768]
```


## Stats Key Formats

This plugin reports both node level and cluster level stats, the StatsD keys will be in the formats:

* `{PREFIX}.node.{NODE_NAME}.{STAT_KEY}`: Node level stats (CPU / JVM / etc.)
* `{PREFIX}.node.{NODE_NAME}.indices.{STAT_KEY}`: Index stats summed across the node (off by default)
* `{PREFIX}.indices.{STAT_KEY}`: Index stats summed across the entire cluster
* `{PREFIX}.index.{INDEX_NAME}.total.{STAT_KEY}`: Index stats summed per index across all shards
* `{PREFIX}.index.{INDEX_NAME}.{SHARD_ID}.{STAT_KEY}` -- Index stats per shard (off by default)


## Bugs/TODO

* Not extensively tested
* In case of a master node failover, counts are starting from 0 again (in case you are wondering about spikes)


## Credits

This is a fork of the [Swoop plugin](https://github.com/swoop-inc/elasticsearch-statsd-plugin) for multi-node clusters on ES 2.x.

Heavily inspired by the excellent [metrics library](http://metrics.dropwizard.io/4.0.0/getting-started.html) by Coda Hale and its GraphiteReporter add-on.


## License

See LICENSE
