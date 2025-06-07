package org.example.realtime.service;

import org.example.realtime.model.StockPrice;
import org.example.realtime.model.HistoricalStockPrice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class StockAnalysisService {

    @Autowired
    private KafkaTemplate<String, StockPrice> kafkaTemplate;

    @Autowired
    private StockPriceService stockPriceService;

    @Autowired
    private HistoricalDataService historicalDataService;

    private final RestTemplate restTemplate = new RestTemplate();

    private final Map<String, LocalDateTime> lastVolumeFetchTimes = new HashMap<>();
    private static final int VOLUME_RATE_LIMIT_SECONDS = 5;

    public Map<String, Object> analyzeTrend(String symbol, String timeframe) {
        Map<String, Object> result = new HashMap<>();
        result.put("symbol", symbol);
        result.put("timeframe", timeframe);

        try {
            // Get current price
            double currentPrice = stockPriceService.getPrice(symbol);
            
            // Get historical prices for the specified timeframe
            LocalDateTime end = LocalDateTime.now();
            LocalDateTime start = end.minus(1, ChronoUnit.DAYS); // Default to 1 day
            if (timeframe.equals("1w")) {
                start = end.minus(7, ChronoUnit.DAYS);
            } else if (timeframe.equals("1m")) {
                start = end.minus(30, ChronoUnit.DAYS);
            }

            List<HistoricalStockPrice> historicalPrices = historicalDataService.getPricesInRange(symbol, start, end);
            
            if (historicalPrices.isEmpty()) {
                result.put("trend", "NEUTRAL");
                result.put("strength", 0.5);
                return result;
            }

            // Calculate trend
            double firstPrice = historicalPrices.get(0).getPrice();
            double priceChange = currentPrice - firstPrice;
            double percentChange = (priceChange / firstPrice) * 100;

            String trend;
            double strength;
            if (percentChange > 2) {
                trend = "BULLISH";
                strength = Math.min(0.5 + (percentChange / 100), 1.0);
            } else if (percentChange < -2) {
                trend = "BEARISH";
                strength = Math.min(0.5 + (Math.abs(percentChange) / 100), 1.0);
            } else {
                trend = "NEUTRAL";
                strength = 0.5;
            }

            result.put("trend", trend);
            result.put("strength", strength);
        } catch (Exception e) {
            result.put("trend", "UNKNOWN");
            result.put("strength", 0.0);
        }
        return result;
    }

    public Map<String, Object> calculateVolatility(String symbol, String timeframe) {
        Map<String, Object> result = new HashMap<>();
        result.put("symbol", symbol);
        result.put("timeframe", timeframe);

        try {
            // Get historical prices
            LocalDateTime end = LocalDateTime.now();
            LocalDateTime start = end.minus(1, ChronoUnit.DAYS);
            if (timeframe.equals("1w")) {
                start = end.minus(7, ChronoUnit.DAYS);
            } else if (timeframe.equals("1m")) {
                start = end.minus(30, ChronoUnit.DAYS);
            }

            List<HistoricalStockPrice> prices = historicalDataService.getPricesInRange(symbol, start, end);
            
            if (prices.size() < 2) {
                result.put("volatility", 0.0);
                result.put("risk_level", "LOW");
                return result;
            }

            // Calculate daily returns
            List<Double> returns = new ArrayList<>();
            for (int i = 1; i < prices.size(); i++) {
                double dailyReturn = (prices.get(i).getPrice() - prices.get(i-1).getPrice()) / prices.get(i-1).getPrice();
                returns.add(dailyReturn);
            }

            // Calculate volatility (standard deviation of returns)
            double mean = returns.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            double variance = returns.stream()
                    .mapToDouble(r -> Math.pow(r - mean, 2))
                    .average()
                    .orElse(0.0);
            double volatility = Math.sqrt(variance);

            // Determine risk level
            String riskLevel;
            if (volatility < 0.01) {
                riskLevel = "LOW";
            } else if (volatility < 0.02) {
                riskLevel = "MODERATE";
            } else {
                riskLevel = "HIGH";
            }

            result.put("volatility", volatility);
            result.put("risk_level", riskLevel);
        } catch (Exception e) {
            result.put("volatility", 0.0);
            result.put("risk_level", "UNKNOWN");
        }
        return result;
    }

    public Map<String, Object> calculateMovingAverages(String symbol, int shortPeriod, int longPeriod) {
        Map<String, Object> result = new HashMap<>();
        result.put("symbol", symbol);

        try {
            // Get historical prices
            LocalDateTime end = LocalDateTime.now();
            LocalDateTime start = end.minus(longPeriod, ChronoUnit.DAYS);
            List<HistoricalStockPrice> prices = historicalDataService.getPricesInRange(symbol, start, end);
            
            if (prices.size() < longPeriod) {
                result.put("short_ma", 0.0);
                result.put("long_ma", 0.0);
                result.put("signal", "NEUTRAL");
                return result;
            }

            // Calculate short-term MA
            double shortMA = prices.subList(prices.size() - shortPeriod, prices.size())
                    .stream()
                    .mapToDouble(HistoricalStockPrice::getPrice)
                    .average()
                    .orElse(0.0);

            // Calculate long-term MA
            double longMA = prices.subList(prices.size() - longPeriod, prices.size())
                    .stream()
                    .mapToDouble(HistoricalStockPrice::getPrice)
                    .average()
                    .orElse(0.0);

            // Determine signal
            String signal;
            if (shortMA > longMA) {
                signal = "BULLISH";
            } else if (shortMA < longMA) {
                signal = "BEARISH";
            } else {
                signal = "NEUTRAL";
            }

            result.put("short_ma", shortMA);
            result.put("long_ma", longMA);
            result.put("signal", signal);
        } catch (Exception e) {
            result.put("short_ma", 0.0);
            result.put("long_ma", 0.0);
            result.put("signal", "UNKNOWN");
        }
        return result;
    }

    public Map<String, Object> analyzeVolume(String symbol, String timeframe) {
        Map<String, Object> result = new HashMap<>();
        result.put("symbol", symbol);
        result.put("timeframe", timeframe);

        try {
            // Check rate limit
            LocalDateTime lastFetch = lastVolumeFetchTimes.get(symbol);
            if (lastFetch != null) {
                LocalDateTime now = LocalDateTime.now();
                long secondsSinceLastFetch = ChronoUnit.SECONDS.between(lastFetch, now);
                if (secondsSinceLastFetch < VOLUME_RATE_LIMIT_SECONDS) {
                    // Use historical data only if we're rate limited
                    List<HistoricalStockPrice> historicalPrices = historicalDataService.getPricesInRange(
                        symbol, 
                        LocalDateTime.now().minus(1, ChronoUnit.DAYS),
                        LocalDateTime.now()
                    );
                    
                    if (!historicalPrices.isEmpty()) {
                        double averageVolume = historicalPrices.stream()
                            .mapToDouble(HistoricalStockPrice::getVolume)
                            .average()
                            .orElse(0.0);
                        
                        result.put("average_volume", averageVolume);
                        result.put("volume_trend", "NEUTRAL");
                        return result;
                    }
                }
            }

            // Get current volume from Yahoo Finance
            String url = "https://query1.finance.yahoo.com/v8/finance/chart/" + symbol;
            Map<String, Object> yahooResponse = restTemplate.getForObject(url, Map.class);
            
            if (yahooResponse != null && yahooResponse.containsKey("chart")) {
                Map<String, Object> chart = (Map<String, Object>) yahooResponse.get("chart");
                Map<String, Object> yahooResult = (Map<String, Object>) ((List<?>) chart.get("result")).get(0);
                Map<String, Object> meta = (Map<String, Object>) yahooResult.get("meta");
                long currentVolume = (long) meta.get("regularMarketVolume");

                // Update last fetch time
                lastVolumeFetchTimes.put(symbol, LocalDateTime.now());

                // Get historical volumes
                LocalDateTime end = LocalDateTime.now();
                LocalDateTime start = end.minus(1, ChronoUnit.DAYS);
                if (timeframe.equals("1w")) {
                    start = end.minus(7, ChronoUnit.DAYS);
                } else if (timeframe.equals("1m")) {
                    start = end.minus(30, ChronoUnit.DAYS);
                }

                List<HistoricalStockPrice> historicalPrices = historicalDataService.getPricesInRange(symbol, start, end);
                
                if (historicalPrices.isEmpty()) {
                    result.put("average_volume", currentVolume);
                    result.put("volume_trend", "NEUTRAL");
                    return result;
                }

                // Calculate average volume
                double averageVolume = historicalPrices.stream()
                        .mapToDouble(HistoricalStockPrice::getVolume)
                        .average()
                        .orElse(currentVolume);

                // Determine volume trend
                String volumeTrend;
                if (currentVolume > averageVolume * 1.2) {
                    volumeTrend = "INCREASING";
                } else if (currentVolume < averageVolume * 0.8) {
                    volumeTrend = "DECREASING";
                } else {
                    volumeTrend = "STABLE";
                }

                result.put("average_volume", averageVolume);
                result.put("volume_trend", volumeTrend);
            }
        } catch (Exception e) {
            // If we get rate limited, try to use historical data
            List<HistoricalStockPrice> historicalPrices = historicalDataService.getPricesInRange(
                symbol, 
                LocalDateTime.now().minus(1, ChronoUnit.DAYS),
                LocalDateTime.now()
            );
            
            if (!historicalPrices.isEmpty()) {
                double averageVolume = historicalPrices.stream()
                    .mapToDouble(HistoricalStockPrice::getVolume)
                    .average()
                    .orElse(0.0);
                
                result.put("average_volume", averageVolume);
                result.put("volume_trend", "NEUTRAL");
            } else {
                result.put("average_volume", 0.0);
                result.put("volume_trend", "UNKNOWN");
            }
        }
        return result;
    }

    public Map<String, Object> calculateTechnicalIndicators(String symbol, int period) {
        Map<String, Object> result = new HashMap<>();
        result.put("symbol", symbol);

        try {
            // Get historical prices
            LocalDateTime end = LocalDateTime.now();
            LocalDateTime start = end.minus(period * 2, ChronoUnit.DAYS);
            List<HistoricalStockPrice> prices = historicalDataService.getPricesInRange(symbol, start, end);
            
            if (prices.size() < period) {
                result.put("rsi", 50.0);
                result.put("macd", 0.0);
                result.put("signal", "NEUTRAL");
                return result;
            }

            // Calculate RSI
            double rsi = calculateRSI(prices, period);

            // Calculate MACD
            double macd = calculateMACD(prices);

            // Determine signal based on RSI and MACD
            String signal;
            if (rsi > 70 && macd > 0) {
                signal = "BEARISH"; // Overbought
            } else if (rsi < 30 && macd < 0) {
                signal = "BULLISH"; // Oversold
            } else if (rsi > 50 && macd > 0) {
                signal = "BULLISH";
            } else if (rsi < 50 && macd < 0) {
                signal = "BEARISH";
            } else {
                signal = "NEUTRAL";
            }

            result.put("rsi", rsi);
            result.put("macd", macd);
            result.put("signal", signal);
        } catch (Exception e) {
            result.put("rsi", 50.0);
            result.put("macd", 0.0);
            result.put("signal", "UNKNOWN");
        }
        return result;
    }

    private double calculateRSI(List<HistoricalStockPrice> prices, int period) {
        List<Double> gains = new ArrayList<>();
        List<Double> losses = new ArrayList<>();

        for (int i = 1; i < prices.size(); i++) {
            double change = prices.get(i).getPrice() - prices.get(i-1).getPrice();
            if (change >= 0) {
                gains.add(change);
                losses.add(0.0);
            } else {
                gains.add(0.0);
                losses.add(Math.abs(change));
            }
        }

        double avgGain = gains.subList(gains.size() - period, gains.size())
                .stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        double avgLoss = losses.subList(losses.size() - period, losses.size())
                .stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        if (avgLoss == 0) return 100.0;
        double rs = avgGain / avgLoss;
        return 100.0 - (100.0 / (1.0 + rs));
    }

    private double calculateMACD(List<HistoricalStockPrice> prices) {
        int fastPeriod = 12;
        int slowPeriod = 26;
        int signalPeriod = 9;

        // Calculate EMAs
        double fastEMA = calculateEMA(prices, fastPeriod);
        double slowEMA = calculateEMA(prices, slowPeriod);

        // Calculate MACD line
        double macdLine = fastEMA - slowEMA;

        // Calculate Signal line (EMA of MACD)
        List<Double> macdValues = new ArrayList<>();
        for (int i = 0; i < prices.size(); i++) {
            double fastEMAi = calculateEMA(prices.subList(0, i + 1), fastPeriod);
            double slowEMAi = calculateEMA(prices.subList(0, i + 1), slowPeriod);
            macdValues.add(fastEMAi - slowEMAi);
        }

        double signalLine = calculateEMA(macdValues, signalPeriod);

        // Return MACD histogram
        return macdLine - signalLine;
    }

    private double calculateEMA(List<?> data, int period) {
        if (data.isEmpty()) return 0.0;
        
        double multiplier = 2.0 / (period + 1);
        double ema = ((Number) data.get(0)).doubleValue();

        for (int i = 1; i < data.size(); i++) {
            double value = ((Number) data.get(i)).doubleValue();
            ema = (value - ema) * multiplier + ema;
        }

        return ema;
    }
} 