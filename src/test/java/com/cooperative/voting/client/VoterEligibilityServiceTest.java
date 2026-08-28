package com.cooperative.voting.client;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.cooperative.voting.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

@ExtendWith(MockitoExtension.class)
class VoterEligibilityServiceTest {

    private static final String BASE_URL = "http://user-info.test";

    @Mock
    private MessageSource messageSource;

    private MockRestServiceServer server;
    private VoterEligibilityService service;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        service = new VoterEligibilityService(messageSource, builder, true, BASE_URL);
    }

    @Test
    void shouldAllowVoteWhenIntegrationIsDisabled() {
        VoterEligibilityService localService = new VoterEligibilityService(
                messageSource,
                RestClient.builder(),
                false,
                BASE_URL
        );

        assertDoesNotThrow(() -> localService.ensureEligible("12345678909"));
    }

    @Test
    void shouldRejectInvalidCpf() {
        server.expect(requestTo(BASE_URL + "/users/12345678909"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.ensureEligible("12345678909")
        );

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, exception.getStatus());
        server.verify();
    }

    @Test
    void shouldRejectAssociateWithoutVotingPermission() {
        server.expect(requestTo(BASE_URL + "/users/12345678909"))
                .andRespond(withSuccess("{\"status\":\"UNABLE_TO_VOTE\"}", MediaType.APPLICATION_JSON));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> service.ensureEligible("12345678909")
        );

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, exception.getStatus());
        server.verify();
    }
}
