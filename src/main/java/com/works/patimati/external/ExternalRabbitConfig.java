package com.works.patimati.external;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Collector → Backend kuyruk topolojisi (Faz 2 revize blueprint §5).
 *
 * <p>{@code patimati.ai} ile AYNI broker, ama BİLEREK ayrı exchange: bu,
 * Collector'ın da taraf olduğu farklı bir iki-taraflı sözleşmedir ve zaten
 * sıkı sürüm kontrolü altındaki Java↔Python sözleşmesine üçüncü bir tarafı
 * karıştırmamak için ayrı tutulur.
 *
 * <p><b>DİKKAT:</b> DLQ, DLX'e kuyruk adıyla değil {@code ingestion.detected}
 * yönlendirme anahtarıyla bağlanır — {@code AiRabbitConfig}'teki aynı
 * gerekçe: RabbitMQ mesajı ölü mektuba düşürürken orijinal anahtarı korur.
 */
@Configuration
public class ExternalRabbitConfig {

    public static final String EXCHANGE = "patimati.external";
    public static final String DLX = "patimati.external.dlx";

    public static final String INGESTION_QUEUE = "external.ingestion";
    public static final String DLQ = "external.ingestion.dlq";

    public static final String INGESTION_ROUTING_KEY = "ingestion.detected";

    public static final int SCHEMA_VERSION = 1;

    @Bean
    DirectExchange externalExchange() {
        return ExchangeBuilder.directExchange(EXCHANGE).durable(true).build();
    }

    @Bean
    DirectExchange externalDeadLetterExchange() {
        return ExchangeBuilder.directExchange(DLX).durable(true).build();
    }

    @Bean
    Queue externalIngestionQueue() {
        return QueueBuilder.durable(INGESTION_QUEUE)
                .deadLetterExchange(DLX)
                .build();
    }

    @Bean
    Queue externalDeadLetterQueue() {
        return QueueBuilder.durable(DLQ).build();
    }

    @Bean
    Binding externalIngestionBinding() {
        return BindingBuilder.bind(externalIngestionQueue()).to(externalExchange()).with(INGESTION_ROUTING_KEY);
    }

    @Bean
    Binding externalDeadLetterBinding() {
        return BindingBuilder.bind(externalDeadLetterQueue())
                .to(externalDeadLetterExchange())
                .with(INGESTION_ROUTING_KEY);
    }

    /** {@code AiRabbitConfig.aiJsonMessageConverter} ile aynı gerekçe: snake_case, bilinmeyen alan hoşgörülü. */
    @Bean
    MessageConverter externalJsonMessageConverter() {
        ObjectMapper mapper = new ObjectMapper()
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.findAndRegisterModules();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new Jackson2JsonMessageConverter(mapper);
    }

    @Bean
    RabbitListenerContainerFactory<?> externalListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter externalJsonMessageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(externalJsonMessageConverter);
        return factory;
    }
}
