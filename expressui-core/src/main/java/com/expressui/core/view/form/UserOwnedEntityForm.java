/*
 * Copyright (c) 2012 Brown Bag Consulting.
 * This file is part of the ExpressUI project.
 * Author: Juan Osuna
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License Version 3
 * as published by the Free Software Foundation with the addition of the
 * following permission added to Section 15 as permitted in Section 7(a):
 * FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
 * Brown Bag Consulting, Brown Bag Consulting DISCLAIMS THE WARRANTY OF
 * NON INFRINGEMENT OF THIRD PARTY RIGHTS.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License.
 *
 * You can be released from the requirements of the license by purchasing
 * a commercial license. Buying such a license is mandatory as soon as you
 * develop commercial activities involving the ExpressUI software without
 * disclosing the source code of your own applications. These activities
 * include: offering paid services to customers as an ASP, providing
 * services from a web application, shipping ExpressUI with a closed
 * source product.
 *
 * For more information, please contact Brown Bag Consulting at this
 * address: juan@brownbagconsulting.com.
 */

package com.expressui.core.view.form;

import com.expressui.core.dao.security.UserDao;
import com.expressui.core.entity.UserOwnedEntity;
import com.expressui.core.entity.security.User;

import javax.annotation.Resource;

/**
 * A form bound to an entity that is owned by a user. A user-owned entity form, for example profile, registration, etc,
 * is typically displayed in the page without requiring a search and selection from results.
 * <p/>
 * The current user is automatically set on the user-owned entity, whenever the entity is saved.
 *
 * @param <T> type that must inherit from both UserOwnedEntity and WritableEntity
 */
public abstract class UserOwnedEntityForm<T extends UserOwnedEntity> extends EntityForm<T> {

    @Resource
    private UserDao userDao;

    @Override
    public void postCreate(T entity) {
        super.postCreate(entity);

        User user = securityService.refreshCurrentUser();
        if (entity.getUser() == null) {
            entity.setUser(user);
        }
    }

    /**
     * Lifecycle method called before entity is saved to database.
     * The current user is automatically set on the entity, whenever the entity is saved.
     *
     * @param entity entity to be saved
     */
    @Override
    public void preSave(T entity) {
        super.preSave(entity);

        User user = userDao.merge(entity.getUser());
        entity.setUser(user);
    }

    @Override
    public void onDisplay() {
        super.onDisplay();

        syncCrudActions();
    }

    @Override
    public void syncCrudActions() {
        super.syncCrudActions();

        getSaveAndCloseButton().setVisible(false);
        getSaveAndCloseButton().setEnabled(false);
    }
}
