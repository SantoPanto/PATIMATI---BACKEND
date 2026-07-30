package com.works.patimati.ai;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.json.JsonMapper;

/**
 * AI servisiyle konuşulan kuyrukların tanımı.
 *
 * <p><b>DİKKAT:</b> Buradaki adlar ve argümanlar, Python tarafındaki
 * {@code app/topoloji.py} ile <b>birebir aynı</b> olmak zorundadır. Aynı kuyruğu
 * iki taraf farklı argümanlarla ilan ederse RabbitMQ {@code PRECONDITION_FAILED}
 * döndürür ve kanalı kapatır — üstelik hata, kuyruğu ikinci ilan eden tarafta
 * çıkar, yani sorunu yanlış yerde ararsınız.
 *
 * <p>Kaynak: PATIMATI-AI deposu, {@code docs/entegrasyon-sozlesmesi.md} §2.
 */
@Configuration
public class AiRabbitConfig {

    public static final String EXCHANGE = "patimati.ai";
    public static final String DLX = "patimati.ai.dlx";

    public static final String REQUEST_QUEUE = "ai.analysis.request";
    public static final String RESULT_QUEUE = "ai.analysis.result";
    public static final String DLQ = "ai.analysis.request.dlq";

    public static final String REQUEST_ROUTING_KEY = "analysis.request";
    public static final String RESULT_ROUTING_KEY = "analysis.result";

    /** Mesaj şemasının sürümü. Değişirse iki taraf birlikte güncellenir (§9). */
    public static final int SCHEMA_VERSION = 1;

    @Bean
    DirectExchange aiExchange() {
        return ExchangeBuilder.directExchange(EXCHANGE).durable(true).build();
    }

    @Bean
    DirectExchange aiDeadLetterExchange() {
        return ExchangeBuilder.directExchange(DLX).durable(true).build();
    }

    /**
     * AI'ya giden istekler. Ayrıştırılamayan mesajlar DLX üzerinden DLQ'ya düşer.
     *
     * <p>{@code deadLetterExchange} argümanı Python tarafında da AYNI şekilde
     * verilir. Biri verip diğeri vermezse kuyruk ilanları çakışır.
     */
    @Bean
    Queue aiRequestQueue() {
        return QueueBuilder.durable(REQUEST_QUEUE)
                .deadLetterExchange(DLX)
                .build();
    }

    @Bean
    Queue aiResultQueue() {
        return QueueBuilder.durable(RESULT_QUEUE).build();
    }

    @Bean
    Queue aiDeadLetterQueue() {
        return QueueBuilder.durable(DLQ).build();
    }

    @Bean
    Binding aiRequestBinding() {
        return BindingBuilder.bind(aiRequestQueue()).to(aiExchange()).with(REQUEST_ROUTING_KEY);
    }

    @Bean
    Binding aiResultBinding() {
        return BindingBuilder.bind(aiResultQueue()).to(aiExchange()).with(RESULT_ROUTING_KEY);
    }

    /**
     * DLQ, DLX'e <b>kuyruk adıyla değil</b> {@code analysis.request} anahtarıyla
     * bağlanır.
     *
     * <p>Sebebi: RabbitMQ bir mesajı ölü mektuba düşürürken
     * ({@code x-dead-letter-routing-key} verilmedikçe) <b>orijinal yönlendirme
     * anahtarını korur</b>. Yanlış anahtarla bağlanırsa kurulum başarılı olur,
     * hata da vermez — mesajlar sadece sessizce yok olur.
     */
    @Bean
    Binding aiDeadLetterBinding() {
        return BindingBuilder.bind(aiDeadLetterQueue())
                .to(aiDeadLetterExchange())
                .with(REQUEST_ROUTING_KEY);
    }

    /**
     * Mesaj gövdesi UTF-8 JSON (§2), alan adları <b>snake_case</b>
     * ({@code ad_id}, {@code photo_urls}...).
     *
     * <p>Adlandırma kuralı burada, AI'ya özel bir {@code ObjectMapper} üzerinde
     * ayarlanıyor. Uygulamanın genel mapper'ını değiştirmek mevcut REST
     * sözleşmelerini kırardı — mobil uygulama camelCase bekliyor.
     *
     * <p>{@code FAIL_ON_UNKNOWN_PROPERTIES} kapalı: sözleşme §9'a göre alan
     * eklemek kırıcı bir değişiklik değildir, iki taraf da tanımadığı alanları
     * yok sayar. Açık bırakılsaydı AI'ya eklenen her yeni alan Java tarafını
     * anında kırardı.
     */
    @Bean
    MessageConverter aiJsonMessageConverter() {
        // Tip JsonMapper: JacksonJsonMessageConverter (Spring AMQP 4 / Jackson 3)
        // ObjectMapper değil, JsonMapper bekliyor.
        JsonMapper aiMapper = JsonMapper.builder()
                .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();
        return new JacksonJsonMessageConverter(aiMapper);
    }

    @Bean
    RabbitTemplate aiRabbitTemplate(ConnectionFactory connectionFactory,
                                    MessageConverter aiJsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(aiJsonMessageConverter);
        return template;
    }

    /**
     * {@code @RabbitListener}'ların kullandığı fabrika.
     *
     * <p>Bu bean olmadan dinleyici, Spring Boot'un varsayılan dönüştürücüsünü
     * kullanır ve snake_case alanları tanımaz: {@code ad_id} okunamaz,
     * sonuç sessizce boş nesneye dönüşür. Yayınlama tarafını ayarlayıp bu
     * tarafı unutmak, bulunması zor bir hata kaynağıdır.
     */
    @Bean
    RabbitListenerContainerFactory<?> aiListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter aiJsonMessageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(aiJsonMessageConverter);
        return factory;
    }
}
