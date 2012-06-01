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

package com.expressui.sample.entity;


import com.expressui.core.entity.WritableEntity;
import com.expressui.core.entity.security.User;
import com.expressui.core.validation.ValidUrl;
import com.expressui.core.view.field.format.DefaultFormats;
import com.expressui.domain.ecbfx.EcbfxService;
import com.expressui.sample.validator.ValidPhone;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;

import javax.annotation.Resource;
import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.Set;

import static com.expressui.core.util.ObjectUtil.isEqual;

@Entity
@Table
public class Account extends WritableEntity {

    @Resource
    @Transient
    private DefaultFormats defaultFormat;

    @Resource
    @Transient
    private EcbfxService ecbfxService;

    private String name;

    private String website;

    private String tickerSymbol;

    private String email;

    private Phone mainPhone;

    private Integer numberOfEmployees;

    private BigDecimal annualRevenue;

    private BigDecimal annualRevenueInUSD;

    @Lob
    private String description;

    @Index(name = "IDX_ACCOUNT_CURRENCY")
    @ForeignKey(name = "FK_ACCOUNT_CURRENCY")
    @ManyToOne(fetch = FetchType.LAZY)
    private Currency currency;

    @ForeignKey(name = "FK_ACCOUNT_ACCOUNT", inverseName = "FK_ACCOUNT_ACCOUNT_TYPES")
    @ManyToMany(fetch = FetchType.LAZY)
    private Set<AccountType> accountTypes = new HashSet<AccountType>();

    @Index(name = "IDX_ACCOUNT_ASSIGNED_TO")
    @ForeignKey(name = "FK_ACCOUNT_ASSIGNED_TO")
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
    private User assignedTo;

    @Index(name = "IDX_ACCOUNT_INDUSTRY")
    @ForeignKey(name = "FK_ACCOUNT_INDUSTRY")
    @ManyToOne(fetch = FetchType.LAZY)
    private Industry industry;

    @Index(name = "IDX_ACCOUNT_BILLING_ADDRESS")
    @ForeignKey(name = "FK_ACCOUNT_BILLING_ADDRESS")
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Address billingAddress = new Address(AddressType.BILLING);

    @Index(name = "IDX_ACCOUNT_SHIPPING_ADDRESS")
    @ForeignKey(name = "FK_ACCOUNT_SHIPPING_ADDRESS")
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Address mailingAddress;

    @OneToMany(mappedBy = "account")
    private Set<Contact> contacts = new HashSet<Contact>();

    @OneToMany(mappedBy = "account")
    private Set<Opportunity> opportunities = new HashSet<Opportunity>();

    public Account() {
    }

    @NotBlank
    @NotNull
    @Size(min = 1, max = 64)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ValidUrl
    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    @Size(min = 1, max = 25)
    public String getTickerSymbol() {
        return tickerSymbol;
    }

    public void setTickerSymbol(String tickerSymbol) {
        this.tickerSymbol = tickerSymbol;
    }

    @Email
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @NotNull
    @ValidPhone
    public Phone getMainPhone() {
        return mainPhone;
    }

    public void setMainPhone(Phone mainPhone) {
        this.mainPhone = mainPhone;
    }

    @Min(0)
    public Integer getNumberOfEmployees() {
        return numberOfEmployees;
    }

    public void setNumberOfEmployees(Integer numberOfEmployees) {
        this.numberOfEmployees = numberOfEmployees;
    }

    @Min(0)
    public BigDecimal getAnnualRevenue() {
        return annualRevenue;
    }

    public void setAnnualRevenue(BigDecimal annualRevenue) {
        if (!isEqual(this.annualRevenue, annualRevenue)) {
            this.annualRevenue = annualRevenue;
            annualRevenueInUSD = calculateAnnualRevenueInUSD();
        }
    }

    public void setAnnualRevenue(double annualRevenue) {
        setAnnualRevenue(new BigDecimal(annualRevenue));
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        if (!isEqual(this.currency, currency)) {
            this.currency = currency;
            annualRevenueInUSD = calculateAnnualRevenueInUSD();
        }
    }

    public BigDecimal getAnnualRevenueInUSD() {
        return annualRevenueInUSD;
    }

    private BigDecimal calculateAnnualRevenueInUSD() {
        if (getAnnualRevenue() == null || getCurrency() == null) {
            return null;
        } else {
            try {
                BigDecimal annualRevenueInUSD = ecbfxService.convert(getAnnualRevenue(), getCurrency().getId(), "USD");
                return annualRevenueInUSD.setScale(0, RoundingMode.HALF_EVEN);
            } catch (Exception e) {
                return null;
            }
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<AccountType> getAccountTypes() {
        return accountTypes;
    }

    public void setAccountTypes(Set<AccountType> accountType) {
        this.accountTypes = accountType;
    }

    public void addAccountType(AccountType accountType) {
        getAccountTypes().add(accountType);
    }

    public User getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(User assignedTo) {
        this.assignedTo = assignedTo;
    }

    public Industry getIndustry() {
        return industry;
    }

    public void setIndustry(Industry industry) {
        this.industry = industry;
    }

    @Valid
    @NotNull
    public Address getBillingAddress() {
        return billingAddress;
    }

    public void setBillingAddress(Address billingAddress) {
        if (billingAddress != null) {
            billingAddress.setAddressType(AddressType.BILLING);
        }
        this.billingAddress = billingAddress;
    }

    @Valid
    public Address getMailingAddress() {
        return mailingAddress;
    }

    public void setMailingAddress(Address mailingAddress) {
        if (mailingAddress != null) {
            mailingAddress.setAddressType(AddressType.MAILING);
        }
        this.mailingAddress = mailingAddress;
    }

    public Set<Contact> getContacts() {
        return contacts;
    }

    public void setContacts(Set<Contact> contacts) {
        this.contacts = contacts;
    }

    public Set<Opportunity> getOpportunities() {
        return opportunities;
    }

    public void setOpportunities(Set<Opportunity> opportunities) {
        this.opportunities = opportunities;
    }

    @PreRemove
    public void preRemove() {
        for (Contact contact : getContacts()) {
            contact.setAccount(null);
        }
        for (Opportunity opportunity : getOpportunities()) {
            opportunity.setAccount(null);
        }
    }
}