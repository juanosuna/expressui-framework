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

package com.expressui.domain.geoplanet;

import com.expressui.domain.RestClientService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.xml.bind.annotation.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Services for fetching country and state codes from Yahoo GeoPlanet:
 * http://developer.yahoo.com/geo/geoplanet/
 */
@Configuration
@Service
public class GeoPlanetService extends RestClientService {

    @Resource
    private GeoPlanetClient geoPlanetClient;

    public Set<CountryInfo> getCountries() {

        Set<CountryInfo> countries = new HashSet<CountryInfo>();

        GeoPlanetResponse geoPlanetResponse = geoPlanetClient.getPlaces("select country from geo.countries where view='long'");
        for (Place place : geoPlanetResponse.places) {
            countries.add(place.country);
        }

        return countries;
    }

    public Set<Place> getStates(Set<String> countriesWithStates) {

        String queryStr = "select country, admin1 from geo.states where place in (%1$s) and view='long'";
        StringBuilder countriesBuilder = new StringBuilder();
        for (String countryWithStates : countriesWithStates) {
            countriesBuilder.append("'").append(countryWithStates).append("',");
        }
        countriesBuilder.deleteCharAt(countriesBuilder.length() - 1);

        Set<Place> places = new HashSet<Place>();
        GeoPlanetResponse geoPlanetResponse = geoPlanetClient.getPlaces(String.format(queryStr, countriesBuilder.toString()));
        for (Place place : geoPlanetResponse.places) {
            if (!place.admin1.code.isEmpty()) {
                places.add(place);
            }
        }

        return places;
    }

    @Bean
    public GeoPlanetClient getGeoPlanetClient(@Value("${geoPlanetService.url:}") String url) throws Exception {
        return create(url, GeoPlanetClient.class);
    }

    public static interface GeoPlanetClient {
        @GET
        @Produces("application/xml")
        GeoPlanetResponse getPlaces(@QueryParam("q") String yql);
    }

    @XmlRootElement(namespace = "", name = "query")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class GeoPlanetResponse {

        @XmlAttribute(namespace = "http://www.yahooapis.com/v1/base.rng")
        public int count;

        @XmlElementWrapper(namespace = "", name = "results")
        @XmlElement(name = "place")
        public List<Place> places;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Place {

        public CountryInfo country;

        public Admin1 admin1;

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class CountryInfo {

        @XmlAttribute
        public String code;

        @XmlAttribute
        public String type;

        @XmlValue
        public String name;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Admin1 {

        @XmlAttribute
        public String code;

        @XmlAttribute
        public String type;

        @XmlValue
        public String name;
    }

}
