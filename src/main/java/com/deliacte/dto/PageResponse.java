package com.deliacte.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    private List<T> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private Boolean first;
    private Boolean last;

    public static <T> PageResponse<T> of(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }

    public static <T, U> PageResponse<U> of(Page<T> page, List<U> content) {
        return PageResponse.<U>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }

    /* ================= NON PAGINÉ ================= */

    public static <T> PageResponse<T> of(List<T> content) {
        int size = content == null ? 0 : content.size();

        return PageResponse.<T>builder()
                .content(content)
                .pageNumber(0)
                .pageSize(size)
                .totalElements(size)
                .totalPages(size > 0 ? 1 : 0)
                .first(true)
                .last(true)
                .build();
    }
}
