package com.example.demo.rep;

import com.example.demo.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRep extends JpaRepository<Employee, Long> {
    List<Employee> findByRole(String role);
    Optional<Employee> findByLogin(String login);
}
