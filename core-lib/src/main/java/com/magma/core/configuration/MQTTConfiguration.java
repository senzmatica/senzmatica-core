package com.magma.core.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.magma.core.gateway.MQTTMessageHandler;
import com.magma.util.MagmaUtil;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.freemarker.FreeMarkerAutoConfiguration;
import org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.ExecutorChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.ArrayList;
import java.util.List;

@Configuration
@IntegrationComponentScan
@EnableAutoConfiguration(exclude = {GsonAutoConfiguration.class, FreeMarkerAutoConfiguration.class})
public class MQTTConfiguration {


    @Value("${mqtt.broker.address}")
    private String mqttBrokerAddress;

    @Value("${mqtt.client.id}")
    private String mqttClientId;

    @Value("${mqtt.client.username}")
    private String mqttClientUsername;

    @Value("${mqtt.client.password}")
    private String mqttClientPassword;

    @Value("${mqtt.sub.topic}")
    private String mqttSubTopic;

    @Value("${mqtt.res.topic}")
    private String mqttResTopic;

    @Value("${mqtt.old.topic}")
    private String mqttOldTopic;

    @Value("${mqtt.sub.qos}")
    private Integer mqttSubQoS;

    @Value("${mqtt.pool.size}")
    private Integer mqttPoolSize;

    @Value("${mqtt.max.pool.size}")
    private Integer mqttMaxPoolSize;

    @Value("${mqtt.que.size}")
    private Integer mqttQueueSize;

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public MessageChannel mqttInputChannel() {
        return new ExecutorChannel(taskExecutor());
    }

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(mqttPoolSize);
        executor.setMaxPoolSize(mqttMaxPoolSize);
        executor.setQueueCapacity(mqttQueueSize);
        return executor;
    }

    @Bean
    public MessageProducer inbound() {

        List<String> topicList = new ArrayList<>();
        if (MagmaUtil.validate(mqttSubTopic)) {
            topicList.add(mqttSubTopic);
        }

        if (MagmaUtil.validate(mqttResTopic)) {
            topicList.add(mqttResTopic);
        }

        if (MagmaUtil.validate(mqttOldTopic)) {
            topicList.add(mqttOldTopic);
        }

        String[] topics = new String[topicList.size()];
        topics = topicList.toArray(topics);

        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(
                        mqttClientId,
                        mqttClientFactory(),
                        topics
                );
        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(mqttSubQoS);
        adapter.setOutputChannel(mqttInputChannel());
        return adapter;
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public MessageHandler handler() {
        return new MQTTMessageHandler();
    }

    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(new String[]{mqttBrokerAddress});
        options.setUserName(mqttClientUsername);
        options.setPassword(mqttClientPassword.toCharArray());
        factory.setConnectionOptions(options);
        return factory;
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttOutboundChannel")
    public MessageHandler mqttOutbound() {
        MqttPahoMessageHandler messageHandler =
                new MqttPahoMessageHandler(mqttClientId + "_", mqttClientFactory());
        messageHandler.setAsync(true);
        messageHandler.setDefaultTopic("SenzMate");
        return messageHandler;
    }

    @Bean
    public MessageChannel mqttOutboundChannel() {
        return new DirectChannel();
    }

    @MessagingGateway(defaultRequestChannel = "mqttOutboundChannel")
    public interface MqttGateway {
        void send(@Header(MqttHeaders.TOPIC) String topic, String out);

        void send(@Header(MqttHeaders.TOPIC) String topic, @Header(MqttHeaders.RETAINED) Boolean retained, String out);
    }
} 