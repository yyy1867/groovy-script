package ml.guxing.script.mq

import org.springframework.amqp.core.AcknowledgeMode
import org.springframework.amqp.core.AmqpAdmin
import org.springframework.amqp.core.AmqpTemplate
import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitAdmin
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.rabbit.listener.MessageListenerContainer
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter
import org.springframework.beans.BeansException
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.event.ContextRefreshedEvent

/**
 * Spring单文件配置RabbitMQ
 */
class RabbitMQMainOne implements ApplicationListener<ContextRefreshedEvent>, ApplicationContextAware {

    private ApplicationContext applicationContext

    @Bean
    ConnectionFactory configCachingConnectionFactory() {
        // 配置使用缓存的连接工厂
        def connectionFactory = new CachingConnectionFactory()
        connectionFactory.setCacheMode(CachingConnectionFactory.CacheMode.CHANNEL)
        connectionFactory.setAddresses("192.168.56.102:5672")
        connectionFactory.setVirtualHost("/hy")
        connectionFactory.setUsername("hy")
        connectionFactory.setPassword("123")
        connectionFactory.setChannelCacheSize(3)
        connectionFactory.setChannelCheckoutTimeout(3000)
        connectionFactory.setApplicationContext(this.applicationContext)
        connectionFactory
    }

    @Bean
    AmqpTemplate configAmqpTemplate(ConnectionFactory connectionFactory) {
        // 配置操作RabbitMQ的模板
        def template = new RabbitTemplate(connectionFactory)
        template.setEncoding("UTF-8")
        template
    }

    @Bean
    AmqpAdmin configAmqpAdmin(AmqpTemplate amqpTemplate) {
        // 配置RabbitMQ的管理模板
        def admin = new RabbitAdmin(amqpTemplate)
        admin.setApplicationContext(this.applicationContext)
//        def one = admin.declareQueue(new Queue("ml.guxing.script.one", true
//                , false, false, ["name": "测试队列", "desc": "测试使用的队列"]))
        admin
    }

    @Bean
    Queue configQueueOne() {
        new Queue("ml.guxing.script.one", true
                , false, false, ["name": "测试队列", "desc": "测试使用的队列"])
    }

    @Bean
    Queue configQueueTwo() {
        new Queue("ml.guxing.script.two", true
                , false, false, ["name": "测试队列2", "desc": "测试使用的队列2"])
    }

    @Bean
    MessageListenerContainer configMessageListenerContainer(ConnectionFactory connectionFactory) {
        // 设置监听容器
        def container = new SimpleMessageListenerContainer(connectionFactory)
        container.setQueueNames("ml.guxing.script.one")
        container.setAcknowledgeMode(AcknowledgeMode.AUTO)
        container.setConcurrentConsumers(1)
        container.setMaxConcurrentConsumers(6)
        container.setReceiveTimeout(1000)
        container.setTxSize(1)
        int index = 1
        container.setConsumerTagStrategy({ query ->
            def tag = "${query}_${index++}"
            println tag
            tag.toString()
        })
        def adapter = new MessageListenerAdapter(new SimpleMessageListener(), "messageHander")
        container.setupMessageListener(adapter)
        container
    }

    static void main(String[] args) {
        def app = new AnnotationConfigApplicationContext(RabbitMQMainOne.class)
        def amqpTemplate = app.getBean(AmqpTemplate.class)
        def beans = app.getBeansOfType(Queue.class).values()
        println beans
        amqpTemplate.convertAndSend("", "ml.guxing.script.one", "测试消息")
        amqpTemplate.convertAndSend("", "ml.guxing.script.one", "测试消息")
        amqpTemplate.convertAndSend("", "ml.guxing.script.one", "测试消息")
        amqpTemplate.convertAndSend("", "ml.guxing.script.one", "测试消息")
        amqpTemplate.convertAndSend("", "ml.guxing.script.one", "测试消息")
        amqpTemplate.convertAndSend("", "ml.guxing.script.one", "测试消息")
        Thread.sleep(1000 * 60 * 10)
        app.close()
    }

    @Override
    void onApplicationEvent(ContextRefreshedEvent event) {
        // 容器启动完成后声明队列
        def app = event.getApplicationContext()
        if (app.getParent() == null) {
            println "容器启动完毕"
        }
    }

    @Override
    void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext
    }
}
