package lt.liutikas.stockdebate.controller;

import lt.liutikas.stockdebate.model.dto.CanLoginRequest;
import lt.liutikas.stockdebate.model.dto.RegisterRequest;
import lt.liutikas.stockdebate.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity getStocks(@RequestBody CanLoginRequest request) {
        return userService.canLogin(request);
    }

    @PostMapping
    public ResponseEntity register(@RequestBody RegisterRequest request) {
        return userService.register(request);
    }

    @GetMapping("/{username}")
    public ResponseEntity getUser(@PathVariable("username") String username) {
        return userService.getUser(username);
    }

}
