package com.urlshortener.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Builder
@Getter
@Setter
@Entity
@Table(name = "click_analytics")
@AllArgsConstructor
@NoArgsConstructor
public class ClickAnalytics extends BaseEntity{

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "url_id", nullable = false)
  private Url url;

  @Column(nullable = true)
  private String referrer;

  @Column(nullable = true)
  private String userAgent;

  @Column(nullable = true)
  private String ipAddress;

  @Column(name = "clicked_at", nullable = false)
  @CreationTimestamp
  private LocalDateTime clickedAt;
}
