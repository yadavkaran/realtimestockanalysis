package org.example.realtime.controller;

import org.example.realtime.model.StockPrice;
import org.example.realtime.service.StockAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/analysis")
public class StockAnalysisController {

    @Autowired
    private StockAnalysisService stockAnalysisService;

    @GetMapping("/trend/{symbol}")
    public ResponseEntity<Map<String, Object>> getStockTrend(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "1d") String timeframe) {
        return ResponseEntity.ok(stockAnalysisService.analyzeTrend(symbol, timeframe));
    }

    @GetMapping("/volatility/{symbol}")
    public ResponseEntity<Map<String, Object>> getVolatilityAnalysis(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "1d") String timeframe) {
        return ResponseEntity.ok(stockAnalysisService.calculateVolatility(symbol, timeframe));
    }

    @GetMapping("/moving-average/{symbol}")
    public ResponseEntity<Map<String, Object>> getMovingAverages(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "20") int shortPeriod,
            @RequestParam(defaultValue = "50") int longPeriod) {
        return ResponseEntity.ok(stockAnalysisService.calculateMovingAverages(symbol, shortPeriod, longPeriod));
    }

    @GetMapping("/volume-analysis/{symbol}")
    public ResponseEntity<Map<String, Object>> getVolumeAnalysis(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "1d") String timeframe) {
        return ResponseEntity.ok(stockAnalysisService.analyzeVolume(symbol, timeframe));
    }

    @GetMapping("/technical-indicators/{symbol}")
    public ResponseEntity<Map<String, Object>> getTechnicalIndicators(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "14") int period) {
        return ResponseEntity.ok(stockAnalysisService.calculateTechnicalIndicators(symbol, period));
    }
} 