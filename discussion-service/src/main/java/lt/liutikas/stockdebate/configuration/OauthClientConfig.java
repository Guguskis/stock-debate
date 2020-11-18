package lt.liutikas.stockdebate.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.AccessTokenProvider;
import org.springframework.security.oauth2.client.token.DefaultAccessTokenRequest;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsAccessTokenProvider;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordAccessTokenProvider;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordResourceDetails;

@Configuration
public class OauthClientConfig {
    //    Taken from https://stackoverflow.com/questions/27864295/how-to-use-oauth2resttemplate
//    https://github.com/mariubog/oauth-client-sample/blob/master/src/main/java/oauth/client/demo/config/OauthClientConfig.java
    @Autowired(required = false)
    ClientHttpRequestFactory clientHttpRequestFactory;

    /*
     * ClientHttpRequestFactory is autowired and checked in case somewhere in
     * your configuration you provided {@link ClientHttpRequestFactory}
     * implementation Bean where you defined specifics of your connection, if
     * not it is instantiated here with {@link SimpleClientHttpRequestFactory}
     */
    private ClientHttpRequestFactory getClientHttpRequestFactory() {
        if (clientHttpRequestFactory == null) {
            clientHttpRequestFactory = new SimpleClientHttpRequestFactory();
        }
        return clientHttpRequestFactory;
    }

    @Bean
    @Qualifier("myRestTemplate")
    public OAuth2RestOperations restTemplate() {
        String tokenUrl = "https://www.reddit.com/api/v1/access_token";
        OAuth2RestTemplate template = new OAuth2RestTemplate(fullAccessResourceDetails(tokenUrl),
                new DefaultOAuth2ClientContext(new DefaultAccessTokenRequest()));
        return prepareTemplate(template, false);
    }


    public OAuth2RestTemplate prepareTemplate(OAuth2RestTemplate template, boolean isClient) {
        template.setRequestFactory(getClientHttpRequestFactory());
        if (isClient) {
            template.setAccessTokenProvider(clientAccessTokenProvider());
        } else {
            template.setAccessTokenProvider(userAccessTokenProvider());
        }
        return template;
    }

    @Bean
    public AccessTokenProvider userAccessTokenProvider() {
        ResourceOwnerPasswordAccessTokenProvider accessTokenProvider = new ResourceOwnerPasswordAccessTokenProvider();
        accessTokenProvider.setRequestFactory(getClientHttpRequestFactory());
        return accessTokenProvider;
    }

    @Bean
    public AccessTokenProvider clientAccessTokenProvider() {
        ClientCredentialsAccessTokenProvider accessTokenProvider = new ClientCredentialsAccessTokenProvider();
        accessTokenProvider.setRequestFactory(getClientHttpRequestFactory());
        return accessTokenProvider;
    }

    @Bean
    public OAuth2ProtectedResourceDetails fullAccessResourceDetails(String tokenUrl) {
        ResourceOwnerPasswordResourceDetails resource = new ResourceOwnerPasswordResourceDetails();
        resource.setAccessTokenUri(tokenUrl);
        resource.setClientId("NhxpfVe5ldAnbA");
        resource.setClientSecret("6Br7EqV3FF_5ekQeOGUMiEsoo1Gzhg");
        resource.setGrantType("password");
        resource.setUsername("RetardStockBot");
        resource.setPassword("retardstockbot");
        return resource;
    }

}