前言，参考Guide哥的JavaGuide的做一个学习总结[链接](https://javaguide.cn/java/concurrent/java-concurrent-questions-03.html#aqs-%E7%9A%84%E5%8E%9F%E7%90%86%E6%98%AF%E4%BB%80%E4%B9%88)
# Semaphore
## 什么是Semaphore？
`Semaphore`俗称信号量，也是基于AQS（`AbstractQueuedSynchronizer`）实现的，`synchronized`与`ReentrantLock`都是一次只允许一个线程访问某个资源，而`Semaphore`（信号量）可以用来控制同时访问特定资源的线程数量。
`Semaphore`的使用简单，假设有N(N>5)个线程来获取`Semaphore`中的共享资源，下面代码表示，同一时刻N个线程中只有5个线程能够获取到锁，其他线程都会阻塞，只有获取到共享资源的线程才能执行，等到有线程释放了共享的资源，其他的阻塞的线程才能获取到。
```java
// 初始共享资源数量
final Semaphore semaphore = new Semaphore(5);
// 获取1个许可
semaphore.acquire();
// 释放1个许可
semaphore.release();
```
**注意：如果大小为1的信号量为互斥锁**
`Semaphore`有两种模式：

- 公平模式：调用`acquire`方法的时候获得许可证的顺序，遵循FIFO，先进先出
- 非公平模式：抢占式，

`Semaphore`对应的两个构造方法：
```java
//默认使用的非公平的
public Semaphore(int permits) {
    sync = new NonfairSync(permits);
}

// 根据fair传入的结果是true还是false决定采用公平还是非公平
public Semaphore(int permits, boolean fair) {
    sync = fair ? new FairSync(permits) : new NonfairSync(permits);
}
```
这两个构造方法中，信号量这个值都是必须要传的，
常用的方法：
```java
public void acquire() throws InterruptedException
public boolean tryAcquire()
public void release()
public int availablePermits()
public final int getQueueLength()
public final boolean hasQueuedThreads()
protected void reducePermits(int reduction)
protected Collection<Thread> getQueuedThreads()
```

- acquire：这个代表阻塞并获取许可
- tryAcquire：代表在没有获取到许可的情况下会返回false，获取成功许可的线程不会阻塞线程
- release：表示释放许可
- availablePermits：返回此信号量中当前可用的许可证的数量
- getQueueLength：返回正在等待获取许可证的线程数
- hasQueuedThreads：表示是否有线程正在等待获取许可证
- reducePermits：减少reduction个许可证
- getQueuedThreads：返回所有等待获取许可证的线程集合
##  Semaphore的原理是什么？
`Semaphore`是共享锁的一种实现，默认构造AQS的`state`值为`permits`，可以将`permits`的值理解为许可证的数量，只有拿到许可证的线程才可以执行。尝试获取许可证的时候，如果state>=0的话，则表示可以获取成功，如果获取成功的话使用CAS操作去

- 公平的实现是如果有排队的线程，那么直接返回-1，乖乖的去排队，如果没有排队的线程，获取state，将state减去你要获得的许可证的数量（一般默认是1），得到一个剩余的许可证数量，如果小于0直接返回，不小于0那么CAS进行修改成功后返回这剩余的许可证数量
- 非公平的实现是取state，将state减去你要获得的许可证的数量（一般默认是1），得到一个剩余的许可证数量，如果小于0直接返回，不小于0那么CAS进行修改成功后返回这剩余的许可证数量

## Semaphore的使用的场景都有哪些？
可以用它来做限流，做流量控制，特别是公共资源有限的应用场景
```java
private static final Semaphore semaphore = new Semaphore(5);

private static final ThreadPoolExecutor pool = new ThreadPoolExecutor(10, 50, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(50));


public static void exec() {
    try {
        semaphore.acquire();
        System.out.println("执行到exec方法");
        Thread.sleep(2000);
    } catch (InterruptedException e) {
        throw new RuntimeException(e);
    }finally {
        semaphore.release();
    }
}
public static void main(String[] args) throws InterruptedException {
    for (; ;) {
        Thread.sleep(200);
        pool.execute(SemaphoreTest::exec);
    }

}
```

## Semaphore的acquire()与release()原理解析
测试代码如下：
```java
private static final Semaphore semaphore = new Semaphore(3);

public static void main(String[] args) {
    for (int i = 0; i < 5; i++) {
        new Thread(() -> {
            try {
                semaphore.acquire();
                System.out.println(Thread.currentThread().getName()+"执行业务逻辑");
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }finally {
                semaphore.release();
            }
        }, "thread"+i).start();
    }
}
```
我们首先看acquire()方法处理：
```java
public void acquire() throws InterruptedException {
	sync.acquireSharedInterruptibly(1);
}
```
```java
public final void acquireSharedInterruptibly(int arg) throws InterruptedException {
    if (Thread.interrupted())
        throw new InterruptedException();
    // tryAcquireShared方法返回的是剩余许可证的数量，如果剩余许可证的数量为负数，则去排队
    if (tryAcquireShared(arg) < 0)
        doAcquireSharedInterruptibly(arg);
}
```
 tryAcquireShared这个方法有公平和非公平都有重写了AQS的这个方法，
![image.png](https://cdn.nlark.com/yuque/0/2024/png/26026237/1704208544150-05f76d8e-0ea3-48f5-940f-1f648b6994a1.png#averageHue=%23416a7a&clientId=ud4b53d7f-35a1-4&from=paste&height=204&id=u5092fdf3&originHeight=408&originWidth=1720&originalType=binary&ratio=2&rotation=0&showTitle=false&size=194235&status=done&style=none&taskId=u464bc91c-8d56-45b5-be82-9be26d18496&title=&width=860)
首先看下公平的实现方式：
![image.png](https://cdn.nlark.com/yuque/0/2024/png/26026237/1704208655964-461dab8e-66db-4012-aad1-90689092a262.png#averageHue=%23212326&clientId=ud4b53d7f-35a1-4&from=paste&height=490&id=u98cc30ce&originHeight=980&originWidth=1386&originalType=binary&ratio=2&rotation=0&showTitle=false&size=147612&status=done&style=none&taskId=ud19324b2-f8e4-4c66-8ef7-cfbf432ae4e&title=&width=693)
公平的实现是如果有排队的线程，那么直接返回-1，乖乖的去排队，如果没有排队的线程，获取state，将state减去你要获得的许可证的数量（一般默认是1），得到一个剩余的许可证数量，如果小于0直接返回，不小于0那么CAS进行修改成功后返回这剩余的许可证数量
其次看下非公平的实现：
![image.png](https://cdn.nlark.com/yuque/0/2024/png/26026237/1704208888326-3020eff7-e0d6-430a-910d-2a97d99fe89e.png#averageHue=%23202225&clientId=ud4b53d7f-35a1-4&from=paste&height=928&id=u972dbdb1&originHeight=1856&originWidth=1826&originalType=binary&ratio=2&rotation=0&showTitle=false&size=331500&status=done&style=none&taskId=u6ecbac82-69ad-4fb7-8b51-4ac88384178&title=&width=913)
可以看到，非公平的实现是取state，将state减去你要获得的许可证的数量（一般默认是1），得到一个剩余的许可证数量，如果小于0直接返回，不小于0那么CAS进行修改成功后返回这剩余的许可证数量。
接下来我们来看排队的方法，doAcquireSharedInterruptibly（java.util.concurrent.locks.AbstractQueuedSynchronizer#doAcquireSharedInterruptibly）
```java
private void doAcquireSharedInterruptibly(int arg) throws InterruptedException {
    // 添加等待者为共享模式的node
    final Node node = addWaiter(Node.SHARED);
    boolean failed = true;
    try {
        for (;;) {
            //predecessor这个方法是获得当前线程的node节点
            final Node p = node.predecessor();
            if (p == head) {
                // 如果没有获取到许可证不会执行if (r >= 0)里面的逻辑
                int r = tryAcquireShared(arg);
                if (r >= 0) {
                    setHeadAndPropagate(node, r);
                    p.next = null; // help GC
                    failed = false;
                    return;
                }
            }
            // shouldParkAfterFailedAcquire是：
            // 修改当前node的前继节点的waitStatus为-1，因为一开始默认值为0，
            // 那么到了第二次循环的时候，node的前继节点的waitStatus为-1，为true，
            // 执行parkAndCheckInterrupt方法，进行阻塞线程
            if (shouldParkAfterFailedAcquire(p, node) &&
                parkAndCheckInterrupt())
                throw new InterruptedException();
        }
    } finally {
        if (failed)
            cancelAcquire(node);
    }
}


// 添加队列等待者
private Node addWaiter(Node mode) {
	//创建一个Node，线程为当前线程nextWaiter（下个等待为共享模式的node）
    Node node = new Node(Thread.currentThread(), mode);
    // 一开始tail为空，执行不了if (pred != null) 的逻辑
    Node pred = tail;
    if (pred != null) {
        node.prev = pred;
        if (compareAndSetTail(pred, node)) {
            pred.next = node;
            return node;
        }
    }
	//执行将节点插入队列中方法
    enq(node);
    return node;
}

//java.util.concurrent.locks.AbstractQueuedSynchronizer.Node#Node(java.lang.Thread, java.util.concurrent.locks.AbstractQueuedSynchronizer.Node)
Node(Thread thread, Node mode) {     // Used by addWaiter
    this.nextWaiter = mode;
    this.thread = thread;
}

// 将节点插入队列，一开始需要初始化操作
private Node enq(final Node node) {
    // 如果说要进行初始化，那么这个循环肯定会执行两遍
    for (;;) {
        Node t = tail;
        if (t == null) { // Must initialize
            if (compareAndSetHead(new Node()))
                tail = head;
        } else {
            node.prev = t;
            if (compareAndSetTail(t, node)) {
                t.next = node;
                return t;
            }
        }
    }
    
}
```
 

# 什么是CountDownLatch?
`CountDownLatch`(闭锁)是一个同步协助器，允许一个或多个线程等待，知道其他线程完成操作集
`CountDownLatch`使用给定的计数值`count`初始化，`await`方法会阻塞直到当前的计数值`count`由于`countDown`方法的调用达到0，`count`为0之后所有等待的线程都会被释放，并且随后对`await`方法的调用都会立即返回。这是个一次性现象，count不会被重置，当`CountDownLatch`使用后，不能再次被使用
`CountDownLatch`的原理：
`CountDownLatch`是共享锁的一种实现，他默认构造AQS的`state`值为`count`，当线程使用`countDown`方法时，其实使用了`tryReleaseShared`方法以CAS的操作来减少state，直至state为0，当调用`await()`方法的时候，如果state不为0，证明任务还没有完成，会一直阻塞，也就是`await`后面的代码不会被执行，知道count个线程调用了`countDown`使state的值被减为0，或者调用`await`的线程被中断，该线程才会从阻塞中被唤醒，`await`方法后面的语句才可被执行
使用场景：

- 让多个线程等待：模拟并发，让并发线程一起执行
测试代码：
```java
public static void main(String[] args) throws InterruptedException {
    CountDownLatch countDownLatch = new CountDownLatch(1);

    for (int i = 0; i < 5; i++) {
        new Thread(() -> {
            try {
                countDownLatch.await();
                System.out.println(Thread.currentThread().getName()+"开始执行");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, "thread"+i).start();
    }
    Thread.sleep(3000);
    countDownLatch.countDown();
}
```

- 让单个线程等待：多个线程（任务）完成后，进行汇总合并
测试代码为：

```java
private static AtomicInteger sum = new AtomicInteger();

public static void main(String[] args) throws InterruptedException {
    CountDownLatch countDownLatch = new CountDownLatch(5);
    long start = System.currentTimeMillis();
    for (int i = 0; i < 5; i++) {
        new Thread(() -> {
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            sum.incrementAndGet();
            countDownLatch.countDown();
        }, "thread" + i).start();
    }
    countDownLatch.await();
    long end = System.currentTimeMillis();
    System.out.println("主线程:在所有任务运行完成后，进行结果sum汇总:" + sum + "执行耗时为：" + (end - start) + "ms");
}
//控制台输出结果
//主线程:在所有任务运行完成后，进行结果sum汇总:5执行耗时为：4046ms
```
 
# 什么是CyclicBarrier？
## 介绍
`CyclicBarrier`是回环栅栏（也叫循环屏障），通过它可以实现让一组线程等待至某个状态（屏障点）之后在全部执行，这点和CountDownLatch类似，但是它比`CountDownLatch`功能更加复杂和丰富，同时叫做回环的是因为当所有等待线程都被释放后，因为`CyclicBarrier`可以被重用
> 值得注意的是：`CountDownLatch`是基于AQS实现的，而`CyclicBarrier`是基于`ReentrantLock`+`Condition`的

## CyclicBarrier的原理是什么？
`CyclicBarrier`内部通过一个count变量作为计数器，`count`的初始值为`parties`属性的初始化的值，每当一个线程到达了栅栏这里，那么就将计数器减1，如果`count`的值不为0那么将添加到条件队列中（单向链表的结构），然后将其阻塞；如果为0，那么会先执行初始化`CyclicBarrier`的时候是否指定了可运行的方法（`Runnable`），其次，进行将条件队列转换为等待队列的数据，然后将其唤醒。
CyclicBarrier的两个构造函数
```java
//每次拦截的线程数
private final int parties;
//计数器
private int count;

//parties每次拦截的数量
//barrierAction是到达屏障（count为0）的时候，优先去执行的一个方法
public CyclicBarrier(int parties, Runnable barrierAction) {
    if (parties <= 0) throw new IllegalArgumentException();
    this.parties = parties;
    this.count = parties;
    this.barrierCommand = barrierAction;
}

public CyclicBarrier(int parties) {
	this(parties, null);
}

```
 `CyclicBarrier`源码解析
测试代码如下：
```java
public class CyclicBarrierTest {

    private static AtomicInteger c = new AtomicInteger();

    public static void main(String[] args) throws InterruptedException {

        CyclicBarrier cyclicBarrier = new CyclicBarrier(3, () -> {
            BarrierActionTest tel = new BarrierActionTest();
            tel.start();
        });


        HashMap<Integer, Integer> map = new HashMap<>();
        for (int i = 0; i < 6; i++) {
            final int ii = i;
            new Thread(() -> {
                map.put(ii, ii + 1);
                try {
                    System.out.println(Thread.currentThread().getName() + "开始阻塞了");
                    //阻塞
                    cyclicBarrier.await();
                    System.out.println(Thread.currentThread().getName() + "开始被唤醒了");
                } catch (InterruptedException | BrokenBarrierException e) {
                    throw new RuntimeException(e);
                }
            }, "thread" + i).start();
        }
        Thread.sleep(4000);
        System.out.println(map);
    }

    static class BarrierActionTest extends Thread {
        @Override
        public void run() {
            System.out.println("唤醒后执行了第"+c.get()+"次唤醒方法");
            c.incrementAndGet();
        }
    }
}
```
 接下来我们看下await方法：
```java
public int await() throws InterruptedException, BrokenBarrierException {
    try {
        // 调用了dowait方法
        return dowait(false, 0L);
    } catch (TimeoutException toe) {
        throw new Error(toe); // cannot happen
    }
}



private int dowait(boolean timed, long nanos)
    throws InterruptedException, BrokenBarrierException,
           TimeoutException {
    // 这里也是用的ReentrantLock
    final ReentrantLock lock = this.lock;
    // 进行加锁
    lock.lock();
    try {
        final Generation g = generation;

        if (g.broken)
            throw new BrokenBarrierException();
    	// 线程中断，抛出异常
        if (Thread.interrupted()) {
            breakBarrier();
            throw new InterruptedException();
        }
    	// count减1
        int index = --count;
        // 如果达到了栅栏后执行下面的逻辑
        if (index == 0) {  // tripped
            boolean ranAction = false;
            try {
                //执行初始化CyclicBarrier指定的Runnable
                final Runnable command = barrierCommand;
                if (command != null)
                    command.run();
                ranAction = true;
                //这个里面做的就是唤醒阻塞的线程，
                //将条件队列转换为入队阻塞队列
                //将count重置为parties
                nextGeneration();
                return 0;
            } finally {
                if (!ranAction)
                    breakBarrier();
            }
        }

        // loop until tripped, broken, interrupted, or timed out
        for (;;) {
            try {
                if (!timed)
                    // 进行等待，阻塞线程
                    trip.await();
                else if (nanos > 0L)
                    nanos = trip.awaitNanos(nanos);
            } catch (InterruptedException ie) {
                if (g == generation && ! g.broken) {
                    breakBarrier();
                    throw ie;
                } else {
                    // We're about to finish waiting even if we had not
                    // been interrupted, so this interrupt is deemed to
                    // "belong" to subsequent execution.
                    Thread.currentThread().interrupt();
                }
            }

            if (g.broken)
                throw new BrokenBarrierException();

            if (g != generation)
                return index;

            if (timed && nanos <= 0L) {
                breakBarrier();
                throw new TimeoutException();
            }
        }
    } finally {
        //删除锁
        lock.unlock();
    }
}

```
 
