package uz.tour.uzbektourbot.entity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdsRepository extends JpaRepository<Ads, Long> {
    Optional<Ads> findByIsActive(Boolean isActive);
}
