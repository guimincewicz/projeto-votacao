package com.cooperative.voting.client;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(prefix = "external.user-info", name = "enabled", havingValue = "false", matchIfMissing = true)
public class LocalVoterEligibilityService implements VoterEligibilityService {
    @Override public void ensureEligible(String cpf) { }
}
