package com.example.outsourcing.domain.search.mapper;

import com.example.outsourcing.domain.search.dto.SearchMenuResponseDto;
import com.example.outsourcing.domain.search.dto.SearchResponseDto;
import com.example.outsourcing.domain.search.dto.SearchShopResponseDto;
import com.example.outsourcing.domain.shop.entity.Menu;
import com.example.outsourcing.domain.shop.entity.Shop;
import java.util.List;

public class SearchMapper {

    public static SearchResponseDto toDto(
        List<Shop> shopList,
        List<Menu> menuList
    ) {

        return new SearchResponseDto(
            shopList.stream().map(shop -> new SearchShopResponseDto(
                shop.getId(),
                shop.getUser().getUsername(),
                shop.getName()
            )).toList(),
            menuList.stream().map(menu -> new SearchMenuResponseDto(
                menu.getId(),
                menu.getShop().getName(),
                menu.getName(),
                menu.getDescription(),
                menu.getPrice()
            )).toList()
        );
    }
}
