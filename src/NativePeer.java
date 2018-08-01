/**
 * 这里简单说明一下native method本地方法
 *
     * Java有能力调用其他语言编写的函数or方法，这个通过JNI(Java Native Interfface)实现。使用时，
     * 通过native关键字告诉JVM这个方法是在外部定义的。但JVM也不知去哪找这个原生方法，
     * 举个C语言的例子，此时需要通过javah命令生成.h文件，示例步骤:
     *      (1).javac生成.class文件，比如javac NativePeer.java
     *      (2).javah生成.h文件，比如javah NativePeer //不需要加.class后缀
     *      (3).编写c语言文件，在其中include进上一步生成的.h文件，然后实现其中声明而未实现的函数
     *      (4).生成dll共享库，然后Java程序load库，调用即可
     *
     * native可以和任何除abstract外的关键字连用，这也说明了这些方法是有实体的，并且能够和其他Java方法一样，
     * 拥有各种Java的特性。
     *
     * native method有效地扩充了jvm，实际上我们所写过的很多代码已经涉及到这种方法了，比如多线程并发控制中，
     * 很多与操作系统的接触点都用到了这种方式，通过非常简洁的接口帮我们实现Java以外的工作
     *
     * 优势:
     *  (1).很多层次上用Java去实现是很麻烦的，而且Java解释执行的效率也差了c语言啥的很多，纯Java实现可能会导致效率不达标，
     *  或者可读性奇差。
     *  (2).Java毕竟不是一个完整的系统，它经常需要一些底层的支持，通过JNI和native method我们就可以实现jre与底层的交互，
     *  得到强大的底层操作系统的支持，使用一些Java本身没有封装的操作系统的特性。
     *
 * @author LightDance
 */
public class NativePeer {

    /**example1*/
    native int nativeMethod(int[] array);

    /**example2*/
    native synchronized int synchronizedNativeMethod();

    /**example3*/
    native static void staticNativeMethod();
}
