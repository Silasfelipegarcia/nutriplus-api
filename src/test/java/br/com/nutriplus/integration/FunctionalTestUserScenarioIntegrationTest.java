package br.com.nutriplus.integration;

import br.com.nutriplus.AbstractIntegrationTest;
import br.com.nutriplus.infrastructure.dev.DevTestUserSpec;
import br.com.nutriplus.infrastructure.dev.FunctionalTestUserSeeder;
import br.com.nutriplus.support.FunctionalTestUserAssertions;
import br.com.nutriplus.support.IntegrationAuthSupport;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Validação funcional automatizada de todos os cenários do catálogo {@link DevTestUserSpec}.
 */
class FunctionalTestUserScenarioIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FunctionalTestUserSeeder functionalTestUserSeeder;

    @ParameterizedTest(name = "{0}")
    @EnumSource(DevTestUserSpec.class)
    void seededUserMatchesFunctionalContract(DevTestUserSpec spec) throws Exception {
        String email = spec.integrationTestEmail();
        functionalTestUserSeeder.seedSpec(spec, email);

        String token = IntegrationAuthSupport.loginAs(
                mockMvc, email, FunctionalTestUserAssertions.defaultPassword());
        var auth = IntegrationAuthSupport.bearerHeaders(token);

        FunctionalTestUserAssertions.assertScenarioContract(mockMvc, auth, spec);
    }
}
