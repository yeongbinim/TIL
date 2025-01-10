package com.example.outsourcing.domain.search.dto;

import java.math.BigDecimal;

public record SearchMenuResponseDto(
    Long id,
    String shopName,
    String name,
    String description,
    BigDecimal price
) {

}
