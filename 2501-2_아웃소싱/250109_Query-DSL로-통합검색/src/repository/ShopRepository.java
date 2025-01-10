package com.example.outsourcing.domain.shop.repository;

import com.example.outsourcing.domain.shop.entity.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Long>, ShopRepositoryCustom {

    // 특정 사용자가 삭제되지 않은 가게를 보유하고 있는지 확인
    boolean existsByUserIdAndIsDeletedFalse(Long userId);

}
