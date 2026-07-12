package com.example.demo.rep;

import com.example.demo.Clients;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRep extends JpaRepository<Clients, Long> {
    Optional<Clients> findByLogin(String login);
    // Метод поиска с пагинацией
    Page<Clients> findBySecondNameContainingIgnoreCaseOrderBySecondNameAsc(String secondName, Pageable pageable);
    // Метод получения всех с пагинацией
    Page<Clients> findAllByOrderBySecondNameAsc(Pageable pageable);


}

