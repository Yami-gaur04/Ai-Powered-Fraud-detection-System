package com.guvi.frauddetection.common;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * Generic container for paginated query results.
 *
 * @param <T> The element type contained in the page
 */
public class Page<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private final List<T> items;
    private final int pageNumber;
    private final int pageSize;
    private final long totalItems;
    private final int totalPages;

    /**
     * Constructs a paginated response wrapper.
     *
     * @param items      the list of items in the current page
     * @param pageNumber current 1-based page index
     * @param pageSize   number of items requested per page
     * @param totalItems total count across all pages
     */
    public Page(List<T> items, int pageNumber, int pageSize, long totalItems) {
        this.items = items != null ? Collections.unmodifiableList(items) : Collections.emptyList();
        this.pageNumber = Math.max(1, pageNumber);
        this.pageSize = Math.max(1, pageSize);
        this.totalItems = Math.max(0, totalItems);
        this.totalPages = (int) Math.ceil((double) this.totalItems / this.pageSize);
    }

    public List<T> getItems() {
        return items;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public long getTotalItems() {
        return totalItems;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public boolean hasNext() {
        return pageNumber < totalPages;
    }

    public boolean hasPrevious() {
        return pageNumber > 1;
    }
}
