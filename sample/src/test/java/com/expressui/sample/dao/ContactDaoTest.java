/*
 * Copyright (c) 2011 Brown Bag Consulting.
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

package com.expressui.sample.dao;

import com.google.i18n.phonenumbers.NumberParseException;
import com.expressui.sample.entity.*;
import com.expressui.sample.view.contact.ContactQuery;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.List;

public class ContactDaoTest extends AbstractDomainTest {

    @Resource
    private ContactDao contactDao;

    @Resource
    private AddressDao addressDao;

    @Resource
    private StateDao stateDao;

    @Resource
    private CountryDao countryDao;

    @Resource
    private ContactQuery contactQuery;

    @Before
    public void createContact() throws NumberParseException {

        Country country = new Country("XX");
        countryDao.persist(country);
        State state = new State("XX-NC", "North Carolina", country);
        stateDao.persist(state);

        Contact contact = new Contact();
        contact.setFirstName("Juan");
        contact.setLastName("Osuna");
        contact.setMainPhone(new Phone("(704) 555-1212", "US"));
        contact.getMainPhone().setPhoneType(PhoneType.BUSINESS);

        Address address = new Address(AddressType.MAILING);
        address.setStreet("100 Main St.");
        address.setCity("Charlotte");
        address.setState(state);
        address.setCountry(country);
        addressDao.persist(address);
        contact.setMailingAddress(address);
        contact.setOtherAddress(null);
        contactDao.persist(contact);
    }

    @Test
    public void findByName() throws NumberParseException {
        contactQuery.setLastName("Osuna");
        List<Contact> contacts = contactQuery.execute();
        Assert.assertNotNull(contacts);
        Assert.assertTrue(contacts.size() > 0);
        Assert.assertEquals("Osuna", contacts.get(0).getLastName());
    }
}
