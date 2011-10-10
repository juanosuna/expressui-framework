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

package com.expressui.sample.dao.init;

import com.expressui.core.entity.security.AllowOrDeny;
import com.expressui.sample.dao.*;
import com.expressui.sample.entity.*;
import com.expressui.sample.entity.security.Permission;
import com.expressui.sample.entity.security.Role;
import com.expressui.sample.entity.security.User;
import com.expressui.sample.entity.security.UserRole;
import com.expressui.sample.util.PhonePropertyFormatter;
import com.expressui.sample.util.PhoneValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

@Service
@Transactional
public class TestDataInitializer {

    @Resource
    private UserDao userDao;

    @Resource
    private UserRoleDao userRoleDao;

    @Resource
    private RoleDao roleDao;

    @Resource
    private PermissionDao permissionDao;

    @Resource
    private ContactDao contactDao;

    @Resource
    private AccountDao accountDao;

    @Resource
    private OpportunityDao opportunityDao;

    @Resource
    private ReferenceDataInitializer referenceDataInitializer;

    public void initialize(int count) {
        initializeRoles();
        initializeUsers();

        for (Integer i = 0; i < count; i++) {
            Contact contact;
            contact = new Contact("first" + i, "last" + i);
            contact.setBirthDate(randomBirthDate());
            contact.setAssignedTo(ReferenceDataInitializer.random(userDao.findAll()));
            contact.setTitle("Vice President");
            contact.setDoNotCall(randomBoolean());
            contact.setEmail("juan@brownbagconsulting.com");
            contact.setDoNotEmail(randomBoolean());
            contact.setLeadSource(referenceDataInitializer.randomLeadSource());
            Address address = randomAddress(i);
            contact.setMailingAddress(address);

            if (randomBoolean()) {
                Address otherAddress = randomAddress(i);
                contact.setOtherAddress(otherAddress);
            }

            try {
                Phone phone = (Phone) new PhonePropertyFormatter().parse(PhoneValidator.getExampleNumber("US", address.getCountry().getId()));
                contact.setMainPhone(phone);
                contact.getMainPhone().setPhoneType(random(PhoneType.class));

                if (randomBoolean()) {
                    phone = (Phone) new PhonePropertyFormatter().parse(PhoneValidator.getExampleNumber("US", address.getCountry().getId()));
                    contact.setOtherPhone(phone);
                    contact.getOtherPhone().setPhoneType(random(PhoneType.class));
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }


            contact.setDescription("Description of contact");

            initializeAccount(contact, i);
            contactDao.persist(contact);
            if (i % 50 == 0) {
                contactDao.flush();
                contactDao.clear();
            }
        }
    }

    public void initializeRoles() {
        Role role = new Role("ROLE_USER");
        role.setDescription("This role belongs to all users and allows user to login.");
        role.setAllowOrDenyByDefault(AllowOrDeny.DENY);
        roleDao.persist(role);

        role = new Role("ROLE_ADMIN");
        role.setDescription("This role allows full access to everything.");
        roleDao.persist(role);

        role = new Role("ROLE_GUEST");
        role.setDescription("This role demonstrates full access to entities but only view access to security entities.");
        role.setAllowOrDenyByDefault(AllowOrDeny.ALLOW);
        roleDao.persist(role);
        Permission permission = new Permission(Role.class.getName());
        permission.setRole(role);
        permission.setView(true);
        permission.setEdit(false);
        permission.setDelete(false);
        permission.setCreate(false);
        permissionDao.persist(permission);
        permission = new Permission(User.class.getName());
        permission.setRole(role);
        permission.setView(true);
        permission.setEdit(false);
        permission.setDelete(false);
        permission.setCreate(false);
        permissionDao.persist(permission);
        permission = new Permission(Contact.class.getName());
        permission.setRole(role);
        permission.setView(true);
        permission.setEdit(true);
        permission.setDelete(true);
        permission.setCreate(true);
        permissionDao.persist(permission);
        permission = new Permission(Account.class.getName());
        permission.setRole(role);
        permission.setView(true);
        permission.setEdit(true);
        permission.setDelete(true);
        permission.setCreate(true);
        permissionDao.persist(permission);
        permission = new Permission(Opportunity.class.getName());
        permission.setRole(role);
        permission.setView(true);
        permission.setEdit(true);
        permission.setDelete(true);
        permission.setCreate(true);
        permissionDao.persist(permission);

        roleDao.flush();
    }

    public void initializeUsers() {
        Role anyUserRole = roleDao.findByName("ROLE_USER");
        Role adminRole = roleDao.findByName("ROLE_ADMIN");
        Role guestRole = roleDao.findByName("ROLE_GUEST");

        User user = new User("admin", "admin");
        userDao.persist(user);
        UserRole userRole = new UserRole(user, anyUserRole);
        userRoleDao.persist(userRole);
        userRole = new UserRole(user, adminRole);
        userRoleDao.persist(userRole);

        user = new User("system", "system");
        userDao.persist(user);
        userRole = new UserRole(user, anyUserRole);
        userRoleDao.persist(userRole);
        userRole = new UserRole(user, adminRole);
        userRoleDao.persist(userRole);

        user = new User("guest", "guest");
        userDao.persist(user);
        userRole = new UserRole(user, anyUserRole);
        userRoleDao.persist(userRole);
        userRole = new UserRole(user, guestRole);
        userRoleDao.persist(userRole);

        userDao.flush();
    }

    private void initializeAccount(Contact contact, int i) {
        Account account = new Account();
        account.setName("Brown Bag" + i);
        contact.setAccount(account);
        account.setWebsite("http://www.brownbagconsulting.com");
        account.setTickerSymbol("EXPUI");

        Address address = randomAddress(i);
        account.setBillingAddress(address);

        account.addAccountType(referenceDataInitializer.randomAccountType());
        account.addAccountType(referenceDataInitializer.randomAccountType());
        account.setAssignedTo(ReferenceDataInitializer.random(userDao.findAll()));
        account.setNumberOfEmployees(ReferenceDataInitializer.random(1, 1000000));
        account.setAnnualRevenue(ReferenceDataInitializer.random(1, 1000000000));
        account.setCurrency(referenceDataInitializer.randomCurrency());
        account.setDescription("Description of account");
        account.setEmail("juan@brownbagconsulting.com");
        account.setIndustry(referenceDataInitializer.randomIndustry());

        try {
            Phone phone = (Phone) new PhonePropertyFormatter().parse(PhoneValidator.getExampleNumber("US", address.getCountry().getId()));
            account.setMainPhone(phone);
            account.getMainPhone().setPhoneType(random(PhoneType.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (randomBoolean()) {
            Address mailingAddress = randomAddress(i);
            account.setMailingAddress(mailingAddress);
        }
        accountDao.persist(account);

        initializeOpportunity(account, i);
    }

    private void initializeOpportunity(Account account, int i) {
        Opportunity opportunity = new Opportunity();
        opportunity.setName("opportunityName" + i);
        opportunity.setAccount(account);

        opportunity.setSalesStage(referenceDataInitializer.randomSalesStage());
        opportunity.setCurrency(referenceDataInitializer.randomCurrency());
        opportunity.setExpectedCloseDate(new Date());
        opportunity.setAssignedTo(ReferenceDataInitializer.random(userDao.findAll()));
        opportunity.setLeadSource(referenceDataInitializer.randomLeadSource());
        opportunity.setDescription("Description of opportunity");
        opportunity.setOpportunityType(random(OpportunityType.class));

        opportunity.setAmount(ReferenceDataInitializer.random(1, 1000000));

        opportunityDao.persist(opportunity);
    }

    private Address randomAddress(int i) {
        Address address = new Address();
        address.setStreet(i + " Main St");
        address.setCity("Mayberry" + i);
        State state = referenceDataInitializer.randomState();
        address.setCountry(state.getCountry());
        address.setState(state);
        if (state.getCountry().getId().equals("CA")) {
            address.setZipCode("A0A 0A0");
        } else {
            address.setZipCode(state.getCountry().getMinPostalCode());
        }

        return address;
    }

    public static Date randomBirthDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.DAY_OF_MONTH, ReferenceDataInitializer.random(1, 28));
        calendar.set(Calendar.MONTH, ReferenceDataInitializer.random(1, 12));
        calendar.set(Calendar.YEAR, ReferenceDataInitializer.random(1920, 2010));

        return calendar.getTime();
    }

    public static boolean randomBoolean() {
        int i = ReferenceDataInitializer.random(0, 1);
        return i == 1;
    }

    @SuppressWarnings("rawtypes")
	public static <T extends Enum> T random(Class<T> enumType) {
        T[] enumConstants = enumType.getEnumConstants();
        Arrays.asList(enumConstants);

        return enumConstants[ReferenceDataInitializer.random(0, enumConstants.length - 1)];
    }
}
