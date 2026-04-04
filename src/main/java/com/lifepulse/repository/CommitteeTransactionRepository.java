package com.lifepulse.repository;

import com.lifepulse.entity.Committee;
import com.lifepulse.entity.CommitteeTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommitteeTransactionRepository extends JpaRepository<CommitteeTransaction, UUID> {

    List<CommitteeTransaction> findByCommittee(Committee committee);

    List<CommitteeTransaction> findByCommitteeAndCycleNumber(Committee committee, int cycleNumber);
}
