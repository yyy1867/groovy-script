package ml.guxing.script

class GCTest {

    private static long MB = 1024 * 1024
    private byte[] cache = new byte[MB * 10]

    static void main(String[] args) {
        def runtime = Runtime.runtime
        def index = 1
        while (true) {
            def a = new GCTest()
            def b = new GCTest()
            println("${runtime.totalMemory() / MB}m ${runtime.maxMemory() / MB}m ${runtime.freeMemory() / MB}m")
            Thread.sleep(1000)
            index++
        }
    }

}
