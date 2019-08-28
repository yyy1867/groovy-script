package ml.guxing.script.jvm


import java.lang.management.ManagementFactory
import java.lang.management.MemoryPoolMXBean
import java.lang.management.MemoryUsage

class JVMMainOne {

    public static void main(String[] args) {
//            JVMManager
        while (true) {
            def temp = new byte[1024 * 4]
            printMemoryInfo()
            printMemoryPoolInfo()
            printCompilationInfo()
            printClassLoadingInfo()
            println()
            Thread.sleep(1000)
        }
        printMemoryInfo()
    }


    static String toMB(long num) {
        String.format("%.1f mb", num / 1024.0 / 1024.0)
    }

    static def toSTR = { MemoryUsage memoryUsage ->
        "初始内存:${toMB(getInit())} 使用内存:${toMB(getUsed())} 申请内存:${toMB(getCommitted())} 最大内存:${toMB(getMax())}"
    }

    static printMemoryInfo() {
        // 内存监控
        def memoryMXBean = ManagementFactory.getMemoryMXBean()
        def toSTR = {
            "初始内存:${toMB(getInit())} 使用内存:${toMB(getUsed())} 申请内存:${toMB(getCommitted())} 最大内存:${toMB(getMax())}"
        }
        def heapMemoryUsage = memoryMXBean.getHeapMemoryUsage()
        def nonHeapMemoryUsage = memoryMXBean.getNonHeapMemoryUsage()
        println("堆内存->\t" + heapMemoryUsage.with(toSTR))
        println("非堆内存->\t" + nonHeapMemoryUsage.with(toSTR))
    }

    static printMemoryPoolInfo() {
        // 内存池实时监控
        def beans = ManagementFactory.getMemoryPoolMXBeans()
        beans.forEach { MemoryPoolMXBean bean ->
            println(bean.with {
                "池名称:${name}\t\t\t\t\t使用:${usage.with(JVMMainOne.toSTR)} "
            })
        }
    }

    static printCompilationInfo() {
        // 编译器实时监控
        def bean = ManagementFactory.getCompilationMXBean()
        println(bean.with {
            "编译器名称:${name} 编译耗时:${totalCompilationTime} ms"
        })
    }

    static printClassLoadingInfo() {
        // ClassLoad实时监控
        def bean = ManagementFactory.getClassLoadingMXBean()
        println(bean.with {
            "当前类总数:${loadedClassCount} 卸载总数:${unloadedClassCount} 加载总数:${totalLoadedClassCount}"
        })
    }
}
