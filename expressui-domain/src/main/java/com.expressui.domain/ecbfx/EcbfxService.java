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

package com.expressui.domain.ecbfx;

import com.expressui.domain.RestClientService;
import org.apache.commons.lang.time.DateUtils;
import org.joda.money.CurrencyUnit;
import org.joda.money.IllegalCurrencyException;
import org.joda.money.Money;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.xml.bind.annotation.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Service for fetching currency exchange rates from European Central Bank.
 * See http://www.ecb.int/stats/eurofxref/eurofxref-daily.xml
 */
@Configuration
@Service
public class EcbfxService extends RestClientService {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private Date rateDay;
    private Date fetchDay;
    private Map<String, BigDecimal> rates;

    @Resource
    private ECBFXClient ecbfxClient;

    public BigDecimal convert(BigDecimal amount, String sourceCurrencyCode, String targetCurrencyCode)
            throws IllegalCurrencyException {

        if (sourceCurrencyCode.equals(targetCurrencyCode)) return amount;

        Map<String, BigDecimal> rates = getFXRates();
        BigDecimal inverseRate = rates.get(sourceCurrencyCode);
        if (inverseRate == null)
            throw new IllegalCurrencyException("Unknown currency: " + sourceCurrencyCode);

        BigDecimal sourceRate = new BigDecimal(1).divide(inverseRate, 10, RoundingMode.HALF_EVEN);
        CurrencyUnit sourceCurrencyUnit = CurrencyUnit.of(sourceCurrencyCode);
        Money amountInSourceCurrency = Money.of(sourceCurrencyUnit, amount, RoundingMode.HALF_EVEN);
        Money amountInEuros;
        if (sourceCurrencyUnit.getCurrencyCode().equals("EUR")) {
            amountInEuros = amountInSourceCurrency;
        } else {
            amountInEuros = amountInSourceCurrency.convertedTo(CurrencyUnit.of("EUR"), sourceRate, RoundingMode.HALF_EVEN);
        }

        BigDecimal targetRate = rates.get(targetCurrencyCode);
        if (targetRate == null) throw new IllegalCurrencyException("Unknown currency: " + targetCurrencyCode);

        Money amountInTargetCurrency = amountInEuros.convertedTo(CurrencyUnit.of(targetCurrencyCode), targetRate, RoundingMode.HALF_EVEN);

        return amountInTargetCurrency.getAmount();
    }

    public Map<String, BigDecimal> getFXRates() {
        if (rateDay == null || (DateUtils.truncatedCompareTo(rateDay, new Date(), Calendar.DAY_OF_MONTH) < 0
                && DateUtils.truncatedCompareTo(fetchDay, new Date(), Calendar.DAY_OF_MONTH) < 0)) {
            fetchFXRates();
        }

        return rates;
    }

    private void fetchFXRates() {
        rates = new HashMap<String, BigDecimal>();

        ECBFXResponse ecbfxResponse = ecbfxClient.getFXRates();
        try {
            rateDay = DATE_FORMAT.parse(ecbfxResponse.mainCube.quoteDate.time);
            fetchDay = DateUtils.truncate(new Date(), Calendar.DAY_OF_MONTH);

        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        for (ECBFXResponse.MainCube.QuoteDate.Rate rate : ecbfxResponse.mainCube.quoteDate.rates) {
            rates.put(rate.currency, new BigDecimal(rate.rate));
        }
        rates.put("EUR", new BigDecimal(1));
    }

    @Bean
    ECBFXClient getEcbfxClient(@Value("${ecbfxService.url:}") String url) throws Exception {
        return create(url, ECBFXClient.class);
    }

    static interface ECBFXClient {
        @GET
        @Produces("application/xml")
        ECBFXResponse getFXRates();
    }

    private static final String NAMESPACE = "http://www.ecb.int/vocabulary/2002-08-01/eurofxref";

    @XmlRootElement(namespace = "http://www.gesmes.org/xml/2002-08-01", name = "Envelope")
    @XmlAccessorType(XmlAccessType.FIELD)
    static class ECBFXResponse {

        @XmlElement(name = "Cube", namespace = NAMESPACE)
        public MainCube mainCube;

        @XmlAccessorType(XmlAccessType.FIELD)
        public static class MainCube {

            @XmlElement(name = "Cube", namespace = NAMESPACE)
            public QuoteDate quoteDate;

            @XmlAccessorType(XmlAccessType.FIELD)
            public static class QuoteDate {

                @XmlAttribute
                public String time;

                @XmlElement(name = "Cube", namespace = NAMESPACE)
                public List<Rate> rates;

                @XmlAccessorType(XmlAccessType.FIELD)
                public static class Rate {

                    @XmlAttribute
                    public String currency;

                    @XmlAttribute
                    public String rate;
                }
            }
        }
    }
}
