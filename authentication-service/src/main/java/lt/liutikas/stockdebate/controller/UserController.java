package lt.liutikas.stockdebate.controller;

import lt.liutikas.stockdebate.model.dto.CanLoginRequest;
import lt.liutikas.stockdebate.model.dto.CanLoginResponse;
import lt.liutikas.stockdebate.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public CanLoginResponse getStocks(@RequestBody CanLoginRequest request) {
        return userService.canLogin(request);
    }
}
