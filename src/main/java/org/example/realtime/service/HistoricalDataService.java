package org.example.realtime.service;

import lombok.RequiredArgsConstructor;
import org.example.realtime.model.HistoricalStockPrice;
import org.example.realtime.repository.HistoricalStockPriceRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class HistoricalDataService {
    
    private final HistoricalStockPriceRepository repository;
    
    public void saveStockPrice(String symbol, double price, double volume) {
        HistoricalStockPrice historicalPrice = new HistoricalStockPrice(symbol, price, volume);
        repository.save(historicalPrice);
    }
    
    public List<HistoricalStockPrice> getRecentPrices(String symbol) {
        return repository.findBySymbolOrderByTimestampDesc(symbol);
    }
    
    public List<HistoricalStockPrice> getPricesInRange(String symbol, LocalDateTime start, LocalDateTime end) {
        return repository.findBySymbolAndTimestampBetweenOrderByTimestampAsc(symbol, start, end);
    }
    
    public Map<String, Double> getPriceStatistics(String symbol, LocalDateTime since) {
        Map<String, Double> statistics = new HashMap<>();
        statistics.put("average", repository.calculateAveragePrice(symbol, since));
        statistics.put("max", repository.findMaxPrice(symbol, since));
        statistics.put("min", repository.findMinPrice(symbol, since));
        return statistics;
    }
} 