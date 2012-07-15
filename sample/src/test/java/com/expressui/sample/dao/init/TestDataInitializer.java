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

package com.expressui.sample.dao.init;

import com.expressui.core.dao.GenericDao;
import com.expressui.core.dao.security.PermissionDao;
import com.expressui.core.dao.security.RoleDao;
import com.expressui.core.dao.security.UserDao;
import com.expressui.core.dao.security.UserRoleDao;
import com.expressui.core.entity.security.*;
import com.expressui.sample.dao.StateDao;
import com.expressui.sample.entity.*;
import com.expressui.sample.formatter.PhonePropertyFormatter;
import com.expressui.sample.validator.PhoneValidator;
import com.expressui.sample.view.LoginPage;
import com.expressui.sample.view.profile.ProfilePage;
import com.expressui.sample.view.registration.RegistrationPage;
import net.sf.cglib.core.Local;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

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
    private GenericDao genericDao;

    @Resource
    private StateDao stateDao;

    @Resource
    private ReferenceDataInitializer referenceDataInitializer;

    public void initialize(int count) {
        initializeRoles();
        initializeUsers();

        int accountCount = count / 10;
        Account currentAccount = null;
        for (Integer contactCount = count; contactCount > 0; contactCount--) {
            Contact contact;
            if (contactCount % 50 == 1) {
                contact = new Contact("Name" + contactCount,
                        "Columns and fields resize automatically");
            } else {
                contact = new Contact("First Name" + contactCount, "Last Name" + contactCount);
            }
            contact.setBirthDate(randomDate(1920, 2010));
            contact.setAssignedTo(ReferenceDataInitializer.random(userDao.findAll()));
            contact.setTitle("Vice President");
            contact.setDoNotCall(randomBoolean());
            contact.setEmail("info@expressui.com");
            contact.setDoNotEmail(randomBoolean());
            contact.setLeadSource(referenceDataInitializer.randomLeadSource());
            Address address = randomAddress(contactCount);
            contact.setMailingAddress(address);

            if (randomBoolean()) {
                Address otherAddress = randomAddress(contactCount);
                contact.setOtherAddress(otherAddress);
            }

            try {
                Phone phone = (Phone) new PhonePropertyFormatter().parse(PhoneValidator.getExampleNumber(Locale.US.getCountry(), address.getCountry().getId()));
                contact.setMainPhone(phone);
                contact.setMainPhoneType(random(PhoneType.class));

                if (randomBoolean()) {
                    phone = (Phone) new PhonePropertyFormatter().parse(PhoneValidator.getExampleNumber(Locale.US.getCountry(), address.getCountry().getId()));
                    contact.setOtherPhone(phone);
                    contact.setOtherPhoneType(random(PhoneType.class));
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }


            contact.setDescription("Description of contact");

            if (contactCount % 10 == 0) {
                currentAccount = initializeAccount(accountCount--);
            }
            contact.setAccount(currentAccount);
            genericDao.persist(contact);

            if (contactCount % 50 == 0) {
                genericDao.flush();
                genericDao.clear();
            }
        }
    }

    public void initializeRoles() {
        Role role = new Role("ROLE_USER");
        role.setDescription("This role belongs to all users and allows user to login.");
        role.setAllowOrDenyByDefault(AllowOrDeny.DENY);
        roleDao.persist(role);

        Permission permission = new Permission(LoginPage.class.getName());
        permission.setRole(role);
        permission.setViewAllowed(true);
        permissionDao.persist(permission);

        permission = new Permission(RegistrationPage.class.getName());
        permission.setRole(role);
        permission.setViewAllowed(true);
        permissionDao.persist(permission);

        permission = new Permission("com.expressui.sample.SampleApplication.language");
        permission.setRole(role);
        permission.setViewAllowed(true);
        permissionDao.persist(permission);

        permission = new Permission("com.expressui.sample.SampleApplication.setEnglish");
        permission.setRole(role);
        permission.setViewAllowed(true);
        permissionDao.persist(permission);

        permission = new Permission("com.expressui.sample.SampleApplication.setGerman");
        permission.setRole(role);
        permission.setViewAllowed(true);
        permissionDao.persist(permission);

        permission = new Permission(Profile.class.getName());
        permission.setRole(role);
        permission.setViewAllowed(true);
        permission.setCreateAllowed(true);
        permission.setEditAllowed(true);
        permissionDao.persist(permission);

        role = new Role("ROLE_ADMIN");
        role.setDescription("This role allows full access to everything.");
        roleDao.persist(role);

        role = new Role("ROLE_GUEST");
        role.setDescription("This role demonstrates full access to entities but only view access to security entities.");
        role.setAllowOrDenyByDefault(AllowOrDeny.ALLOW);
        roleDao.persist(role);

        permission = new Permission(ProfilePage.class.getName());
        permission.setRole(role);
        permission.setViewAllowed(false);
        permissionDao.persist(permission);

        permission = new Permission(Role.class.getName());
        permission.setRole(role);
        permission.setViewAllowed(true);
        permission.setEditAllowed(false);
        permission.setDeleteAllowed(false);
        permission.setCreateAllowed(false);
        permissionDao.persist(permission);

        permission = new Permission(User.class.getName());
        permission.setRole(role);
        permission.setViewAllowed(true);
        permission.setEditAllowed(false);
        permission.setDeleteAllowed(false);
        permission.setCreateAllowed(false);
        permissionDao.persist(permission);

        permission = new Permission(Permission.class.getName());
        permission.setRole(role);
        permission.setViewAllowed(true);
        permission.setEditAllowed(false);
        permission.setDeleteAllowed(false);
        permission.setCreateAllowed(false);
        permissionDao.persist(permission);

        roleDao.flush();
    }

    public void initializeUsers() {
        Role anyUserRole = roleDao.findByName("ROLE_USER");
        Role adminRole = roleDao.findByName("ROLE_ADMIN");
        Role guestRole = roleDao.findByName("ROLE_GUEST");

        User user = new User("anonymous", "anonymous");
        userDao.persist(user);
        UserRole userRole = new UserRole(user, anyUserRole);
        userRoleDao.persist(userRole);

        user = new User("admin", "admin");
        userDao.persist(user);
        userRole = new UserRole(user, anyUserRole);
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

    private Account initializeAccount(int i) {
        Account account = new Account();
        account.setName("Account Name" + i);
        account.setWebsite("www.expressui.com");
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
        account.setEmail("info@expressui.com");
        account.setIndustry(referenceDataInitializer.randomIndustry());

        try {
            Phone phone = (Phone) new PhonePropertyFormatter().parse(PhoneValidator.getExampleNumber(Locale.US.getCountry(), address.getCountry().getId()));
            account.setMainPhone(phone);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (randomBoolean()) {
            Address mailingAddress = randomAddress(i);
            account.setMailingAddress(mailingAddress);
        }
        genericDao.persist(account);

        initializeOpportunity(account, i);

        return account;
    }

    private void initializeOpportunity(Account account, int i) {
        Opportunity opportunity = new Opportunity();
        if (i % 20 == 1) {
            opportunity.setName("Columns and fields resize automatically");
        } else {
            opportunity.setName("Opportunity Name" + i);
        }
        opportunity.setAccount(account);

        opportunity.setSalesStage(referenceDataInitializer.randomSalesStage());
        opportunity.setCurrency(referenceDataInitializer.randomCurrency());
        if (opportunity.getSalesStage().getId().startsWith("Closed")) {
            opportunity.setActualCloseDate(randomDate(2005, 2011));
        } else {
            opportunity.setExpectedCloseDate(randomDate(2012, 2015));
        }
        opportunity.setAssignedTo(ReferenceDataInitializer.random(userDao.findAll()));
        opportunity.setLeadSource(referenceDataInitializer.randomLeadSource());
        opportunity.setDescription("Description of opportunity");
        opportunity.setOpportunityType(random(OpportunityType.class));

        opportunity.setAmount(ReferenceDataInitializer.random(1, 1000000));

        genericDao.persist(opportunity);
    }

    private Address randomAddress(int i) {
        Address address = new Address();

        int randomNumber = ReferenceDataInitializer.random(0, 3);
        State state;
        switch (randomNumber) {
            case 0:
                address.setStreet(i + " Main St");
                address.setCity("Toronto");
                state = stateDao.find("CA-ON");
                address.setState(state);
                address.setZipCode("M4C 1L1");
                address.setCountry(state.getCountry());
                break;
            case 1:
                address.setStreet(i + " South Tryon St");
                address.setCity("Charlotte");
                state = stateDao.find("US-NC");
                address.setState(state);
                address.setZipCode("28202");
                address.setCountry(state.getCountry());
                break;
            case 2:
                address.setStreet(i + " Paseo de la Reforma");
                address.setCity("Mexico City");
                state = stateDao.find("MX-DIF");
                address.setState(state);
                address.setZipCode("06000");
                address.setCountry(state.getCountry());
                break;
            case 3:
                address.setStreet(i + " Victoria St");
                address.setCity("Melbourne");
                state = stateDao.find("AU-VIC");
                address.setState(state);
                address.setZipCode("3053");
                address.setCountry(state.getCountry());
        }

        return address;
    }

    public static Date randomDate(int startYear, int endYear) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.DAY_OF_MONTH, ReferenceDataInitializer.random(1, 28));
        calendar.set(Calendar.MONTH, ReferenceDataInitializer.random(1, 12));
        calendar.set(Calendar.YEAR, ReferenceDataInitializer.random(startYear, endYear));

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
