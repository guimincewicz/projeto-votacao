package com.cooperative.voting.client;

import com.cooperative.voting.exception.BusinessException;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Service
public class VoterEligibilityService {

    private static final Logger log = LoggerFactory.getLogger(VoterEligibilityService.class);

    private final boolean enabled;
    private final MessageSource messageSource;
    private final RestClient restClient;

    public VoterEligibilityService(
            MessageSource messageSource,
            RestClient.Builder restClientBuilder,
            @Value("${external.user-info.enabled}") boolean enabled,
            @Value("${external.user-info.base-url}") String baseUrl
    ) {
        this.messageSource = messageSource;
        this.enabled = enabled;
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    public void ensureEligible(String cpf) {
        if (!enabled) {
            return;
        }

        try {
            UserInfoResponse response = restClient.get()
                    .uri("/users/{cpf}", cpf)
                    .retrieve()
                    .body(UserInfoResponse.class);

            if (response == null || !"ABLE_TO_VOTE".equals(response.status())) {
                throw new BusinessException(message("associate.not-allowed"));
            }
        } catch (RestClientException exception) {
            if (isInvalidCpf(exception)) {
                throw new BusinessException(message("cpf.invalid"));
            }

            log.warn("Could not validate voter eligibility", exception);
            throw new BusinessException(HttpStatus.SERVICE_UNAVAILABLE, message("user-info.unavailable"));
        }
    }

    private boolean isInvalidCpf(RestClientException exception) {
        return exception instanceof RestClientResponseException responseException
                && responseException.getStatusCode() == HttpStatus.NOT_FOUND;
    }

    private String message(String key) {
        return messageSource.getMessage(key, null, Locale.getDefault());
    }

    private record UserInfoResponse(String status) {
    }
}
