前言，参考Guide哥的JavaGuide的做一个学习总结[链接](https://javaguide.cn/java/jvm/class-loading-process.html)
## 什么是类加载机制？
类加载机制就是将我们的class文件加载到内存中，并对数据进行校验、转换解析和初始化，最后形成可以被虚拟机直接使用的Java类型，这个过程被称作为虚拟机的类加载机制。
## 类的生命周期
类从被加载到虚拟机内存中开始到卸载除内存为止，他的整个生命周期简单的分为7个阶段：加载、验证、准备、解析、初始化、使用和卸载。其中，验证、准备和解析这三个阶段可以称之为连接。
![image.png](https://cdn.nlark.com/yuque/0/2024/png/26026237/1704354963800-18061716-58d0-49ae-88aa-b538c4b073fd.png#averageHue=%23fdfdfb&clientId=u24ebdcfc-be1c-4&from=paste&height=680&id=u1b773c43&originHeight=680&originWidth=2078&originalType=binary&ratio=1&rotation=0&showTitle=false&size=292293&status=done&style=none&taskId=u3b9537fa-6e9e-4f4f-9d8d-b781c554b5d&title=&width=2078)
其中加载、验证、准备、初始化、卸载这五个节点的顺序是固定的，解析阶段不一定，他可以在初始化后进行开始，因为要支持运行时绑定的特性（动态绑定或晚期绑定），因为有的时候这些阶段会互相交叉的混合进行的，会在一个阶段执行的过程中、调用、激活另一个阶段
## 类加载过程
类加载过程分为三大步：加载--->连接--->初始化，其中连接又分为了：验证--->准备--->解析，这三步
那么每个步骤都是做的什么事情呢：

- 加载：在硬盘上查找并通过IO读入字节码问价，使用到类时才会被加载，例如调用类的main()方法等等，在加载阶段会在内存中生成一个代表这个类的`Class`对象，作为方法区这个类的各种数据的访问入口
- 验证：校验字节码文件的正确性（文件格式的验证、元数据验证、字节码验证、符号引用验证）
- 准备：给类的静态变量分配内存，并赋予默认值
- 解析：虚拟机将常量池内的**符号引用**替换为**直接引用**的过程， 符号引用可以简单的理解为：我们写的Java程序中一些方法（比如写的mian()方法、写的test方法等），定义的一些变量什么的，可以简单的理解为这些，可以是任何的字面量，但是符号引用与虚拟机实现的内存的布局无关，最后会加载到JVM的内存地址中去，加载完之后要有对应的内存地址，这个内存地址就是直接引用，
总结也就是：将常量吃内的符号引用替换为直接引用的过程，也就是得到类、字段、方法在内存中的指针或者偏移量
- 初始化：对类的静态变量初始化为指定的值，执行静态代码块

需要注意的是，如果在运行过程中如果使用到其他类，会逐步加载这些类。像程序中引入的依赖包中类并不是一次性全部加载的，是用到是才会去加载：
测试代码：
```java
public class TestOne {

    static {
        System.out.println("------load TestOne-----");
    }

    public static void main(String[] args) {
        A a = new A();
        System.out.println("------load test-----");
        B b = null;
        //B b = new B();
    }
}

class A {
    static {
        System.out.println("------load A-----");
    }
    public A() {
        System.out.println("------init A-----");
    }
}
class B {
    static {
        C c = new C();
        System.out.println("------load B-----");
    }
    public B() {
        System.out.println("------init B-----");
    }
}

class C {
    static {
        System.out.println("------load C-----");
    }
    public C() {
        System.out.println("------init C-----");
    }
}

//B b = null; 输出结果
------load TestOne-----
------load A-----
------init A-----
------load test-----


//B b = new B(); 输出结果
------load TestOne-----
------load A-----
------init A-----
------load test-----
------load C-----
------init C-----
------load B-----
------init B-----


```
从第一个输出结果中可以看到B如果没有被用到的话，不会去加载
但是第二个输出结果中，因为B的静态代码块中使用了C，那么C先会去处理，这个也就是上面所说的解析为什么不是按照顺序执行的一个小例子
## 类的卸载
卸载类即该类的Class对象被GC
卸载类满足的三个要求：

- 该类的所有实力对象都已被GC，也就是说堆中不存在该类的实例对象
- 该类没有在其他的任何地方被引用
- 该类的类加载器的实例已被GC

所以，在JVM生命周期中，由JVM自带的类加载器加载的类是不会被卸载的。但是由于自定义的类加载器加载的类是可能被卸载的
## 类加载器和双亲委派机制
### 类加载器
上面的类加载过程主要是通过类加载器来实现的，Java里有如下几个类加载器

- 引导类加载器：负责加载支撑JVM运行的位于JRE的lib目录下的核心库，比如rt.jar、charsets.jar等
- 扩展类加载器：负载加载支撑JVM运行的位于JRE的lib目录下的ext扩展目录中的JAR类包
- 应用程序类加载器：负责加载ClassPath路径下的类包，主要就是加载程序中我们自己写的那些类
- 自定义加载器：负责加载用户自定义路径下的类包

这几个类加载的关系为：
![image.png](https://cdn.nlark.com/yuque/0/2024/png/26026237/1704363108630-19a34ab0-7248-4e12-a221-83b5642e7ac9.png#averageHue=%23f9f9f9&clientId=u24ebdcfc-be1c-4&from=paste&height=568&id=u53930ca0&originHeight=1052&originWidth=972&originalType=binary&ratio=1&rotation=0&showTitle=false&size=91996&status=done&style=none&taskId=u94ff0f38-8aaa-434d-8feb-b6e31b62ba3&title=&width=525)
每个类加载器都有一个parent属性，代表是父类的加载器
构建扩展类加载器和应用程序类加载器这个是由`Launcher`初始化的时候构建的，并绑定的上下级关系，
而`Launcher`的初始化的步骤是由引导类加载也就是虚拟机去调用的，这个地方debug不了，
接下来我们看下`Launcher`的构造方法（只看下主要的方法）：
```java
public Launcher() {
    // 创建扩展类加载器
    ClassLoader extcl;
    try {
        extcl = ExtClassLoader.getExtClassLoader();
    } catch (IOException e) {
        throw new InternalError(
            "Could not create extension class loader", e);
    }

    // 创建应用程序类加载器
    try {
        loader = AppClassLoader.getAppClassLoader(extcl);
    } catch (IOException e) {
        throw new InternalError(
            "Could not create application class loader", e);
    }
}

public static ClassLoader getAppClassLoader(final ClassLoader extcl)
            throws IOException
        {
            final String s = System.getProperty("java.class.path");
            final File[] path = (s == null) ? new File[0] : getClassPath(s);

            return AccessController.doPrivileged(
                new PrivilegedAction<AppClassLoader>() {
                    public AppClassLoader run() {
                    URL[] urls =
                        (s == null) ? new URL[0] : pathToURLs(path);
                    // 构造应用程序类加载器
                    return new AppClassLoader(urls, extcl);
                }
            });
        }

        AppClassLoader(URL[] urls, ClassLoader parent) {
            // 将扩展类加载器作为自己的父属性（注意不是父类，是parent属性）的加载器
            super(urls, parent, factory);
            ucp = SharedSecrets.getJavaNetAccess().getURLClassPath(this);
            ucp.initLookupCache(this);
        }
```
类加载样例代码：
```java
public class ZhangTestOne {
    public static void main(String[] args) {
        System.out.println(String.class.getClassLoader());
        System.out.println((com.sun.crypto.provider.DESKeyFactory.class.getClassLoader()));
        System.out.println(ZhangTestOne.class.getClassLoader());


        System.out.println();
        ClassLoader appClassLoader = ClassLoader.getSystemClassLoader();
        ClassLoader extClassloader = appClassLoader.getParent();
        ClassLoader bootstrapLoader = extClassloader.getParent();
        System.out.println("the appClassLoader : " + appClassLoader);
        System.out.println("the extClassloader : " + extClassloader);
        System.out.println("the bootstrapLoader : " + bootstrapLoader);
    }
}


//输出结果
null
sun.misc.Launcher$ExtClassLoader@7f31245a
sun.misc.Launcher$AppClassLoader@18b4aac2

the appClassLoader : sun.misc.Launcher$AppClassLoader@18b4aac2
the extClassloader : sun.misc.Launcher$ExtClassLoader@7f31245a
the bootstrapLoader : null


```
从数据结果中可以看出，appClassLoader的父加载器为extClassloader，extClassloader的父加载器为bootstrapLoader， 因为bootstrapLoader是虚拟机实现的的所以这里为null
###  双亲委派机制
双亲委派机制就是指的：加载某个类时会先委托父加载器去寻找目标类，找不到在往上层去找，如果还是是找不到目标类，那么则由子加载器再去加载。
看下loadClass的源代码（java.lang.ClassLoader#loadClass(java.lang.String, boolean)）：
```java
protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
    synchronized (getClassLoadingLock(name)) {
        // 先去找是否已经加载过
        Class<?> c = findLoadedClass(name);
        if (c == null) {
            long t0 = System.nanoTime();
            try {
                // 判断父类加载器是否为空
                if (parent != null) {
                    // 调用父类加载器
                    c = parent.loadClass(name, false);
                } else {
                    // 到顶层了，如果都没有，去引导类加载器找寻
                    c = findBootstrapClassOrNull(name);
                }
            } catch (ClassNotFoundException e) {
                // ClassNotFoundException thrown if class not found
                // from the non-null parent class loader
            }

            if (c == null) {
                // If still not found, then invoke findClass in order
                // to find the class.
                long t1 = System.nanoTime();
                //都会调用URLClassLoader的findClass方法在加载器的类路径里查找并加载该类
                c = findClass(name);

                // this is the defining class loader; record the stats
                sun.misc.PerfCounter.getParentDelegationTime().addTime(t1 - t0);
                sun.misc.PerfCounter.getFindClassTime().addElapsedTimeFrom(t1);
                sun.misc.PerfCounter.getFindClasses().increment();
            }
        }
        if (resolve) {
            resolveClass(c);
        }
        return c;
    }
}
```
 为什么要设计双亲委派机制：

- 沙箱安全机制：编码规范，有的时候会有在自己的项目中定义一些：java.lang.String这些不需要应用程序加载的类，如果我们写了这个类，那么就会容易篡改里面的一些东西，可以防止核心API库被所以篡改
- 避免类的重复加载：当父类加载器已经加载了该类是，就没有必要再加载一次了，保证被加载类的唯一性

比如说我们定义一个String类，包名为java.lang， 代码如下:
```java
package java.lang;

public class String {
    public static void main(String[] args) {
        System.out.println("test");
    }
}

//输出结果
错误: 在类 java.lang.String 中找不到 main 方法, 请将 main 方法定义为:
   public static void main(String[] args)
否则 JavaFX 应用程序类必须扩展javafx.application.Application
```
 因为java.lang.String已经被加载过了，故在里面找不到main方法
自定义类加载器示例：
自定类加载器只需要继承java.lang.ClassLoader类，该类有两个核心方法，一个是loadClass(String, boolean),实现了双亲委派机制，还有一个是findClass，默认实现是空方法，所以自定义类加载器主要是重写findClass方法。
代码如下:
```java
public class MyClassLoader extends ClassLoader {
    private String classPath;
    public MyClassLoader(String classPath) {
        this.classPath = classPath;
    }
    private byte[] loadByte(String name) throws Exception {
        String path = name.replace(".", "/").concat(".class");
        FileInputStream fis = new FileInputStream(classPath + "/" + path);
        int len = fis.available();
        byte[] data = new byte[len];
        fis.read(data);
        fis.close();
        return data;

    }
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            byte[] data = loadByte(name);
            //defineClass将一个字节数组转为Class对象，这个字节数组是class文件读取后最终的字节 数组。
            return defineClass(name, data, 0, data.length);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ClassNotFoundException();
        }
    }
}


public class TestTwo {
    public static void main(String[] args) throws Exception {
        MyClassLoader myClassLoaderTest = new MyClassLoader("/Users/zhanghui/dev/github");
        Class<?> aClass = myClassLoaderTest.loadClass("com.leguan.jvmone.User1");
        Object o = aClass.newInstance();
        Method t1 = aClass.getDeclaredMethod("sou", null);
        t1.invoke(o, null);
        System.out.println(aClass.getClassLoader().getClass().getName());
    }
}


//输出结果
user1
sun.misc.Launcher$AppClassLoader

```
 从输出结果中可以看出，输出的类加载器是AppClassLoader这是为什么呢，因为这个是因为，在项目中相同的包下已经存在一个User1的类了，那么这个类会首先由AppClassLoader去加载，如果我们将项目的User1的类class文件删除，那么这个时候读区的类加载器就是自定义的加载器了
```java
user1
sun.misc.Launcher$AppClassLoader
```
### 该如何打破双亲委派机制
上面我们介绍了是在loadClass中使用的父类递增的方式去加载，那么我们只需要重写这个方法就好了，不要调用父加载器的loadClass



