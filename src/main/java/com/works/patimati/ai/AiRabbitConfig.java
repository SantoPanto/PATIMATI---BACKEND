package com.works.patimati.ai;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    public static final String RESULT_DLQ = "ai.analysis.result.dlq";

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

    /**
     * AI'dan dönen sonuçlar. {@code onResult}'ta (AiAnalysisListener)
     * beklenmeyen bir istisna -- ya da mesaj hiç ayrıştırılamıyorsa
     * (Jackson2JsonMessageConverter dönüştürme hatası) -- bu kuyruğun DLX'i
     * olmadan mesaj SONSUZA DEK aynı kuyruğa yeniden teslim edilirdi (zehirli
     * mesaj): {@code aiListenerContainerFactory}'nin
     * {@code defaultRequeueRejected}'ı false yapılmadıkça Spring AMQP
     * varsayılanı `true`'dur, yani reddedilen mesaj DLX'e DEĞİL aynı kuyruğa
     * geri gider -- bu yüzden ikisi birlikte gerekir, yalnızca
     * {@code deadLetterExchange} yetmez.
     */
    @Bean
    Queue aiResultQueue() {
        return QueueBuilder.durable(RESULT_QUEUE)
                .deadLetterExchange(DLX)
                .build();
    }

    @Bean
    Queue aiDeadLetterQueue() {
        return QueueBuilder.durable(DLQ).build();
    }

    @Bean
    Queue aiResultDeadLetterQueue() {
        return QueueBuilder.durable(RESULT_DLQ).build();
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
     * Sonuç kuyruğunun ölü mektupları, {@code aiDeadLetterBinding} ile AYNI
     * gerekçeyle -- orijinal yönlendirme anahtarıyla, {@code RESULT_QUEUE}'nun
     * kendi anahtarı {@code analysis.result} -- kendi DLQ'suna bağlanır.
     * İstek ve sonuç kuyruklarının ölü mektupları BİLEREK aynı DLQ'da
     * TOPLANMAZ: aynı DLX'i (tek exchange, mevcut altyapı) paylaşırlar ama
     * her birinin kendi kuyruğu vardır, tıpkı istek tarafındaki gibi.
     */
    @Bean
    Binding aiResultDeadLetterBinding() {
        return BindingBuilder.bind(aiResultDeadLetterQueue())
                .to(aiDeadLetterExchange())
                .with(RESULT_ROUTING_KEY);
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
        ObjectMapper aiMapper = new ObjectMapper()
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // java.time desteği AÇIKÇA tanıtılmalı.
        //
        // Sözleşme §6'daki cevapta processed_at bir zaman damgası
        // ("2026-08-06T17:05:52Z") ve DTO tarafındaki karşılığı Instant.
        // Elle kurulan bir ObjectMapper bu tipi TANIMAZ:
        //   Java 8 date/time type `java.time.Instant` not supported by default
        // Hata AI'nın CEVABINI çözerken çıkıyor; yani analiz başarıyla bitiyor,
        // sonuç kuyruğa yazılıyor, ama ilana hiç işlenmiyor ve ilan sonsuza
        // kadar PENDING kalıyor. Dışarıdan bakınca "AI çalışmıyor" görünüyor.
        //
        // Bu daha önce fark edilmedi çünkü proje Jackson 3 kullanırken
        // java.time desteği çekirdekte geliyordu; Jackson 2'ye dönülünce
        // ayrı modül gerekti (jackson-datatype-jsr310 zaten sınıf yolunda).
        // findAndRegisterModules kullanmak sürüm değişikliklerine dayanıklı:
        // sınıf yolunda ne varsa onu kaydeder.
        aiMapper.findAndRegisterModules();

        // Tarihler epoch sayısı değil ISO-8601 metni olarak yazılsın —
        // sözleşmedeki biçim bu ve Python tarafı böyle okuyor.
        aiMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return new Jackson2JsonMessageConverter(aiMapper);
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
     *
     * <p><b>{@code defaultRequeueRejected(false)} BİLEREK verildi.</b> Spring
     * AMQP'nin kendi varsayılanı {@code true}'dur: dinleyici (ör. onResult)
     * bir istisna fırlattığında ya da mesaj hiç dönüştürülemediğinde,
     * container mesajı reddeder AMA {@code requeue=true} ile — yani mesaj
     * DLX'e DEĞİL, aynı kuyruğa geri döner ve anında yeniden teslim edilir.
     * Sonuç: zehirli mesaj sonsuz döngüye girer, kuyruk tıkanır, ardından
     * gelen tüm analiz sonuçları da işlenemez hâle gelir — {@code aiResultQueue}
     * üzerinde bir DLX tanımlamak TEK BAŞINA bunu önlemez, ikisi birlikte
     * gerekir (bkz. {@code aiResultQueue} javadoc'u).
     */
    @Bean
    RabbitListenerContainerFactory<?> aiListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter aiJsonMessageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(aiJsonMessageConverter);
        factory.setDefaultRequeueRejected(false);
        return factory;
    }
}
