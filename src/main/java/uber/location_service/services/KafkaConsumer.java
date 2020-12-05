package uber.location_service.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import uber.location_service.structures.SupplyInstance;

//@Service
public class KafkaConsumer {
   private final Logger logger = LoggerFactory.getLogger(KafkaConsumer.class);
   private final SupplyLocationImpl impl;
   private final ObjectMapper jsonMapper = new ObjectMapper();

   //@Autowired
   public KafkaConsumer(final SupplyLocationImpl impl) {
      this.impl = impl;
   }

   //@KafkaListener(topics = "supply-location", groupId = "group_id")
   public void consume(String message) throws JsonProcessingException {
      logger.debug(String.format("#### -> Kafka consumed message -> %s", message));

      final SupplyInstance ins = jsonMapper.readValue(message, SupplyInstance.class);
      ins.getLocation().transformToRadians();
      impl.updateSupply(ins);
   }
}