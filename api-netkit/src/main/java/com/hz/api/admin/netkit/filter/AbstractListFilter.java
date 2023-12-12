/*
 * Created by zhangzxiang91@gmail.com on 2021/05/08.
 */
package com.hz.api.admin.netkit.filter;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author zhangzxiang91@gmail.com
 * @date 2021/05/08.
 */
public abstract class AbstractListFilter implements PacketFilter {

	protected final List<PacketFilter> filters;

	protected AbstractListFilter() {
		filters = new ArrayList<PacketFilter>();
	}

	protected AbstractListFilter(PacketFilter... filters) {
		this(new ArrayList<PacketFilter>(Arrays.asList(filters)));
	}

	protected AbstractListFilter(List<PacketFilter> filters) {
		Objects.requireNonNull(filters, "Parameter must not be null.");
		for (PacketFilter filter : filters) {
			Objects.requireNonNull(filter, "Parameter must not be null.");
		}
		this.filters = filters;
	}

	public void addFilter(PacketFilter filter) {
		Objects.requireNonNull(filter, "Parameter must not be null.");
		filters.add(filter);
	}

}
