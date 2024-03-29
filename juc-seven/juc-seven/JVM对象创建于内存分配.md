## 对象创建
对象创建的步骤图如下：
![image.png](https://cdn.nlark.com/yuque/0/2024/png/26026237/1704709743829-8613ce07-faf5-4525-8bfc-39e8572cebff.png#averageHue=%23fafafa&clientId=u5892dac6-2f54-4&from=paste&height=723&id=ueee6a194&originHeight=1200&originWidth=606&originalType=binary&ratio=2&rotation=0&showTitle=false&size=67438&status=done&style=none&taskId=u1d906a72-3bed-4fe8-9213-bba1672ce97&title=&width=365)
### 类检查
虚拟机遇到一条new指令的时候，首先将去检查张辉个指令的参数是否能在常量池中定位到一个类的符号引用，并且检查张辉个符号引用代表的类是否已经被加载、解析和初始化过。如果没有，那必须先执行相应的类加载过程
### 分配内存
在类的加载通过后，接下来为新生对象分配内存，对象所需的内存大小在类加载后便可完全确定，为对象分配空间的任务等同于把一块确定大小的内存从Java堆中划分出来
两个问题：

- 如何划分内存？
- 在并发情况下，可能出现正在给对象A分配内存，指针还没来得及去修改，对象B又同时使用了原来的指针来分配内存的情况？

划分内存的方式：

- 指针碰撞（默认使用指针碰撞）
如果Java堆中的内存是相对规整的，所有用过的内存都放在一起规整起来，空闲的内存放到另一边，中间放着一个指针作为分界点的指示器，那所分配的内存就仅仅是把哪个指针向空闲的空间那边挪动一段与对象大小相等的距离
- 空闲列表
如果Java堆中的空间是不规整的，已使用的内存和空闲的内存相互交错，那就没有办法进行指针碰撞了，虚拟机就必须维护一个列表，记录上哪些内存块是可用的，在分配的时候从列表中找到一块足够大的内存空间划分给对象实例，并更新列表上的记录

解决并发的方式：

- CAS：虚拟机采用CAS配上**失败重试**的方式保证更新操作的原子性来对分配内存空间的动作进行同步处理
- 本地线程分配缓冲（Thread Local Allocation Buffer,简称TLAB）
把内存分配的动作按照线程划分在不同的空间之中进行，也就是每个线程在Java堆中预先分配一小块内存。如果新的实例不够在TLAB划分的内存中分配的，则还是会在堆区中进行分配，
可以通过**­-XX:+/­-UseTLAB**参数来设定虚拟机是否使用**TLAB**(JVM会默认开启**­-XX:+UseTLAB**)，­
**-XX:TLABSize **指定TLAB大小。
### 初始化
内存分配完成之后，虚拟机需要将分配到的内存空间都初始化为零值（不含对象头），如果使用TLAB，这一工作过程也可以提起至TLAB分配进行。这一步操作保证了对象的实例字段在Java代码中可以不赋初始值就直接使用，程序能访问到这些字段的数据类型的锁对应的零值。
### 设置对象头
一个对象是由三部分组成：

- 对象头：
**MarkWord：**比如hash码，对象所属的年代，对象锁，锁状态标志，偏向锁（线程）ID，Epoch(偏向锁撤销会+1，以class为单位)，偏向时间
**Klass Point类型指针**：就是指向的类元数据信息（在方法区中）：虚拟机通过这个指针来确定这个对象是哪个类的实例。 32位4字节，64位开启指针压缩或最大堆内存<32g时4字节，否则8字节。jdk1.8默认开启指针压缩后为4字节，当在JVM参数中关闭指针压缩（-XX:- UseCompressedOops）后，长度为8字节，
**数组长度**：只有数组对象才有，如果对象是一个数组，那在对象头中还必须有一块数据用于记录数组长度。4字节
- 实例数据：
存放累积的属性数据信息，包括父类的属性信息
- 对齐填充：
由于虚拟机要求**对象起始地址必须是8字节的整数倍**。填充数据不是必须存在的，仅仅是为了对齐

示意图如下：
![image.png](https://cdn.nlark.com/yuque/0/2024/png/26026237/1704791368846-c3020f61-fbfc-4b0c-bdd6-b84f7effc6a4.png#averageHue=%23455146&clientId=u4f2d7391-aed2-4&from=paste&height=750&id=u1b68503f&originHeight=1500&originWidth=840&originalType=binary&ratio=2&rotation=0&showTitle=false&size=90474&status=done&style=none&taskId=ua8af70b1-5798-423e-958a-fbe051f89ce&title=&width=420)
对象头中Mark Word的构造（32位和64位）：
32位如图所示：

| 锁状态 | 25bit |  | 4bit | 1bit | 2bit |
| --- | --- | --- | --- | --- | --- |
|  | 23bit | 2bit | 分代年龄 | 是否偏向锁 | 锁标志位 |
| 无锁态 | 对象的hashCode |  | 分代年龄 | 0 | 01 |
| 偏向锁 | 线程ID | Epoch | 
 | 1 | 01 |
| 轻量级锁 | 指向栈中锁记录的指针 |  |  |  |  00  |
| 重量级锁 | 指向互斥锁（重量级锁）的指针 |  |  |  | 10 |
| GC标记 | 空 |  |  |  | 11 |

64位如图所示：

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

举例查看
我们在不添加任何JVM的参数，使用默认情况下，举例代码如下：
首先借助一个依赖包：
```java
<dependency>
    <groupId>org.openjdk.jol</groupId>
    <artifactId>jol-core</artifactId>
    <version>0.10</version>
</dependency>
```
 demo代码：

```java
public class JOLDemo {
    public static void main(String[] args) {
        ClassLayout classLayout = ClassLayout.parseInstance(new Object());
        System.out.println(classLayout.toPrintable());
        ClassLayout classLayout2 = ClassLayout.parseInstance(new int[]{});
        System.out.println(classLayout2.toPrintable());
        ClassLayout classLayout3 = ClassLayout.parseInstance(new ADemo());
        System.out.println(classLayout3.toPrintable());
    }
}

class ADemo {
    private int id;
    private String name;
    private byte b;
    private Object o;
}
```
 看下对应的输出结果：
![image.png](https://cdn.nlark.com/yuque/0/2024/png/26026237/1704792270347-485dd5d5-232f-44a6-8dcf-9a82bec0dedc.png#averageHue=%23242528&clientId=u4f2d7391-aed2-4&from=paste&height=878&id=u3f771ca0&originHeight=1756&originWidth=2238&originalType=binary&ratio=2&rotation=0&showTitle=false&size=309634&status=done&style=none&taskId=u168d660c-becc-47b4-8155-35935018674&title=&width=1119)
type为object header都是对象头的部分
接下来我们看下输出的第一个结果：
前面两个object header为mark word部分占用8个字节，第三个obejct header为klass pointer占用4个字节，因为要满足8的整数倍，所以对齐填充占用了4个字节，一共是16个字节
第二个结果：
前面两个object header为mark word部分占用8个字节，第三个obejct header为klass pointer占用4个字节，第四个obejct header为数组长度占用4个字节，满足8的整数倍了，所以无需对齐填充，总共占用16个字节
第三个结果：
前面两个object header为mark word部分占用8个字节，第三个obejct header为klass pointer占用4个字节，A类中定义了4个属性，int类型为4个字节，byte类型为1个字节（但是内部做了对齐填充）占用了4个字节，string类型因为默认会开启压缩对象头中的类型指针klass Pointer，占用了4个字节，同样的Object和String也是类型，占用4个字节，不满足8的整数倍，填充4个字节，总共为32字节
 ‐XX:+UseCompressedOops 默认开启的压缩所有指针
‐XX:+UseCompressedClassPointers 默认开启的压缩对象头里的类型指针Klass Pointer（这个JVM参数大概的意思就是：如果你生命的对象中有引用类型相关的属性，JVM默认会开启压缩指针）
### 执行<init>方法
执行<init>方法，即对象进行初始化，为属性赋值为默认值（例如静态变量中我们写的值）和执行构造方法

## 对象的内存分配
### 对象栈上分配：
通过JVM内存分配明白了，JAVA中创建的对象是在堆上进行分配的，当对象没有被引用的时候，需要依靠GC进行回收内存，如果对象数量较多的话，会频繁的进行GC，影响了机器的性能，为了减少临时对象在堆内存分配的数量，JVM会通过**逃逸分析来确定是否是在栈上进行分配，**如果能在栈上进行分配就会避免了在堆内存中创建对象，减少临时对象的产生，避免了频繁的GC，
逃逸分析：就是分析对象的动态作用域，当一个对象在方法中被定义后，可能会被外部的方法所引用，例如作为参数传递到其他地方中
```java
public User test1(){
    User user = new User();
    user.setName("张三");
    return user;
}

public void test2(){
    User user = new User();
    user.setName("张三");
}
```
例如上面的代码中：
**逃逸分析：**
test1方法，因为返回了User对象，不确定是不是被外部引用着，代表逃出了当前方法，不会在栈上分配
test2方法，因为没有返回，整个过程是在自己的方法中完成的，证明User对象没有逃逸出当前方法，则会转变为栈上分配。
JVM对于这种情况可以通过开启逃逸分析参数(-XX:+DoEscapeAnalysis)来优化对象内存分配位置，使其通过标量替换优 先分配在栈上(栈上分配)，JDK7之后默认开启逃逸分析，如果要关闭使用参数(-XX:-DoEscapeAnalysis)
**标量替换：**
前提是得通过逃逸分析确定该对象不会被外部访问，并且对象可以被进一步分解时，JVM不会创建该对象，而是将该对象成员变量分解若干个被这个方法使用的成员变量所代替，这些代替的成员变量在栈帧或寄存器上分配空间，这样就 不会因为没有一大块连续空间导致对象内存不够分配。开启标量替换参数(-XX:+EliminateAllocations)，JDK7之后默认 开启。
### 对象在Eden上分配
大多数情况下，对象在新生代中Eden区进行分配，当Eden区没有足够的空间进行分配的时候，将发起一次Minor GC，
**Minor GC** 和 **Full GC**有什么不同呢

- **Minor GC/Young GC：**指的是发生在新生代的垃圾收集动作，Minor GC非常频繁，回收速度一般也比较快
- **Major GC/Full GC**：一般会回收老年代、年轻代、方法区的垃圾，Major GC的速度一般会比Minor GC慢一些

**Eden与Surivvor区默认比例为8:1:1（但是默认的都是如此吗：参考这个**[**链接**](https://www.yuque.com/weizhi-dfvww/esczur/pqgd5g3sw26ocnde)**）**
大量的对象被分配在Eden区，Eden区满了之后会触发一次Minor GC，可能99%以上的对象成为垃圾被回收调，剩余存活的对象挪到为空的那块survivor区，下一次Eden区满了之后又会触发Minor GC，把Eden区和Survivor区垃圾对象回收掉，剩余的对象一次性挪到另外一块为空的Survivor区，因为新生代的对象都是朝生夕死的，存活时间很短。
JVM默认有这个参数-XX:+UseAdaptiveSizePolicy(默认开启)，会导致这个8:1:1比例自动变化，如果不想这个比例有变 化可以设置参数-XX:-UseAdaptiveSizePolicy
### 大对象直接进入老年代
大对象就是需要大量连续内存空间的对象（比如字符串、数组），JVM参数 -XX:PretenureSizeThreshold 可以设置大 对象的大小，如果对象超过设置大小会直接进入老年代，不会进入年轻代，这个参数只在 Serial 和ParNew两个收集器下有效。
比如设置JVM参数：-XX:PretenureSizeThreshold=1000000 (单位是字节) -XX:+UseSerialGC ，再执行下上面的第一 个程序会发现大对象直接进了老年代 为什么要这样呢？ 为了避免为大对象分配内存时的复制操作而降低效率。
### 长期存活的对象将进入老年代
既然虚拟机采用了分代收集的思想来管理内存，那么内存回收时就必须能识别哪些对象应放在新生代，哪些对象应放在 老年代中。为了做到这一点，虚拟机给每个对象一个对象年龄（Age）计数器。 如果对象在 Eden 出生并经过第一次 Minor GC 后仍然能够存活，并且能被 Survivor 容纳的话，将被移动到 Survivor 空间中，并将对象年龄设为1。对象在 Survivor 中每熬过一次 MinorGC，年龄就增加1岁，当它的年龄增加到一定程度 （默认为15岁，CMS收集器默认6岁，不同的垃圾收集器会略微有点不同），就会被晋升到老年代中。对象晋升到老年代 的年龄阈值，可以通过参数 -XX:MaxTenuringThreshold 来设置。
### 对象动态年龄判断
当触发一次Minor GC后，对象将要放到Survivor其中的一个区中，一批对象的总大小大与这块Survivor区域的内存大小的50%（**-XX:TargetSurvivorRatio可以指定**），那么此时大于等于这批对象年龄最大值的对象，就可以直接进入老年代了，例如Survivor区域中有一批对象，年龄1+年龄2+年龄n的多个年龄总和超过了Survivor区域的50%，此时就会包年龄n(含)以上的对象都放入到老年代。这个规则其实是希望那些可能是长期存活的对象，尽早的进入老年代。**对象动态年龄的判断一般是在Minor GC之后触发的**。
### 老年代空间分配担保机制
年轻代每次Minor GC之前JVM都会**计算老年代剩余可用空间**
如果老年代可用空间**小于**年轻代里现有的所有对象大小之和（包括垃圾对象），如果为否那么进行Minor GC，
如果小于的话，接下来会去判断一个参数**"-XX:-HandlePromotionFailure"**(jdk1.8默认就设置了)的参数是否设置了
如果有这个参数，就会看老年代的可用内存大小，是否大于之前每一次Minor GC后进入老年代对象的平均大小
如果没有配置这个参数或者老年代可用内存大小小于每一次Minor GC后进入老年代对象的平均大小，就会触发一次Full GC，对年轻代和老年代一起回收一次垃圾
如果回收完还是没有足够的内存存放新的对象，就会发生“OOM”
当然，如果Minor GC之后剩余存活的需要挪动到老年代的对象大小还是大于老年代可用空间，那么也会触发Full GC，Full GC完之后如果还是没有空间能容纳需要挪到老年代对象的话，还是会发生“OOM”
![image.png](https://cdn.nlark.com/yuque/0/2024/png/26026237/1704857611542-e23ab7bd-d0f0-45b5-9444-69c74c8d55cd.png#averageHue=%23f6f6f6&clientId=u4f2d7391-aed2-4&from=paste&height=1396&id=ufc147856&originHeight=1396&originWidth=2038&originalType=binary&ratio=2&rotation=0&showTitle=false&size=362101&status=done&style=none&taskId=u013d4921-ce6c-424e-957d-3d8f6a5225f&title=&width=2038)
### 对象内存回收
堆中几乎放着所有的对象示例，对堆垃圾回收前的第一步就是判断哪些对象已经死亡（即不能再被任何途径使用的对象）
#### 引用计数法
给对象中添加一个引用计数器，每当有一个地方引用它，计数器就加1；当引用失效，计数器就减1，任何时候计数器为0的对象就是不可能在被使用的
这个方法实现简单，效率高，但是目前主流的虚拟机只能够并没有选择这个算法来管理内存，其最主要的原因是它很难解决对象之间项目循环引用的问题。
所谓对象之间的项目引用问题，如下代码所示：除了对象objA和objB相互引用着对方之外，这两个对象之间再无任何引用。但是他们因为相互引用着对方，导致他们的引用计数器的值都不为0，于是引用计数器算法无法通知GC回收器回收他们。
```java
public class ReferenceCountingGc {
    Object instance = null;

    public static void main(String[] args) {
        ReferenceCountingGc objA = new ReferenceCountingGc();
        ReferenceCountingGc objB = new ReferenceCountingGc();
        objA.instance = objB;
        objB.instance = objA;
        objA = null;
        objB = null;
    }
}
```
#### 可达性分析算法
将“GC Roots” 对象作为起点，从这些节点开始向下搜索引用的对象，找到的对象都标记为非垃圾对象，其余未标记的对象都是垃圾对象
GC Root根节点：线程栈的本地变量、静态变量、本地方法栈的变量等等
![image.png](https://cdn.nlark.com/yuque/0/2024/png/26026237/1704875214910-49b66705-0cd9-4f5a-bf21-d27998e9821f.png#averageHue=%23ececea&clientId=u4f2d7391-aed2-4&from=paste&height=451&id=u1d3ffe01&originHeight=904&originWidth=1318&originalType=binary&ratio=2&rotation=0&showTitle=false&size=353968&status=done&style=none&taskId=u3803d53e-31a4-4e40-b36d-5d57cd71208&title=&width=657)
### 常见的引用类型
Java的引用类型一般分为四种：强引用、软引用、弱引用、虚引用
#### 强引用
普通的变量引用
```java
public static User user = new User();
```
#### 软引用
将对象用SoftReference软引用类型的对象包裹，正常情况下不会被回收，但是GC做完后发现释放不出内存空间存放新的对象，则会把这些软引用的对象回收掉。软引用可用来实现内存敏感的高速缓存
```java
public static SoftReference<User> user = new SoftReference<User>(new User());
```
软引用在实际中有重要的应用，例如浏览器的后退按钮。按后退时，这个后退时显示的网页内容是重新进行请求还是从 缓存中取出呢？这就要看具体的实现策略了。

1. 如果一个网页在浏览结束时就进行内容的回收，则按后退查看前面浏览过的页面时，需要重新构建
2. 如果将浏览过的网页存储到内存中会造成内存的大量浪费，甚至会造成内存溢出
#### 弱引用
将对象用WeakReference软引用类型的对象包裹，弱引用跟没引用差不多，GC会直接回收掉，很少用
```java
public static WeakReference<User> user = new WeakReference<User>(new User());
```
#### 虚引用
虚引用也称为幽灵引用或者幻影引用，它是最弱的一种引用关系，几乎不用
### finalize()方法最终判定对象是否存活
即使在可达性分析算法中不可达的对象，也并非是“非死不可”的，这时候它们暂时处于“缓刑”阶段，要真正宣告一 个对象死亡，至少要经历再次标记过程。
**标记的前提是对象在进行可达性分析后发现没有与GC Roots相连接的引用链**
**1. 第一次标记并进行一次筛选。**
筛选的条件是此对象是否有必要执行finalize()方法。 
当对象没有覆盖finalize方法，对象将直接被回收。
**2. 第二次标记**
如果这个对象覆盖了finalize方法，finalize方法是对象脱逃死亡命运的最后一次机会，如果对象要在finalize()中成功拯救自己，只要重新与引用链上的任何的一个对象建立关联即可，譬如把自己赋值给某个类变量或对象的成员变量，那在第 二次标记时它将移除出“即将回收”的集合。如果对象这时候还没逃脱，那基本上它就真的被回收了。 注意：一个对象的finalize()方法只会被执行一次，也就是说通过调用finalize方法自我救命的机会就一次
### 如何判断一个类是无用的类
方法区主要回收的是无用的类，那么如何判断一个类是无用的类的呢？
类需要同时满足下面3个条件才能算是 “无用的类” ：

- 该类所有的实例都已经被回收，也就是 Java 堆中不存在该类的任何实例。
- 加载该类的 ClassLoader 已经被回收。
- 该类对应的 java.lang.Class 对象没有在任何地方被引用，无法在任何地方通过反射访问该类的方法。
### 



