package com.example.outsourcing.domain.shop.repository;

import com.example.outsourcing.domain.shop.entity.Menu;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long>, MenuRepositoryCustom {

    // shopId와 삭제되지 않은 메뉴 필터링
    List<Menu> findByShopIdAndIsDeletedFalse(Long shopId);

    // shopId와 메뉴 이름으로 중복 체크 (삭제되지 않은 메뉴만 대상)
    boolean existsByShopIdAndNameAndIsDeletedFalse(Long shopId, String name);

    List<Menu> findByIdIn(List<Long> ids);

}
