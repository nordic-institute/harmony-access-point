package metricss;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;

import java.util.concurrent.TimeUnit;

public class MyMetrics {

	static final MetricRegistry metricsRegistry = new MetricRegistry();
	static ConsoleReporter reporter;

	public static MetricRegistry getMetricsRegistry() {
		return metricsRegistry;
	}

	public static void startReport() {
		reporter = ConsoleReporter.forRegistry(metricsRegistry)
				.convertRatesTo(TimeUnit.SECONDS)
				.convertDurationsTo(TimeUnit.SECONDS)
				.build();
		reporter.start(1, TimeUnit.HOURS);
	}

	public static String getName4Timer(){
		StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
		StackTraceElement e = stacktrace[2];
		String className = e.getClassName();
		String methodName = e.getMethodName();
		return className + "." + methodName;
	}

	public static void report() {
		reporter.report();
	}




}
