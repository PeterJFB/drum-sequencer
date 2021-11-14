package restserver;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

/**
 * Contains the class annotations which will be common for all integrationtests.
 */
@SpringBootTest(classes = SequencerServerApplication.class)
@TestPropertySource(locations = { "classpath:test.properties" })
@ContextConfiguration(classes = { IntegrationTestConfiguration.class })
public abstract class AbstractIntegrationTest {

}