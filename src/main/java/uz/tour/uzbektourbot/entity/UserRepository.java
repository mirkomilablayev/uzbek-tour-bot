package uz.tour.uzbektourbot.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.tour.uzbektourbot.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByChatId(String realId);
}
