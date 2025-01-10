package com.example.outsourcing.domain.search.dto;

import java.util.List;

public record SearchResponseDto(
    List<SearchShopResponseDto> shops,
    List<SearchMenuResponseDto> menus
) {

}
