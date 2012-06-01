#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.view;

import ${package}.dao.PersonQuery;
import ${package}.entity.Person;
import com.expressui.core.view.results.CrudResults;
import com.expressui.core.view.results.ResultsFieldSet;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
@SuppressWarnings({"serial"})
public class PersonResults extends CrudResults<Person> {

    @Resource
    private PersonQuery personQuery;

    @Resource
    private PersonForm personForm;

    @Override
    public PersonQuery getEntityQuery() {
        return personQuery;
    }

    @Override
    public PersonForm getEntityForm() {
        return personForm;
    }

    @Override
    public void init(ResultsFieldSet resultsFields) {

        resultsFields.setPropertyIds(
                "firstName",
                "lastName",
                "lastModified",
                "modifiedBy"
        );
    }
}
