package ml.guxing.script

class ProxyTest {

    static addmethod() {
        GroovyClassLoadTest.getMetaClass()."printTest" = {
            println("hsdjkadhj")
        }
        println("方法已修改")
    }

    static void main(String[] args) {
        def test = new GroovyClassLoadTest() 
        test.printTest()
        addmethod()
        test.printTest()
        println ""
        GroovyClassLoadTest.main()
    }

}
