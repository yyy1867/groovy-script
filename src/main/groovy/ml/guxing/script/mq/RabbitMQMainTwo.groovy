package ml.guxing.script.mq

import org.springframework.amqp.AmqpException
import org.springframework.amqp.core.*
import org.springframework.amqp.rabbit.annotation.EnableRabbit
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerEndpoint
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitAdmin
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.messaging.handler.annotation.Headers
import org.springframework.messaging.handler.annotation.Payload

@EnableRabbit
class RabbitMQMainTwo {

    @Bean
    ConnectionFactory connectionFactory() {
        CachingConnectionFactory factory = new CachingConnectionFactory()
        factory.setUri("amqp://hy:123@192.168.56.102:5672")
        factory.setVirtualHost("/hy")
        factory.setConnectionTimeout(3000)
        factory.setCacheMode(CachingConnectionFactory.CacheMode.CHANNEL)
        return factory
    }

    @Bean
    AmqpAdmin configAmqpAdmin(AmqpTemplate amqpTemplate) {
        def admin = new RabbitAdmin(amqpTemplate)
//        def arguments = ["x-dead-letter-exchange": "ml_guxing_script_1", "x-dead-letter-routing-key": "ml.guxing.script.timeout", "x-message-ttl": 3000]
        def arguments = ["x-dead-letter-exchange": "", "x-dead-letter-routing-key": "ml.guxing.script.four", "x-message-ttl": 3000]
        admin.declareExchange(new TopicExchange("ml_guxing_script_1", false, true, ["name": "默认交换机", "desc": "测试消息分发的默认交换机"]))
        admin.declareQueue(new Queue("ml.guxing.script.three", false, true, true, arguments))
        admin.declareBinding(new Binding("ml.guxing.script.three", Binding.DestinationType.QUEUE, "ml_guxing_script_1", "ml.guxing.script.three.proxy", null))

        admin.declareQueue(new Queue("ml.guxing.script.four", true, true, true, ["name": "超时队列"]))
        admin
    }

    @Bean
    AmqpTemplate configAmqpTemplate(ConnectionFactory connectionFactory) {
        def template = new RabbitTemplate(connectionFactory)
        template
    }

    @Bean(name = "rabbitListenerContainerFactory")
    RabbitListenerContainerFactory configRabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        def factory = new SimpleRabbitListenerContainerFactory()
        factory.setConnectionFactory(connectionFactory)
        def interfaces = factory.getClass().getInterfaces()
        if (factory instanceof RabbitListenerContainerFactory) {
            println "兼容 : RabbitListenerContainerFactory"
        }
        factory
    }

    @Bean
    RabbitListenerConfigurer configRabbitListenerConfigurer() {
        new RabbitListenerConfigurer() {
            @Override
            void configureRabbitListeners(RabbitListenerEndpointRegistrar registrar) {
                def adapter = new MessageListenerAdapter(new SimpleMessageListener())
                adapter.setDefaultListenerMethod("messageHander")
                def endpoint = new SimpleRabbitListenerEndpoint()
                endpoint.setId("1")
                endpoint.setQueueNames("ml.guxing.script.one")
                endpoint.setMessageListener(adapter)
//                registrar.registerEndpoint(endpoint)
            }
        }
    }

    @RabbitListener(queues = ["ml.guxing.script.one", "ml.guxing.script.four"])
    void messageHander(@Payload String body, @Headers Map<String, Object> headers) {
        println "收到消息:${headers["amqp_receivedRoutingKey"]} -> ${body} tag:[${headers["amqp_deliveryTag"]}]"
    }

    static void main(String[] args) {
        def app = new AnnotationConfigApplicationContext(RabbitMQMainTwo.class)
        def beans = app.getBeansOfType(BeanPostProcessor.class)
        if (beans) {
            beans.each { name, bean -> println("key:${name} -> ${bean.getClass()}") }
        }
        def conneactionFactory = app.getBean(CachingConnectionFactory.class)
        def connection = conneactionFactory.createConnection()
        connection.close()
        def amqpTemplate = app.getBean(AmqpTemplate.class)
        for (i in 1..3) {
            amqpTemplate.convertAndSend("", "ml.guxing.script.one", "测试消息2 - " + i)
        }
        amqpTemplate.convertAndSend("", "ml.guxing.script.three", "测试消息直发")
        amqpTemplate.convertAndSend("", "ml.guxing.script.three", "测试消息直发时效", new MessagePostProcessor() {
            @Override
            Message postProcessMessage(Message message) throws AmqpException {
                message.getMessageProperties().setExpiration("1000")
                return message
            }
        })
        amqpTemplate.convertAndSend("ml_guxing_script_1", "ml.guxing.script.three", "测试消息路由转发不代理")
        amqpTemplate.convertAndSend("ml_guxing_script_1", "ml.guxing.script.three.proxy", "测试消息路由转发代理")
        Thread.sleep(1000 * 60 * 10)
    }

}
