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

package com.expressui.core.view.page;

import java.util.HashMap;
import java.util.Map;

/**
 * Place for storing beans during a page conversation, which exists for as long as user remains on a single page.
 */
public class PageConversation {
    private String id;
    private Map<String, Object> beans = new HashMap<String, Object>();

    public PageConversation(String id) {
        this.id = id;
    }

    /**
     * Gets this conversations id.
     *
     * @return conversation id
     */
    public String getId() {
        return id;
    }

    /**
     * Puts a bean into this conversation's storage.
     *
     * @param key key
     * @param bean value
     */
    public void put(String key, Object bean) {
        beans.put(key, bean);
    }

    /**
     * Gets a bean from this conversation's storage.
     * @param key
     * @return value
     */
    public Object get(String key) {
        return beans.get(key);
    }

    /**
     * Removes a bean from this conversation's storage.
     * @param key key
     * @return removed bean
     */
    public Object remove(String key) {
        return beans.remove(key);
    }

    /**
     * Asks if this conversation contains a bean.
     *
     * @param key key
     * @return true if this conversation contains the bean.
     */
    public boolean containsKey(String key) {
        return beans.containsKey(key);
    }
}
