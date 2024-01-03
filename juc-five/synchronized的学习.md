参考了Guide哥的synchronized [原文地址](https://javaguide.cn/java/concurrent/java-concurrent-questions-02.html#synchronized-%E5%85%B3%E9%94%AE%E5%AD%97)
带着几个疑问来学习synchronized

1. 这个玩意是啥
2. 为啥要用它
3. 怎么用它
4. 底层原理
# 这玩意是个啥子？
synchronized 是Java中一个关键字，主要解决的事多个线程之间访问资源的同步性，可以保证被它修饰方法或者代码块在任意时刻只能有一个线程执行
# 为啥用它
用它可以保证多线程的处理条件下，结果是正确的，比如下面这个简单的代码:
```java
public class Test {

    private static int count = 0;
    public static void add() {
        count++;
    }
    public static void sub(){
        count--;
    }
    public static void main(String[] args) throws InterruptedException {
        new Thread(()->{
            for (int i = 0; i < 50000; i++) {
                add();
            }
        }).start();
        new Thread(()->{
            for (int i = 0; i < 50000; i++) {
                sub();
            }
        }).start();
        Thread.sleep(1000);
        System.out.println(count);
    }
}
```
运行一下，这个代码输出的count存在不确定性，这个就是多个线程之间对共享资源进行写的时候，存在线程不安全。这个就是要用synchronized 来保证同步，当然也可以用其他的方式处理，比如JUC包下的一些处理方式。
# 怎么用它
## 使用方面：
例如上面的代码，我们可以将synchronized 加在add和sub的方法上面
`synchronized` 关键字使用方式主要有3个方面

- 修饰实例方法
- 修饰静态方法
- 修饰代码块
1.  修饰实例方法（锁当前对象实例）：
给当前对象实例加锁，进入同步代码前要获得**当前对象实例的锁** 
```java
synchronized void method() {
    //业务代码
}
```

2.  修饰静态方法（锁当前类）
给当前类加锁，会作用于类的所有对象实例，进入同步代码前要获得当前class的锁
这是因为静态成员不属于任何一个实例对象，归整个类所有，不依赖于类的特定实例，被类的所有实例共享 
```java
synchronized static void method() {
    //业务代码
}
```

3.  修饰代码块（锁指定对象/类）
对括号里执行的对象/类加锁： 
   - `synchronized(object)` 表示进入同步代码库前要获得 **给定对象的锁**。
   - `synchronized(类.class)` 表示进入同步代码前要获得 **给定 Class 的锁**
```java
synchronized(this) {
    //业务代码
}
```
## 使用总结
| 分类 | 具体分类 | 被锁的对象 | 伪代码 |
| --- | --- | --- | --- |
| 方法 | 实例方法 | 类的实例对象 | synchronized void method() {
//业务代码
} |
|  | 静态方法 | 类 | synchronized static void method() {
//业务代码
} |
| 代码块 | 实例对象 | 类的实例对象 | synchronized(this) {
//业务代码
} |
|  | class对象 | 类 | synchronized(Test.class) {
//业务代码
} |
|  | 任意实例对象Object | 实例对象Object | String lock = ""
synchronized(lock) {
//业务代码
} |

# 底层原理
## synchronized的描述和JDK做的优化
synchronized是JVM内置锁，基于**Monitor**机制实现，依赖底层操作系统的互斥原语Mutex(互斥量)，这是一个重量级锁，性能较低。JDK1.5之前是采用这样重量级的方式来实现的，1.5之后做了优化如：锁粗化（Lock Coarsening）、锁消除（Lock Elimination）、轻量级锁（Lightweight Locking）、偏向锁（Biased Locking）、自适应自旋（Adaptive Spinning）等技术来减少锁操 作的开销，内置锁的并发性能已经基本与Lock持平
## 锁的状态：
锁主要存在四种状态，依次是：无锁状态、偏向锁状态、轻量级锁状态、重量级锁状态，他们会随着竞争的激烈而逐渐升级。需要**注意的是锁可以升级，但是不可降级**，这种策略是为了提高获得锁和释放锁的效率。
## 指令描述
Java虚拟机通过一个同步结构支持方法和方法中的指令序列的同步：monitor
同步方法是通过access_flags中设置的ACC_SYNCHRONIZED标志来实现；同步代码块是通过monitorenter和monitorexit来实现。两个指令的执行是JVM通过调用操作系统的互斥 原语mutex来实现，被阻塞的线程会被挂起、等待重新调度，会导致“用户态和内核态”两个态 之间来回切换，对性能有较大影响。
### 字节码的标识
#### 同步方法的标识
下面这个是同步方法上的标识：使用javap命令查看相关的字节码信息 
```java
// 初始代码
public class TestTwo {
    private static int count = 0;
    public synchronized static void add() {
        count++;
    }
    public synchronized static void sub(){
        count--;
    }
    public static void main(String[] args) throws InterruptedException {
        new Thread(()->{
            for (int i = 0; i < 50000; i++) {add();}
        }).start();
        new Thread(()->{
            for (int i = 0; i < 50000; i++) {sub();}
        }).start();
        Thread.sleep(1000);
        System.out.println(count);
    }
}

//使用javap命令查看的其中一项：javap -verbose TestTwo.class
  public static synchronized void sub();
    descriptor: ()V
    flags: ACC_PUBLIC, ACC_STATIC, ACC_SYNCHRONIZED
    Code:
      stack=2, locals=0, args_size=0
         0: getstatic     #2                  // Field count:I
         3: iconst_1
         4: isub
         5: putstatic     #2                  // Field count:I
         8: return
      LineNumberTable:
        line 18: 0
        line 19: 8
```
可以看到在sub的flags的的标识上加入了`ACC_SYNCHRONIZED`
#### 同步代码块的字节码
接下来看同步代码块的字节码标识
```java
public class TestThree {
    private static int count = 0;
    private static String lock = "";
    public static void add() {
        synchronized (lock) {
            count++;
        }
    }
    public static void sub(){
        synchronized (lock) {
            count--;
        }
    }
    public static void main(String[] args) throws InterruptedException {
        new Thread(()->{
            for (int i = 0; i < 50000; i++) {add();}
        }).start();
        new Thread(()->{
            for (int i = 0; i < 50000; i++) {sub();}
        }).start();
        Thread.sleep(1000);
        System.out.println(count);
    }
}

// 字节码信息之一
public static void sub();
    descriptor: ()V
    flags: ACC_PUBLIC, ACC_STATIC
    Code:
      stack=2, locals=2, args_size=0
         0: getstatic     #2                  // Field lock:Ljava/lang/String;
         3: dup
         4: astore_0
         5: monitorenter
         6: getstatic     #3                  // Field count:I
         9: iconst_1
        10: isub
        11: putstatic     #3                  // Field count:I
        14: aload_0
        15: monitorexit
        16: goto          24
        19: astore_1
        20: aload_0
        21: monitorexit
        22: aload_1
        23: athrow
        24: return
      Exception table:
         from    to  target type
             6    16    19   any
            19    22    19   any
      LineNumberTable:
        line 23: 0
        line 24: 6
        line 25: 14
        line 26: 24
      StackMapTable: number_of_entries = 2
        frame_type = 255 /* full_frame */
          offset_delta = 19
          locals = [ class java/lang/Object ]
          stack = [ class java/lang/Throwable ]
        frame_type = 250 /* chop */
          offset_delta = 4
```
从上面可以看出:**synchronized**同步语句块的实现使用的是monitorenter和monitorexit指令，其中monitorenter指令只想同步代码块开始的位置，monitorexit指令则知明同步代码块的结束位置。上面的字节码中包含一个 monitorenter 指令以及两个 monitorexit 指令，这是为了保证锁在同步代码块代码正常执行以及出现异常的这两种情况下都能被正确释放。当执行 monitorenter 指令时，线程试图获取锁也就是获取 **对象监视器 monitor** 的持有权。
在执行monitorenter时，会尝试获取对象的锁，如果锁的计数器为0则表示可以被获取，获取后将锁计数器设为1也就是加1，
对象锁的拥有者线程才可以执行monitorexit指令来释放锁。在执行monitorexit指令后，将锁计数器设为0，表明锁被释放，其他线程可以尝试获取锁，这也就是为什么锁可以升级，而不能降级，因为其他线程是根据这个计数器条件来进行获取锁的，只能恢复为无锁的状态
如果获取对象锁失败，那当前线程就要阻塞等待，直到锁被另外一个线程释放为止。
## Java的Monitor
### Monitor（管程/监视器）
Monitor，直译为“监视器”，而操作系统领域一般翻译为“管程”。管程是指管理共享变量以及对共享变量操作的过程，让它们支持并发。在Java 1.5之前，Java语言提供的唯一并发语言 就是管程，Java 1.5之后提供的SDK并发包也是以管程为基础的。除了Java之外，C/C++、C#等 高级语言也都是支持管程的。synchronized关键字和wait()、notify()、notifyAll()这三个方法是 Java中实现管程技术的组成部分。
### MESA模型
在管程的发展史上，先后出现过三种不同的模型，分别是Hasen模型、Hoare模型、MESA模型。现在广泛使用的是MESA模型。
MESA模型的状态流转图如下：
![管程模型流程图.png](https://cdn.nlark.com/yuque/0/2023/png/26026237/1703837891771-2276d7ff-f579-451f-92a5-9f80d1b20618.png#averageHue=%23141414&clientId=u68993171-79d8-4&from=ui&height=544&id=u62f017c0&originHeight=655&originWidth=710&originalType=binary&ratio=2&rotation=0&showTitle=false&size=49858&status=done&style=none&taskId=uc80ea20a-1f16-40a7-97ba-e19b6e3fdb0&title=&width=590)
管程中引入了条件变量的概念，而且每个条件变量都对应有个等待队列。条件变量和等待队列的作用就是为了解决线程之前的同步问题
对于MESA管程来说，有个编程范式：
```java
while(条件不满足) {
    wait()
}
```
唤醒的时间和获取到锁继续执行的时间是不一致的，被唤醒的线程再次执行时可能条件又不满足了，所以循环校验条件，MESA模型的wait()方法还有一个超时参数，为了避免线程进入等待队列永久阻塞。
Java参考了MESA模型，语言内置的管程（synchronized）对MESA模型进行了精简，MESA模型中，条件变量可以有多个，Java语言内置的管程只有一个条件变量
![管程模型流程图 (1).png](https://cdn.nlark.com/yuque/0/2023/png/26026237/1703838325600-7017ea4a-e4f4-43a4-bcd6-8f5ee16a61a7.png#averageHue=%2310100f&clientId=u68993171-79d8-4&from=ui&id=ucf5d7196&originHeight=655&originWidth=710&originalType=binary&ratio=2&rotation=0&showTitle=false&size=46708&status=done&style=none&taskId=u3abcedc4-6619-4ffe-8038-1826107764a&title=)
### Monitor机制在Java的体现
Java.lang.Object类定义了wait(),notify(),notifyAll()方法，这些方法的具体实现，依赖于ObjectMonitor实现，是JVM内部实现的一套机制。
ObjectMonitor主要的数据结构如下：
```c
ObjectMonitor() { 
    _header = NULL; //对象头 markOop 
    _count = 0; 
    _waiters = 0, 
    _recursions = 0; // 锁的重入次数 
    _object = NULL; //存储锁对象 
    _owner = NULL; // 标识拥有该monitor的线程（当前获取锁的线程）
    _WaitSet = NULL; // 等待线程（调用wait）组成的双向循环链表，_WaitSet是第一个节点 
    _WaitSetLock = 0 ; 
    _Responsible = NULL ; 
    _succ = NULL ; 
    _cxq = NULL ; //多线程竞争锁会先存到这个单向链表中 （FILO栈结构） 
    FreeNext = NULL ; 
    _EntryList = NULL ; //存放在进入或重新进入时被阻塞(blocked)的线程 (也是存竞争锁失败的线程) 
    _SpinFreq = 0 ; 
    _SpinClock = 0 ; 
    OwnerIsThread = 0 ; 
    _previous_owner_tid = 0; 
} 
```
在获取锁时，是将当前线程插入到cxq的头部，而释放锁时，默认策略（QMode=0）是，如果EntryList为空，则将cxq中的元素按照原有顺序插入到EntryList中，并唤醒第一个线程，也就是当EntryList为空时，是后来的线程先获取锁。EntryList不为空，直接从EntryList中唤醒线程，非公平锁
## 对象的内存布局
在Hotspot虚拟机中，对象在内存中存储的布局可以分为三块区域：对象头（Header）、实例数据（Instance Data）、和对齐填充（Padding）

- 对象头：比如hash码，对象所属的年代，对象锁，锁状态标志，偏向锁（线程）ID，Epoch(偏向锁撤销会+1，以class为单位)，偏向时间，数组长度（数组对象才有）等。
- 实例数据：存放累的属性数据信息，包括父类的属性信息
- 对齐填充：由于虚拟机要求**对象起始地址必须是8字节的整数倍**。填充数据不是必须存在的，仅仅是为了对齐

**对象头：**

- Mark Word：
用于存储对象自身的运行时数据，如哈希码（hashCode）、GC分代年龄、锁状态标志、线程持有的锁、偏向线程ID、偏向时间戳等，这部分数据的长度在32位和64位的虚拟机中分别为32bit和64bit，称之为“Mark Word”
- Klass Pointer：
对象头的另外一部分是klass类型指针，即对象指向它的类元数据的指针，虚拟机通过这个指针来确定这个对象是哪个类的实例。 32位4字节，64位开启指针压缩或最大堆内存<32g时4字节，否则8字节。jdk1.8默认开启指针压缩后为4字节，当在JVM参数中关闭指针压缩（-XX:- UseCompressedOops）后，长度为8字节
- 数组长度（只有数组对象有）：
如果对象是一个数组，那在对象头中还必须有一块数据用于记录数组长度。4字节

32位JVM下的对象结构描述：

| 锁状态 | 25bit |  | 4bit | 1bit | 2bit |
| --- | --- | --- | --- | --- | --- |
|  | 23bit | 2bit | 分代年龄 | 是否偏向锁 | 锁标志位 |
| 无锁态 | 对象的hashCode |  | 分代年龄 | 0 | 01 |
| 偏向锁 | 线程ID | Epoch | 
 | 1 | 01 |
| 轻量级锁 | 指向栈中锁记录的指针 |  |  |  |  00  |
| 重量级锁 | 指向互斥锁（重量级锁）的指针 |  |  |  | 10 |
| GC标记 | 空 |  |  |  | 11 |

64位JVM下的对象结构描述：

| 锁状态 | 56bit |  | 1bit | 4bit | 1bit | 2bit |
| --- | --- | --- | --- | --- | --- | --- |
|  |  | 是否偏向锁 |  |  | 锁标志位 |
| 无锁态 | unused： 25bit | 对象hashCode：31bit | unused | 分代年龄 | 0 | 01 |
| 偏向锁 | 线程ID：54bit | Epoch：2bit | 
 | 1 | 1 | 01 |
| 轻量级锁 | 指向栈中锁记录的指针(ptr_to_lock_record) |  |  |  | 
 |  00  |
| 重量级锁 | 指向互斥锁（重量级锁）的指针(pre_to_heacyweight_monitor) |  |  |  | 
 | 10 |
| GC标记 | 空 |  |  |  | 
 | 11 |

- pro_to_lock_record：轻量级锁状态下，指向栈中锁记录的指针。当锁获取是无竞争是，JVM通过原子操作而不是OS互斥，这种技术称为轻量级锁定。在轻量级锁定的情况下，JVM通过CAS操作在对象的Mark Word中设置只想锁记录的指针
- ptr_to_heavyweight_monitor：重量级锁状态下，指向对象监视器Monitor的指针。如果两个不同的线程同时在同一个对象上竞争，则必须将轻量级锁定升级到Monitor以管理等待的线程。在重量级锁定的情况下，JVM在对象的ptr_to_heavyweight_monitor设置指向Monotor的指针

**偏向锁：**当JVM启动了偏向锁模式（jdk1.6默认开启），新创建的对象的Mark Word中的Thread Id为0，说明此时处于可偏向但未偏向任何线程，也叫做匿名偏向状态
**偏向锁延迟：**偏向锁存在偏向锁延迟机制：虚拟机在启动后有个4s的延迟才会对每个新建的对象开启偏向锁模式，因为JVM启动时要进行一系列其他的负载活动，比如装载配置，系统类初始化等等。在这个过程中会使用大量的synchronized关键字对对象枷锁，且这些锁大多数都不是偏向锁，为了减少初始化时间，JVM默认延时加载偏向锁。
```
//关闭延迟开启偏向锁
‐XX:BiasedLockingStartupDelay=0
//禁止偏向锁
‐XX:‐UseBiasedLocking
//启用偏向锁
‐XX:+UseBiasedLocking
```
借助的依赖
```
<dependency>
    <groupId>org.openjdk.jol</groupId>
    <artifactId>jol-core</artifactId>
    <version>0.10</version>
</dependency>
```
默认情况下偏向锁的状态
```java
public static void main(String[] args) throws InterruptedException {
        Object o = new Object();
        System.out.println("one----" + ClassLayout.parseInstance(o).toPrintable());
    	Thread.sleep(5000);
        Object o1 = new Object();
        System.out.println("two----" + ClassLayout.parseInstance(o1).toPrintable());
}


控制台输出情况为无锁状态
one----java.lang.Object object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           01 00 00 00 (00000001 00000000 00000000 00000000) (1)
      4     4        (object header)                           00 00 00 00 (00000000 00000000 00000000 00000000) (0)
      8     4        (object header)                           d5 01 00 f8 (11010101 00000001 00000000 11111000) (-134217259)
     12     4        (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 4 bytes external = 4 bytes total

睡眠5秒之后线程为偏向状态，此时处于匿名偏向状态
two----java.lang.Object object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           05 00 00 00 (00000101 00000000 00000000 00000000) (5)
      4     4        (object header)                           00 00 00 00 (00000000 00000000 00000000 00000000) (0)
      8     4        (object header)                           d5 01 00 f8 (11010101 00000001 00000000 11111000) (-134217259)
     12     4        (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 4 bytes external = 4 bytes total
```
**  将偏向锁禁用：使用-XX:-UseBiasedLocking启动参数**
```java
public static void main(String[] args) throws InterruptedException {
        Object o = new Object();
        System.out.println("one----" + ClassLayout.parseInstance(o).toPrintable());
    	Thread.sleep(5000);
        Object o1 = new Object();
        System.out.println("two----" + ClassLayout.parseInstance(o1).toPrintable());
}


控制台输出情况为无锁状态
one----java.lang.Object object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           01 00 00 00 (00000001 00000000 00000000 00000000) (1)
      4     4        (object header)                           00 00 00 00 (00000000 00000000 00000000 00000000) (0)
      8     4        (object header)                           d5 01 00 f8 (11010101 00000001 00000000 11111000) (-134217259)
     12     4        (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 4 bytes external = 4 bytes total

睡眠5秒后仍无锁状态
two----java.lang.Object object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           01 00 00 00 (00000001 00000000 00000000 00000000) (1)
      4     4        (object header)                           00 00 00 00 (00000000 00000000 00000000 00000000) (0)
      8     4        (object header)                           d5 01 00 f8 (11010101 00000001 00000000 11111000) (-134217259)
     12     4        (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 4 bytes external = 4 bytes total
```
开启偏向，并设置延迟为0 -XX:+UseBiasedLocking -XX:BiasedLockingStartupDelay=0 JVM参数
```java
public static void main(String[] args) throws InterruptedException {
		Object o = new Object();
        System.out.println("one----" + ClassLayout.parseInstance(o).toPrintable());
}


控制台输出情况，为偏向锁状态
one----java.lang.Object object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           05 00 00 00 (00000101 00000000 00000000 00000000) (5)
      4     4        (object header)                           00 00 00 00 (00000000 00000000 00000000 00000000) (0)
      8     4        (object header)                           d5 01 00 f8 (11010101 00000001 00000000 11111000) (-134217259)
     12     4        (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 4 bytes external = 4 bytes total
```

**需要注意的是，如果说关闭了延迟偏向，那么新创建的对象都为偏向锁**
**偏向锁撤销：**

- 偏向锁撤销之调用对象hashCode：调用锁对象的hashCode方法会导致该对象的偏向锁被撤销，因为对于一个对象，HashCode只会生成一次并保存，**偏向锁没有地方存储hashCode**
   - 轻量级锁会在锁记录中记录hashCode
   - 重量级锁会在Monitor中记录hashCode
   - 当对象处理可偏向（也就是偏向线程ID为0）和已偏向的状态下，调用HashCode计算将会使对象再也无法偏向
      - 当对象处于可偏向时，MarkWord将会变成未锁定状态，并只能升级成轻量级锁
      - 当对象正处理偏向锁是，调用HashCode将使偏向锁强制升级为重量级锁
- 偏向锁撤销之调用wait/notify：偏向锁状态执行obj.notify()会升级为轻量级锁，调用obj.wait() 会升级为重量级锁（因为这个时候用的就是Monitor对象了）

**轻量级锁：**倘若偏向锁失败，虚拟机并不会立即升级为重量级锁，它还会尝试使用一个轻量级锁的优化手段，此时Mark Word的结构也变成轻量级锁的结构。轻量级锁所适应的场景是线程交替执行同步块的场合，如果存在同一时间多个线程访问同一把锁的场合，就会导致轻量级锁膨胀为重量级锁，变为轻量级锁的时候，将会把mark word 复制一份到当前的线程的栈中（CAS尝试获取是一部分，还有一个就是要修改对象的Mark Word的值，比如说释放后变为无锁状态，修改重入次数，也就是恢复并赋值）
## synchronized锁的优化
### 批量重偏向和批量撤销
从偏向锁的加锁和解锁的过程中可以看出，当只有一个线程反复进入进入同步块时，偏向锁带来的性能开销基本可以忽略，但是当有其他线程尝试获得锁时，就需要等待JVM的安全点（safe point）时，再将偏向锁撤销为无锁状态或升级为轻量级，会消耗一定性能，所以在多线程竞争频繁的情况下，偏向锁不仅不能提高性能，还会导致性能下降。于是就有了批量重偏向于批量撤销的机制
原理：
以class为单位，为每个class都维护了一个偏向锁撤销计数器，每一次该class的对象发生偏向锁撤销操作时，该计数器+1，当这个值达到重偏向阈值（默认20）时，JVM就认为该calss的偏向锁有问题，因此会进行批量重偏向。
每个class对象会有一个对应的epochziduan，每个处于偏向锁状态对象的Mark Word中也有该字段，其初始值为创建该对象时class中的epoch的值。每次发生批量重偏向时，就将该值+1，同时遍历JVM中所有线程的站，找到该class所有正处于加锁状态的偏向锁，将其epoch字段改为新值。下次获得锁时，发现当前对象的epoch值和class的epoch不相等，那就算当前已经偏向了其他线程，也不会执行撤销操作，而是直接通过CAS操作将其Mark Word的Thread Id改成当前线程id
当达到重偏向阈值（默认20）后，假设该class计数器继续增长，当其达到批量撤销的阈值后（默认40）,JVM就认为该calss的使用场景存在多线程竞争，会标记该class为不可偏向，之后，对于该class的锁，直接走轻量级锁的逻辑

批量重偏向的样例代码如下：
```java
public static void main(String[] args) throws Exception {
    //休眠五秒创建的对象为偏向锁状态
    Thread.sleep(5000);
    List<A> list = new ArrayList<>();
    new Thread(() -> {
        //创建50个对象
        for (int i = 0; i < 50; i++) {
            A lock = new A();
            synchronized (lock) {
                list.add(lock);
            }
        }
        //线程保活
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }, "thread1").start();
    Thread.sleep(3000);
    System.out.println("打印线程1的第20个对象信息\n" + ClassLayout.parseInstance(list.get(19)).toPrintable());


    new Thread(() -> {
        for (int i = 0; i < 40; i++) {
            A lock = list.get(i);
            synchronized (lock) {
                if ((i >= 18 && i <= 23) || i >= 38) {
                    String name = Thread.currentThread().getName();
                    System.out.println(name + "第" + (i + 1) + "次加锁执行中\n" + ClassLayout.parseInstance(lock).toPrintable());
                }
            }
        }
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }, "thread2").start();
}
```
控制台输出结果：
```java
打印线程1的第20个对象信息
com.leguan.A object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           05 78 01 60 (00000101 01111000 00000001 01100000) (1610708997)
      4     4        (object header)                           a8 7f 00 00 (10101000 01111111 00000000 00000000) (32680)
      8     4        (object header)                           41 c1 00 f8 (01000001 11000001 00000000 11111000) (-134168255)
     12     4        (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 4 bytes external = 4 bytes total

thread2第19次加锁执行中
com.leguan.A object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           d0 b9 8c 08 (11010000 10111001 10001100 00001000) (143440336)
      4     4        (object header)                           00 70 00 00 (00000000 01110000 00000000 00000000) (28672)
      8     4        (object header)                           41 c1 00 f8 (01000001 11000001 00000000 11111000) (-134168255)
     12     4        (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 4 bytes external = 4 bytes total

thread2第20次加锁执行中
com.leguan.A object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           05 91 11 61 (00000101 10010001 00010001 01100001) (1628541189)
      4     4        (object header)                           a8 7f 00 00 (10101000 01111111 00000000 00000000) (32680)
      8     4        (object header)                           41 c1 00 f8 (01000001 11000001 00000000 11111000) (-134168255)
     12     4        (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 4 bytes external = 4 bytes total

thread2第21次加锁执行中
com.leguan.A object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           05 91 11 61 (00000101 10010001 00010001 01100001) (1628541189)
      4     4        (object header)                           a8 7f 00 00 (10101000 01111111 00000000 00000000) (32680)
      8     4        (object header)                           41 c1 00 f8 (01000001 11000001 00000000 11111000) (-134168255)
     12     4        (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 4 bytes external = 4 bytes total

thread2第22次加锁执行中
com.leguan.A object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           05 91 11 61 (00000101 10010001 00010001 01100001) (1628541189)
      4     4        (object header)                           a8 7f 00 00 (10101000 01111111 00000000 00000000) (32680)
      8     4        (object header)                           41 c1 00 f8 (01000001 11000001 00000000 11111000) (-134168255)
     12     4        (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 4 bytes external = 4 bytes total

thread2第23次加锁执行中
com.leguan.A object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           05 91 11 61 (00000101 10010001 00010001 01100001) (1628541189)
      4     4        (object header)                           a8 7f 00 00 (10101000 01111111 00000000 00000000) (32680)
      8     4        (object header)                           41 c1 00 f8 (01000001 11000001 00000000 11111000) (-134168255)
     12     4        (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 4 bytes external = 4 bytes total

thread2第24次加锁执行中
com.leguan.A object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           05 91 11 61 (00000101 10010001 00010001 01100001) (1628541189)
      4     4        (object header)                           a8 7f 00 00 (10101000 01111111 00000000 00000000) (32680)
      8     4        (object header)                           41 c1 00 f8 (01000001 11000001 00000000 11111000) (-134168255)
     12     4        (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 4 bytes external = 4 bytes total

thread2第39次加锁执行中
com.leguan.A object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           05 91 11 61 (00000101 10010001 00010001 01100001) (1628541189)
      4     4        (object header)                           a8 7f 00 00 (10101000 01111111 00000000 00000000) (32680)
      8     4        (object header)                           41 c1 00 f8 (01000001 11000001 00000000 11111000) (-134168255)
     12     4        (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 4 bytes external = 4 bytes total

thread2第40次加锁执行中
com.leguan.A object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           05 91 11 61 (00000101 10010001 00010001 01100001) (1628541189)
      4     4        (object header)                           a8 7f 00 00 (10101000 01111111 00000000 00000000) (32680)
      8     4        (object header)                           41 c1 00 f8 (01000001 11000001 00000000 11111000) (-134168255)
     12     4        (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 4 bytes external = 4 bytes total
```
从控制台输出结果可以看到，线程1的第20个对象是为偏向线程1的存在，这个时候线程2在进行执行，因为此时还未超过规定的阈值20，所以升级为了轻量级锁，往后便超过了批量重偏向的阈值20，后面对象进行批量重偏向，偏向了线程2

批量撤销代码如下
```java
public static void main(String[] args) throws Exception {
    //休眠五秒创建的对象为偏向锁状态
    Thread.sleep(5000);
    List<A> list = new ArrayList<>();
    new Thread(() -> {
        //创建50个对象
        for (int i = 0; i < 50; i++) {
            A lock = new A();
            synchronized (lock) {
                list.add(lock);
            }
        }
        //线程保活
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }, "thread1").start();
    Thread.sleep(3000);
    System.out.println("打印线程1的第20个对象信息\n" + ClassLayout.parseInstance(list.get(19)).toPrintable());


    new Thread(() -> {
        for (int i = 0; i < 40; i++) {
            A lock = list.get(i);
            String name = Thread.currentThread().getName();
            if ((i >= 18 && i <= 23) || i >= 38) {
                    System.out.println(name + "第" + (i + 1) + "次加锁准备中\n" + ClassLayout.parseInstance(lock).toPrintable());
            }
            synchronized (lock) {
                if ((i >= 18 && i <= 23) || i >= 38) {
                    System.out.println(name + "第" + (i + 1) + "次加锁执行中\n" + ClassLayout.parseInstance(lock).toPrintable());
                }
            }
        }
        try {
            Thread.sleep(100000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }, "thread2").start();


    System.out.println("打印的第11个对象信息\n" + ClassLayout.parseInstance(list.get(10)).toPrintable());
    System.out.println("打印的第20个对象信息\n" + ClassLayout.parseInstance(list.get(19)).toPrintable());
    System.out.println("打印的第21个对象信息\n" + ClassLayout.parseInstance(list.get(20)).toPrintable());
    System.out.println("打印的第41个对象信息\n" + ClassLayout.parseInstance(list.get(41)).toPrintable());
    System.out.println("批量重偏向时新创建的对象\n" + ClassLayout.parseInstance(new A()).toPrintable());

    Thread.sleep(3000);
    new Thread(() -> {
        for (int i = 0; i < 50; i++) {
            A lock = list.get(i);
            String name = Thread.currentThread().getName();
            if ((i >= 18 && i <= 23) || (i >= 38 && i <= 42)) {
                System.out.println(name + "第" + (i + 1) + "次加锁准备中\n" + ClassLayout.parseInstance(lock).toPrintable());
            }
            synchronized (lock) {
                if ((i >= 18 && i <= 23) || (i >= 38 && i <= 42)) {
                    System.out.println(name + "第" + (i + 1) + "次加锁执行中\n" + ClassLayout.parseInstance(lock).toPrintable());
                }
            }
        }
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }, "thread3").start();
    Thread.sleep(4000);
    System.out.println("批量重撤销后新创建的对象\n" + ClassLayout.parseInstance(new A()).toPrintable());
}
```
 控制台输出结果：
```java
打印线程1的第20个对象信息
com.leguan.A object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           05 e0 06 04 (00000101 11100000 00000110 00000100) (67559429)
      4     4        (object header)                           f9 7f 00 00 (11111001 01111111 00000000 00000000) (32761)
      8     4        (object header)                           41 c1 00 f8 (01000001 11000001 00000000 11111000) (-134168255)
     12     4        (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 4 bytes external = 4 bytes total

打印的第11个对象信息
com.leguan.A object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           01 00 00 00 (00000001 00000000 00000000 00000000) (1)
      4     4        (object header)                           00 00 00 00 (00000000 00000000 00000000 00000000) (0)
      8     4        (object header)                           41 c1 00 f8 (01000001 11000001 00000000 11111000) (-134168255)
     12     4        (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 4 bytes external = 4 bytes total

thread2第19次加锁准备中
com.leguan.A object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           05 e0 06 04 (00000101 11100000 00000110 00000100) (67559429)
      4     4        (object header)                           f9 7f 00 00 (11111001 01111111 00000000 00000000) (32761)
      8     4        (object header)                           41 c1 00 f8 (01000001 11000001 00000000 11111000) (-134168255)
     12     4        (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 4 bytes external = 4 bytes total

thread2第19次加锁执行中
com.leguan.A object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           d0 f9 ec 05 (11010000 11111001 11101100 00000101) (99416528)
      4     4        (object header)                           00 70 00 00 (00000000 01110000 00000000 00000000) (28672)
      8     4        (object header)                           41 c1 00 f8 (01000001 11000001 00000000 11111000) (-134168255)
     12     4        (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 4 bytes external = 4 bytes total

打印的第20个对象信息
com.leguan.A object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           05 e0 06 04 (00000101 11100000 00000110 00000100) (67559429)
      4     4        (object header)                           f9 7f 00 00 (11111001 01111111 00000000 00000000) (32761)
      8     4        (object header)                           41 c1 00 f8 (01000001 11000001 00000000 11111000) (-134168255)
     12     4        (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 4 bytes external = 4 bytes total

thread2第20次加锁准备中
com.leguan.A object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           05 e0 06 04 (00000101 11100000 00000110 00000100) (67559429)
      4     4        (object header)                           f9 7f 00 00 (11111001 01111111 00000000 00000000) (32761)
      8     4        (object header)                           41 c1 00 f8 (01000001 11000001 00000000 11111000) (-134168255)
     12     4        (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 4 bytes external = 4 bytes total

打印的第21个对象信息
com.leguan.A object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           05 e0 06 04 (00000101 11100000 00000110 00000100) (67559429)
      4     4        (object header)                           f9 7f 00 00 (11111001 01111111 00000000 00000000) (32761)
      8     4        (object header)                           41 c1 00 f8 (01000001 11000001 00000000 11111000) (-134168255)
     12     4        (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 4 bytes external = 4 bytes total

打印的第41个对象信息
com.leguan.A object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           05 e0 06 04 (00000101 11100000 00000110 00000100) (67559429)
      4     4        (object header)                           f9 7f 00 00 (11111001 01111111 00000000 00000000) (32761)
      8     4        (object header)                           41 c1 00 f8 (01000001 11000001 00000000 11111000) (-134168255)
     12     4        (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 4 bytes external = 4 bytes total

thread2第20次加锁执行中
com.leguan.A object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           05 99 02 04 (00000101 10011001 00000010 00000100) (67279109)
      4     4        (object header)                           f9 7f 00 00 (11111001 01111111 00000000 00000000) (32761)
      8     4        (object header)                           41 c1 00 f8 (01000001 11000001 00000000 11111000) (-134168255)
     12     4        (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 4 bytes external = 4 bytes total

批量重偏向时新创建的对象
com.leguan.A object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           05 01 00 00 (00000101 00000001 00000000 00000000) (261)
      4     4        (object header)                           00 00 00 00 (00000000 00000000 00000000 00000000) (0)
      8     4        (object header)                           41 c1 00 f8 (01000001 11000001 00000000 11111000) (-134168255)
     12     4        (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 4 bytes external = 4 bytes total

thread2第21次加锁准备中
com.leguan.A object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           05 e0 06 04 (00000101 11100000 00000110 00000100) (67559429)
      4     4        (object header)                           f9 7f 00 00 (11111001 01111111 00000000 00000000) (32761)
      8     4        (object header)                           41 c1 00 f8 (01000001 11000001 00000000 11111000) (-134168255)
     12     4        (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 4 bytes external = 4 bytes total

thread2第21次加锁执行中
com.leguan.A object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           05 99 02 04 (00000101 10011001 00000010 00000100) (67279109)
      4     4        (object header)                           f9 7f 00 00 (11111001 01111111 00000000 00000000) (32761)
      8     4        (object header)                           41 c1 00 f8 (01000001 11000001 00000000 11111000) (-134168255)
     12     4        (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 4 bytes external = 4 bytes total

thread2第22次加锁准备中
com.leguan.A object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           05 e0 06 04 (00000101 11100000 00000110 00000100) (67559429)
      4     4        (object header)                           f9 7f 00 00 (11111001 01111111 00000000 00000000) (32761)
      8     4        (object header)                           41 c1 00 f8 (01000001 11000001 00000000 11111000) (-134168255)
     12     4        (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 4 bytes external = 4 bytes total

thread2第22次加锁执行中
com.leguan.A object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           05 99 02 04 (00000101 10011001 00000010 00000100) (67279109)
      4     4        (object header)                           f9 7f 00 00 (11111001 01111111 00000000 00000000) (32761)
      8     4        (object header)                           41 c1 00 f8 (01000001 11000001 00000000 11111000) (-134168255)
     12     4        (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 4 bytes external = 4 bytes total

thread2第23次加锁准备中
com.leguan.A object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           05 e0 06 04 (00000101 11100000 00000110 00000100) (67559429)
      4     4        (object header)                           f9 7f 00 00 (11111001 01111111 00000000 00000000) (32761)
      8     4        (object header)                           41 c1 00 f8 (01000001 11000001 00000000 11111000) (-134168255)
     12     4        (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 4 bytes external = 4 bytes total

thread2第23次加锁执行中
com.leguan.A object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           05 99 02 04 (00000101 10011001 00000010 00000100) (67279109)
      4     4        (object header)                           f9 7f 00 00 (11111001 01111111 00000000 00000000) (32761)
      8     4        (object header)                           41 c1 00 f8 (01000001 11000001 00000000 11111000) (-134168255)
     12     4        (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 4 bytes external = 4 bytes total

thread2第24次加锁准备中
com.leguan.A object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           05 e0 06 04 (00000101 11100000 00000110 00000100) (67559429)
      4     4        (object header)                           f9 7f 00 00 (11111001 01111111 00000000 00000000) (32761)
      8     4        (object header)                           41 c1 00 f8 (01000001 11000001 00000000 11111000) (-134168255)
     12     4        (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 4 bytes external = 4 bytes total

thread2第24次加锁执行中
com.leguan.A object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           05 99 02 04 (00000101 10011001 00000010 00000100) (67279109)
      4     4        (object header)                           f9 7f 00 00 (11111001 01111111 00000000 00000000) (32761)
      8     4        (object header)                           41 c1 00 f8 (01000001 11000001 00000000 11111000) (-134168255)
     12     4        (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 4 bytes external = 4 bytes total

thread2第39次加锁准备中
com.leguan.A object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           05 e0 06 04 (00000101 11100000 00000110 00000100) (67559429)
      4     4        (object header)                           f9 7f 00 00 (11111001 01111111 00000000 00000000) (32761)
      8     4        (object header)                           41 c1 00 f8 (01000001 11000001 00000000 11111000) (-134168255)
     12     4        (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 4 bytes external = 4 bytes total

thread2第39次加锁执行中
com.leguan.A object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           05 99 02 04 (00000101 10011001 00000010 00000100) (67279109)
      4     4        (object header)                           f9 7f 00 00 (11111001 01111111 00000000 00000000) (32761)
      8     4        (object header)                           41 c1 00 f8 (01000001 11000001 00000000 11111000) (-134168255)
     12     4        (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 4 bytes external = 4 bytes total

thread2第40次加锁准备中
com.leguan.A object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           05 e0 06 04 (00000101 11100000 00000110 00000100) (67559429)
      4     4        (object header)                           f9 7f 00 00 (11111001 01111111 00000000 00000000) (32761)
      8     4        (object header)                           41 c1 00 f8 (01000001 11000001 00000000 11111000) (-134168255)
     12     4        (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 4 bytes external = 4 bytes total

thread2第40次加锁执行中
com.leguan.A object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           05 99 02 04 (00000101 10011001 00000010 00000100) (67279109)
      4     4        (object header)                           f9 7f 00 00 (11111001 01111111 00000000 00000000) (32761)
      8     4        (object header)                           41 c1 00 f8 (01000001 11000001 00000000 11111000) (-134168255)
     12     4        (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 4 bytes external = 4 bytes total

thread3第19次加锁准备中
com.leguan.A object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           01 00 00 00 (00000001 00000000 00000000 00000000) (1)
      4     4        (object header)                           00 00 00 00 (00000000 00000000 00000000 00000000) (0)
      8     4        (object header)                           41 c1 00 f8 (01000001 11000001 00000000 11111000) (-134168255)
     12     4        (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 4 bytes external = 4 bytes total

thread3第19次加锁执行中
com.leguan.A object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           d0 29 fd 05 (11010000 00101001 11111101 00000101) (100477392)
      4     4        (object header)                           00 70 00 00 (00000000 01110000 00000000 00000000) (28672)
      8     4        (object header)                           41 c1 00 f8 (01000001 11000001 00000000 11111000) (-134168255)
     12     4        (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 4 bytes external = 4 bytes total

thread3第20次加锁准备中
com.leguan.A object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           05 99 02 04 (00000101 10011001 00000010 00000100) (67279109)
      4     4        (object header)                           f9 7f 00 00 (11111001 01111111 00000000 00000000) (32761)
      8     4        (object header)                           41 c1 00 f8 (01000001 11000001 00000000 11111000) (-134168255)
     12     4        (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 4 bytes external = 4 bytes total

thread3第20次加锁执行中
com.leguan.A object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           d0 29 fd 05 (11010000 00101001 11111101 00000101) (100477392)
      4     4        (object header)                           00 70 00 00 (00000000 01110000 00000000 00000000) (28672)
      8     4        (object header)                           41 c1 00 f8 (01000001 11000001 00000000 11111000) (-134168255)
     12     4        (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 4 bytes external = 4 bytes total

thread3第21次加锁准备中
com.leguan.A object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           05 99 02 04 (00000101 10011001 00000010 00000100) (67279109)
      4     4        (object header)                           f9 7f 00 00 (11111001 01111111 00000000 00000000) (32761)
      8     4        (object header)                           41 c1 00 f8 (01000001 11000001 00000000 11111000) (-134168255)
     12     4        (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 4 bytes external = 4 bytes total

thread3第21次加锁执行中
com.leguan.A object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           d0 29 fd 05 (11010000 00101001 11111101 00000101) (100477392)
      4     4        (object header)                           00 70 00 00 (00000000 01110000 00000000 00000000) (28672)
      8     4        (object header)                           41 c1 00 f8 (01000001 11000001 00000000 11111000) (-134168255)
     12     4        (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 4 bytes external = 4 bytes total

thread3第22次加锁准备中
com.leguan.A object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           05 99 02 04 (00000101 10011001 00000010 00000100) (67279109)
      4     4        (object header)                           f9 7f 00 00 (11111001 01111111 00000000 00000000) (32761)
      8     4        (object header)                           41 c1 00 f8 (01000001 11000001 00000000 11111000) (-134168255)
     12     4        (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 4 bytes external = 4 bytes total

thread3第22次加锁执行中
com.leguan.A object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           d0 29 fd 05 (11010000 00101001 11111101 00000101) (100477392)
      4     4        (object header)                           00 70 00 00 (00000000 01110000 00000000 00000000) (28672)
      8     4        (object header)                           41 c1 00 f8 (01000001 11000001 00000000 11111000) (-134168255)
     12     4        (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 4 bytes external = 4 bytes total

thread3第23次加锁准备中
com.leguan.A object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           05 99 02 04 (00000101 10011001 00000010 00000100) (67279109)
      4     4        (object header)                           f9 7f 00 00 (11111001 01111111 00000000 00000000) (32761)
      8     4        (object header)                           41 c1 00 f8 (01000001 11000001 00000000 11111000) (-134168255)
     12     4        (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 4 bytes external = 4 bytes total

thread3第23次加锁执行中
com.leguan.A object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           d0 29 fd 05 (11010000 00101001 11111101 00000101) (100477392)
      4     4        (object header)                           00 70 00 00 (00000000 01110000 00000000 00000000) (28672)
      8     4        (object header)                           41 c1 00 f8 (01000001 11000001 00000000 11111000) (-134168255)
     12     4        (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 4 bytes external = 4 bytes total

thread3第24次加锁准备中
com.leguan.A object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           05 99 02 04 (00000101 10011001 00000010 00000100) (67279109)
      4     4        (object header)                           f9 7f 00 00 (11111001 01111111 00000000 00000000) (32761)
      8     4        (object header)                           41 c1 00 f8 (01000001 11000001 00000000 11111000) (-134168255)
     12     4        (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 4 bytes external = 4 bytes total

thread3第24次加锁执行中
com.leguan.A object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           d0 29 fd 05 (11010000 00101001 11111101 00000101) (100477392)
      4     4        (object header)                           00 70 00 00 (00000000 01110000 00000000 00000000) (28672)
      8     4        (object header)                           41 c1 00 f8 (01000001 11000001 00000000 11111000) (-134168255)
     12     4        (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 4 bytes external = 4 bytes total

thread3第39次加锁准备中
com.leguan.A object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           05 99 02 04 (00000101 10011001 00000010 00000100) (67279109)
      4     4        (object header)                           f9 7f 00 00 (11111001 01111111 00000000 00000000) (32761)
      8     4        (object header)                           41 c1 00 f8 (01000001 11000001 00000000 11111000) (-134168255)
     12     4        (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 4 bytes external = 4 bytes total

thread3第39次加锁执行中
com.leguan.A object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           d0 29 fd 05 (11010000 00101001 11111101 00000101) (100477392)
      4     4        (object header)                           00 70 00 00 (00000000 01110000 00000000 00000000) (28672)
      8     4        (object header)                           41 c1 00 f8 (01000001 11000001 00000000 11111000) (-134168255)
     12     4        (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 4 bytes external = 4 bytes total

thread3第40次加锁准备中
com.leguan.A object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           05 99 02 04 (00000101 10011001 00000010 00000100) (67279109)
      4     4        (object header)                           f9 7f 00 00 (11111001 01111111 00000000 00000000) (32761)
      8     4        (object header)                           41 c1 00 f8 (01000001 11000001 00000000 11111000) (-134168255)
     12     4        (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 4 bytes external = 4 bytes total

thread3第40次加锁执行中
com.leguan.A object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           d0 29 fd 05 (11010000 00101001 11111101 00000101) (100477392)
      4     4        (object header)                           00 70 00 00 (00000000 01110000 00000000 00000000) (28672)
      8     4        (object header)                           41 c1 00 f8 (01000001 11000001 00000000 11111000) (-134168255)
     12     4        (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 4 bytes external = 4 bytes total

thread3第41次加锁准备中
com.leguan.A object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           05 e0 06 04 (00000101 11100000 00000110 00000100) (67559429)
      4     4        (object header)                           f9 7f 00 00 (11111001 01111111 00000000 00000000) (32761)
      8     4        (object header)                           41 c1 00 f8 (01000001 11000001 00000000 11111000) (-134168255)
     12     4        (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 4 bytes external = 4 bytes total

thread3第41次加锁执行中
com.leguan.A object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           d0 29 fd 05 (11010000 00101001 11111101 00000101) (100477392)
      4     4        (object header)                           00 70 00 00 (00000000 01110000 00000000 00000000) (28672)
      8     4        (object header)                           41 c1 00 f8 (01000001 11000001 00000000 11111000) (-134168255)
     12     4        (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 4 bytes external = 4 bytes total

thread3第42次加锁准备中
com.leguan.A object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           05 e0 06 04 (00000101 11100000 00000110 00000100) (67559429)
      4     4        (object header)                           f9 7f 00 00 (11111001 01111111 00000000 00000000) (32761)
      8     4        (object header)                           41 c1 00 f8 (01000001 11000001 00000000 11111000) (-134168255)
     12     4        (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 4 bytes external = 4 bytes total

thread3第42次加锁执行中
com.leguan.A object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           d0 29 fd 05 (11010000 00101001 11111101 00000101) (100477392)
      4     4        (object header)                           00 70 00 00 (00000000 01110000 00000000 00000000) (28672)
      8     4        (object header)                           41 c1 00 f8 (01000001 11000001 00000000 11111000) (-134168255)
     12     4        (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 4 bytes external = 4 bytes total

thread3第43次加锁准备中
com.leguan.A object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           05 e0 06 04 (00000101 11100000 00000110 00000100) (67559429)
      4     4        (object header)                           f9 7f 00 00 (11111001 01111111 00000000 00000000) (32761)
      8     4        (object header)                           41 c1 00 f8 (01000001 11000001 00000000 11111000) (-134168255)
     12     4        (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 4 bytes external = 4 bytes total

thread3第43次加锁执行中
com.leguan.A object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           d0 29 fd 05 (11010000 00101001 11111101 00000101) (100477392)
      4     4        (object header)                           00 70 00 00 (00000000 01110000 00000000 00000000) (28672)
      8     4        (object header)                           41 c1 00 f8 (01000001 11000001 00000000 11111000) (-134168255)
     12     4        (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 4 bytes external = 4 bytes total

批量重撤销后新创建的对象
com.leguan.A object internals:
 OFFSET  SIZE   TYPE DESCRIPTION                               VALUE
      0     4        (object header)                           01 00 00 00 (00000001 00000000 00000000 00000000) (1)
      4     4        (object header)                           00 00 00 00 (00000000 00000000 00000000 00000000) (0)
      8     4        (object header)                           41 c1 00 f8 (01000001 11000001 00000000 11111000) (-134168255)
     12     4        (loss due to the next object alignment)
Instance size: 16 bytes
Space losses: 0 bytes internal + 4 bytes external = 4 bytes total
```
从控制台的输出结果看出，线程3的输出的前几个线程是由无锁状态升级为轻量级锁的状态，后续的因为因为已经重偏向了线程二所以是从偏向锁升级了为了轻量级锁，那么41往后的达到了偏向锁撤销的阈值40,批量撤销偏向锁，升级为轻量级锁，后续创建的所有的关于这个对象默认都会为无锁的状态，不会再为偏向锁的状态了

应用场景：

- 批量重偏向：是为了解决，一个线程创建了大量对象并执行了初始的同步操作，后来另一个线程也来将这些对象作为锁对象进行操作，这样会导致大量的偏向锁撤销操作（JVM参数：intx BiasedLockingBulkRebiasThreshold = 20 //默认偏向锁批量重偏向阈值）
- 批量撤销：是为了解决，在明显对线程竞争剧烈的场景下使用偏向锁是不合适的（JVM参数intx BiasedLockingBulkRevokeThreshold = 40 //默认偏向锁批量撤销阈值 ），超过这个值，后面创建的就为无锁的状态了，不会为偏向锁的状态

可以通过-XX:BiasedLockingBulkRebiasThreshold 和 -XX:BiasedLockingBulkRevokeThreshold 来手动设置阈值

1. 批量重偏向和批量撤销时针对类的优化，和对象无关
2. 偏向锁重偏向一次后不可在此重偏向
3. 当某个类已经出发批量撤销机制后，JVM会默认当前类产生了严重的问题，剥夺了该类的新实例对象使用偏向锁的权利
### 自旋的优化
重量级锁竞争的时候，还可以使用自旋来进行优化，如果当前线程自旋成功（即这时候持锁线程已经退出了同步块，释放了锁），这时当前线程就可以避免阻塞

- 自旋会占用CPU时间，单核CPU自旋就是浪费，多核CPU自旋才能发挥优势
- 在Jdk1.6之后自旋是自适应的，比如对象刚刚的一次自旋操作成功后，那么认为这次自旋的成功的可能性会高，就多自旋几次，反正，就会减少自旋甚至不自旋，比较智能
- Jdk1.7之后不能控制是否开启自旋功能

自旋的目的是为了减少线程刮起的次数，尽量避免直接挂起线程（挂起操作涉及系统调用，存在用户态和内核态的切换，这才是重量级锁最大的开销）

### 锁的粗化
假设一系列的连续操作都会对同一个对象反复加锁及解锁，甚至加锁操作是出现在循环体中的，即使没有出现线程竞争，频繁地进行互斥同步操作也会导致不必要的性能消耗。如果JVM检测到有一连串零碎的操作都是对同一对象的加锁，将会扩大加锁同步的范围（即锁粗化）到整个操作序列的外部例如：
```java
StringBuffer buffer = new StringBuffer();

//锁的粗化
public void append(){
    buffer.append("AAA").append("BBB").append("CCC");
}
```
上述代码每次调用buffer.append方法都需要加锁和解锁，如果JVM检测到有一连串的对同一个对象加锁和解锁的操作，就会将其合并成一次范围更大的加锁和解锁操作，即在第一次append方法是进行加锁，最后一次append方法结束后进行解锁。

### 锁消除
锁消除即删除不必要的加锁操作。锁消除是Java虚拟机在JIT编译期间，通过对运行上下文的扫描，去除不可能存在共享资源竞争的锁，通过锁消除，可以接上毫无意义的请求锁的时间
样例一：
```java
/**
 * 锁消除
 *  ‐XX:+EliminateLocks 开启锁消除(jdk8默认开启）
 *  -XX:-EliminateLocks 关闭锁消除
 * @param s1
 * @param s2
 */
public void append(String s1, String s2) {
    StringBuffer stringBuffer = new StringBuffer();
    stringBuffer.append(s1).append(s2);
}



public static void main(String[] args) {
    LockEliminationTest lockEliminationTest = new LockEliminationTest();
    long start = System.currentTimeMillis();
    for (int i = 0; i < 1000000; i++) {
        lockEliminationTest.append("1","2");
    }
    long end = System.currentTimeMillis();
    System.out.println("执行时间："+ (end-start)+"ms");
}
```
StringBuffer的append是个同步方法，但是append方法中的StringBuffer属于一个局部变量，不可能从该方法中逃逸出去，其实这个过程是线程安全的，可能将锁消除
样例二：
```java
public static void main(String[] args) {
    for (int i = 0; i < 10; i++) {
        new Thread(() -> {
            Object o = new Object();
            synchronized (o) {
                // 业务逻辑
            }
        }).start();
    }
}
```

**逃逸分析：**
逃逸分析，是一种可以有效减少Java程序中同步负载和内存堆分配压力的跨函数全局数据流分析算法，通过逃逸分析，Java HotSpot编译器能够分析出一个新的对象的引用的使用范围从而决定是否要将这个对象分配到堆上。逃逸分析的基本行为就是分析对象动态作用域
**方法逃逸（对象逃出当前方法）：**
当一个对象在方法中被定义后，它可能被外部方法所引用，例如作为调用参数传递到其他的方法中。样例一就没有逃出当前方法
**线程逃逸（对象逃出当前线程）：**
这个对象甚至可能被其他线程访问到，例如赋值给类变量或可以在其他线程中访问的实例变量。样例二就没有逃出当前线程
使用逃逸分析，编译器可以对代码做以下优化：

1.  同步省略或锁消除(Synchronization Elimination)。如果一个对象被发现只能从一个线程被访问到，那么对于这个对象的操作可以不考虑同步。 
2.  将堆分配转化为栈分配(Stack Allocation)。如果一个对象在子程序中被分配，要使指向该对象的指针永远不会逃逸，对象可能是栈分配的候选，而不是堆分配。样例二中在每个线程中都创建了对象，而这个对象又没有逃出线程，就直接在线程栈中分配了，避免了放到堆中，GC还得回收 
3.  分离对象或标量替换(Scalar Replacement)。有的对象可能不需要作为一个连续的内存结构存 在也可以被访问到，那么对象的部分（或全部）可以不存储在内存，而是存储在CPU寄存器中。 
```java
public class EscapeTest {
    /**
     * 1 -XX:+DoEscapeAnalysis //表示开启逃逸分析 (jdk1.8默认开启）
     * 2 -XX:-DoEscapeAnalysis //表示关闭逃逸分析。
     * 3 -XX:+EliminateAllocations //开启标量替换(默认打开)
     * 4 -XX:-EliminateAllocations //关闭标量替换
     * 5 -XX:+EliminateLocks //开启锁消除(jdk1.8默认开启）
     */
    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i < 500000; i++) {
            test();
        }
        Thread.sleep(Integer.MAX_VALUE);
    }

    /**
     * 标量替换
     */
    public static void test() {
        Point point = new Point(1, 2);
        System.out.println("ponit x:" + point.getX() + "point y:" + point.getY());
      }
}
```
在test方法中会认为是这样的格式，这种就称之为标量替换 
```java
int x = 1;
int y = 2;
```

可以使用jps命令查看对应的java线程，在用jmap -histo <进程id> 查看对应的class和实例数 ,会发现实例数并不是500000。

## 锁升级的流程：
![升级锁过程.png](https://cdn.nlark.com/yuque/0/2023/png/26026237/1703752459393-71312a72-7278-4f87-b616-4af9608449d9.png#averageHue=%23191919&clientId=ua2dc69c0-647b-4&from=ui&id=u03e34e95&originHeight=1902&originWidth=2948&originalType=binary&ratio=1&rotation=0&showTitle=false&size=469542&status=done&style=none&taskId=u690b1821-4b24-48f6-8f68-01e54aa4c9f&title=)

## 重量级锁加锁过程
![重量级锁加锁过程.png](https://cdn.nlark.com/yuque/0/2023/png/26026237/1703752554621-1c1c624e-8820-4c21-80fb-978cf95156b1.png#averageHue=%23875a50&clientId=ua2dc69c0-647b-4&from=ui&id=uf3ce1e7a&originHeight=2999&originWidth=1176&originalType=binary&ratio=1&rotation=0&showTitle=false&size=364812&status=done&style=none&taskId=ue6b78735-0e8b-4770-875a-dcf00cc70cc&title=)

# 总结：

1. synchronized的使用
2. 锁的状态
3. synchronized的字节码
4. JVM重量级锁的ObjectMonitor对象
5. synchronized的优化
6. 锁升级和重量级锁加锁的流程图介绍

