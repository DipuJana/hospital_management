package com.jana.hospital_management.service;

import org.springframework.data.domain.*;

import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class PaginationService {

    public Pageable createPageable(
            int page,
            int size,
            String sortBy,
            String direction,
            Set<String> allowedFields
    ) {

        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page number cannot be negative"
            );
        }

        if (size <= 0 || size > 50) {
            throw new IllegalArgumentException(
                    "Page size must be between 1 and 50"
            );
        }

        if (!allowedFields.contains(sortBy)) {
            throw new IllegalArgumentException(
                    "Invalid sort field: " + sortBy +
                            ". Allowed fields: " + allowedFields
            );
        }

        Sort.Direction sortDirection;

        try {
            sortDirection =
                    Sort.Direction.fromString(direction);
        }
        catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Invalid sort direction: " + direction
            );
        }

        return PageRequest.of(
                page,
                size,
                Sort.by(sortDirection, sortBy)
        );
    }
}