package com.kumuluz.ee.jnosql.common;

import com.kumuluz.ee.rest.beans.QueryOrder;
import com.kumuluz.ee.rest.beans.QueryParameters;
import com.kumuluz.ee.rest.enums.OrderDirection;
import org.jnosql.artemis.Pagination;
import org.jnosql.artemis.Sorts;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.Optional;

public class JNoSQLQueryParameters extends QueryParameters {

	public Sorts getSorts() {
		List<QueryOrder> queryOrders = getOrder();
		Sorts sorts = Sorts.sorts();

		for (QueryOrder queryOrder : queryOrders) {
			if (queryOrder.getOrder().equals(OrderDirection.ASC)) {
				sorts.asc(queryOrder.getField());
			} else if (queryOrder.getOrder().equals(OrderDirection.DESC)) {
				sorts.desc(queryOrder.getField());
			} else {
				throw new IllegalArgumentException("Unknown order type: " + queryOrder.getOrder());
			}
		}
		return sorts;
	}

	public Optional<Pagination> getPagination() {
		Long offset = getOffset();
		Long limit = getLimit();

		if (offset == null || limit == null)
			return Optional.empty();

		if (offset % limit != 0) {
			// this method can not be used, since we aren't sure what the initial offset is for
			return Optional.empty();
		}

		Pagination pagination = Pagination.page(offset / limit).size(limit);
		return Optional.of(pagination);
	}

	@Context
	private static UriInfo uriInfo;

	public static void main(String[] args) {
		JNoSQLQueryParameters parameters = JNoSQLQueryParameters.query(uriInfo.getRequestUri().getQuery()).build();
	}
}
