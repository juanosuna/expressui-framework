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

package com.expressui.sample.view.account.related;

import com.expressui.core.view.field.format.JDKBridgePropertyFormatter;
import com.expressui.core.view.results.ResultsFieldSet;
import com.expressui.core.view.tomanyrelationship.ToManyAggregationRelationshipResults;
import com.expressui.core.view.tomanyrelationship.ToManyRelationship;
import com.expressui.sample.dao.query.RelatedOpportunitiesQuery;
import com.expressui.sample.entity.Opportunity;
import com.expressui.sample.view.select.OpportunitySelect;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.NumberFormat;
import java.util.Locale;

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

@Component
@Scope(SCOPE_PROTOTYPE)
public class RelatedOpportunities extends ToManyRelationship<Opportunity> {

    @Resource
    private RelatedOpportunitiesResults relatedOpportunitiesResults;

    @Override
    public String getTypeCaption() {
        return "Company Sales Opportunities";
    }

    @Override
    public RelatedOpportunitiesResults getResults() {
        return relatedOpportunitiesResults;
    }

    @Component
    @Scope(SCOPE_PROTOTYPE)
    public static class RelatedOpportunitiesResults extends ToManyAggregationRelationshipResults<Opportunity> {

        @Resource
        private RelatedOpportunitiesQuery relatedOpportunitiesQuery;

        @Resource
        private OpportunitySelect opportunitySelect;

        @Override
        public RelatedOpportunitiesQuery getEntityQuery() {
            return relatedOpportunitiesQuery;
        }

        @Override
        public OpportunitySelect getEntitySelect() {
            return opportunitySelect;
        }

        @Override
        public void init(ResultsFieldSet resultsFields) {
            resultsFields.setPropertyIds(
                    "name",
                    "salesStage",
                    "valueWeightedInUSD",
                    "expectedCloseDate"
            );

            resultsFields.setLabel("valueWeightedInUSD", "Weighted Amount");
            NumberFormat numberFormat = NumberFormat.getCurrencyInstance(Locale.US);
            numberFormat.setMaximumFractionDigits(0);
            JDKBridgePropertyFormatter formatter = new JDKBridgePropertyFormatter(numberFormat);
            resultsFields.setPropertyFormatter("valueWeightedInUSD", formatter);
        }

        @Override
        public String getChildPropertyId() {
            return "opportunities";
        }

        @Override
        public String getParentPropertyId() {
            return "account";
        }
    }
}

