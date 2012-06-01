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

package com.expressui.domain.geocode;

import com.expressui.domain.RestClientService;
import com.vaadin.event.MouseEvents;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.vaadin.vol.*;

import javax.annotation.Resource;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Service for creating maps using Open Layers and Google
 */
@Configuration
@Service
public class MapService extends RestClientService {

    @Resource
    private GeocodeClient geocodeClient;

    /**
     * Create a map
     *
     * @param address   address to center the map on
     * @param name      name of the map
     * @param zoomLevel zoom level
     * @return map component
     */
    public OpenLayersMap createMap(String address, final String name, Integer zoomLevel) {

        final OpenLayersMap map = new OpenLayersMap();
        GoogleStreetMapLayer googleStreets = new GoogleStreetMapLayer();
        map.addLayer(googleStreets);
        double[] coordinates = getCoordinates(address);
        if (coordinates == null) {
            return null;
        }

        map.setCenter(coordinates[0], coordinates[1]);
        map.setZoom(zoomLevel);

        MarkerLayer markerLayer = new MarkerLayer();
        final Marker marker = new Marker(coordinates[0], coordinates[1]);
        markerLayer.addMarker(marker);
        map.addLayer(markerLayer);

        marker.addClickListener(new MouseEvents.ClickListener() {
            public void click(MouseEvents.ClickEvent event) {
                final Popup popup = new Popup(marker.getLon(), marker.getLat(), name);
                popup.setAnchor(marker);
                popup.setPopupStyle(Popup.PopupStyle.FRAMED_CLOUD);
                popup.addListener(new Popup.CloseListener() {
                    public void onClose(Popup.CloseEvent event) {
                        map.removeComponent(popup);
                    }
                });
                map.addPopup(popup);
            }
        });

        map.removeControl(Control.LayerSwitcher);

        return map;
    }

    /**
     * Get longitude and latitude coordinates for given address.
     *
     * @param address address to query for
     * @return first item is longitude, second is latitude
     */
    public double[] getCoordinates(String address) {

        GeocodeResponse geocodeResponse = geocodeClient.getGeocode(address, false);
        if (geocodeResponse.status.equals("ZERO_RESULTS")) {
            return null;
        } else {
            double[] coordinates = new double[2];
            coordinates[0] = geocodeResponse.result.geometry.location.lng;
            coordinates[1] = geocodeResponse.result.geometry.location.lat;

            return coordinates;
        }
    }

    @Bean
    public GeocodeClient getGeocodeClient(@Value("${geoCodeService.url:}") String url) throws Exception {
        return create(url, GeocodeClient.class);
    }

    public static interface GeocodeClient {
        @GET
        @Produces("application/xml")
        GeocodeResponse getGeocode(@QueryParam("address") String address, @QueryParam("sensor") boolean hasSensor);
    }

    @XmlRootElement(namespace = "", name = "GeocodeResponse")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class GeocodeResponse {
        public String status;

        public Result result;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Result {
        public Geometry geometry;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Geometry {
        public Location location;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Location {
        public double lat;

        public double lng;
    }
}
