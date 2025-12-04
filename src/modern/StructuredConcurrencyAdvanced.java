package modern;

import java.util.concurrent.StructuredTaskScope;

/**
 * Advanced Structured Concurrency patterns Demonstrates error handling and task
 * coordination
 */
public class StructuredConcurrencyAdvanced {

	public static void main(String[] args) {
		System.out.println("=== Structured Concurrency Advanced ===\n");

		try {
			// Example 1: Shutdown on failure
			demonstrateShutdownOnFailure();

			// Example 2: Shutdown on success (race scenario)
			demonstrateShutdownOnSuccess();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Demonstrates automatic cancellation when one task fails
	 */
	private static void demonstrateShutdownOnFailure() throws Exception {
		System.out.println("Example 1: Shutdown on Failure");
		System.out.println("Scenario: Fetch data from 3 services, cancel all if any fails\n");

		try {
			fetchFromMultipleServices(true); // Will fail
		} catch (Exception e) {
			System.out.println("Expected failure: " + e.getMessage());
		}
		System.out.println();
	}

	@SuppressWarnings("preview")
	private static String fetchFromMultipleServices(boolean simulateFailure) throws Exception {

		try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {

			StructuredTaskScope.Subtask<String> service1 = scope.fork(() -> {
				Thread.sleep(100);
				System.out.println("  Service 1: Processing...");
				return "Data from Service 1";
			});

			StructuredTaskScope.Subtask<String> service2 = scope.fork(() -> {
				Thread.sleep(50);
				if (simulateFailure) {
					System.out.println("  Service 2: Failed!");
					throw new RuntimeException("Service 2 unavailable");
				}
				return "Data from Service 2";
			});

			StructuredTaskScope.Subtask<String> service3 = scope.fork(() -> {
				Thread.sleep(150);
				System.out.println("  Service 3: Processing...");
				return "Data from Service 3";
			});

			// Wait for all subtasks (or first failure)
			scope.join();

			// If any task failed → cancel others + throw original error
			scope.throwIfFailed();

			// All tasks succeeded: combine results
			return String.format("%s, %s, %s", service1.get(), service2.get(), service3.get());
		}
	}

	/**
	 * Demonstrates racing multiple tasks (first successful wins)
	 */
	private static void demonstrateShutdownOnSuccess() throws Exception {
		System.out.println("Example 2: Shutdown on Success (Race)");
		System.out.println("Scenario: Query multiple mirrors, use first response\n");

		String result = queryMultipleMirrors();
		System.out.println("Winner: " + result);
		System.out.println();
	}

	private static String queryMultipleMirrors() throws Exception {
		try (var scope = new StructuredTaskScope.ShutdownOnSuccess<String>()) {

			// Fork requests to multiple mirrors
			scope.fork(() -> {
				Thread.sleep(150);
				System.out.println("  Mirror 1: Responded (slow)");
				return "Data from Mirror 1";
			});

			scope.fork(() -> {
				Thread.sleep(50);
				System.out.println("  Mirror 2: Responded (fast - winner!)");
				return "Data from Mirror 2";
			});

			scope.fork(() -> {
				Thread.sleep(200);
				System.out.println("  Mirror 3: Responded (slowest)");
				return "Data from Mirror 3";
			});

			// Wait for first successful completion
			scope.join();

			// Return the first successful result
			return scope.result();
		}
	}
}
