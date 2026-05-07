package org.scottishtecharmy.oyci.quarkus.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;
import org.scottishtecharmy.oyci.quarkus.request.ApplyLeaveRequest;
import org.scottishtecharmy.oyci.quarkus.response.LeaveResponse;
import org.scottishtecharmy.oyci.quarkus.enums.LeaveStatus;
import org.scottishtecharmy.oyci.quarkus.exception.BusinessValidationException;
import org.scottishtecharmy.oyci.quarkus.exception.ResourceNotFoundException;
import org.scottishtecharmy.oyci.quarkus.model.Leave;
import org.scottishtecharmy.oyci.quarkus.model.Staff;
import org.scottishtecharmy.oyci.quarkus.repository.LeaveRepository;
import org.scottishtecharmy.oyci.quarkus.repository.StaffRepository;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class LeaveService {

    private static final Logger LOG = Logger.getLogger(LeaveService.class);

    @Inject
    LeaveRepository leaveRepository;

    @Inject
    StaffRepository staffRepository;

    /**
     * Apply for leave
     */
    @Transactional
    public LeaveResponse applyLeave(ApplyLeaveRequest request) {
        LOG.infof("Applying leave for staffId=%d, type=%s, from=%s to=%s",
                request.getStaffId(), request.getLeaveType(),
                request.getStartDatetime(), request.getEndDatetime());

        // Validate staff exists and is active
        Staff staff = staffRepository.findByIdOptional(request.getStaffId())
                .orElseThrow(() -> {
                    LOG.warnf("Staff not found with ID: %d", request.getStaffId());
                    return new ResourceNotFoundException("Staff", "staffId", request.getStaffId());
                });

        if (staff.getIsActive() != null && !staff.getIsActive()) {
            LOG.warnf("Leave rejected: staff ID %d is inactive", request.getStaffId());
            throw new BusinessValidationException("Cannot apply leave for an inactive staff member");
        }

        // Validate dates
        if (request.getEndDatetime().isBefore(request.getStartDatetime())) {
            throw new BusinessValidationException("End date must be after start date");
        }
        if (request.getEndDatetime().isEqual(request.getStartDatetime())) {
            throw new BusinessValidationException("End date cannot be the same as start date");
        }

        // Check for overlapping APPROVED leave requests only (exclude cancelled)
        List<Leave> overlapping = leaveRepository.list(
                "staff.staffId = ?1 and status = 'APPROVED' and startDatetime < ?2 and endDatetime > ?3",
                request.getStaffId(), request.getEndDatetime(), request.getStartDatetime()
        );
        if (!overlapping.isEmpty()) {
            LOG.warnf("Overlapping approved leave found for staffId=%d", request.getStaffId());
            throw new BusinessValidationException("Staff already has an approved leave overlapping with the requested period");
        }

        // Create and persist leave
        Leave leave = new Leave();
        leave.setStaff(staff);
        leave.setStartDatetime(request.getStartDatetime());
        leave.setEndDatetime(request.getEndDatetime());
        leave.setLeaveType(request.getLeaveType().name());
        leave.setReason(request.getReason());
        leave.setStatus(LeaveStatus.APPLIED.name());
        leave.setRecordCreatedBy("SYSTEM");
        leave.setRecordUpdatedBy("SYSTEM");
        leaveRepository.persist(leave);
        LOG.infof("Leave applied successfully: id=%d, staffId=%d, type=%s",
                leave.getId(), request.getStaffId(), request.getLeaveType());

        return LeaveResponse.from(leave);
    }

    /**
     * Get all leave requests for a staff member
     */
    public List<LeaveResponse> getLeaveByStaff(Long staffId) {
        LOG.infof("Fetching leave requests for staffId=%d", staffId);
        staffRepository.findByIdOptional(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff", "staffId", staffId));
        List<LeaveResponse> results = leaveRepository.findByStaffId(staffId)
                .stream()
                .map(LeaveResponse::from)
                .collect(Collectors.toList());
        LOG.infof("Found %d leave records for staffId=%d", results.size(), staffId);
        return results;
    }

    /**
     * Get all leave requests (admin view)
     */
    public List<LeaveResponse> getAllLeave() {
        LOG.info("Fetching all leave requests");
        List<LeaveResponse> results = leaveRepository.findAllActive()
                .stream()
                .map(LeaveResponse::from)
                .collect(Collectors.toList());
        LOG.infof("Found %d total leave records", results.size());
        return results;
    }

    /**
     * Update an existing leave request (only allowed if not yet approved)
     */
    @Transactional
    public LeaveResponse updateLeave(Long leaveId, ApplyLeaveRequest request) {
        LOG.infof("Updating leave id=%d for staffId=%d", leaveId, request.getStaffId());

        Leave leave = leaveRepository.findActiveById(leaveId)
                .orElseThrow(() -> {
                    LOG.warnf("Leave not found with ID: %d", leaveId);
                    return new ResourceNotFoundException("Leave", "id", leaveId);
                });

        if (LeaveStatus.APPROVED.name().equals(leave.getStatus())) {
            throw new BusinessValidationException("Cannot update an already approved leave request");
        }

        if (request.getEndDatetime().isBefore(request.getStartDatetime())) {
            throw new BusinessValidationException("End date must be after start date");
        }
        if (request.getEndDatetime().isEqual(request.getStartDatetime())) {
            throw new BusinessValidationException("End date cannot be the same as start date");
        }

        List<Leave> overlapping = leaveRepository.list(
                "staff.staffId = ?1 and status = 'APPROVED' and id != ?2 and startDatetime < ?3 and endDatetime > ?4",
                leave.getStaff().getStaffId(), leaveId, request.getEndDatetime(), request.getStartDatetime()
        );
        if (!overlapping.isEmpty()) {
            throw new BusinessValidationException("Updated dates overlap with an already approved leave");
        }

        leave.setStartDatetime(request.getStartDatetime());
        leave.setEndDatetime(request.getEndDatetime());
        leave.setLeaveType(request.getLeaveType().name());
        leave.setReason(request.getReason());
        leave.setRecordUpdatedBy("SYSTEM");

        LOG.infof("Leave updated successfully: id=%d, type=%s", leaveId, request.getLeaveType());
        return LeaveResponse.from(leave);
    }

    /**
     * Cancel a leave request (only allowed if not yet approved)
     */
    @Transactional
    public LeaveResponse cancelLeave(Long leaveId) {
        LOG.infof("Cancelling leave id=%d", leaveId);

        Leave leave = leaveRepository.findActiveById(leaveId)
                .orElseThrow(() -> {
                    LOG.warnf("Leave not found with ID: %d", leaveId);
                    return new ResourceNotFoundException("Leave", "id", leaveId);
                });


        leave.setStatus(LeaveStatus.CANCELLED.name());
        leave.setRecordUpdatedBy("SYSTEM");
        LOG.infof("Leave cancelled successfully: id=%d", leaveId);
        return LeaveResponse.from(leave);
    }
}

