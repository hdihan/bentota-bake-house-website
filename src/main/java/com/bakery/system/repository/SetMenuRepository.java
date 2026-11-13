package com.bakery.system.repository;

import com.bakery.system.entity.SetMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SetMenuRepository extends JpaRepository<SetMenu, Long> {
    List<SetMenu> findByStatus(String status);
    List<SetMenu> findByCategory(String category);
}