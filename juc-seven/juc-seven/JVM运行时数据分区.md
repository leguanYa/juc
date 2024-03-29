参考Guide哥的JavaGuide[链接](https://javaguide.cn/java/jvm/memory-area.html#java-%E8%99%9A%E6%8B%9F%E6%9C%BA%E6%A0%88)
## 前言
对于Java程序而言，在虚拟机的自动内存管理机制下，不用再像C语言那样手动去操作，不容易出现内存泄漏和内存溢出的问题。同样的正是因为把内存的控制权交给虚拟机去控制，一旦出现内存泄露和溢出的问题，如果不了解是怎么使用的，那么排查问题是一个非常难的问题
## 运行时数据区域
基于JDK1.8的数据分区如下图所示
![image.png](https://cdn.nlark.com/yuque/0/2024/png/26026237/1704422945750-df38e921-23cb-4dda-8131-6794dfa17e0f.png#averageHue=%23373e45&clientId=u91a2b459-fc6d-4&from=paste&height=331&id=ub1296dfb&originHeight=804&originWidth=1584&originalType=binary&ratio=1&rotation=0&showTitle=false&size=52229&status=done&style=none&taskId=u66f555d6-f713-4786-b15c-19ad0a10bb5&title=&width=652)
绿色的三个（虚拟机栈、本地方法栈、程序计数器）是线程私有的
粉色的两个（堆、方法区）是线程之间共享的
那么每个模块都是负责什么事情呢？
### 程序计数器
程序计数器是一个比较小的内存空间，可以看作是当前线程所执行的字节码的行号指示器，字节码的解释器工作是通过改变这个这个计数器的值来选取下一条需要执行的字节码的指令，分支、循环、跳转、异常处理、线程恢复等功能都需要依赖这个计数器完成
为什么称他是线程私有的呢，因为多线程之间，可能存在切换或者线程到指定的地方因为CPU的时间片被释放等原因导致阻塞，所以每个线程之间要独立存储，故称之为“线程私有”。
主要的两个作用

- 字节码解释器通过改变程序计数器来一次读取指令，从而实现代码流程上的控制，如：顺序执行、选择、循环、异常处理等
- 在多线程情况下，程序计数器用于记录当前线程执行的位置，从而当线程切换或者被唤醒的时候能够知道该线程运行到哪里了
### Java虚拟机栈
与程序计数器一样，也是线程私有的，生命周期与线程相同，随着线程的创建而创建，随着线程的死亡而死亡。
方法调用的时候，都需要栈帧去传递，每一次的方法调用都会有一个对应的栈帧被压入栈中，每个方法结束后，都会有一个栈帧被弹出
栈由一个个栈帧组成，而每个栈帧都拥有：局部变量表、操作数栈、动态连接、方法返回地址。和数据结构上的栈类似，都是先进后出的数据结构。如图所示：
![image.png](https://cdn.nlark.com/yuque/0/2024/png/26026237/1704433314372-8a376948-ae81-4be9-8d8a-920fded7f715.png#averageHue=%23b6db94&clientId=u91a2b459-fc6d-4&from=paste&height=595&id=ufed382e8&originHeight=1200&originWidth=478&originalType=binary&ratio=1&rotation=0&showTitle=false&size=48505&status=done&style=none&taskId=u9a49e0c2-0185-475e-b249-91c0315238a&title=&width=237)
接下来看一个简单的代码：
```java
public class LeGuanTest {
    public int compute() {
        int a = 1;
        int b = 2;
        int c = (a + b) * 10;
        return c;
    }
    public static void main(String[] args) {
        LeGuanTest leGuanTest = new LeGuanTest();
        int compute = leGuanTest.compute();
        System.out.println(compute);
    }
}
```
使用javap -c  LeGuanTest.class 对代码进行反汇编的结果如下
```java
Compiled from "LeGuanTest.java"
public class com.leguan.jvmone.LeGuanTest {
  public com.leguan.jvmone.LeGuanTest();
    Code:
       0: aload_0
       1: invokespecial #1                  // Method java/lang/Object."<init>":()V
       4: return

  public int compute();
    Code:
       0: iconst_1
       1: istore_1
       2: iconst_2
       3: istore_2
       4: iload_1
       5: iload_2
       6: iadd
       7: bipush        10
       9: imul
      10: istore_3
      11: iload_3
      12: ireturn

  public static void main(java.lang.String[]);
    Code:
       0: new           #2                  // class com/leguan/jvmone/LeGuanTest
       3: dup
       4: invokespecial #3                  // Method "<init>":()V
       7: astore_1
       8: aload_1
       9: invokevirtual #4                  // Method compute:()I
      12: istore_2
      13: getstatic     #5                  // Field java/lang/System.out:Ljava/io/PrintStream;
      16: iload_2
      17: invokevirtual #6                  // Method java/io/PrintStream.println:(I)V
      20: return
}

```
首先看下public int compute();中这些属性，
iconst_1 将int类型常量1压入**操作数栈 ，**istore_1 将int类型值存入局部变量1 ，
iload_1 从局部变量1中装载int类型值，iload_2 从局部变量2中装载int类型值，iadd进行相加，bipush 将一个8位带符号整数压入栈，imul 执行int类型的乘法，istore_3 将int类型值存入局部变量3，iload_3 从局部变量3中装载int类型值，ireturn 从方法中返回int类型的数，istore_1、iload_1这样的操作都是在操作数栈上完成的，
**操作数栈**：主要作为方法调用的中转站使用，用户存放方法执行过程产生的中间计算结果。
**局部变量表：**主要存放了编译器克制的各种数据类型（boolean、byte、char、short、int、float、long、double）、对象引用（reference类型，它不同于对象本身，可能是一个指向对象其实地址的引用指针，也可能是指向一个代表对象的句柄或其他与此对象相关的位置）
**动态链接：**主要服务一个方法需要调用其他方法的场景，Class文件的常量池保存有大量的符号引用，比如说方法的符号引用，当一个方法要调用其他方法，需要将常量池中指向方法的符号引用转化器在内存地址的直接引用。动态链接的作用就是为了符号引用转换为调用方法的直接引用，故这个过程称之为动态链接。
栈空间虽然不是无限的，但一般正常情况下是不会出问题的，不过，如果函数调用陷入无限的循环的时候，就会导致栈中被压入太多栈帧而导致占用太多空间，导致栈空间过深，那么当线程请求栈的深度超过当前Java虚拟机最大深度的时候，就抛出`StackOverFlowError`错误
Java 方法有两种返回方式，一种是 return 语句正常返回，一种是抛出异常。不管哪种返回方式，都会导致栈帧被弹出。也就是说， **栈帧随着方法调用而创建，随着方法结束而销毁。无论方法正常完成还是异常完成都算作方法结束。**
除了 `StackOverFlowError` 错误之外，栈还可能会出现OutOfMemoryError错误，这是因为如果栈的内存大小可以动态扩展， 如果虚拟机在动态扩展栈时无法申请到足够的内存空间，则抛出OutOfMemoryError异常。
总结出现的两种错误：

- StackOverFlowError：若栈的内存大小不允许动态扩展，那么当线程请求栈的深度超过当前Java虚拟机的最大深度的时候，就抛出该异常
- OutOfMemoryError：如果栈的内存大小可以动态扩展，如果虚拟机在动态扩展栈时无法申请到足够的内存空间，则抛出该异常
### 本地方法栈：
本地方法栈执行的是虚拟机使用到的Native方法服务，在HotSpot虚拟机中和Java虚拟机合二而已，本地方法栈被执行的时候，在本地方法栈上也会创建一个栈帧，用于存放该本地方法的局部变量表、操作数栈、动态链接、出口信息。
方法执行完毕后相应的栈帧也会出栈并释放内存空间，也会出现 `StackOverFlowError` 和 `OutOfMemoryError` 两种错误。
### 堆
Java 世界中“几乎”所有的对象都在堆中分配，但是，随着 JIT 编译器的发展与逃逸分析技术逐渐成熟，栈上分配、标量替换优化技术将会导致一些微妙的变化，所有的对象都分配到堆上也渐渐变得不那么“绝对”了。从 JDK 1.7 开始已经默认开启逃逸分析，如果某些方法中的对象引用没有被返回或者未被外面使用（也就是未逃逸出去），那么对象可以直接在栈上分配内存。
Java 堆是垃圾收集器管理的主要区域，因此也被称作 **GC 堆（Garbage Collected Heap）**。从垃圾回收的角度，由于现在收集器基本都采用分代垃圾收集算法，所以 Java 堆还可以细分为：新生代和老年代；再细致一点有：Eden、Survivor、Old 等空间。进一步划分的目的是更好地回收内存，或者更快地分配内存。
分布图如下：
![image.png](https://cdn.nlark.com/yuque/0/2024/png/26026237/1704441065960-3713a21b-4796-405d-8d5d-c5ebccc9129d.png#averageHue=%23b8dd96&clientId=u7b70f449-554a-4&from=paste&height=250&id=u280080e9&originHeight=564&originWidth=1704&originalType=binary&ratio=1&rotation=0&showTitle=false&size=52998&status=done&style=none&taskId=u775811ed-2f3b-4821-8b29-dcb5af7bc98&title=&width=756)
从图中可以看出可以分为两大类：

- 新生代：这里面又包含了Eden和survivor区（分别又分为s0、s1）
- 老年代

新创建的对象大部分情况下会在Eden区分配，经过minor GC后存活的对象在GC年龄中+1放到s0区，然后如果在经过minor GC的话会将Eden区和s0区存活的对象放到s1区，在经过minor GC的话将Eden区和s1区存活的对象放到s0区，如此往下，如果达到了默认的配置的GC年龄15会将对象移到老年代中，如果老年代的容量也满了，那么会触发Full GC，
注意：进行GC的时候会触发STW（Stop The world），会停止用户线程的执行，这样做的原因是：

- 避免浮动的垃圾：如果不暂停的话，可能会导致刚刚标记完之后，又产生了新的垃圾
- 确保一致性：要确保在同一个快照下执行，如果此时还有用户线程进来，会导致分析结果的不准确性
- 防止引用混乱：在进行复制和标记算法的整理中，会导致原引用的对象地址发生变化
### 方法区
方法区也称之为MetaSpace（元空间），使用的直接物理内存，当虚拟机要使用一个类的时候，他需要读取并解析Class文件或去相关信息，在将信息存入到方法区，方法区会存储已被虚拟机加载的**类信息、字段信息、方法信息、常量、静态变量、即时编译器编译后的代码缓存等数据**
这一部分也是会出现OOM的风险的，如果说MetaSpace的空间超过了一定的值那么这个时候会需要进行Full GC的，也会STW的，
**-XX：MaxMetaspaceSize**： 设置元空间最大值， 默认是-1， 即不限制， 或者说只受限于本地内存大小。 
**-XX：MetaspaceSize**： 指定元空间触发Fullgc的初始阈值(元空间无固定初始大小)， 以字节为单位，默认是21M，达到该值就会触发full gc进行类型卸载， 同时收集器会对该值进行调整： 如果释放了大量的空间， 就适当降低该值； 如果释放了很少的空间， 那么在不超过-XX：MaxMetaspaceSize（如果设置了的话） 的情况下， 适当提高该值。
