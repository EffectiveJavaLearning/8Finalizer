/**
 * 用于说明父类子类终结方法的调用习惯.这样可以保证就算子类终结过程异常，超类的终结方法也会得到执行.
 *
 * 如果覆盖掉父类的终结方法，又忘记手动调用super.finalize()的话，那么父类的终结方法就再也得不到执行了。
 * 如果想防范这种有可能出现的粗心，代价就是为每个要终结的对象创建一个附加对象，把终结方法放在一个匿名的类中，
 * 而该匿名类的唯一作用就是终结它的外围实例(enclosing finalizer).这种匿名类的实例称为
 * "终结方法守护者(finalizer guardian)".{@link Foo}。
 *
 * @author LightDance
 */
public class SubClassFinalizer {

    @Override
    protected void finalize() throws Throwable {
        try{
            //在这里执行子类的终结方法
        }finally {
            //在这里调用父类的终结方法
            super.finalize();
        }
    }
}
