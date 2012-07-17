#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.dao;

import com.expressui.core.security.SecurityService;
import org.junit.Before;
import com.expressui.core.MainApplication;
import com.expressui.core.security.SecurityService;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Locale;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/spring/applicationContext-properties.xml",
        "classpath:/spring/applicationContext-data-access.xml",
        "classpath:/spring/applicationContext-messages.xml",
        "classpath:/spring/applicationContext-core-scan.xml",
        "classpath:/spring/applicationContext-scan.xml",
        "classpath:/spring/applicationContext-test-scope.xml",
})
@Transactional
public abstract class AbstractDomainTest {

    @Resource
    protected SecurityService securityService;

    @BeforeClass
    public static void beforeClass() {
        MainApplication mainApplication = mock(MainApplication.class);
        MainApplication.setInstance(mainApplication);
        when(mainApplication.getLocale()).thenReturn(Locale.getDefault());
    }

    @Before
    public void before() {
        securityService.loginAsDefaultSystemUser();
    }
}
