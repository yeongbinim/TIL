package com.example.outsourcing.domain.shop.repository;

import com.example.outsourcing.domain.shop.entity.Menu;
import java.util.List;

public interface MenuRepositoryCustom {

    List<Menu> searchByKeyword(String keyword);
}
