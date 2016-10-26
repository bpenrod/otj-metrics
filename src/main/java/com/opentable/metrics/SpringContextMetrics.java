package com.opentable.metrics;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.MetricSet;
import com.google.common.collect.ImmutableMap;

import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
class SpringContextMetrics {
    private static final String PREFIX = "metrics.spring.context.";

    private final MetricRegistry metricRegistry;

    private final Counter closes = new Counter();
    private final Counter refreshes = new Counter();
    private final Counter starts = new Counter();
    private final Counter stops = new Counter();

    private SpringContextMetrics(final MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    private MetricSet makeMetricSet() {
        return MetricSets.prefix(PREFIX, () -> new ImmutableMap.Builder<String, Metric>()
                .put("close", closes)
                .put("refresh", refreshes)
                .put("start", starts)
                .put("stop", stops)
                .build()
        );
    }

    @PostConstruct
    private void postConstruct() {
        metricRegistry.registerAll(makeMetricSet());
    }

    @PreDestroy
    private void preDestroy() {
        MetricSets.removeAll(metricRegistry, makeMetricSet());
    }

    @EventListener
    private void closed(final ContextClosedEvent event) {
        closes.inc();
    }

    @EventListener
    private void refreshed(final ContextRefreshedEvent event) {
        refreshes.inc();
    }

    @EventListener
    private void started(final ContextStartedEvent event) {
        starts.inc();
    }

    @EventListener
    private void stopped(final ContextStoppedEvent event) {
        stops.inc();
    }
}
