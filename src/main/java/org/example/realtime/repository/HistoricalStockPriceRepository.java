package org.example.realtime.repository;

import org.example.realtime.model.HistoricalStockPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HistoricalStockPriceRepository extends JpaRepository<HistoricalStockPrice, Long> {
    
    List<HistoricalStockPrice> findBySymbolOrderByTimestampDesc(String symbol);
    
    List<HistoricalStockPrice> findBySymbolAndTimestampBetweenOrderByTimestampAsc(
        String symbol, LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT AVG(h.price) FROM HistoricalStockPrice h WHERE h.symbol = ?1 AND h.timestamp >= ?2")
    Double calculateAveragePrice(String symbol, LocalDateTime since);
    
    @Query("SELECT MAX(h.price) FROM HistoricalStockPrice h WHERE h.symbol = ?1 AND h.timestamp >= ?2")
    Double findMaxPrice(String symbol, LocalDateTime since);
    
    @Query("SELECT MIN(h.price) FROM HistoricalStockPrice h WHERE h.symbol = ?1 AND h.timestamp >= ?2")
    Double findMinPrice(String symbol, LocalDateTime since);
} 