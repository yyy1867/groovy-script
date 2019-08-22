package ml.guxing.script;

public class GroovyClassLoadTest {

    public void printTest() {
        printClass();
        System.out.println("这是未经过修改前的test方法");
    }

    private void printClass() {
        System.out.println(this.getClass());
        System.out.println(this.getClass().getClassLoader());
    }

    public static void main(String[] args) {
        GroovyClassLoadTest test = new GroovyClassLoadTest();
        test.printTest();
        ProxyTest.addmethod();
        test.printTest();
    }
}
