This component provides a number of metrics for your application,
including metrics for the embedded Jetty component, the JVM, a
rudimentary health-check, and an extensible health-checking framework.
In addition, these metrics are automatically sent to Graphite, depending
on your application's configuration.

Requirements
------------
This component requires several classes to be injected.  The first
several are:
- `javax.management.MBeanServer`
- `com.codahale.metrics.MetricRegistry`
- `com.codahale.metrics.health.HealthCheckRegistry`

Instances of these classes are provided by the `RestHttpServer` in
`otj-server`.

More include:
- `com.opentable.service.ServiceInfo`
- `com.opentable.service.AppInfo`
- `com.opentable.service.EnvInfo`

These classes hail from `otj-core`, and are also provided automatically
for you by `otj-server`.  Note that `ServiceInfo` is an interface, so
you are expected to declare as a Spring Bean an _implementation_.  This
implementation, along with the several other info beans, will be used
to generate the Graphite metric namespace prefix.

Enabling
--------
As in [the demo server][1], the simplest route will probably to use the
`RestHttpServer` from `otj-metrics`.  This will automatically set up
metric tracking for you.

Health Checking
---------------
This module provides a basic report on the health of your application by
detecting whether the Spring application context has been refreshed or
closed.  There is also a framework for plugging in additional health
checks.  The results of all of these are made available at `/health`.

Annotations
-----------
This component provides automatic support for [the `@Timed`, etc.
annotations on Spring Beans][7].  There is no need to add
`@EnableMetrics` in your application.  (See
`MetricAnnotationConfiguration` for more details.)  Do note that the
metric names automatically generated as a result of these annotations
will be a function of the package structure, class naming, and function
naming (unless you use the `name`/`absolute` parameters).  Therefore, if
you rearrange or refactor your code, your metric names may implicitly be
changed as well.

Graphite
--------
In order to enable Graphite reporting of metrics, add the following
property to your configuration.

    ot.graphite.graphite-host

For a listing of what [Carbon][2] hosts to connect to in the region in
which your application is deployed, see [the internal DNS entries][3].

The default is for it to be unset, and in this case, Graphite reporting
will not occur.  The library will log if this is the case.  In addition,
if the environment variables accompanying cluster deployment are not
available, Graphite reporting will not occur.  In this case too, the
library will log.  If you want to test in a non-deployed environment,
you must mock the `$OT_ENV*` environment variables.  See more on these
in [the Java Services documentation][4].

You may also customize Graphite reporting further with the following two
configuration properties.

    ot.graphite.graphite-port
    ot.graphite.reporting-period

The second is of type `java.time.Duration`, so, for example, to specify
a ten second period, you would use `PT10s` as the property value.

The prefix used for Graphite metrics is as follows.

    app_metrics.service-name[-env-flavor].env-type.env-location.instance-x

Where the `service-name` is as you've defined it in your application,
the environment type, location, and flavor are as described in [the Java
Services documentation][4], and the instance number is as declared by
Singularity.

Underneath this prefix namespace, this library defines its metrics with
the following namespaces.

- JVM: `jvm`
- Jetty.  Dropwizard uses the fully-qualified class names for your
  webserver's handlers to create the metric namespaces.  This means that
  depending on the implementation, you may see some of the following.
  - `org.eclipse.jetty`
  - `org.springframework.boot.context.embedded.jetty`

NMT
---
In order to take advantage of [NMT][5] metrics, you will need to enable
a JVM argument:

    -XX:NativeMemoryTracking=summary

If this JVM argument is not present, the NMT metrics will not be
tracked (but a warning will be logged via the `otj-jvm` library).

AdminServlet
------------
You may optionally enable [the Dropwizard Metrics AdminServlet][6] by
including the `MetricsHttpConfiguration` class in your application's
configuration.  By default, it will make the servlet available at
`/metrics`, but you may customize the path at which the metrics are
available by providing the following configuration property.

    ot.metrics.http.path

See the source code for the format.

TODO
----
- Document how to enable metrics without using `RestHttpServer`.
- Document the Health API (`HealthHttpConfiguration`).

[1]: https://github.com/opentable/service-demo
[2]: https://github.com/graphite-project/carbon
[3]: https://github.com/opentable/ot-dns/blob/master/internal/otenv.com.db
[4]: https://wiki.otcorp.opentable.com/display/CP/ArchTeam+Java+Services
[5]: https://docs.oracle.com/javase/8/docs/technotes/guides/troubleshoot/tooldescr007.html
[6]: http://metrics.dropwizard.io/3.1.0/manual/servlets/#adminservlet
[7]: https://github.com/ryantenney/metrics-spring