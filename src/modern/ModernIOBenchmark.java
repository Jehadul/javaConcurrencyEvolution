package modern;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.time.Duration;
import java.time.Instant;

/**
 * I/O-bound workload using Virtual Threads Direct comparison to traditional
 * thread pool approach
 */
public class ModernIOBenchmark {

	private static final int REQUEST_COUNT = 10_000;

	public static void main(String[] args) throws Exception {
		System.out.println("=== Modern I/O Benchmark (Java 21+) ===");
		System.out.println("Requests: " + REQUEST_COUNT);
		System.out.println("Using: Virtual Threads (one per request)");
		System.out.println();

		runBenchmark();
	}

	private static void runBenchmark() throws Exception {
		// Virtual thread executor - creates one virtual thread per task
		try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {

			AtomicInteger completedRequests = new AtomicInteger(0);
			CountDownLatch latch = new CountDownLatch(REQUEST_COUNT);

			Instant startTime = Instant.now();

			// Submit all requests (10,000 concurrent virtual threads!)
			for (int i = 0; i < REQUEST_COUNT; i++) {
				final int requestId = i;
				executor.submit(() -> {
					try {
						handleRequest(requestId);
						completedRequests.incrementAndGet();
					} finally {
						latch.countDown();
					}
				});
			}

			// Wait for completion
			latch.await();
			Instant endTime = Instant.now();

			// Calculate metrics
			Duration duration = Duration.between(startTime, endTime);
			double durationSeconds = duration.toMillis() / 1000.0;
			double throughput = REQUEST_COUNT / durationSeconds;

			System.out.println("Results:");
			System.out.println("  Total Duration: " + String.format("%.2f", durationSeconds) + " seconds");
			System.out.println("  Throughput: " + String.format("%.2f", throughput) + " requests/sec");
			System.out.println("  Completed: " + completedRequests.get());

			// Memory info
			Runtime runtime = Runtime.getRuntime();
			long memoryUsed = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);
			System.out.println("  Memory Used: " + memoryUsed + " MB");
			System.out.println();
			System.out.println("Note: 10,000 concurrent virtual threads!");
		}
	}

	/**
	 * Simulates handling an HTTP request with database query Same logic as
	 * traditional version, but runs on virtual threads
	 */
	private static void handleRequest(int requestId) {
		try {
			// Simulate network I/O (50ms) - virtual thread yields
			Thread.sleep(50);

			// Simulate database query (30ms) - virtual thread yields
			Thread.sleep(30);

			// Simulate response processing (20ms) - virtual thread yields
			Thread.sleep(20);

		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
}
