package lt.liutikas.stockdebate.repository;

import lt.liutikas.stockdebate.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public interface UserRepository extends JpaRepository<User, Integer> {
}
