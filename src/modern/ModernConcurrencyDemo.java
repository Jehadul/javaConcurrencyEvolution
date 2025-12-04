package modern;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.StructuredTaskScope;

/**
 * Modern concurrency using Virtual Threads (Java 21+) Demonstrates improved
 * scalability and simplicity
 */
public class ModernConcurrencyDemo {

	public static void main(String[] args) throws Exception {
		System.out.println("=== Modern Concurrency Demo (Java 21+) ===\n");

		// Test 1: Virtual threads - millions possible
		testVirtualThreads();

		// Test 2: Structured Concurrency
		testStructuredConcurrency();

		// Test 3: Scoped Values
		testScopedValues();
	}

	/**
	 * Test 1: Creating and managing virtual threads
	 */
	private static void testVirtualThreads() throws Exception {
		System.out.println("Test 1: Virtual Threads");
		Instant start = Instant.now();

		// Create executor that creates virtual thread per task
		try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {

			// Submit 10,000 tasks (would be impossible with platform threads)
			var futures = new java.util.ArrayList<Future<String>>();

			for (int i = 0; i < 10_000; i++) {
				final int taskId = i;
				Future<String> future = executor.submit(() -> {
					Thread.sleep(100); // Blocking is OK with virtual threads
					return "Task " + taskId + " on " + Thread.currentThread();
				});
				futures.add(future);
			}

			// Collect results
			int completed = 0;
			for (var future : futures) {
				future.get();
				completed++;
			}

			Duration duration = Duration.between(start, Instant.now());
			System.out.println("Completed " + completed + " tasks in " + duration.toMillis() + " ms");
			System.out.println("Virtual thread overhead: minimal\n");
		}
	}

	/**
	 * Test 2: Structured Concurrency for task management
	 */
	private static void testStructuredConcurrency() throws Exception {
		System.out.println("Test 2: Structured Concurrency");
		Instant start = Instant.now();

		String result = fetchUserData(123);

		Duration duration = Duration.between(start, Instant.now());
		System.out.println("Result: " + result);
		System.out.println("Duration: " + duration.toMillis() + " ms");
		System.out.println("All subtasks automatically managed\n");
	}

	/**
	 * Fetches user data using structured concurrency Automatically handles errors
	 * and cancellation
	 */
	@SuppressWarnings("preview")
	private static String fetchUserData(int userId) throws Exception {

		try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {

			// Correct type: Subtask<String>
			StructuredTaskScope.Subtask<String> userFuture = scope.fork(() -> {
				Thread.sleep(50);
				return "User-" + userId;
			});

			StructuredTaskScope.Subtask<String> profileFuture = scope.fork(() -> {
				Thread.sleep(50);
				return "Profile-" + userId;
			});

			StructuredTaskScope.Subtask<String> preferencesFuture = scope.fork(() -> {
				Thread.sleep(50);
				return "Preferences-" + userId;
			});

			// Wait for all tasks
			scope.join();

			// If any task failed → automatically cancel others
			scope.throwIfFailed();

			// All tasks succeeded → get results
			return String.format("%s + %s + %s", userFuture.get(), profileFuture.get(), preferencesFuture.get());
		}
	}

	/**
	 * Test 3: Scoped Values for context sharing
	 */
	@SuppressWarnings({ "preview" })
	private static void testScopedValues() {
		System.out.println("Test 3: Scoped Values");

		// Define scoped value
		final ScopedValue<String> REQUEST_ID = ScopedValue.newInstance();

		// Bind value and run operations
		ScopedValue.where(REQUEST_ID, "REQ-12345").run(() -> {
			System.out.println("Request ID: " + REQUEST_ID.get());

			// Value available in nested calls
			processRequest(REQUEST_ID);

			// Value available in virtual threads
			Thread.startVirtualThread(() -> {
				System.out.println("  In virtual thread: " + REQUEST_ID.get());
			});
		});

		System.out.println("Scoped value automatically cleaned up\n");
	}

	private static void processRequest(ScopedValue<String> requestId) {
		System.out.println("  Processing: " + requestId.get());
	}
}
