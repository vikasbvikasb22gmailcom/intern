package com.hospital.queue.service;

import com.hospital.queue.dto.AdminDashboardDto;
import com.hospital.queue.dto.UserDto;
import com.hospital.queue.entity.User;
import com.hospital.queue.enums.AppointmentStatus;
import com.hospital.queue.enums.Role;
import com.hospital.queue.exception.ResourceNotFoundException;
import com.hospital.queue.repository.AppointmentRepository;
import com.hospital.queue.repository.DoctorRepository;
import com.hospital.queue.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final ModelMapper modelMapper;

    public AdminDashboardDto getDashboard() {
        LocalDate today = LocalDate.now();

        long totalPatients = userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.ROLE_PATIENT).count();
        long totalDoctors = doctorRepository.count();
        long totalToday = appointmentRepository.countActiveAppointmentsForDoctorAndDate(0L, today) +
                appointmentRepository.findAll().stream()
                        .filter(a -> a.getAppointmentDate().equals(today)).count();

        // Per-day counts
        var todayAppointments = appointmentRepository.findAll().stream()
                .filter(a -> a.getAppointmentDate().equals(today))
                .collect(Collectors.toList());

        long pending = todayAppointments.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.PENDING ||
                        a.getStatus() == AppointmentStatus.CONFIRMED).count();
        long completed = todayAppointments.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.COMPLETED).count();
        long cancelled = todayAppointments.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.CANCELLED).count();

        // Appointments by specialization
        Map<String, Long> bySpec = appointmentRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        a -> a.getDoctor().getSpecialization(),
                        LinkedHashMap::new,
                        Collectors.counting()
                ));

        // Appointments by status
        Map<String, Long> byStatus = appointmentRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        a -> a.getStatus().name(),
                        LinkedHashMap::new,
                        Collectors.counting()
                ));

        return AdminDashboardDto.builder()
                .totalPatients(totalPatients)
                .totalDoctors(totalDoctors)
                .totalAppointmentsToday(todayAppointments.size())
                .pendingAppointmentsToday(pending)
                .completedAppointmentsToday(completed)
                .cancelledAppointmentsToday(cancelled)
                .totalAppointmentsAllTime(appointmentRepository.count())
                .appointmentsBySpecialization(bySpec)
                .appointmentsByStatus(byStatus)
                .build();
    }

    public Page<UserDto> getAllPatients(String search, Pageable pageable) {
        Page<User> users = (search != null && !search.isBlank())
                ? userRepository.findByRoleAndSearch(Role.ROLE_PATIENT, search, pageable)
                : userRepository.findByRole(Role.ROLE_PATIENT, pageable);
        return users.map(u -> modelMapper.map(u, UserDto.class));
    }

    public UserDto getPatientById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", id));
        return modelMapper.map(user, UserDto.class);
    }

    @Transactional
    public UserDto toggleUserStatus(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        user.setEnabled(!user.isEnabled());
        return modelMapper.map(userRepository.save(user), UserDto.class);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        userRepository.delete(user);
    }
}
