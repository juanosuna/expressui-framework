#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.dao;

import ${package}.entity.Person;
import com.expressui.core.dao.query.StructuredEntityQuery;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
public class PersonQuery extends StructuredEntityQuery<Person> {

    private String lastName;

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public List<Predicate> buildCriteria(CriteriaBuilder builder, CriteriaQuery<Person> query, Root<Person> person) {
        List<Predicate> predicates = new ArrayList<Predicate>();

        if (hasValue(lastName)) {
            ParameterExpression<String> lastNameExp = builder.parameter(String.class, "lastName");
            predicates.add(builder.like(builder.upper(person.<String>get("lastName")), lastNameExp));
        }

        return predicates;
    }

    @Override
    public void setParameters(TypedQuery<Serializable> typedQuery) {
        if (hasValue(lastName)) {
            typedQuery.setParameter("lastName", "%" + lastName.toUpperCase() + "%");
        }
    }

    @Override
    public String toString() {
        return "PersonQuery{" +
                "lastName='" + lastName +
                '}';
    }
}
