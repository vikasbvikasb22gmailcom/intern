package com.hospital.queue.service;

import com.hospital.queue.dto.DoctorDto;
import com.hospital.queue.entity.Doctor;
import com.hospital.queue.entity.DoctorSchedule;
import com.hospital.queue.entity.User;
import com.hospital.queue.enums.Role;
import com.hospital.queue.exception.BadRequestException;
import com.hospital.queue.exception.ResourceNotFoundException;
import com.hospital.queue.repository.DoctorRepository;
import com.hospital.queue.repository.DoctorScheduleRepository;
import com.hospital.queue.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final DoctorScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    @Transactional
    public DoctorDto.DoctorResponse createDoctor(DoctorDto.CreateDoctorRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered");
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROLE_DOCTOR)
                .enabled(true)
                .build();
        userRepository.save(user);

        Doctor doctor = Doctor.builder()
                .user(user)
                .specialization(request.getSpecialization())
                .qualification(request.getQualification())
                .licenseNumber(request.getLicenseNumber())
                .consultationDurationMinutes(request.getConsultationDurationMinutes())
                .consultationFee(request.getConsultationFee())
                .experienceYears(request.getExperienceYears())
                .bio(request.getBio())
                .maxPatientsPerDay(request.getMaxPatientsPerDay())
                .available(true)
                .build();
        doctorRepository.save(doctor);

        return mapToResponse(doctor);
    }

    public Page<DoctorDto.DoctorResponse> getAllDoctors(Pageable pageable) {
        return doctorRepository.findByAvailableTrue(pageable).map(this::mapToResponse);
    }

    public Page<DoctorDto.DoctorResponse> searchDoctors(String query, Pageable pageable) {
        return doctorRepository.searchDoctors(query, pageable).map(this::mapToResponse);
    }

    public Page<DoctorDto.DoctorResponse> getDoctorsBySpecialization(String specialization, Pageable pageable) {
        return doctorRepository.findBySpecialization(specialization, pageable).map(this::mapToResponse);
    }

    public List<String> getAllSpecializations() {
        return doctorRepository.findAllSpecializations();
    }

    public DoctorDto.DoctorResponse getDoctorById(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", id));
        return mapToResponse(doctor);
    }

    @Transactional
    public DoctorDto.DoctorResponse updateDoctor(Long id, DoctorDto.UpdateDoctorRequest request) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", id));

        if (request.getSpecialization() != null) doctor.setSpecialization(request.getSpecialization());
        if (request.getQualification() != null) doctor.setQualification(request.getQualification());
        if (request.getLicenseNumber() != null) doctor.setLicenseNumber(request.getLicenseNumber());
        if (request.getConsultationDurationMinutes() != null) doctor.setConsultationDurationMinutes(request.getConsultationDurationMinutes());
        if (request.getConsultationFee() != null) doctor.setConsultationFee(request.getConsultationFee());
        if (request.getExperienceYears() != null) doctor.setExperienceYears(request.getExperienceYears());
        if (request.getBio() != null) doctor.setBio(request.getBio());
        if (request.getAvailable() != null) doctor.setAvailable(request.getAvailable());
        if (request.getMaxPatientsPerDay() != null) doctor.setMaxPatientsPerDay(request.getMaxPatientsPerDay());

        return mapToResponse(doctorRepository.save(doctor));
    }

    @Transactional
    public DoctorDto.ScheduleResponse addOrUpdateSchedule(Long doctorId, DoctorDto.ScheduleRequest request) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", doctorId));

        if (request.getStartTime().isAfter(request.getEndTime())) {
            throw new BadRequestException("Start time must be before end time");
        }

        DoctorSchedule schedule = scheduleRepository
                .findByDoctorIdAndDayOfWeek(doctorId, request.getDayOfWeek())
                .orElse(DoctorSchedule.builder().doctor(doctor).build());

        schedule.setDayOfWeek(request.getDayOfWeek());
        schedule.setStartTime(request.getStartTime());
        schedule.setEndTime(request.getEndTime());
        schedule.setActive(request.getIsActive() != null ? request.getIsActive() : true);
        schedule.setNotes(request.getNotes());

        DoctorSchedule saved = scheduleRepository.save(schedule);
        return modelMapper.map(saved, DoctorDto.ScheduleResponse.class);
    }

    public List<DoctorDto.ScheduleResponse> getDoctorSchedule(Long doctorId) {
        return scheduleRepository.findByDoctorIdAndIsActiveTrue(doctorId)
                .stream()
                .map(s -> modelMapper.map(s, DoctorDto.ScheduleResponse.class))
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteSchedule(Long scheduleId) {
        DoctorSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule", scheduleId));
        scheduleRepository.delete(schedule);
    }

    private DoctorDto.DoctorResponse mapToResponse(Doctor doctor) {
        DoctorDto.DoctorResponse response = new DoctorDto.DoctorResponse();
        response.setId(doctor.getId());
        response.setUserId(doctor.getUser().getId());
        response.setFirstName(doctor.getUser().getFirstName());
        response.setLastName(doctor.getUser().getLastName());
        response.setEmail(doctor.getUser().getEmail());
        response.setPhone(doctor.getUser().getPhone());
        response.setSpecialization(doctor.getSpecialization());
        response.setQualification(doctor.getQualification());
        response.setLicenseNumber(doctor.getLicenseNumber());
        response.setConsultationDurationMinutes(doctor.getConsultationDurationMinutes());
        response.setConsultationFee(doctor.getConsultationFee());
        response.setExperienceYears(doctor.getExperienceYears());
        response.setBio(doctor.getBio());
        response.setAvailable(doctor.isAvailable());
        response.setMaxPatientsPerDay(doctor.getMaxPatientsPerDay());
        response.setSchedules(
                scheduleRepository.findByDoctorIdAndIsActiveTrue(doctor.getId())
                        .stream()
                        .map(s -> modelMapper.map(s, DoctorDto.ScheduleResponse.class))
                        .collect(Collectors.toList())
        );
        return response;
    }
}
