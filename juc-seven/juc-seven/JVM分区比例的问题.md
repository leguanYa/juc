我们知道**默认情况下**
jvm中新生代与老年代的比例是1:2，就相当于新生代占用整个堆空间的**1/3** 
而新生代中Eden与Survivor区（又有s0区和s1区），这个比例为8:1:1，就相当于Eden占用了新生代空间的**8/10**
具体的我们使用下面的命令可以查看
```java
java -XX:+PrintFlagsFinal -version | grep 'Ratio'
```
展示如下：
![](https://cdn.nlark.com/yuque/0/2024/png/26026237/1704710059605-664ec3b8-c616-4bb4-8c06-ef000714fb82.png#averageHue=%23130e0d&clientId=u743ba93c-2997-4&id=WRKkO&originHeight=478&originWidth=1882&originalType=binary&ratio=1&rotation=0&showTitle=false&status=done&style=none&taskId=u9df6c2d0-12ff-425c-9849-f2f98cb4d15&title=)
可以看到默认情况下NewRatio（年轻代和年老代的比值。）为2，SurvivorRatio（Eden和Survivor的比值）为8，
当然了这个值也可以修改
使用-XX:NewRatio=n 和 -XX:SurvivorRatio=n进行修改
接下来继续看有没有按照默认的值去进行分区，这里用的是jdk9,
#### **1、先用-XX:+UseParallelOldGC -XX:+UseParallelGC先看下，因为jdk1.8默认是这俩**
**因为用的是jdk9，所以启动参数上加上了，不然默认用的UseG1GC**
如下图：NewSize为170.5MB，OldSize为341.5MB符合1:2
但是看Heap Usage，Eden Space为513，相比PS Old Generation为341.5MB还要更多，
另外Eden Space与From Space、To Space也不是8:1:1的比例，
![](https://cdn.nlark.com/yuque/0/2024/png/26026237/1704710102217-b5a8de8b-3818-42dd-94ad-847dabd39137.png#averageHue=%23171110&clientId=u743ba93c-2997-4&id=Eezh6&originHeight=1600&originWidth=866&originalType=binary&ratio=1&rotation=0&showTitle=false&status=done&style=none&taskId=u0b69f25c-6e09-42c7-a626-e9fa70ee817&title=)

#### 2、接下来我们用-XX:+UseSerialGC：Serial+Serial Old 的 GC 回收器组合。试下
看 Heap Usage 详情，多了一块 New Generation=Eden+1 Survivor Space。
如果再加上另外 1 个 Survivor Space(From Space 或者 To Space)，刚好是 42.0MB，同 OldSize 的比值是 1：2，没有任何问题。
另外，Eden、Survivor From、Survivor To 的比值也明显是 8：1：1。
![](https://jvm-1300830970.cos.ap-shanghai.myqcloud.com/%E4%BD%BF%E7%94%A8-XX%3A%2BUseSerialGC%E5%86%85%E5%AD%98%E5%88%86%E5%8C%BA.png#id=sEtrJ&originHeight=1740&originWidth=1396&originalType=binary&ratio=1&rotation=0&showTitle=false&status=done&style=none&title=)

#### 3、在看-XX:+UseConcMarkSweepGC：ParNew+CMS 的 GC 回收器组合。
看Heap Usage详情，和-XX:+UseSerizlGC基本一致，没啥问题
![](https://jvm-1300830970.cos.ap-shanghai.myqcloud.com/%E4%BD%BF%E7%94%A8-XX%3A%2BUseConcMarkSweepGC%E5%86%85%E5%AD%98%E5%8D%A0%E7%94%A8.png#id=iqpiY&originHeight=1714&originWidth=1358&originalType=binary&ratio=1&rotation=0&showTitle=false&status=done&style=none&title=)

#### 4、接下来看-XX:+UseG1GC：G1 回收器
看 Heap Usage 详情，也不合符 NewRatio 标识的 1：2 的默认比值。另外 From Space 和 To Space 也消失了
![](https://jvm-1300830970.cos.ap-shanghai.myqcloud.com/%E4%BD%BF%E7%94%A8G1%20-XX%3A%2BUseG1GC%E7%9A%84%E5%86%85%E5%AD%98%E5%88%86%E5%B8%83.png#id=ctPuq&originHeight=1704&originWidth=1398&originalType=binary&ratio=1&rotation=0&showTitle=false&status=done&style=none&title=)

为什么会出现这样的情况呢，因为UseAdaptiveSizePolicy，该配置默认是开启的，直到最新版的jdk17依然还是开启的
该参数堆-XX:+UseSerialGC和-XX:+UseConcMarkSweepGC无论开启与否，均不生效
G1 回收器也是遵循分代收集理论的，但是会把连续的 Java 堆不区分新生代、老年代的情况下而划分为大小相等的 Region，每个 Region 都会根据需要扮演新生代或老年代的空间。
G1 仍然保留了新生代、老年代，并且新生代也区分 Eden 和 Survivor，只是 Survivor 不再区分 From 和 To。
而 ZGC 和 ShenandoahGC 的话，颠覆的比较彻底，已经不再区分新生代和老年代了，也就是说不再使用分代收集，默认比值多少的问题已经没有意义了。
该参数对应的是 GC 自适应的调节策略(GC Ergonomics)，如果开启，那么 JVM 会根据系统的运行情况，动态调整一些参数，包括：新生代和老年代的比值。
Eden、Survivor From、Survivor To 的比值;大对象直接进入老年代的阈值等，以达到吞吐量优先的目标。
#### 5、关闭UseAdaptiveSizePolicy看一下
开启参数是在 -XX 后面带加号，关闭参数是在 -XX 后面带减号。启动进程时候添加 JVM 参数 -XX:-UseAdaptiveSizePolicy。
下图可以看到关闭了之后是按照默认的比例
![](https://jvm-1300830970.cos.ap-shanghai.myqcloud.com/%E5%B9%B6%E8%A1%8CGC%E5%85%B3%E9%97%AD%E4%BA%86-XX%3A-UseAdaptiveSizePolicy%E5%86%85%E5%AD%98%E5%88%86%E5%8C%BA.png #id=Xb0AH&originHeight=1590&originWidth=1712&originalType=binary&ratio=1&rotation=0&showTitle=false&status=done&style=none&title=)
