package com.mrdinner.repository;

import com.mrdinner.entity.Menu;
import com.mrdinner.entity.MenuType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {
    Optional<Menu> findByType(MenuType type);
}

