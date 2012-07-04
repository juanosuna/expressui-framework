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
import com.expressui.core.validation.AssertTrueForProperties;
import com.expressui.domain.ecbfx.EcbfxService;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;
import org.hibernate.validator.constraints.NotBlank;

import javax.annotation.Resource;
import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

import static com.expressui.core.util.ObjectUtil.isEqual;
import static com.expressui.core.util.StringUtil.isEmpty;

@Entity
@Table
public class Opportunity extends WritableEntity {

    @Resource
    @Transient
    private EcbfxService ecbfxService;

    private String name;

    @Enumerated(EnumType.STRING)
    private OpportunityType opportunityType = OpportunityType.NEW;

    @Temporal(TemporalType.DATE)
    private Date expectedCloseDate;

    @Temporal(TemporalType.DATE)
    private Date actualCloseDate;

    private BigDecimal amount;

    @Index(name = "IDX_OPPORTUNITY_CURRENCY")
    @ForeignKey(name = "FK_OPPORTUNITY_CURRENCY")
    @ManyToOne(fetch = FetchType.LAZY)
    private Currency currency;

    private double probability;

    private BigDecimal amountInUSD;

    private BigDecimal valueWeightedInUSD;

    @Lob
    private String description;

    @Index(name = "IDX_OPPORTUNITY_LEAD_SOURCE")
    @ForeignKey(name = "FK_OPPORTUNITY_LEAD_SOURCE")
    @ManyToOne(fetch = FetchType.LAZY)
    private LeadSource leadSource;

    @Index(name = "IDX_OPPORTUNITY_SALES_STAGE")
    @ForeignKey(name = "FK_OPPORTUNITY_SALES_STAGE")
    @ManyToOne(fetch = FetchType.LAZY)
    private SalesStage salesStage;

    @Index(name = "IDX_OPPORTUNITY_ACCOUNT")
    @ForeignKey(name = "FK_OPPORTUNITY_ACCOUNT")
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.REFRESH)
    private Account account;

    @Index(name = "IDX_OPPORTUNITY_USER")
    @ForeignKey(name = "FK_OPPORTUNITY_USER")
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
    private User assignedTo;

    public Opportunity() {
    }

    public Opportunity(String name) {
        this.name = name;
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

    public OpportunityType getOpportunityType() {
        return opportunityType;
    }

    public void setOpportunityType(OpportunityType opportunityType) {
        this.opportunityType = opportunityType;
    }

    public Date getExpectedCloseDate() {
        return expectedCloseDate;
    }

    public void setExpectedCloseDate(Date expectedCloseDate) {
        this.expectedCloseDate = expectedCloseDate;
    }

    public Date getActualCloseDate() {
        return actualCloseDate;
    }

    public void setActualCloseDate(Date actualCloseDate) {
        this.actualCloseDate = actualCloseDate;
    }

    @AssertTrueForProperties(errorProperty = "actualCloseDate", message = "Required if Sales Stage is closed")
    public boolean isActualCloseDateValidIfSalesStageClosed() {
        if (getSalesStage() != null && getSalesStage().getId().startsWith("Closed")) {
            return !isEmpty(getActualCloseDate());
        } else {
            return true;
        }
    }

    @AssertTrueForProperties(errorProperty = "actualCloseDate", message = "Must be empty if Sales Stage is open")
    public boolean isActualCloseDateValidIfSalesStageOpen() {
        if (getSalesStage() != null && !getSalesStage().getId().startsWith("Closed")) {
            return isEmpty(getActualCloseDate());
        } else {
            return true;
        }
    }

    @AssertTrueForProperties(errorProperty = "expectedCloseDate", message = "Must be empty if Sales Stage is closed")
    public boolean isExpectedCloseDateValidIfSalesStageClosed() {
        if (getSalesStage() != null && getSalesStage().getId().startsWith("Closed")) {
            return isEmpty(getExpectedCloseDate());
        } else {
            return true;
        }
    }

    @Min(0)
    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        if (!isEqual(this.amount, amount)) {
            this.amount = amount;
            valueWeightedInUSD = calculateValueWeightedInUSD();
            amountInUSD = calculateAmountInUSD();
        }
    }

    public void setAmount(double amount) {
        setAmount(new BigDecimal(amount));
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        if (!isEqual(this.currency, currency)) {
            this.currency = currency;
            valueWeightedInUSD = calculateValueWeightedInUSD();
            amountInUSD = calculateAmountInUSD();
        }
    }

    public double getProbability() {
        return probability;
    }

    public void setProbability(double probability) {
        if (this.probability != probability) {
            this.probability = probability;
            valueWeightedInUSD = calculateValueWeightedInUSD();
        }
    }

    public BigDecimal getValueWeightedInUSD() {
        return valueWeightedInUSD;
    }

    private BigDecimal calculateValueWeightedInUSD() {
        if (getAmount() == null || getCurrency() == null) {
            return null;
        } else {
            try {
                BigDecimal amountInUSD = ecbfxService.convert(getAmount(), getCurrency().getId(), "USD");
                amountInUSD = amountInUSD.setScale(0, RoundingMode.HALF_EVEN);
                BigDecimal valueWeightedInUSD = amountInUSD.multiply(new BigDecimal(getProbability()));
                return valueWeightedInUSD.setScale(0, RoundingMode.HALF_EVEN);
            } catch (Exception e) {
                return null;
            }
        }
    }

    public BigDecimal getAmountInUSD() {
        return amountInUSD;
    }


    private BigDecimal calculateAmountInUSD() {
        if (getAmount() == null || getCurrency() == null) {
            return null;
        } else {
            try {
                BigDecimal amountInUSD = ecbfxService.convert(getAmount(), getCurrency().getId(), "USD");
                amountInUSD = amountInUSD.setScale(0, RoundingMode.HALF_EVEN);
                return amountInUSD.setScale(0, RoundingMode.HALF_EVEN);
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

    public LeadSource getLeadSource() {
        return leadSource;
    }

    public void setLeadSource(LeadSource leadSource) {
        this.leadSource = leadSource;
    }

    @NotNull
    public SalesStage getSalesStage() {
        return salesStage;
    }

    public void setSalesStage(SalesStage salesStage) {
        if (!isEqual(this.salesStage, salesStage)) {
            this.salesStage = salesStage;
            setProbability(salesStage.getProbability());
        }
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public User getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(User assignedTo) {
        this.assignedTo = assignedTo;
    }

    @PreRemove
    public void preRemove() {
        setAccount(null);
    }

    @Override
    public String toString() {
        return getName();
    }
}