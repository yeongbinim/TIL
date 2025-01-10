package com.example.outsourcing.domain.shop.repository;

import com.example.outsourcing.domain.shop.entity.Shop;
import java.util.List;

public interface ShopRepositoryCustom {

    List<Shop> searchByKeyword(String keyword);
}
