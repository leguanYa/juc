# AQS是什么？
java.util.concurrent包中的大多数同步器实现都是围绕着共同的基础行为，比如等待队列、条件队列、独占获取、共享获取等，而这些行为的抽象就是基于AbstractQueuedSynchronizer（简称AQS）实现的，AQS是一个抽象同步框架，可以用来实现一个依赖状态的同步器。 因此使用AQS能简单高效的构造出应用广泛的大量的同步器，比如ReentrantLock，Semaphore，其他的诸如ReentrantReadWriteLock、SynchronousQueue等等皆是基于AQS实现的

- 一般都是通过一个内部类Sync继承于AQS
- 奖同步器所有调用都映射到Sync对应的方法

AQS的原理是什么：
AQS的核心思想是，如果被请求的共享资源为空闲，则将当前线程置为有效线程，并且将公共资源设置为锁定状态，如果当前请求的共享资源为锁定状态，那么就需要一套线程阻塞等待以及被唤醒时锁分配的机制，这个机制AQS时用CLH队列锁实现的，即将暂时获取不到锁的线程加入到队列中
AQS具备的特性：

- 阻塞等待队列
- 共享/独占
- 公平/非公平
- 可重入
- 允许中断

AQS内部维护属性volatile int state

- state表示资源的可用状态

State三种访问方式：

- getState（）
- setState（）
- compareAndSetState（）

AQS定义两种资源共享方式

- Exclusive-独占，只有一个线程能执行，如ReentrantLock 
- Share-共享，多个线程可以同时执行，如Semaphore/CountDownLatch

AQS定义两种队列

- 同步等待队列：主要用于维护获取锁时入队的线程
- 条件等待队列：调用await()的时候会释放锁，然后线程会加入到条件队列，调用 signal()唤醒的时候会把条件队列中的线程节点移动到同步队列中，等待再次获得锁

AQS中定义了五个队列中节点状态（state变量）：

- 值为0，初始化状态，表示当前节点在sync队列中，等待着获取锁
- CANCELLED，值为1，表示当前的线程被取消；
- SIGNAL，值为-1，表示当前节点的后继节点包含的线程需要运行，也就是unpark；
- CONDITION，值为-2，表示当前节点在等待condition，也就是在condition队列 中；
- PROPAGATE，值为-3，表示当前场景下后续的acquireShared能够得以执行；

不同的自定义同步器竞争共享资源的方式也不同。自定义同步器在实现时只需要实现共享 资源state的获取与释放方式即可，至于具体线程等待队列的维护（如获取资源失败入队/唤醒出 队等），AQS已经在顶层实现好了。自定义同步器实现时主要实现以下几种方法：

- isHeldExclusively()：该线程是否正在独占资源。只有用到condition才需要去实现 它
- tryAcquire(int)：独占方式。尝试获取资源，成功则返回true，失败则返回false。
- tryRelease(int)：独占方式。尝试释放资源，成功则返回true，失败则返回false。
- tryAcquireShared(int)：共享方式。尝试获取资源。负数表示失败；0表示成功，但 没有剩余可用资源；正数表示成功，且有剩余资源
- tryReleaseShared(int)：共享方式。尝试释放资源，如果释放后允许唤醒后续等待 结点返回true，否则返回false。

同步等待队列
AQS当中的同步等待队列也称CLH队列，是FIFO先进先出线程等待队列，Java中的CLH队列是原 CLH队列的一个变种,线程由原自旋机制改为阻塞机制
是FIFO先进先出线程等待队列，Java中的CLH队列是原 CLH队列的一个变种,线程由原自旋机制改为阻塞机制

- 当前线程如果获取同步状态失败时，AQS则会将当前线程已经等待状态等信息构造 成一个节点（Node）并将其加入到CLH同步队列，同时会阻塞当前线程
- 当同步状态释放时，会把首节点唤醒（公平锁），使其再次尝试获取同步状态
- 通过signal或signalAll将条件队列中的节点转移到同步队列。（由条件队列转化为同步队列）

![image.png](https://cdn.nlark.com/yuque/0/2023/png/26026237/1703843500745-ca83119e-cf67-47d5-a1c9-b8d8b5e2301f.png#averageHue=%23fbfbfb&clientId=uf0332604-debf-4&from=paste&height=1408&id=u0304da48&originHeight=1408&originWidth=2432&originalType=binary&ratio=1&rotation=0&showTitle=false&size=147444&status=done&style=none&taskId=u010dcfc6-522d-4e2c-9d45-8e1e5d7beae&title=&width=2432)
条件等待队列

- AQS中条件队列是使用单向列表保存的，用nextWaiter来连接:
- 调用await方法阻塞线程； 当前线程存在于同步队列的头结点，调用await方法进行阻塞（从同步队列转化到条 件队列）

Condition接口详解
![image.png](https://cdn.nlark.com/yuque/0/2023/png/26026237/1703843881454-c5228051-bb85-4b67-8dee-ead94826ddbc.png#averageHue=%2330343c&clientId=uf0332604-debf-4&from=paste&height=428&id=u889de796&originHeight=428&originWidth=1076&originalType=binary&ratio=1&rotation=0&showTitle=false&size=68478&status=done&style=none&taskId=u92a1b6a1-7495-4d0d-aaa8-3eff745b399&title=&width=1076)

1. 调用Condition#await方法会释放当前持有的锁，然后阻塞当前线程，同时向 Condition队列尾部添加一个节点，所以调用Condition#await方法的时候必须持有锁。
2. 调用Condition#signal方法会将Condition队列的首节点移动到阻塞队列尾部，然后唤 醒因调用Condition#await方法而阻塞的线程(唤醒之后这个线程就可以去竞争锁了)，所 以调用Condition#signal方法的时候必须持有锁，持有锁的线程唤醒被因调用 Condition#await方法而阻塞的线程。

等待唤醒机制之await/signal测试
```java
public static void main(String[] args) {
    ReentrantLock lock = new ReentrantLock();
    Condition condition = lock.newCondition();

    new Thread(() -> {
        lock.lock();
        String name = Thread.currentThread().getName();
        System.out.println(name+"开始处理任务");
        try {
            condition.await();
            System.out.println(name+"处理任务完成");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }, "thread1").start();
    

    new Thread(() -> {
        lock.lock();
        String name = Thread.currentThread().getName();
        System.out.println(name+"开始处理任务");
        try {
            Thread.sleep(2000);
            condition.signal();
            System.out.println(name+"处理任务完成");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }, "thread2").start();
}
```


ReentrantLock详解
ReetrantLock是一种基于AQS框架的应用实现，是JDK中的一种线程并发访问的同步手段，功能类似于synchronized是一种互斥锁，可以保证线程安全。
相对于synchronized，ReentrantLock具备如下特点：

- 可中断
- 可以设置超时时间
- 可以设置为公平锁
- 支持多个条件变量
- 与synchronized一样，都支持可重入

![image.png](https://cdn.nlark.com/yuque/0/2023/png/26026237/1703844616358-82e74270-42b1-482b-be6a-2dd925c6bdbc.png#averageHue=%231f2124&clientId=uf0332604-debf-4&from=paste&height=874&id=u9d2cdf5f&originHeight=874&originWidth=1604&originalType=binary&ratio=1&rotation=0&showTitle=false&size=160543&status=done&style=none&taskId=u88b84c14-eef9-4a14-87a9-f0f8105b0fe&title=&width=1604)
点synchronized和ReentrantLock的区别：

- synchronized是JVM层次的锁实现，ReentrantLock是JDK层次的锁实现；
- synchronized的锁状态是无法在代码中直接判断的，但是ReentrantLock可以通过 ReentrantLock#isLocked判断；
- synchronized是非公平锁，ReentrantLock是可以是公平也可以是非公平的；
- synchronized是不可以被中断的，而ReentrantLock#lockInterruptibly方法是可以 被中断的；
- 在发生异常时synchronized会自动释放锁，而ReentrantLock需要开发者在finally 块中显示释放锁；
- ReentrantLock获取锁的形式有多种：如立即返回是否成功的tryLock(),以及等待指 定时长的获取，更加灵活；
- synchronized在特定的情况下对于已经在等待的线程是后来的线程先获得锁，而ReentrantLock对于已经在等待的线程是先来的线程 先获得锁；

ReentrantLock加锁流程
测试代码：
```java
private static ReentrantLock lock = new ReentrantLock();
private static int sum = 0;

public static void main(String[] args) throws InterruptedException {
    for (int i = 0; i < 3; i++) {
        new Thread(() -> {
            lock.lock();
            try {
                for (int i1 = 0; i1 < 10000; i1++) {
                    sum++;
                }
            }finally {
                lock.unlock();
            }
        },"thread"+i).start();
    }
    Thread.sleep(2000);
    System.out.println(sum);
}
```
声明锁为非公平的锁，这里使用了三个线程去加锁，
我们先看线程0执行的过程：
java.util.concurrent.locks.ReentrantLock.NonfairSync#lock
![image.png](https://cdn.nlark.com/yuque/0/2024/png/26026237/1704188484296-098b9bc3-cf8f-45e6-bd12-ccaed96f3a38.png#averageHue=%2323272f&clientId=u75e23d14-75ce-4&from=paste&height=622&id=u045b8a1a&originHeight=622&originWidth=1420&originalType=binary&ratio=1&rotation=0&showTitle=false&size=95134&status=done&style=none&taskId=ubc231ab7-bd3a-4298-a1d3-87014cce763&title=&width=1420)
这里首先会进行CAS将state变量从0修改成为1，成功后设置线程的拥有者为当前线程，设置成功后进行返回



接下来我们将debug切换为线程1，执行lock方法，CAS失败，进入else逻辑执行java.util.concurrent.locks.AbstractQueuedSynchronizer#acquire方法
![image.png](https://cdn.nlark.com/yuque/0/2024/png/26026237/1704188646583-33832d2c-7e95-4536-b39f-7e90aff1e2c4.png#averageHue=%23242b37&clientId=u75e23d14-75ce-4&from=paste&height=376&id=u3c405b27&originHeight=376&originWidth=1350&originalType=binary&ratio=1&rotation=0&showTitle=false&size=59429&status=done&style=none&taskId=u6862c089-5c3b-41e5-b508-3e405fdcaf7&title=&width=1350)
![image.png](https://cdn.nlark.com/yuque/0/2024/png/26026237/1704188700942-a337d966-063a-48b8-bf3c-db6d6c3fa50d.png#averageHue=%23222328&clientId=u75e23d14-75ce-4&from=paste&height=374&id=u1292c123&originHeight=374&originWidth=1566&originalType=binary&ratio=1&rotation=0&showTitle=false&size=66475&status=done&style=none&taskId=uabd9d3f6-cf21-4381-b258-39ad6d13eb1&title=&width=1566)
首先进行尝试获取
java.util.concurrent.locks.ReentrantLock.NonfairSync#tryAcquire
------>java.util.concurrent.locks.ReentrantLock.Sync#nonfairTryAcquire
![image.png](https://cdn.nlark.com/yuque/0/2024/png/26026237/1704188781626-50d14cf8-e889-4a05-941b-c747c5400641.png#averageHue=%23202124&clientId=u75e23d14-75ce-4&from=paste&height=376&id=u6f0b76b7&originHeight=376&originWidth=1354&originalType=binary&ratio=1&rotation=0&showTitle=false&size=45141&status=done&style=none&taskId=u3b7d5d80-30d5-4bc7-a39e-c59ff2a100b&title=&width=1354)
![image.png](https://cdn.nlark.com/yuque/0/2024/png/26026237/1704188797761-c5f12a05-1170-460e-8d1d-a6ad4a80efcc.png#averageHue=%23212226&clientId=u75e23d14-75ce-4&from=paste&height=1044&id=u259a5eb4&originHeight=1044&originWidth=1586&originalType=binary&ratio=1&rotation=0&showTitle=false&size=157522&status=done&style=none&taskId=u271fbd2b-6ad0-41b9-b199-e0e9d4d5a07&title=&width=1586)
执行nonfairTryAcquire方法获取state的值，发现不为0也不是重入操作，那么返回false
那么接下来执行java.util.concurrent.locks.AbstractQueuedSynchronizer#acquire方法中的
java.util.concurrent.locks.AbstractQueuedSynchronizer#addWaiter（添加队列等待者）
![image.png](https://cdn.nlark.com/yuque/0/2024/png/26026237/1704188973746-5629d480-c896-4776-a568-d026972249a8.png#averageHue=%2322252b&clientId=u75e23d14-75ce-4&from=paste&height=910&id=u0bf903d8&originHeight=910&originWidth=1410&originalType=binary&ratio=1&rotation=0&showTitle=false&size=147347&status=done&style=none&taskId=uf2ce85f8-c3e5-4558-970a-f0e8fd5d9ed&title=&width=1410)
创建一个Node节点为独占模式，这个时候tail为空执行enq方法（java.util.concurrent.locks.AbstractQueuedSynchronizer#enq）
![image.png](https://cdn.nlark.com/yuque/0/2024/png/26026237/1704189058188-5cc008e3-bfb8-4d95-b00a-c5143e7c0568.png#averageHue=%2321242b&clientId=u75e23d14-75ce-4&from=paste&height=876&id=u26f404cf&originHeight=876&originWidth=1582&originalType=binary&ratio=1&rotation=0&showTitle=false&size=112309&status=done&style=none&taskId=u8878699d-9141-4155-94f0-d13d5fc7889&title=&width=1582)
第一次循环执行，发现tail为空，执行CAS设置头节点，成功后再将tail置为head地址，此时的Node的结构为
![image.png](https://cdn.nlark.com/yuque/0/2024/png/26026237/1704189462851-b8b2f9f0-8647-4ff3-9a84-b22a29d082b6.png#averageHue=%23fafafa&clientId=u75e23d14-75ce-4&from=paste&height=230&id=u4125d68d&originHeight=230&originWidth=548&originalType=binary&ratio=1&rotation=0&showTitle=false&size=16842&status=done&style=none&taskId=u1dbca8eb-e071-4fab-a7dd-d2b9967aa71&title=&width=548)
那么紧接着执行第二次循环：
这个时候tail和head都是同一个地址
这个时候tail已经不为空了，执行else逻辑，将当前线程的node前节点指向为tail，然后在CAS设置当前线程的node数据为尾节点，设置成功后，将t的next指针指向当前线程的node，此时的双向链表的数据结构为
![image.png](https://cdn.nlark.com/yuque/0/2024/png/26026237/1704189966476-e167d998-8cb2-47c3-a672-eb21d22be18a.png#averageHue=%23f9f9f9&clientId=u75e23d14-75ce-4&from=paste&height=225&id=ua78b37a3&originHeight=225&originWidth=556&originalType=binary&ratio=1&rotation=0&showTitle=false&size=18266&status=done&style=none&taskId=ubc7cb9e5-5ba8-4848-9dea-ff48de2edca&title=&width=556)
然后返回node节点，跳出循环条件，然后执行java.util.concurrent.locks.AbstractQueuedSynchronizer#acquireQueued 方法
![image.png](https://cdn.nlark.com/yuque/0/2024/png/26026237/1704190075472-85684095-1e4d-4c87-a887-af60b929917f.png#averageHue=%23202228&clientId=u75e23d14-75ce-4&from=paste&height=1404&id=ube3542a3&originHeight=1404&originWidth=2034&originalType=binary&ratio=1&rotation=0&showTitle=false&size=254835&status=done&style=none&taskId=ucf6a75fc-c7d6-41a1-adfc-0fe8954c15e&title=&width=2034)
第一次进入循环体，final Node p = node.predecessor(); 这个方法是获取node的前节点，当前node的前节点是head，所以p==head条件为true，那么在继续执行tryAcquire方法，上面这个代码已经贴了，尝试获取返回的是false，
那么往下继续执行，执行shouldParkAfterFailedAcquire(p, node) （java.util.concurrent.locks.AbstractQueuedSynchronizer#shouldParkAfterFailedAcquire）
![image.png](https://cdn.nlark.com/yuque/0/2024/png/26026237/1704190411475-7c1b8ffc-18ab-4930-821e-abc81c2d7e35.png#averageHue=%231f2126&clientId=u75e23d14-75ce-4&from=paste&height=1772&id=u1651372b&originHeight=1772&originWidth=2938&originalType=binary&ratio=1&rotation=0&showTitle=false&size=369820&status=done&style=none&taskId=u5dd3132e-2af9-49cd-8c16-6bd5a70e217&title=&width=2938)
这个时候pred的waitStatus为0，执行else逻辑CAS修改waitStatus修改为-1（只有为-1的时候，代表这个节点的next节点才可以被唤醒），然后返回false，那么此时的结构为：
![image.png](https://cdn.nlark.com/yuque/0/2024/png/26026237/1704190637828-4cf6ce82-6d94-49cf-8a00-f337fd77c41a.png#averageHue=%23f4f4f4&clientId=u75e23d14-75ce-4&from=paste&height=187&id=u915258b3&originHeight=187&originWidth=488&originalType=binary&ratio=1&rotation=0&showTitle=false&size=20227&status=done&style=none&taskId=u75a3c916-6614-4a33-a970-699b02ee67c&title=&width=488)
那么在继续执行第二次循环，再继续执行shouldParkAfterFailedAcquire方法的时候，返回的是负一，执行后面的方法parkAndCheckInterrupt进行阻塞线程（java.util.concurrent.locks.AbstractQueuedSynchronizer#parkAndCheckInterrupt）
那么此时thread1被挂起


接下来来看thread2:
CAS设置state失败，进入acquire方法，进入addWaiter，此时tail已经不为null了，将thread2的node前节点设置为tail，这个时候tail为thread1，然后再将thread2 CAS设置为tail节点，再将tail的next设置为thread2，返回替换read2的node，此时的链表的结构为：
![image.png](https://cdn.nlark.com/yuque/0/2024/png/26026237/1704191111840-1eda4338-e219-4c2a-8212-fd1032789314.png#averageHue=%23f4f4f4&clientId=u75e23d14-75ce-4&from=paste&height=182&id=ub0903b60&originHeight=182&originWidth=725&originalType=binary&ratio=1&rotation=0&showTitle=false&size=23864&status=done&style=none&taskId=u22a7e229-ce1e-4475-862e-d9ea4c06777&title=&width=725)
进入acquireQueued方法
第一次循环发现thread2的前节点不为头节点，执行shouldParkAfterFailedAcquire方法，判断前节点的node的waitStatus，此时为0，通过CAS设置-1，返回false，此时的链表的数据结构为：
![image.png](https://cdn.nlark.com/yuque/0/2024/png/26026237/1704191340684-9430582d-003f-4aab-88c3-53c7e085c199.png#averageHue=%23f4f4f4&clientId=u75e23d14-75ce-4&from=paste&height=178&id=u15c48233&originHeight=178&originWidth=728&originalType=binary&ratio=1&rotation=0&showTitle=false&size=25187&status=done&style=none&taskId=u9d734177-fca8-4aa1-82fd-e3ea435d705&title=&width=728)
紧接着继续第二次循环，这个时候shouldParkAfterFailedAcquire返回为true，执行parkAndCheckInterrupt阻塞thread2，

那么此时thread0执行unlock()解锁方法java.util.concurrent.locks.AbstractQueuedSynchronizer#release
![image.png](https://cdn.nlark.com/yuque/0/2024/png/26026237/1704191587771-29bdaa0c-d0a7-408f-be2d-c2b6a9b8f432.png#averageHue=%2320232b&clientId=u75e23d14-75ce-4&from=paste&height=760&id=u1c7b796a&originHeight=760&originWidth=1740&originalType=binary&ratio=1&rotation=0&showTitle=false&size=146837&status=done&style=none&taskId=u85e61711-c4bf-4aed-93b6-52c044d2303&title=&width=1740)
这个里面首先进行尝试进行解锁java.util.concurrent.locks.ReentrantLock.Sync#tryRelease
![image.png](https://cdn.nlark.com/yuque/0/2024/png/26026237/1704191664594-ef2c1df0-d0e4-4e89-9733-98cd675fdc70.png#averageHue=%2323262e&clientId=u75e23d14-75ce-4&from=paste&height=686&id=u21db76f4&originHeight=686&originWidth=1432&originalType=binary&ratio=1&rotation=0&showTitle=false&size=112254&status=done&style=none&taskId=uf2ef0270-427a-4191-9c28-4b5827881ac&title=&width=1432)
这个里面进行设置state的值，因为ReentrantLock支持重入，所以这里每次进来都是减1，如果c等于0设置free状态为true，设置当前线程的拥有者为null，然后在设置state的值，释放完成之后返回true，
获取当前的head，发现不为null并且head的waitStatus也不为0，执行unparkSuccessor方法，来唤醒head节点的next的线程（java.util.concurrent.locks.AbstractQueuedSynchronizer#unparkSuccessor）
![image.png](https://cdn.nlark.com/yuque/0/2024/png/26026237/1704191943057-1b66cdc9-6e53-4aee-9956-ad68ec8a2e69.png#averageHue=%23202328&clientId=u75e23d14-75ce-4&from=paste&height=1544&id=u543c28ba&originHeight=1544&originWidth=1766&originalType=binary&ratio=1&rotation=0&showTitle=false&size=269004&status=done&style=none&taskId=u05a11cf9-3121-47be-928e-66ab7feab5f&title=&width=1766)
首先进来，CAS将head的waitStatus设置为0，此时的数据结构为：
![image.png](https://cdn.nlark.com/yuque/0/2024/png/26026237/1704192131696-5c6ba73b-19ef-439e-82c5-0c0ea4e17d43.png#averageHue=%23f5f5f5&clientId=u75e23d14-75ce-4&from=paste&height=189&id=ubf596a98&originHeight=189&originWidth=743&originalType=binary&ratio=1&rotation=0&showTitle=false&size=25642&status=done&style=none&taskId=u972be28f-4b70-4d14-a436-0a687c4f562&title=&width=743)
然后获取head的下一个节点，此时是thread1，执行LockSupport.unpark唤醒thread1

此时thread1被唤醒
![image.png](https://cdn.nlark.com/yuque/0/2024/png/26026237/1704192180964-194e7193-60bb-4f66-a464-6c07b4115332.png#averageHue=%2321242a&clientId=u75e23d14-75ce-4&from=paste&height=1158&id=u556e92d6&originHeight=1158&originWidth=1818&originalType=binary&ratio=1&rotation=0&showTitle=false&size=234896&status=done&style=none&taskId=u6cbf6f03-6c0f-4e64-8e0a-2e4478f6235&title=&width=1818)
进行尝试获取锁，因为thread0已经将waitStatus改为0了，此时尝试获取锁成功，设置head为thread1，thread1的前节点的next置为null，此时thread1的前节点的已经没有任何用了，等待被GC了，此时的数据结构为：
![image.png](https://cdn.nlark.com/yuque/0/2024/png/26026237/1704192492432-6b6fd432-8081-4593-a244-875d76221a4d.png#averageHue=%23f6f6f6&clientId=u75e23d14-75ce-4&from=paste&height=188&id=uddb3786c&originHeight=188&originWidth=783&originalType=binary&ratio=1&rotation=0&showTitle=false&size=27197&status=done&style=none&taskId=u6acdf278-746d-41cd-9eff-98ab712a2d0&title=&width=783)
后续的就是thread1，释放锁，唤醒thread2了
