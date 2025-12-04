package traditional;

import java.util.concurrent.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Traditional concurrency using platform threads (Java 8)
 * Demonstrates limitations of thread pools
 */
public class TraditionalConcurrencyDemo {
    
    public static void main(String[] args) throws Exception {
        System.out.println("=== Traditional Concurrency Demo (Java 8) ===\n");
        
        // Test 1: ExecutorService with fixed thread pool
        testFixedThreadPool();
        
        // Test 2: CompletableFuture chains
        testCompletableFuture();
        
        // Test 3: ForkJoinPool for parallel processing
        testForkJoinPool();
    }
    
    /**
     * Test 1: Fixed thread pool handling multiple tasks
     */
    private static void testFixedThreadPool() throws Exception {
        System.out.println("Test 1: Fixed Thread Pool");
        long startTime = System.currentTimeMillis();
        
        // Create thread pool with limited threads
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Future<String>> futures = new ArrayList<>();
        
        // Submit 1000 tasks (but only 10 can run concurrently)
        for (int i = 0; i < 1000; i++) {
            final int taskId = i;
            Future<String> future = executor.submit(() -> {
                Thread.sleep(100); // Simulate I/O operation
                return "Task " + taskId + " completed by " + 
                       Thread.currentThread().getName();
            });
            futures.add(future);
        }
        
        // Wait for all tasks to complete
        int completed = 0;
        for (Future<String> future : futures) {
            future.get(); // Blocking call
            completed++;
        }
        
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);
        
        long duration = System.currentTimeMillis() - startTime;
        System.out.println("Completed " + completed + " tasks in " + 
                         duration + " ms");
        System.out.println("Average time per task: " + 
                         (duration / completed) + " ms\n");
    }
    
    /**
     * Test 2: CompletableFuture for async operations
     */
    private static void testCompletableFuture() throws Exception {
        System.out.println("Test 2: CompletableFuture Chains");
        long startTime = System.currentTimeMillis();
        
        // Create async pipeline
        CompletableFuture<String> future = CompletableFuture
            .supplyAsync(() -> {
                sleep(50);
                return "User Data";
            })
            .thenApplyAsync(userData -> {
                sleep(50);
                return userData + " -> Processed";
            })
            .thenApplyAsync(processed -> {
                sleep(50);
                return processed + " -> Validated";
            })
            .thenApply(validated -> {
                return validated + " -> Complete";
            });
        
        String result = future.get();
        long duration = System.currentTimeMillis() - startTime;
        
        System.out.println("Result: " + result);
        System.out.println("Duration: " + duration + " ms\n");
    }
    
    /**
     * Test 3: ForkJoinPool for parallel computation
     */
    @SuppressWarnings("serial")
	private static void testForkJoinPool() {
        System.out.println("Test 3: ForkJoinPool Computation");
        long startTime = System.currentTimeMillis();
        
        try (ForkJoinPool pool = new ForkJoinPool()) {
			// Parallel computation task
			RecursiveTask<Long> task = new RecursiveTask<Long>() {
			    @Override
			    protected Long compute() {
			        long sum = 0;
			        for (int i = 0; i < 1_000_000; i++) {
			            sum += i;
			        }
			        return sum;
			    }
			};
			
			Long result = pool.invoke(task);
			long duration = System.currentTimeMillis() - startTime;
			
			System.out.println("Computation result: " + result);
			System.out.println("Duration: " + duration + " ms\n");
			
			pool.shutdown();
		}
    }
    
    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
