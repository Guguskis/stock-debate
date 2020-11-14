package lt.liutikas.stockdebate.service;

import lt.liutikas.stockdebate.model.User;
import lt.liutikas.stockdebate.model.dto.CanLoginRequest;
import lt.liutikas.stockdebate.model.dto.CanLoginResponse;
import lt.liutikas.stockdebate.model.dto.RegisterRequest;
import lt.liutikas.stockdebate.repository.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserServiceTest {

    private UserService userService;
    private UserRepository userRepositoryMock;

    @Before
    public void setUp() {
        userRepositoryMock = mock(UserRepository.class);
        userService = new UserService(userRepositoryMock);
    }

    @Test
    public void canLogin_providedCorrectCombination_returnsTrueAndUser() {
        CanLoginRequest request = new CanLoginRequest();
        request.setUsername("Trump");
        request.setPassword("TinyHands");

        User user = new User();
        user.setUsername("Trump");
        user.setPassword("TinyHands");

        when(userRepositoryMock.findByUsernameIgnoreCase("Trump"))
                .thenReturn(user);

        ResponseEntity responseEntity = userService.canLogin(request);
        CanLoginResponse response = (CanLoginResponse) responseEntity.getBody();

        assertTrue(response.isCanLogin());
    }

    @Test
    public void canLogin_providedNotExistingUsername_returnsWrappedFalse() {
        CanLoginRequest request = new CanLoginRequest();
        request.setUsername("Trump");
        request.setPassword("TinyHands");

        User user = new User();
        user.setUsername("Trump");
        user.setPassword("TinyHands");

        ResponseEntity responseEntity = userService.canLogin(request);
        CanLoginResponse response = (CanLoginResponse) responseEntity.getBody();

        assertFalse(response.isCanLogin());
    }

    @Test
    public void canLogin_providedIncorrectPassword_returnsWrappedFalse() {
        CanLoginRequest request = new CanLoginRequest();
        request.setUsername("Trump");
        request.setPassword("TinyHands");

        User user = new User();
        user.setUsername("Trump");
        user.setPassword("tinyhands");

        when(userRepositoryMock.findByUsernameIgnoreCase("Trump"))
                .thenReturn(user);

        ResponseEntity responseEntity = userService.canLogin(request);
        CanLoginResponse response = (CanLoginResponse) responseEntity.getBody();

        assertFalse(response.isCanLogin());
    }

    @Test
    public void register_providedNotTakenUsername_returnsUser() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("Trump");
        request.setPassword("TinyHands");

        User user = new User();
        user.setId(1);
        user.setUsername("Trump");
        user.setPassword("TinyHands");

        when(userRepositoryMock.save(any()))
                .thenReturn(user);

        ResponseEntity responseEntity = userService.register(request);
        User response = (User) responseEntity.getBody();

        assertEquals(user.getId(), response.getId());
        assertEquals(user.getUsername(), response.getUsername());
        assertEquals(user.getPassword(), response.getPassword());
    }

    @Test
    public void register_providedTakenUsername_returnsBadResponseMessage() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("Trump");
        request.setPassword("TinyHands");

        User user = new User();
        user.setId(1);
        user.setUsername("Trump");
        user.setPassword("TinyHands");

        when(userRepositoryMock.findByUsernameIgnoreCase("Trump"))
                .thenReturn(user);

        ResponseEntity responseEntity = userService.register(request);
        String response = (String) responseEntity.getBody();

        assertEquals("Username already taken", response);
    }

    @Test
    public void getUser_providedExistingUsername_returnsUserNoPassword() {
        User user = new User();
        user.setId(1);
        user.setUsername("Trump");
        user.setPassword("TinyHands");

        when(userRepositoryMock.findByUsernameIgnoreCase("Trump"))
                .thenReturn(user);

        ResponseEntity responseEntity = userService.getUser("Trump");
        User response = (User) responseEntity.getBody();

        assertEquals(user.getId(), response.getId());
        assertEquals(user.getUsername(), response.getUsername());
        assertNull(response.getPassword());
    }

    @Test
    public void getUser_providedNotExistingUsername_returnsMessage() {
        User user = new User();
        user.setId(1);
        user.setUsername("Trump");
        user.setPassword("TinyHands");

        ResponseEntity responseEntity = userService.getUser("Trump");
        String response = (String) responseEntity.getBody();

        assertEquals("User not found", response);
    }

}