主要参考的是Guide哥的网站（https://javaguide.cn/），做一个学习笔记

学习一个东西的3要素

#### 1. 它是什么
#### 2. 为什么要用它
#### 3. 怎么用它

### 1、线程池简介介绍：

线程池是一种多线程的处理方式，管理了多个线程的资源的一个池子，简单的举个例子来说，比如说你要处理一个集合，这个集合数据很多大小为10000，执行一次的话，需要很长时间例如说10秒左右，那么可以同等的拆分为容量相同的几个，这里为了举例方便我们拆分为10个吧，然后用线程池去跑，之前10秒才可以执行，现在大概需要1秒左右。

### 2、为什么要用到它？

刚才上面列举了一个列子，如果说一次执行时间很耗时的话，可以拆分一下，利用线程池去跑，提升效率

* 降低资源消耗：通过重复利用已创建的线程，降低线程创建和销毁造成的消耗
* 提高响应速度：当任务到达时，可以不需要等待线程创建就能立即执行
* 提高线程的可管理性：线程是稀缺的资源，如果无限制的创建，不仅会消耗系统资源，还会降低系统的稳定性。使用线程池可以进行统一的分配，监控和调优，提高系统的稳定性
* 避免大量线程造成的阻塞和上下文切换问题，提高系统的并发能力

### 3、该如何用？

<img alt="image" src="https://github.com/leguanYa/juc/blob/master/img/9091694081897_.pic.jpg?raw=true" width="600">

这个类图中重要的就是是ThreadPoolExecutor，ScheduledThreadPoolExecutor这个线程池也是继承的ThreadPoolExecutor

我们来看ThreadPoolExecutor，看使用参数最多的一个构造函数

```java
    public ThreadPoolExecutor(int corePoolSize, // 核心线程数量
                              int maximumPoolSize,// 最大线程数量
                              long keepAliveTime, // 存活时间，当线程数大于核心线程数时，多的空余线程存活的时间
                              TimeUnit unit, // 时间单位
                              BlockingQueue<Runnable> workQueue,// 队列，存放任务的
                              ThreadFactory threadFactory, // 线程工厂
                              RejectedExecutionHandler handler // 拒绝策略
                             ) {
        if (corePoolSize < 0 ||
            maximumPoolSize <= 0 ||
            maximumPoolSize < corePoolSize ||
            keepAliveTime < 0)
            throw new IllegalArgumentException();
        if (workQueue == null || threadFactory == null || handler == null)
            throw new NullPointerException();
        this.acc = System.getSecurityManager() == null ?
                null :
                AccessController.getContext();
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.workQueue = workQueue;
        this.keepAliveTime = unit.toNanos(keepAliveTime);
        this.threadFactory = threadFactory;
        this.handler = handler;
    }
```

corePoolSize：当任务没有到达队列最大容量是，可以同时处理的线程数

maximumPoolSize：当任务队列达到了最大容量时，最多同时处理的线程数

workQueue：任务队列，新来执行的任务，会先判断是否达到了核心线程数，如果达到了，会先往队列里面扔

keepAliveTime（保持活动时间）：线程池中的线程数量大于corePoolSize的时候，如果这时候没有新的任务提交的话，核心线程外的线程不会立即销毁，而是会等待，直到等待的时间超过了定义的时间，才会被回收销毁。避免无限制的增加线程数。

threadFactory：线程工厂，创建新线程的时候回用到

handler：拒绝策略

拒绝策略定义：

如果当前同时运行的线程数量达到最大线程数量并且队列也已经被放满了任务时，定义的一些策略：

* `ThreadPoolExecutor.AbortPolicy`：抛出 `RejectedExecutionException`来拒绝新任务的处理。
* `ThreadPoolExecutor.CallerRunsPolicy`：调用执行自己的线程运行任务，也就是直接在调用`execute`方法的线程中运行(`run`)被拒绝的任务，如果执行程序已关闭，则会丢弃该任务。因此这种策略会降低对于新任务提交速度，影响程序的整体性能。如果您的应用程序可以承受此延迟并且你要求任何一个任务请求都要被执行的话，你可以选择这个策略。
* `ThreadPoolExecutor.DiscardPolicy`：不处理新任务，直接丢弃掉。
* `ThreadPoolExecutor.DiscardOldestPolicy`：此策略将丢弃最早的未处理的任务请求。

线程池创建的两种方式：

方式一： 通过**`ThreadPoolExecutor`**构造函数来创建（推荐）

方式二：通过Executor提供的Executors工具类创建（不推荐）

* Executors.newCachedThreadPool()：该方法返回一个可根据实际情况调整线程数量的线程池。线程池的线程数量不确定，但若有空闲线程可以复用，则会优先使用可复用的线程。若所有线程均在工作，又有新的任务提交，则会创建新的线程处理任务。所有线程在当前任务执行完毕后，将返回线程池进行复用。
* Executors.newFixedThreadPool(10)：该方法返回一个固定线程数量的线程池。该线程池中的线程数量始终不变。当有一个新的任务提交时，线程池中若有空闲线程，则立即执行。若没有，则新的任务会被暂存在一个任务队列中，待有线程空闲时，便处理在任务队列中的任务。
* Executors.newSingleThreadExecutor()：该方法返回一个只有一个线程的线程池。若多余一个任务被提交到该线程池，任务会被保存在一个任务队列中，待线程空闲，按先入先出的顺序执行队列中的任务。
* Executors.newScheduledThreadPool(1)：该返回一个用来在给定的延迟后运行任务或者定期执行任务的线程池。

方式二的几种弊端：

* newFixedThreadPool与newSingleThreadExecutor都是使用的无界队列LinkedBlockingQueue，默认队列长度都是为Integer.MAX_VALUE，可能会堆积大量请求，从而导致OOM
* newCachedThreadPool是使用的同步队列SynchronousQueue， 允许创建的线程数量为 `Integer.MAX_VALUE` ，可能会创建大量线程，从而导致 OOM。还有最大的线程数量maximumPoolSize给的是Integer.MAX_VALUE，
* **`ScheduledThreadPool` 和 `SingleThreadScheduledExecutor`** : 使用的无界的延迟阻塞队列`DelayedWorkQueue`，任务队列最大长度为 `Integer.MAX_VALUE`,可能堆积大量的请求，从而导致 OOM。

