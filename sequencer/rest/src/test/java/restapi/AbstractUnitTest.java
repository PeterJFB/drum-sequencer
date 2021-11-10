package restapi;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import restserver.SequencerServerApplication;

/**
 * Contains the class annotations which will be common for all unittests.
 */
@ContextConfiguration(classes = { UnitTestConfiguration.class, SequencerServerApplication.class })
@TestPropertySource(locations = { "classpath:test.properties" })
public class AbstractUnitTest {

}
