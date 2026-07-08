package com.example.demo;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Связь с клиентом (чтобы работал order.client.login)
    @ManyToOne
    @JoinColumn(name = "client_id")
    private Clients client;

    // Связь со списком книг (чтобы работал order.books)
    @ManyToMany
    @JoinTable(
            name = "order_books",
            joinColumns = @JoinColumn(name = "order_id"),
            inverseJoinColumns = @JoinColumn(name = "book_id")
    )
    private List<Book> books;

    private String status = "PENDING"; // По умолчанию ожидает

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    private LocalDate orderDate = LocalDate.now();
}