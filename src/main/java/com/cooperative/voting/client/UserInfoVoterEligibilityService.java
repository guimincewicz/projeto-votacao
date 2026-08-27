package com.cooperative.voting.client;
import com.cooperative.voting.exception.AssociateNotAllowedException;
import com.cooperative.voting.exception.InvalidCpfException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Service
@ConditionalOnProperty(prefix = "external.user-info", name = "enabled", havingValue = "true")
public class UserInfoVoterEligibilityService implements VoterEligibilityService {
    private static final Logger log = LoggerFactory.getLogger(UserInfoVoterEligibilityService.class);
    private final RestClient client;
    public UserInfoVoterEligibilityService(RestClient.Builder builder, @Value("${external.user-info.base-url}") String baseUrl) { this.client = builder.baseUrl(baseUrl).build(); }
    @Override public void ensureEligible(String cpf) {
        try {
            String response = client.get().uri("/users/{cpf}", cpf).retrieve().body(String.class);
            if (response == null || !response.contains("ABLE_TO_VOTE") || response.contains("UNABLE_TO_VOTE")) throw new AssociateNotAllowedException("Associate is not allowed to vote");
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) throw new InvalidCpfException("Invalid CPF");
            log.warn("User-info service returned status {}", exception.getStatusCode());
            throw new AssociateNotAllowedException("Could not validate associate eligibility");
        } catch (RuntimeException exception) {
            if (exception instanceof AssociateNotAllowedException || exception instanceof InvalidCpfException) throw exception;
            log.warn("User-info service communication failed", exception);
            throw new AssociateNotAllowedException("Could not validate associate eligibility");
        }
    }
}
