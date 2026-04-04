package com.lifepulse.repository;

import com.lifepulse.entity.Committee;
import com.lifepulse.entity.CommitteeMember;
import com.lifepulse.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommitteeMemberRepository extends JpaRepository<CommitteeMember, UUID> {

    List<CommitteeMember> findByCommittee(Committee committee);

    List<CommitteeMember> findByCommitteeAndStatus(Committee committee, CommitteeMember.MemberStatus status);

    Optional<CommitteeMember> findByCommitteeAndUser(Committee committee, User user);

    Optional<CommitteeMember> findByCommitteeAndUserAndStatus(Committee committee, User user,
            CommitteeMember.MemberStatus status);

    // Find who's turn is in a specific cycle
    Optional<CommitteeMember> findByCommitteeAndTurnCycle(Committee committee, int turnCycle);

    long countByCommitteeAndStatus(Committee committee, CommitteeMember.MemberStatus status);
}
