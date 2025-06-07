package org.example.realtime.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.example.realtime.model.StockPrice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class StockProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();
    private List<String> allSymbols = new ArrayList<>();

    @Value("${stock.topic.name:stock-prices}")
    private String topic;

    @Value("${alpha.vantage.api.key}")
    private String apiKey;

    public StockProducerService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Autowired
    private StockPriceService stockPriceService;

    @Scheduled(fixedRate = 60000) // Update symbols list every minute
    public void updateSymbolsList() {
        try {
            String url = "https://www.alphavantage.co/query?function=LISTING_STATUS&apikey=" + apiKey;
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response != null && response.containsKey("data")) {
                List<Map<String, String>> data = (List<Map<String, String>>) response.get("data");
                allSymbols = data.stream()
                    .map(item -> item.get("symbol"))
                    .filter(Objects::nonNull)
                    .toList();
                System.out.println("Updated symbols list. Total symbols: " + allSymbols.size());
            }
        } catch (Exception e) {
            System.err.println("Failed to update symbols list: " + e.getMessage());
        }
    }

    @Scheduled(fixedRate = 10000) // Send data every 10 seconds
    public void sendStockPrice() {
        if (allSymbols.isEmpty()) {
            updateSymbolsList();
            return;
        }

        // Process a batch of symbols each time
        int batchSize = 5; // Process 5 symbols at a time to respect API limits
        int startIndex = (int) (System.currentTimeMillis() / 10000) % allSymbols.size();
        
        for (int i = 0; i < batchSize; i++) {
            int index = (startIndex + i) % allSymbols.size();
            String symbol = allSymbols.get(index);
            Double price = stockPriceService.getPrice(symbol);

            if (price == null) continue;

            StockPrice stock = new StockPrice(symbol, price, 0.0, 0.0, 0.0, price, price, price, price);
            try {
                String message = objectMapper.writeValueAsString(stock);
                String cleanTopic = StringUtils.trimAllWhitespace(topic);
                kafkaTemplate.send(cleanTopic, symbol, message);
                System.out.println("Sent live data: " + message);
            } catch (Exception e) {
                System.err.println("Failed to send message to Kafka: " + e.getMessage());
            }
        }
    }

}
