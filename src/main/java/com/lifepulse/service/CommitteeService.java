package com.lifepulse.service;

import com.lifepulse.dto.*;
import com.lifepulse.entity.*;
import com.lifepulse.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommitteeService {

        private final CommitteeRepository committeeRepository;
        private final CommitteeMemberRepository committeeMemberRepository;
        private final CommitteeTransactionRepository committeeTransactionRepository;
        private final UserRepository userRepository;

        @Transactional
        public CommitteeResponse createCommittee(CommitteeRequest request, UUID organizerId) {
                User organizer = userRepository.findById(organizerId)
                                .orElseThrow(() -> new RuntimeException("Organizer not found"));
                Committee committee = Committee.builder()
                                .name(request.getName())
                                .currency(request.getCurrency())
                                .totalMembers(request.getTotalMembers())
                                .amountPerMember(request.getAmountPerMember())
                                .frequency(request.getFrequency())
                                .payoutFrequency(request.getPayoutFrequency() != null ? request.getPayoutFrequency()
                                                : request.getFrequency())
                                .totalCycles(request.getTotalMembers()) // total cycles = total members
                                .currentCycle(1)
                                .startDate(request.getStartDate() != null ? request.getStartDate() : LocalDate.now())
                                .status(Committee.CommitteeStatus.ACTIVE)
                                .createdBy(organizer)
                                .build();

                committee = committeeRepository.save(committee);

                // Add organizer as member with turnCycle 1
                CommitteeMember organizerMember = CommitteeMember.builder()
                                .committee(committee)
                                .user(organizer)
                                .turnCycle(1)
                                .role(CommitteeMember.MemberRole.ORGANIZER)
                                .build();

                committeeMemberRepository.save(organizerMember);

                // Optionally add members from the request list
                if (request.getMembers() != null && !request.getMembers().isEmpty()) {
                        for (CommitteeRequest.MemberRequest memberReq : request.getMembers()) {
                                CommitteeMember.CommitteeMemberBuilder memberBuilder = CommitteeMember.builder()
                                                .committee(committee)
                                                .turnCycle(memberReq.getTurn())
                                                .role(CommitteeMember.MemberRole.MEMBER);

                                if (memberReq.getEmail() != null && !memberReq.getEmail().isBlank()) {
                                        userRepository.findByEmailIgnoreCase(memberReq.getEmail())
                                                        .ifPresentOrElse(memberBuilder::user, () -> memberBuilder
                                                                        .customName(memberReq.getName()));
                                } else {
                                        memberBuilder.customName(memberReq.getName());
                                }

                                committeeMemberRepository.save(memberBuilder.build());
                        }
                } else {
                        // Fallback: Default placeholder members if none provided (for UI backwards
                        // compatibility if needed)
                        for (int i = 2; i <= committee.getTotalMembers(); i++) {
                                CommitteeMember placeholder = CommitteeMember.builder()
                                                .committee(committee)
                                                .turnCycle(i)
                                                .customName("Member " + i)
                                                .role(CommitteeMember.MemberRole.MEMBER)
                                                .build();
                                committeeMemberRepository.save(placeholder);
                        }
                }

                return mapToResponse(committeeRepository.findById(committee.getId())
                                .orElseThrow(() -> new RuntimeException("Committee not found after save")));
        }

        public List<CommitteeResponse> getAllCommitteesForUser(UUID userId) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
                return committeeRepository.findAllAccessibleByUser(user).stream()
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());
        }

        public CommitteeResponse getCommitteeById(UUID committeeId, UUID userId) {
                Committee committee = committeeRepository.findById(committeeId)
                                .orElseThrow(() -> new RuntimeException("Committee not found"));
                // Verify access implicitly
                return mapToResponse(committee);
        }

        @Transactional
        public CommitteeTransactionResponse markPaid(UUID committeeId, CommitteePaymentRequest request,
                        UUID organizerId) {
                User organizer = userRepository.findById(organizerId).orElseThrow();
                Committee committee = committeeRepository.findById(committeeId)
                                .orElseThrow(() -> new RuntimeException("Committee not found"));

                User payer = userRepository.findById(request.getUserId())
                                .orElseThrow(() -> new RuntimeException("User not found"));

                // Only Organizer can mark physical cash, or this acts as wallet flow
                CommitteeMember member = committeeMemberRepository.findByCommitteeAndUser(committee, payer)
                                .orElseThrow(() -> new RuntimeException("Member not in committee"));

                CommitteeTransaction transaction = CommitteeTransaction.builder()
                                .committee(committee)
                                .fromUser(payer)
                                .toRecipient(committee.getCreatedBy().getId().toString()) // To organizer
                                .amount(request.getAmount())
                                .type(CommitteeTransaction.TransactionType.CONTRIBUTION)
                                .cycleNumber(committee.getCurrentCycle())
                                .method(request.getMethod())
                                .status(CommitteeTransaction.TransactionStatus.COMPLETED)
                                .build();

                return mapToTransactionResponse(committeeTransactionRepository.save(transaction));
        }

        @Transactional
        public CommitteeTransactionResponse distributePot(UUID committeeId, UUID organizerId) {
                User organizer = userRepository.findById(organizerId).orElseThrow();
                Committee committee = committeeRepository.findById(committeeId)
                                .orElseThrow(() -> new RuntimeException("Committee not found"));

                if (!committee.getCreatedBy().getId().equals(organizer.getId())) {
                        throw new RuntimeException("Only the organizer can distribute the pot");
                }

                CommitteeMember recipient = committeeMemberRepository
                                .findByCommitteeAndTurnCycle(committee, committee.getCurrentCycle())
                                .orElseThrow(() -> new RuntimeException("Could not find recipient for current cycle"));

                if (recipient.isHasReceivedPot()) {
                        throw new RuntimeException("Pot already distributed for this cycle");
                }

                BigDecimal potAmount = committee.getAmountPerMember()
                                .multiply(BigDecimal.valueOf(committee.getTotalMembers()));

                CommitteeTransaction payout = CommitteeTransaction.builder()
                                .committee(committee)
                                .fromUser(organizer) // Organizer acts as the sender of the pot
                                .toRecipient(recipient.getUser().getId().toString())
                                .amount(potAmount)
                                .type(CommitteeTransaction.TransactionType.POT_PAYOUT)
                                .cycleNumber(committee.getCurrentCycle())
                                .method(CommitteeTransaction.PaymentMethod.WALLET) // Default payout is wallet
                                .status(CommitteeTransaction.TransactionStatus.COMPLETED)
                                .build();

                recipient.setHasReceivedPot(true);
                committeeMemberRepository.save(recipient);

                committeeTransactionRepository.save(payout);

                // Advance to next cycle or complete
                if (committee.getCurrentCycle() < committee.getTotalCycles()) {
                        committee.setCurrentCycle(committee.getCurrentCycle() + 1);
                } else {
                        committee.setStatus(Committee.CommitteeStatus.COMPLETED);
                }
                committeeRepository.save(committee);

                return mapToTransactionResponse(payout);
        }

        @Transactional
        public CommitteeTransactionResponse coverDefault(UUID committeeId, CommitteePaymentRequest request,
                        UUID organizerId) {
                User organizer = userRepository.findById(organizerId).orElseThrow();
                Committee committee = committeeRepository.findById(committeeId)
                                .orElseThrow(() -> new RuntimeException("Committee not found"));

                User defaulter = userRepository.findById(request.getUserId())
                                .orElseThrow(() -> new RuntimeException("Defaulter not found"));

                // Organizer pays on behalf of defaulter
                CommitteeTransaction coverTransaction = CommitteeTransaction.builder()
                                .committee(committee)
                                .fromUser(organizer)
                                .toRecipient(committee.getCreatedBy().getId().toString()) // to the pot
                                .amount(request.getAmount())
                                .type(CommitteeTransaction.TransactionType.POT_COVER_BY_ORGANIZER)
                                .cycleNumber(committee.getCurrentCycle())
                                .method(request.getMethod())
                                .status(CommitteeTransaction.TransactionStatus.COMPLETED)
                                .build();

                return mapToTransactionResponse(committeeTransactionRepository.save(coverTransaction));
        }

        public List<CommitteeTransactionResponse> getTransactions(UUID committeeId) {
                Committee committee = committeeRepository.findById(committeeId)
                                .orElseThrow(() -> new RuntimeException("Committee not found: " + committeeId));
                return committeeTransactionRepository.findByCommittee(committee).stream()
                                .map(this::mapToTransactionResponse)
                                .collect(Collectors.toList());
        }

        private CommitteeResponse mapToResponse(Committee committee) {
                return CommitteeResponse.builder()
                                .id(committee.getId())
                                .name(committee.getName())
                                .currency(committee.getCurrency())
                                .totalMembers(committee.getTotalMembers())
                                .amountPerMember(committee.getAmountPerMember())
                                .frequency(committee.getFrequency())
                                .payoutFrequency(committee.getPayoutFrequency())
                                .totalCycles(committee.getTotalCycles())
                                .currentCycle(committee.getCurrentCycle())
                                .startDate(committee.getStartDate())
                                .status(committee.getStatus())
                                .createdBy(mapToUserResponse(committee.getCreatedBy()))
                                .createdAt(committee.getCreatedAt())
                                .members(committee.getMembers().stream().map(this::mapToMemberResponse)
                                                .collect(Collectors.toList()))
                                .build();
        }

        private CommitteeMemberResponse mapToMemberResponse(CommitteeMember member) {
                return CommitteeMemberResponse.builder()
                                .id(member.getId())
                                .user(member.getUser() != null ? mapToUserResponse(member.getUser()) : null)
                                .customName(member.getCustomName())
                                .turnCycle(member.getTurnCycle())
                                .hasReceivedPot(member.isHasReceivedPot())
                                .role(member.getRole())
                                .joinedAt(member.getJoinedAt())
                                .build();
        }

        private UserResponse mapToUserResponse(User user) {
                return UserResponse.builder()
                                .id(user.getId())
                                .name(user.getName())
                                .email(user.getEmail())
                                .logoUrl(user.getLogoUrl())
                                .build();
        }

        private CommitteeTransactionResponse mapToTransactionResponse(CommitteeTransaction transaction) {
                return CommitteeTransactionResponse.builder()
                                .id(transaction.getId())
                                .fromUser(mapToUserResponse(transaction.getFromUser()))
                                .toRecipient(transaction.getToRecipient())
                                .amount(transaction.getAmount())
                                .type(transaction.getType())
                                .cycleNumber(transaction.getCycleNumber())
                                .method(transaction.getMethod())
                                .status(transaction.getStatus())
                                .paidAt(transaction.getPaidAt())
                                .build();
        }
}
