package com.bakery.system.repository;

import com.bakery.system.entity.SetMenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SetMenuItemRepository extends JpaRepository<SetMenuItem, Long> {
}