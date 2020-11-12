package lt.liutikas.stockdebate.service;

import lt.liutikas.stockdebate.model.dto.CanLoginRequest;
import lt.liutikas.stockdebate.model.dto.CanLoginResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UserService {

    private final Logger LOG = LoggerFactory.getLogger(UserService.class);

    public CanLoginResponse canLogin(CanLoginRequest request) {
        LOG.info(String.format("Attempting to login user '%s'", request.getUsername()));

        CanLoginResponse canLoginResponse = new CanLoginResponse();
        canLoginResponse.setCanLogin(true);
        return canLoginResponse;
    }
}
