package com.mercedesbenz.carversation.repository;

import com.mercedesbenz.carversation.data.entity.UserEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository

public interface UsersRepository extends JpaRepository<UserEntity, Long> {

    UserEntity findByVin(String vin);

    @Modifying
    @Transactional
    @Query("UPDATE users c SET c.lat = :lat, c.lng = :lng WHERE c.vin = :vin")
    int updateCarLocation(@Param("vin") String vin, @Param("lat") double lat, @Param("lng") double lng);

    @Query(value = """
            SELECT * FROM users 
            WHERE vin != :vin 
              AND ST_DistanceSphere(
                    ST_SetSRID(ST_MakePoint(lng, lat), 4326),
                    ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)
                  ) <= :radius
            """, nativeQuery = true)
    List<UserEntity> findNearbyUsers(
            @Param("vin") String vin,
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("radius") double radiusInMeters
    );
}