package com.mobydigital.academy.news.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongSerializer;    // ✅
import org.apache.kafka.common.serialization.StringSerializer; // ✅
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaConfigProducer {

  @Value("${BOOTSTRAP_SERVER}")
    private String bootstrapServers;

  @Value("${API_KEY}")
    private String username;

  @Value("${API_SECRET}")
    private String password;

  @Bean
  public Map<String, Object> producerProperties() {
    Map<String, Object> props = new HashMap<>();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers); // ✅ ajustá
    props.put("security.protocol", "SASL_SSL");
    props.put("sasl.mechanism", "PLAIN");
    props.put("sasl.jaas.config",
    String.format("org.apache.kafka.common.security.plain.PlainLoginModule required username=\"%s\" password=\"%s\";", username, password));
    
      props.put(ProducerConfig.ACKS_CONFIG, "all");
    props.put(ProducerConfig.RETRIES_CONFIG, 3);
    props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
    props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
    props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);

    // ✅ Serializers correctos y *matching* con KafkaTemplate<Long, String>
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    return props;
  }

  @Bean
  public ProducerFactory<Long, String> producerFactory() {
    return new DefaultKafkaProducerFactory<>(producerProperties());
  }

  @Bean
  public KafkaTemplate<Long, String> createTemplate() {
    return new KafkaTemplate<>(producerFactory());
  }
}
