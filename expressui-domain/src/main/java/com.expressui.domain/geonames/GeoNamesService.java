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

package com.expressui.domain.geonames;

import com.expressui.domain.RestClientService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Services for fetching currency code and postal code information about countries
 * from http://www.geonames.org.
 */
@Configuration
@Service
public class GeoNamesService extends RestClientService {

    @Resource
    private GeoNamesClient geoNamesClient;

    public Map<String, CountryInfo> getCountries() {

        Map<String, CountryInfo> countries = new HashMap<String, CountryInfo>();

        GeoNamesResponse geoNamesResponse = geoNamesClient.getPostalCodeCountryInfo("josuna");
        for (CountryInfo country : geoNamesResponse.countries) {
            countries.put(country.countryCode, country);
        }

        return countries;
    }

    public Map<String, String> getCurrencyCodes() {

        Map<String, String> currencyCodes = new HashMap<String, String>();

        GeoNamesResponse geoNamesResponse = geoNamesClient.getCountryInfo("josuna");
        for (CountryInfo info : geoNamesResponse.countries) {
            currencyCodes.put(info.countryCode, info.currencyCode);
        }

        return currencyCodes;
    }

    @Bean
    public GeoNamesClient getGeoNamesClient(@Value("${geoNamesService.url}") String url) throws Exception {
        return create(url, GeoNamesClient.class);
    }

    @Path("/")
    public static interface GeoNamesClient {
        @Path("/postalCodeCountryInfo")
        @GET
        @Produces("application/xml")
        GeoNamesResponse getPostalCodeCountryInfo(@QueryParam("username") String username);

        @Path("/countryInfo")
        @GET
        @Produces("application/xml")
        GeoNamesResponse getCountryInfo(@QueryParam("username") String username);
    }

    @XmlRootElement(namespace = "", name = "geonames")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class GeoNamesResponse {

        @XmlElement(name = "country")
        public List<CountryInfo> countries;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class CountryInfo {

        public String countryCode;
        public String minPostalCode;
        public String maxPostalCode;
        public String currencyCode;
    }
}
