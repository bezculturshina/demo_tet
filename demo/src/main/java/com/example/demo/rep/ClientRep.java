package com.example.demo.rep;

import com.example.demo.Clients;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientRep extends JpaRepository<Clients, Long> {
    Optional<Clients> findByLogin(String login);
}

