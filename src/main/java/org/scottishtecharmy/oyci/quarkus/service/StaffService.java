package org.scottishtecharmy.oyci.quarkus.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import org.scottishtecharmy.oyci.quarkus.dto.AvailabilityRequest;
import org.scottishtecharmy.oyci.quarkus.dto.HolidayRequest;
import org.scottishtecharmy.oyci.quarkus.dto.MaxHoursRequest;
import org.scottishtecharmy.oyci.quarkus.entity.*;
import org.scottishtecharmy.oyci.quarkus.enums.Role;

import java.util.List;

@ApplicationScoped
public class StaffService {

    public List<User> listAllStaff() {
        return User.list("role", Role.STAFF);
    }

    public User findById(Long id) {
        User user = User.findById(id);
        if (user == null) throw new NotFoundException("Staff member not found");
        return user;
    }

    @Transactional
    public User create(User user, AuthService authService) {
        if (User.findByEmail(user.email) != null) {
            throw new BadRequestException("Email already in use");
        }
        user.passwordHash = authService.hashPassword(user.passwordHash);
        user.role = Role.STAFF;
        user.persist();
        return user;
    }

    @Transactional
    public User update(Long id, User updated) {
        User user = User.findById(id);
        if (user == null) throw new NotFoundException("Staff member not found");
        user.name = updated.name;
        user.email = updated.email;
        user.maxHoursPerWeek = updated.maxHoursPerWeek;
        user.dateOfBirth = updated.dateOfBirth;
        return user;
    }

    @Transactional
    public void delete(Long id) {
        User user = User.findById(id);
        if (user == null) throw new NotFoundException("Staff member not found");
        user.delete();
    }

    // ===== Self-Service =====

    @Transactional
    public void updateAvailability(Long id, List<AvailabilityRequest> windows) {
        User user = User.findById(id);
        if (user == null) throw new NotFoundException("Staff member not found");
        UserAvailability.delete("user.id", id);
        for (AvailabilityRequest w : windows) {
            UserAvailability av = new UserAvailability();
            av.user = user;
            av.dayOfWeek = w.dayOfWeek;
            av.startTime = w.startTime;
            av.endTime = w.endTime;
            av.persist();
        }
    }

    @Transactional
    public void updateMaxHours(Long id, MaxHoursRequest request) {
        User user = User.findById(id);
        if (user == null) throw new NotFoundException("Staff member not found");
        user.maxHoursPerWeek = request.maxHoursPerWeek;
    }

    @Transactional
    public void updateTags(Long id, List<Long> tagIds) {
        User user = User.findById(id);
        if (user == null) throw new NotFoundException("Staff member not found");
        user.tags.clear();
        for (Long tagId : tagIds) {
            Tag tag = Tag.findById(tagId);
            if (tag != null) user.tags.add(tag);
        }
    }

    public List<UserAvailability> getAvailability(Long id) {
        if (User.findById(id) == null) throw new NotFoundException("Staff member not found");
        return UserAvailability.findByUserId(id);
    }

    public List<UserHoliday> getHolidays(Long id) {
        if (User.findById(id) == null) throw new NotFoundException("Staff member not found");
        return UserHoliday.findByUserId(id);
    }

    @Transactional
    public UserHoliday addHoliday(Long id, HolidayRequest request) {
        User user = User.findById(id);
        if (user == null) throw new NotFoundException("Staff member not found");
        UserHoliday holiday = new UserHoliday();
        holiday.user = user;
        holiday.startDate = request.startDate;
        holiday.endDate = request.endDate;
        holiday.persist();
        return holiday;
    }

    @Transactional
    public void deleteHoliday(Long userId, Long holidayId) {
        UserHoliday holiday = UserHoliday.findById(holidayId);
        if (holiday == null || !holiday.user.id.equals(userId)) {
            throw new NotFoundException("Holiday not found");
        }
        holiday.delete();
    }

    public List<EventAssignment> getSchedule(Long id) {
        if (User.findById(id) == null) throw new NotFoundException("Staff member not found");
        return EventAssignment.findByUserId(id);
    }
}

