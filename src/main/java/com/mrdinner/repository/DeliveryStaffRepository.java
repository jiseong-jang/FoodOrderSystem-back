package com.mrdinner.repository;

import com.mrdinner.entity.DeliveryStaff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryStaffRepository extends JpaRepository<DeliveryStaff, String> {
    List<DeliveryStaff> findByIsBusyFalse();
    Optional<DeliveryStaff> findByEmployeeId(String employeeId);
}

