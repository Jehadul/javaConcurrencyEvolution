# **Java Concurrency Evolution**

This repository demonstrates the evolution of **Java
concurrency**---from traditional thread-based programming to modern
structured concurrency and virtual threads introduced in recent Java
versions. It includes hands-on examples, benchmarks, and comparative
analyses to help you understand how concurrency models have improved
over time.

## **📁 Project Structure**

    java-concurrency-evolution/
    ├── traditional/
    │   ├── TraditionalConcurrencyDemo.java
    │   ├── TraditionalIOBenchmark.java
    ├── modern/
    │   ├── ModernConcurrencyDemo.java
    │   ├── ModernIOBenchmark.java
    │   ├── StructuredConcurrencyAdvanced.java
    ├── comparison/
    │   ├── ConcurrencyPerformanceComparison.java
    └── README.md

## **📌 Overview**

### **1. Traditional Concurrency (Thread-Based Model)**

Located in `traditional/`.

-   Classic `Thread` and `Runnable` usage\
-   Thread pools using `ExecutorService`\
-   Blocking I/O simulations\
-   High memory usage & context switching challenges

### **2. Modern Concurrency (Virtual Threads & Structured Concurrency)**

Located in `modern/`.

-   Virtual Threads (Project Loom, Java 21+)\
-   Structured Concurrency APIs\
-   Scalable concurrency with lightweight threads

### **3. Performance Comparison**

Located in `comparison/`.

-   Benchmarks\
-   CPU-bound vs I/O-bound testing\
-   Result comparison

## **🛠 Requirements**

-   Java 21+

## **📄 License**

Open-source for learning and experimentation.
