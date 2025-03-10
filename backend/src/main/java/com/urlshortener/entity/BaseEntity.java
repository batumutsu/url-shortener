package com.urlshortener.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity implements Serializable {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @JsonIgnore
  private UUID id;

  @Temporal(TemporalType.TIMESTAMP)
  @UpdateTimestamp
  private LocalDateTime updatedAt;

  @Temporal(TemporalType.TIMESTAMP)
  @CreationTimestamp
  private LocalDateTime createdAt;
}
