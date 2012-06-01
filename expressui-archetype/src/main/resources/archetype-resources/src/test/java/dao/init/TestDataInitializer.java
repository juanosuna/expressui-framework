#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.dao.init;

import ${package}.entity.Person;
import com.expressui.core.dao.GenericDao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Transactional
public class TestDataInitializer {

    @Resource
    private GenericDao genericDao;

    public void initialize(int count) {

        for (Integer i = 0; i < count; i++) {
            Person person;
            person = new Person("first" + i, "last" + i);
            genericDao.persist(person);

            if (i % 50 == 0) {
                genericDao.flush();
                genericDao.clear();
            }
        }
    }
}
