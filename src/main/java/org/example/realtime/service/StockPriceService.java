package org.example.realtime.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class StockPriceService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final Map<String, Double> lastPrices = new HashMap<>();
    private final Map<String, LocalDateTime> lastFetchTimes = new HashMap<>();
    private static final int RATE_LIMIT_SECONDS = 5; // Minimum time between requests for the same symbol

    public Double getPrice(String symbol) {
        try {
            // Check if we need to wait before making another request
            LocalDateTime lastFetch = lastFetchTimes.get(symbol);
            if (lastFetch != null) {
                LocalDateTime now = LocalDateTime.now();
                long secondsSinceLastFetch = ChronoUnit.SECONDS.between(lastFetch, now);
                if (secondsSinceLastFetch < RATE_LIMIT_SECONDS) {
                    // Return cached price if available
                    return lastPrices.getOrDefault(symbol, 0.0);
                }
            }

            String url = "https://query1.finance.yahoo.com/v8/finance/chart/" + symbol;
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response != null && response.containsKey("chart")) {
                Map<String, Object> chart = (Map<String, Object>) response.get("chart");
                Map<String, Object> result = (Map<String, Object>) ((List<?>) chart.get("result")).get(0);
                Map<String, Object> meta = (Map<String, Object>) result.get("meta");
                double price = (double) meta.get("regularMarketPrice");
                
                // Update cache and last fetch time
                lastPrices.put(symbol, price);
                lastFetchTimes.put(symbol, LocalDateTime.now());
                
                return price;
            }
            return lastPrices.getOrDefault(symbol, 0.0);
        } catch (Exception e) {
            System.err.println("Error fetching price for " + symbol + ": " + e.getMessage());
            // Return cached price if available, otherwise 0.0
            return lastPrices.getOrDefault(symbol, 0.0);
        }
    }

    public Map<String, Double> getLastPrices() {
        return new HashMap<>(lastPrices);
    }
}
