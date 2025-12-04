package com.mrdinner.repository;

import com.mrdinner.entity.KitchenStaff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KitchenStaffRepository extends JpaRepository<KitchenStaff, String> {
    List<KitchenStaff> findByIsBusyFalse();
    Optional<KitchenStaff> findByEmployeeId(String employeeId);
}

