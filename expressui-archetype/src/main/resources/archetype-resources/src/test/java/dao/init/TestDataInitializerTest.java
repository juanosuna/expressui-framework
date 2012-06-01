#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.dao.init;

import ${package}.dao.AbstractDomainTest;
import org.junit.Test;
import org.springframework.test.context.transaction.TransactionConfiguration;

import javax.annotation.Resource;

@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = false)
public class TestDataInitializerTest extends AbstractDomainTest {

    @Resource
    private TestDataInitializer testDataInitializer;

    @Test
    public void initialize() throws Exception {
        testDataInitializer.initialize(1000);
    }
}
