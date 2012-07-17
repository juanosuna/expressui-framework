package com.expressui.core.view.security.role.related;

import com.expressui.core.dao.query.ToManyRelationshipQuery;
import com.expressui.core.dao.security.UserRoleDao;
import com.expressui.core.dao.security.query.RelatedUsersQuery;
import com.expressui.core.entity.security.Role;
import com.expressui.core.entity.security.User;
import com.expressui.core.entity.security.UserRole;
import com.expressui.core.util.SpringApplicationContext;
import com.expressui.core.view.results.ResultsFieldSet;
import com.expressui.core.view.security.select.UserSelect;
import com.expressui.core.view.security.user.UserForm;
import com.expressui.core.view.tomanyrelationship.ManyToManyRelationship;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

/**
 * Users related to a role.
 */
@Component
@Scope(SCOPE_PROTOTYPE)
public class RelatedUsers extends ManyToManyRelationship<User, UserRole> {

    @Resource
    private UserRoleDao userRoleDao;

    @Resource
    private UserSelect userSelect;

    @Resource
    private RelatedUsersQuery relatedUsersQuery;

    @Override
    public UserRoleDao getAssociationDao() {
        return userRoleDao;
    }

    @Override
    public UserSelect getEntitySelect() {
        return userSelect;
    }

    @Override
    public UserForm createEntityForm() {
        return SpringApplicationContext.getBean(UserForm.class);
    }

    @Override
    public ToManyRelationshipQuery getEntityQuery() {
        return relatedUsersQuery;
    }

    @Override
    public void preAdd() {
        Role parentRole = relatedUsersQuery.getParent();
        userSelect.getResults().getEntityQuery().setDoesNotBelongToRole(parentRole);
    }

    @Override
    public void init(ResultsFieldSet resultsFields) {
        resultsFields.setPropertyIds(
                "loginName",
                "lastModified",
                "modifiedBy"
        );
    }

    @Override
    public String getChildPropertyId() {
        return "userRoles";
    }

    @Override
    public String getParentPropertyId() {
        return "userRoles";
    }

    @Override
    public UserRole createAssociationEntity(User user) {
        return new UserRole(user, relatedUsersQuery.getParent());
    }
}
