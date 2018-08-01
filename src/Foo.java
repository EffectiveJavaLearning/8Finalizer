/**
 * 用来说明如何用匿名类作为终结方法守护者(finalizer guardian)
 *
 * 外围类的每个实例都保存这么一个匿名类的唯一引用，这样两者就可以同时启动终结过程。
 * 守卫者被终结时，会执行外围实例所希望的终结行为，就好像它是外围类的一个方法一样.
 *
 * *.外围类Foo没有终结方法，所以Foo的子类是否调用父类的终结方法无所谓。
 *   对于每一个带有终结方法的非final共有类，需要实现终结方法时，都应该考虑用这种方式。
 *
 * @author LightDance
 */
public class Foo {

    //...

    private final Object finalizerGuardian = new Object(){
        @Override
        protected void finalize() throws Throwable {
            //finalizer outer class (Foo)
            super.finalize();
        }
    };

    //...
}
