#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.entity;

import com.expressui.core.entity.NameableEntity;
import com.expressui.core.entity.WritableEntity;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table
public class Person extends WritableEntity implements NameableEntity {

    private String firstName;

    private String lastName;

    public Person() {
    }

    public Person(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @NotBlank
    @NotNull
    @Size(min = 1, max = 32)
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @NotBlank
    @NotNull
    @Size(min = 1, max = 32)
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public String getName() {
        if (getFirstName() == null && getLastName() != null) {
            return getFirstName();
        } else if (getFirstName() != null && getLastName() == null) {
            return getLastName();
        } else if (getFirstName() == null && getLastName() == null) {
            return null;
        } else {
            return getLastName() + ", " + getFirstName();
        }
    }

    @Override
    public String toString() {
        return getName();
    }
}