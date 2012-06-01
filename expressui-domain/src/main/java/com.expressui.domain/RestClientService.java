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

package com.expressui.domain;

import com.expressui.core.util.ApplicationProperties;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jboss.resteasy.client.spring.RestClientProxyFactoryBean;

import javax.annotation.Resource;
import java.net.URI;

/**
 * A client for creating and using various REST client services, using RESTEasy.
 */
public abstract class RestClientService {

    @Resource
    private ApplicationProperties applicationProperties;

    /**
     * Create a REST client
     *
     * @param uri   uri of the service
     * @param clazz client class
     * @param <T>   class type
     * @return REST client
     * @throws Exception
     */
    public <T> T create(String uri, Class<T> clazz) throws Exception {
        RestClientProxyFactoryBean restClientFactory = new RestClientProxyFactoryBean();
        restClientFactory.setBaseUri(new URI(uri));
        restClientFactory.setServiceInterface(clazz);
        if (applicationProperties.getHttpProxyHost() != null && applicationProperties.getHttpProxyPort() != null) {

            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpHost proxy = new HttpHost(applicationProperties.getHttpProxyHost(),
                    applicationProperties.getHttpProxyPort());

            if (applicationProperties.getHttpProxyUsername() != null
                    && applicationProperties.getHttpProxyPassword() != null) {

                httpClient.getCredentialsProvider().setCredentials(
                        new AuthScope(applicationProperties.getHttpProxyHost(),
                                applicationProperties.getHttpProxyPort()),
                        new UsernamePasswordCredentials(applicationProperties.getHttpProxyUsername(),
                                applicationProperties.getHttpProxyPassword()));
            }

            httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
            restClientFactory.setHttpClient(httpClient);
        }
        restClientFactory.afterPropertiesSet();
        return (T) restClientFactory.getObject();
    }
}
