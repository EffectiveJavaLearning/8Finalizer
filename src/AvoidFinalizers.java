import java.util.Timer;

/**
 * Java中，GC虽然能够自动处理很多事情，但是比如来自其他编程语言所占资源的回收，是GC难以自动完成的，
 * 这时候或许需要借助额外的手段解决它。
 *
 * 终结方法(finalizer)由于调用时间不确定，危险且不可预测，会导致行为不稳定、性能降低、可移植性差等问题，
 * 所以我们应该尽量避免使用终结方法。
 *
 * “终结方法并不是C++中析构器的对应物”，在Java中，当一个对象变得不可到达时，会被GC处理掉，并回收占用空间，
 * 而C++中是由构造方法所对应的析构器完成回收资源工作。但C++中析构器也会做一些除回收资源以外的工作，
 * 在Java中则由try-catch-finally完成。
 *
 * 为什么不提倡用终结方法：
 *
 * *千万不要依赖终结方法更新重要的状态信息，比如释放共享资源锁。
 *
 * (1).终结方法主要缺点是它不能够保证被及时地执行。由于终结方法的线程优先级很低，从解除对某一对象的所有引用开始，
 * 到它的终结方法被执行，间隔时间是任意的，这样的话对时间要求严格的任务就会受到严重影响。比如，
 * 企图用终结方法关闭打开的文件，由于打开文件的描述符是一种非常有限的资源，而JVM又经常延迟执行终结方法，
 * 于是大量的文件描述符得不到释放，有可能会造成之后的文件打开失败、程序崩溃等。
 *
 * (2).不同的JVM对于“如何及时地执行终结方法”这件事的处理上可能会截然不同，有可能导致在不同电脑上，
 * 对同一个依赖终结方法执行时间点的任务，其运行结果差别可能非常大。
 *
 * (3).Java语言规范甚至不能保证终结方法一定会被执行。针对某些无法访问的对象的终结方法根本没有被执行是完全有可能的，
 * 就算用{@link System#gc()}和{@link System#runFinalization()}也是一样，它俩只是增加了finalizer被执行的机会，
 * 并不能保证一定执行。唯一号称能够保证终结方法一定会被执行的只有{@link System#runFinalizersOnExit(boolean)}和
 * {@link Runtime#runFinalizersOnExit(boolean)}，但这俩方法也因为其致命缺陷而被废弃。
 * 这也是为什么别依赖终结方法释放共享锁的至关重要的一条原因。
 *
 * **如果未被捕获的异常在终结方法中被抛出，那么这个异常时有可能会被忽略的，而该对象的终结方法也随之停止。
 * 这时候，如果其他线程希望使用这种状态下的对象，那么极有可能会发生一些奇奇怪怪的行为。并且，
 * 在终结方法中的异常与正常情况有所不同，可能不会终止线程，甚至不会打印出警告和栈轨迹。
 *
 * ***此外，终结方法的的性能损失不可忽视，会大大增加从创建对象到销毁对象所用时间。
 *
 * 显式终结方法代替finalizer:
 * 下面介绍解决方案：提供显式的终止方法，并要求该类的客户端在每个实例不再有用的时候调用这个方法。
 *  注：这种方案要在该对象的私有域中记录下“该对象是否已失效”，如果这些对象在终结后被调用，其他方法应检查这个记录，
 *  并决定是否抛出IllegalStateException.
 *
 * 典型的显示终结例子有{@link java.io.InputStream} , {@link java.io.OutputStream} ,
 * {@link java.sql.Connection#close()}.还有一个{@link Timer#cancel()}的例子，防止后面任务被加到该timeer中，
 * 令其更温和地终结自己。其他如awt包中一些跟GUI相关的例子，多由于性能不好而不被关注。
 *
 * 显式终结{@link #terminate()}方法常跟try-finally配合使用，以保证即使有异常导致线程终止，该终结方法也一定会被运行
 *
 * 终结方法的合法用途：
 * (1).调用显式的终结方法时，终结方法可以充当“安全网”，虽然未必及时，但万一显式终结方法无法被调用，
 * 可以防止一下资源无法回收，并能在日志中给出警告。决定是否使用这种方式前，应该考虑这种额外保护是否值得付出这些额外代价。
 * 前面提过的几个例子都使用了finalizer作为显式终结方法的安全网。
 *
 * (2).第二种合理用途与“本地对等体”(native peer)有关，主要涉及到Java调用非Java代码的问题{@link NativePeer}
 * native peer是一个本地对象(其他编程语言)，普通对象(java)通过本地方法(native method)，
 * 委托任务给本地对象(other programming language)。由于本地对等体并非普通对象，所以GC不会知道它。当它的Java对等体被回收时，
 * 它不会被回收。若该本地对等体并未占用关键资源，可以用finalizer终结它；若占用了关键资源，就必须用显式终结方法。所以，
 * 显式终结方法或finalizer应保证关键资源被及时释放，它可以是本地方法，也可以调用本地方法。
 * （有点绕，建议先看下{@link NativePeer}理解了本地方法再回来看这段）
 *
 * ****终结方法链并不会被自动执行，如果子类覆盖了父类的终结方法，那就必须手动调用父类的终结方法。
 * 建议在一个try块中执行子类的构造方法，并在其对应的finally块中执行父类的终结方法，这样可以保证就算子类终结过程异常，
 * 超类的终结方法也会得到执行。{@link SubClassFinalizer}
 *
 * 综上，除非是作为安全网，或者为了终止非关键的本地资源，否则不要用终结方法。
 * 而在这些很少见的情况下，如果使用终结方法，就一定不能忘记调用父类的super.finalize();.
 * 如果作为安全网，要记得记录下日志，打印出终结方法的非法使用情况；
 * 如果要为非final的类写终结方法，要考虑使用guardian.
 *
 * @author LightDance
 */
public class AvoidFinalizers {

    private boolean isLive = true;

    public AvoidFinalizers(){
        getRes();
    }

    private void getRes(){
        //获取or申请一些资源
    }

    private void releaseRes(){
        //释放这些资源
    }

    void sayHello(){
        if (isLive){
            System.out.println("hello");
        }else {
            throw new IllegalStateException();
        }
    }

    void terminate(){
        isLive = false;
        releaseRes();
    }

    public static void main(String[] args) {
        AvoidFinalizers foo = new AvoidFinalizers();
        try{
            foo.sayHello();
        }catch (IllegalStateException e){
            e.printStackTrace();
        }finally {
            foo.terminate();//显式地改变状态、释放资源
        }
    }

}
