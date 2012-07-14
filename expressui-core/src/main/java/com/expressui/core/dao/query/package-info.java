/**
 * A query framework, optimized for sorting, paging and fetching joined entities.
 * <p/>
 * {@link EntityQuery} provides a lightweight option for storing query parameters, sort criteria and tracking pages,
 * where a subclass can define the query logic using any technique: JPA Criteria API, JPQL or native SQL.
 * <p/>
 * {@link StructuredEntityQuery} provides a more heavyweight solution that uses the Criteria API to facilitate
 * paging together with fetch joins, a combination that is otherwise problematic with Hibernate.
 * See {@link StructuredEntityQuery} for more information.
 */
package com.expressui.core.dao.query;