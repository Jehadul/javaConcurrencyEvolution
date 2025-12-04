package comparison;

import java.util.concurrent.*;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Direct performance comparison between traditional and modern concurrency
 */
public class ConcurrencyPerformanceComparison {

	private static final int[] TASK_COUNTS = { 100, 1_000, 10_000, 50_000 };
	private static final int TASK_DURATION_MS = 100; // Simulated I/O time

	public static void main(String[] args) throws Exception {
		System.out.println("=== Concurrency Performance Comparison ===\n");
		System.out.println("Task duration: " + TASK_DURATION_MS + "ms (simulated I/O)\n");

		for (int taskCount : TASK_COUNTS) {
			System.out.println("Testing with " + taskCount + " tasks:");
			System.out.println("-".repeat(50));

			// Test traditional approach
			BenchmarkResult traditional = benchmarkTraditional(taskCount);
			System.out.println("Traditional (Thread Pool 200):");
			System.out.println(traditional);

			// Test modern approach
			BenchmarkResult modern = benchmarkModern(taskCount);
			System.out.println("Modern (Virtual Threads):");
			System.out.println(modern);

			// Calculate improvement
			double speedup = (double) traditional.durationMs / modern.durationMs;
			double memoryReduction = (double) traditional.memoryMB / modern.memoryMB;

			System.out.println("Improvement:");
			System.out.println("  Speedup: " + String.format("%.2fx", speedup));
			System.out.println("  Memory reduction: " + String.format("%.2fx", memoryReduction));
			System.out.println();
		}
	}

	/**
	 * Benchmark using traditional thread pool
	 */
	private static BenchmarkResult benchmarkTraditional(int taskCount) throws Exception {

		// Force garbage collection before test
		System.gc();
		Thread.sleep(100);

		Runtime runtime = Runtime.getRuntime();
		long memoryBefore = runtime.totalMemory() - runtime.freeMemory();

		ExecutorService executor = Executors.newFixedThreadPool(200);
		List<Future<Void>> futures = new ArrayList<>();

		Instant start = Instant.now();

		for (int i = 0; i < taskCount; i++) {
			Future<Void> future = executor.submit(() -> {
				Thread.sleep(TASK_DURATION_MS);
				return null;
			});
			futures.add(future);
		}

		// Wait for all
		for (Future<Void> future : futures) {
			future.get();
		}

		Duration duration = Duration.between(start, Instant.now());

		executor.shutdown();
		executor.awaitTermination(1, TimeUnit.MINUTES);

		long memoryAfter = runtime.totalMemory() - runtime.freeMemory();
		long memoryUsed = (memoryAfter - memoryBefore) / (1024 * 1024);

		return new BenchmarkResult("Traditional", taskCount, duration.toMillis(), memoryUsed);
	}

	/**
	 * Benchmark using virtual threads
	 */
	private static BenchmarkResult benchmarkModern(int taskCount) throws Exception {

		// Force garbage collection before test
		System.gc();
		Thread.sleep(100);

		Runtime runtime = Runtime.getRuntime();
		long memoryBefore = runtime.totalMemory() - runtime.freeMemory();

		try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
			List<Future<Void>> futures = new ArrayList<>();

			Instant start = Instant.now();

			for (int i = 0; i < taskCount; i++) {
				Future<Void> future = executor.submit(() -> {
					Thread.sleep(TASK_DURATION_MS);
					return null;
				});
				futures.add(future);
			}

			// Wait for all
			for (Future<Void> future : futures) {
				future.get();
			}

			Duration duration = Duration.between(start, Instant.now());

			long memoryAfter = runtime.totalMemory() - runtime.freeMemory();
			long memoryUsed = (memoryAfter - memoryBefore) / (1024 * 1024);

			return new BenchmarkResult("Modern", taskCount, duration.toMillis(), memoryUsed);
		}
	}

	/**
	 * Data class to hold benchmark results
	 */
	static class BenchmarkResult {
		String approach;
		int taskCount;
		long durationMs;
		long memoryMB;

		BenchmarkResult(String approach, int taskCount, long durationMs, long memoryMB) {
			this.approach = approach;
			this.taskCount = taskCount;
			this.durationMs = durationMs;
			this.memoryMB = memoryMB;
		}

		@Override
		public String toString() {
			double throughput = (taskCount * 1000.0) / durationMs;
			return String.format("  Duration: %,d ms\n  Memory: %d MB\n  Throughput: %.2f tasks/sec", durationMs,
					memoryMB, throughput);
		}
	}
}
