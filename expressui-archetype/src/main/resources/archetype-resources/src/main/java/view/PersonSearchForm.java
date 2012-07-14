#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.view;

import ${package}.dao.PersonQuery;
import com.expressui.core.view.form.FormFieldSet;
import com.expressui.core.view.form.SearchForm;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
@SuppressWarnings({"serial", "rawtypes"})
public class PersonSearchForm extends SearchForm<PersonQuery> {

    @Override
    public void init(FormFieldSet formFields) {

        formFields.setCoordinates("lastName", 1, 1);
    }
}
