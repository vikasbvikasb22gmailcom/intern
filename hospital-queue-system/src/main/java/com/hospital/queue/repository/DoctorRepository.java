package com.hospital.queue.repository;

import com.hospital.queue.entity.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    Optional<Doctor> findByUserId(Long userId);

    List<Doctor> findByAvailableTrue();

    Page<Doctor> findByAvailableTrue(Pageable pageable);

    @Query("SELECT d FROM Doctor d WHERE d.available = true AND LOWER(d.specialization) LIKE LOWER(CONCAT('%', :specialization, '%'))")
    Page<Doctor> findBySpecialization(String specialization, Pageable pageable);

    @Query("SELECT d FROM Doctor d WHERE d.available = true AND (LOWER(d.specialization) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(d.user.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(d.user.lastName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Doctor> searchDoctors(String search, Pageable pageable);

    @Query("SELECT DISTINCT d.specialization FROM Doctor d WHERE d.available = true")
    List<String> findAllSpecializations();
}
