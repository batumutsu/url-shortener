package com.urlshortener.repository;

import com.urlshortener.entity.Url;
import com.urlshortener.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UrlRepository extends JpaRepository<Url, UUID> {
  Optional<Url> findByShortCode(String shortCode);
  List<Url> findByUser(User user);

  @Modifying
  @Query("UPDATE Url u SET u.clicks = u.clicks + 1 WHERE u.shortCode = :shortCode")
  void incrementClicks(String shortCode);

  boolean existsByShortCode(String shortCode);

  Optional<Url> findByLongUrlAndUser(String longUrl, User user);
}
