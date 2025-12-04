package traditional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * I/O-bound workload simulation using traditional threads
 * Simulates HTTP server handling concurrent requests
 */
public class TraditionalIOBenchmark {
    
    private static final int REQUEST_COUNT = 10_000;
    private static final int THREAD_POOL_SIZE = 200;
    
    public static void main(String[] args) throws Exception {
        System.out.println("=== Traditional I/O Benchmark (Java 8) ===");
        System.out.println("Requests: " + REQUEST_COUNT);
        System.out.println("Thread Pool Size: " + THREAD_POOL_SIZE);
        System.out.println();
        
        runBenchmark();
    }
    
    private static void runBenchmark() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(
            THREAD_POOL_SIZE
        );
        
        AtomicInteger completedRequests = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(REQUEST_COUNT);
        
        long startTime = System.nanoTime();
        
        // Submit all requests
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
        long endTime = System.nanoTime();
        
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);
        
        // Calculate metrics
        double durationSeconds = (endTime - startTime) / 1_000_000_000.0;
        double throughput = REQUEST_COUNT / durationSeconds;
        
        System.out.println("Results:");
        System.out.println("  Total Duration: " + 
                         String.format("%.2f", durationSeconds) + " seconds");
        System.out.println("  Throughput: " + 
                         String.format("%.2f", throughput) + " requests/sec");
        System.out.println("  Completed: " + completedRequests.get());
        
        // Memory info
        Runtime runtime = Runtime.getRuntime();
        long memoryUsed = (runtime.totalMemory() - runtime.freeMemory()) 
                         / (1024 * 1024);
        System.out.println("  Memory Used: " + memoryUsed + " MB");
    }
    
    /**
     * Simulates handling an HTTP request with database query
     */
    private static void handleRequest(int requestId) {
        try {
            // Simulate network I/O (50ms)
            Thread.sleep(50);
            
            // Simulate database query (30ms)
            Thread.sleep(30);
            
            // Simulate response processing (20ms)
            Thread.sleep(20);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
