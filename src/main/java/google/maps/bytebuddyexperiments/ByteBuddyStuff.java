package google.maps.bytebuddyexperiments;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.loading.ClassReloadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.pool.TypePool;

import java.lang.instrument.Instrumentation;

import static net.bytebuddy.matcher.ElementMatchers.named;

public class ByteBuddyStuff {

    private static Instrumentation instrumentation;

    public static void main(String[] args) {

        // obtain the class loader
        // don't access Foo.class before ByteBuddy can get its hand on it. The jvm for good reasons will give you a
        // UnsupportedOperationException: class redefinition failed: attempted to add a method
        // https://stackoverflow.com/questions/40774684/error-while-redefining-a-method-with-bytebuddy-class-redefinition-failed-atte
        ClassLoader classLoader = Hu.class.getClassLoader();
        TypePool typepool = TypePool.Default.of(classLoader);

        TypeDescription fooType = typepool.describe("google.maps.bytebuddystuff.Foo").resolve();
        TypeDescription fooInterceptorType = typepool.describe("google.maps.bytebuddystuff.FooInterceptor").resolve();

        new ByteBuddy().rebase(fooType, ClassFileLocator.ForClassLoader.of(classLoader))
                .method(named("foo")).intercept(MethodDelegation.to(fooInterceptorType))
                .method(named("getNum")).intercept(MethodDelegation.to(fooInterceptorType))
                .make()
                .load(classLoader, ClassReloadingStrategy.of(instrumentation));

        Foo f = new Foo();
        f.foo();
        f.getNum(11);
        System.out.println("hu!");
    }

    // to trigger pass jvm parameter: -javaagent:<path to byte-buddy-agent.jar>
    public static void premain(String agentArgs, Instrumentation inst) {
        ByteBuddyStuff.instrumentation = inst;
    }
}
