// Purpose: Repository interface for CollateralAsset entity
// File: collateral-service/src/main/java/com/nextgenloan/collateral/repository/CollateralAssetRepository.java
// Dependencies: Spring Data JPA

package com.nextgenloan.collateral_service.repository;

import com.nextgenloan.collateral_service.entity.CollateralAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CollateralAssetRepository extends JpaRepository<CollateralAsset, Long> {

    Optional<CollateralAsset> findByCollateralReference(String collateralReference);

    List<CollateralAsset> findByCustomerNumber(String customerNumber);

    List<CollateralAsset> findByCustomerNumberAndStatus(String customerNumber, String status);

    List<CollateralAsset> findByLoanNumber(String loanNumber);

    boolean existsByCollateralReference(String collateralReference);

    List<CollateralAsset> findByStatus(String status);
}
