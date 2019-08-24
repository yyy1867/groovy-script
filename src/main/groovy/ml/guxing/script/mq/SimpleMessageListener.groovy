package ml.guxing.script.mq

class SimpleMessageListener {

    static messageHander(String message) {
        println("收到消息 -> ${message}")
    }

}
