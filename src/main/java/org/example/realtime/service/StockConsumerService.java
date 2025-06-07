package org.example.realtime.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.realtime.model.StockPrice;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StockConsumerService {

    private final ObjectMapper objectMapper;
    private final HistoricalDataService historicalDataService;

    @KafkaListener(topics = "stock-prices", groupId = "stock-consumer-group")
    public void consume(ConsumerRecord<String, String> record) {
        try {
            StockPrice stock = objectMapper.readValue(record.value(), StockPrice.class);
            // Save to historical database
            historicalDataService.saveStockPrice(stock.getSymbol(), stock.getPrice(), stock.getVolume());
            System.out.println("Consumed and saved: " + stock);
        } catch (Exception e) {
            System.err.println(" Failed to process message: " + record.value());
            e.printStackTrace();
        }
    }
}
