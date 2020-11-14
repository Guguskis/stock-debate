package lt.liutikas.stockdebate.service;

import lt.liutikas.stockdebate.model.User;
import lt.liutikas.stockdebate.model.dto.CanLoginRequest;
import lt.liutikas.stockdebate.model.dto.CanLoginResponse;
import lt.liutikas.stockdebate.model.dto.RegisterRequest;
import lt.liutikas.stockdebate.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class UserService {

    private final Logger LOG = LoggerFactory.getLogger(UserService.class);

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public ResponseEntity canLogin(CanLoginRequest request) {
        boolean canLogin = false;

        User user = repository.findByUsernameIgnoreCase(request.getUsername());

        if (user != null) {
            if (user.getPassword().equals(request.getPassword())) {
                canLogin = true;
            }
        }

        if (canLogin) {
            LOG.info(String.format("User '%s' successful login", request.getUsername()));
        } else {
            LOG.info(String.format("User '%s' failed to login. Reason: incorrect password or username", request.getUsername()));
        }

        CanLoginResponse canLoginResponse = new CanLoginResponse();
        canLoginResponse.setCanLogin(canLogin);

        return ResponseEntity.ok(canLoginResponse);
    }

    public ResponseEntity register(RegisterRequest request) {

        User existingUser = repository.findByUsernameIgnoreCase(request.getUsername());

        if (existingUser != null) {
            LOG.info(String.format("User '%s' failed to register. Reason: username already taken", request.getUsername()));
            return ResponseEntity.badRequest().body("Username already taken");
        }

        User userToSave = new User();
        userToSave.setUsername(request.getUsername());
        userToSave.setPassword(request.getPassword());

        User savedUser = repository.save(userToSave);
        return ResponseEntity.ok(savedUser);
    }

    public ResponseEntity getUser(String username) {
        User user = repository.findByUsernameIgnoreCase(username);

        if (user == null) {
            LOG.info(String.format("User '%s' not found", username));
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        return ResponseEntity.ok(user);
    }
}
