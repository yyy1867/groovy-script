package ml.guxing.script.proxy;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ProxyMainOne {

    static interface JDKProxyInf {
        void run();
    }

    static class JDKProxyInfImpl implements JDKProxyInf {
        @Override
        public void run() {
            owner();
            System.out.println("原始的实现方法:" + this);
        }

        private void owner() {
            System.out.println("这是私有方法");
        }
    }

    static class JDKInvocationHandler implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            System.out.println("这里使用了JDK的代理方法--开始");
            if (proxy instanceof JDKProxyInf) {
                System.out.println("JDK代理的实现方法!");
            }
            System.out.println("这里使用了JDK的代理方法--结束");
            return null;
        }
    }

    public static void main(String[] args) {
        JDKProxyInf obj = new JDKProxyInfImpl();
        // 调用原始方法
        obj.run();
        // 使用JDK代理
        JDKProxyInf proxy = (JDKProxyInf) Proxy.newProxyInstance(JDKProxyInf.class.getClassLoader()
                , new Class[]{JDKProxyInf.class}, new JDKInvocationHandler());
        proxy.run();
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(JDKProxyInfImpl.class);
        enhancer.setCallback(new MethodInterceptor() {

            @Override
            public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
//                System.out.println(proxy);
                if ("owner".equals(method.getName())) {
                    System.out.println("这里使用了CGLIB的代理方法--开始");
                    System.out.println("CGLIB实现的方法! -> " + method.getName());
                    System.out.println("这里使用了CGLIB的代理方法--结束");
                }
                return proxy.invokeSuper(obj, args);
            }
        });
        JDKProxyInf cglibProxy = (JDKProxyInf) enhancer.create();
        cglibProxy.run();
    }

}
