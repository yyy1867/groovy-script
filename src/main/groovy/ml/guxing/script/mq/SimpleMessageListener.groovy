package ml.guxing.script.mq


import org.springframework.amqp.core.Message
import org.springframework.amqp.core.MessageListener

import java.nio.charset.Charset

class SimpleMessageListener implements MessageListener {

    SimpleMessageListener() {
        super()
        println("已创建消费者实例:${SimpleMessageListener.class} 线程:${Thread.currentThread().getName()}")
    }

    @Override
    void onMessage(Message message) {
        def properties = message.getProperties()
        def body = message.getBody()
        println new String(body, Charset.defaultCharset())
    }
    
}
