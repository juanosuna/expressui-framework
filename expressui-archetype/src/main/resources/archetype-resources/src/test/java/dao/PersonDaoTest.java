#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.dao;

import ${package}.entity.Person;
import com.expressui.core.dao.GenericDao;
import com.google.i18n.phonenumbers.NumberParseException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.List;

public class PersonDaoTest extends AbstractDomainTest {

    @Resource
    private GenericDao genericDao;

    @Resource
    private PersonQuery personQuery;

    @Before
    public void createPerson() throws NumberParseException {

        Person person = new Person();
        person.setFirstName("Juan");
        person.setLastName("Osuna");

        genericDao.persist(person);
    }

    @Test
    public void findByName() throws NumberParseException {
        personQuery.setLastName("Osuna");
        List<Person> persons = personQuery.execute();
        Assert.assertNotNull(persons);
        Assert.assertTrue(persons.size() > 0);
        Assert.assertEquals("Osuna", persons.get(0).getLastName());
    }
}
