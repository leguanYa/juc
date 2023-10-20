1、线程的基础知识
进程：

* 程序是由执行和数据组成，但这些指令要运行，数据要读写，就必须将指令加载至CPU，数据加载到内存。在指令运行过程中，还需要用到磁盘、网络等设备。<font color='red'>进程就是用来加载至指令、管理内存、管理IO的</font>
* 当一个程序被运行，从磁盘加载这个程序的代码至内存，这时就开启了一个进程
* 进程可以视为程序的一个实例，大部分程序可以同时运行多个实例进程（例如记事本、画图、浏览器等），也有的程序只能启动一个进程实例（例如网易云）
* <font color= 'red'>操作系统会以进程为单位，分配系统资源（例如CPU时间片，内存等资源），进程是资源分配的最小单位</font>

线程：

* 线程是进程的实体，一个进程可以拥有多个线程，一个线程必须有一个父进程
* 一个线程就是一个指令流，将指令流中的一条条指令以一定的顺序交给CPU执行
* 线程，有时被称为轻量级进程（Lightweight Process, LWP）,是操作系统调度（CPU执行的最小单元）

进程和线程的区别
* 进程基本上相互独立，二线程存在与进程内，是进程的一个子集
* 进程拥有共享的资源，如内存空间等，供其内部的线程共享
* 进程间通信较为复杂  
* * 同一台计算机的进程通信称为IPC
* * 不同计算机之间的进程通信需要通过网络，并遵守共同的协议，例如HTTP
* 线程通信相对简单，因为他们共享进程内的内存，多个线程可以访问同一个共享变量
* 线程更轻量，线程上下文切换一般要比进程上下文切换低

