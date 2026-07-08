package com.example.demo.rep;

import com.example.demo.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Rep extends JpaRepository<Book, Long> {
}
