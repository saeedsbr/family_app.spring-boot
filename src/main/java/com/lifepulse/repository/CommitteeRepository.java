package com.lifepulse.repository;

import com.lifepulse.entity.Committee;
import com.lifepulse.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommitteeRepository extends JpaRepository<Committee, UUID> {

    List<Committee> findByCreatedBy(User createdBy);

    @Query("SELECT DISTINCT c FROM Committee c LEFT JOIN c.members m WHERE c.createdBy = :user OR m.user = :user")
    List<Committee> findAllAccessibleByUser(@Param("user") User user);
}
