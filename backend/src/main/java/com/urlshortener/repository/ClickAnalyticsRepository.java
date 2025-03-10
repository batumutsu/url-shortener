package com.urlshortener.repository;

import com.urlshortener.entity.ClickAnalytics;
import com.urlshortener.entity.Url;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ClickAnalyticsRepository extends JpaRepository<ClickAnalytics, UUID> {
  List<ClickAnalytics> findByUrl(Url url);

  @Query("SELECT COUNT(c) FROM ClickAnalytics c WHERE c.url = :url AND c.clickedAt BETWEEN :startDate AND :endDate")
  long countByUrlAndDateRange(Url url, LocalDateTime startDate, LocalDateTime endDate);
}
