package com.aleksastajic.liteerp.orders.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record OrderCreateRequest(
        @NotBlank String customerRef,
        @NotEmpty List<@Valid OrderCreateItemRequest> items
) {
}
