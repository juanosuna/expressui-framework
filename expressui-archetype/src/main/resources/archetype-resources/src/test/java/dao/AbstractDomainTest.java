#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.dao;

import com.expressui.core.entity.security.Role;
import com.expressui.core.entity.security.User;
import com.expressui.core.security.SecurityService;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

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

    @Before
    public void before() {
        securityService.loginAsDefaultSystemUser();
    }
}
