package uz.tour.uzbektourbot.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.tour.uzbektourbot.entity.User;

import javax.jws.soap.SOAPBinding;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByChatId(String realId);
    List<User> findAllByIsAdmin(Boolean isAdmin);
}
