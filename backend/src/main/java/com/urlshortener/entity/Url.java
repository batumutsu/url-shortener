package com.urlshortener.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.ColumnDefault;

@Builder
@Getter
@Setter
@Entity
@Table(name = "urls")
@AllArgsConstructor
@NoArgsConstructor
public class Url extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "short_code", nullable = false, unique = true, length = 10)
  private String shortCode;

  @Column(name = "long_url", nullable = false, length = 2048)
  private String longUrl;

  @Column(nullable = false)
  @ColumnDefault("0")
  private Integer clicks;

  @OneToMany(mappedBy = "url", cascade = CascadeType.ALL)
  private List<ClickAnalytics> clickAnalytics;
}