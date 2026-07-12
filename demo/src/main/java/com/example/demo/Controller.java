package com.example.demo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@AllArgsConstructor
public class Controller {

    private final Service sr;

//    @GetMapping("/rest-api/4")
//    public String q(){
//        return "сосать Америка";
//    }

    // =========================================================================
    // --- ОБЩИЕ ЭНДПОИНТЫ / АВТОРИЗАЦИЯ ---
    // =========================================================================

    @PostMapping("/rest-api/auth/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody Clients client) {
        String result = sr.registerClient(client);
        if (result.equals("Успешная регистрация!")) {
            return ResponseEntity.ok(Map.of("message", result));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", result));
        }
    }

    @PostMapping("/rest-api/auth/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest loginRequest) {
        var clientOpt = sr.getClientRepository().findByLogin(loginRequest.getLogin());
        if (clientOpt.isPresent()) {
            Clients c = clientOpt.get();
            if (sr.getPasswordEncoder().matches(loginRequest.getPassword(), c.getPassword())) {
                return ResponseEntity.ok(Map.of(
                        "id", c.getId(),
                        "role", "CUSTOMER",
                        "firstName", c.getFirstName(),
                        "secondName", c.getSecondName(),
                        "message", "Успешный вход"
                ));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Неверный пароль"));
            }
        }

        var empOpt = sr.getEmployeeRepository().findByLogin(loginRequest.getLogin());
        if (empOpt.isPresent()) {
            Employee e = empOpt.get();
            if (sr.getPasswordEncoder().matches(loginRequest.getPassword(), e.getPassword())) {
                return ResponseEntity.ok(Map.of(
                        "id", e.getId(),
                        "role", e.getRole().replace("ROLE_", ""),
                        "firstName", e.getFirstName(),
                        "secondName", e.getSecondName(),
                        "message", "Успешный вход"
                ));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Неверный пароль"));
            }
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Пользователь не найден"));
    }

    // =========================================================================
    // --- ЭНДПОИНТЫ ПОКУПАТЕЛЯ (CUSTOMER) ---
    // =========================================================================

    // Получение всех книг для главной страницы покупателя
    @GetMapping("/rest-api/customer/books")
    public ResponseEntity<Page<Book>> getBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        // Если в Service еще нет метода, можно вызвать репозиторий напрямую через PageRequest:
        return ResponseEntity.ok(sr.getBookRepository().findAll(PageRequest.of(page, size)));
    }

    // Оформление заказа покупателем
    @PostMapping("/rest-api/customer/checkout")
    public ResponseEntity<Map<String, String>> checkout(
            @RequestBody List<Long> bookIds,
            @RequestHeader("X-User-Id") Long clientId) {

        String result = sr.checkoutBooksSafe(bookIds, clientId);

        if (result.equals("SUCCESS")) {
            return ResponseEntity.ok(Map.of("message", "Заказ успешно оформлен!"));
        } else if (result.equals("ERROR_INSUFFICIENT_STOCK")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Ошибка: На складе недостаточно книг."));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Ошибка: Одна из книг не найдена."));
        }
    }

    // Личная история заказов текущего покупателя
    @GetMapping("/rest-api/customer/orders/my")
    public ResponseEntity<List<Order>> getMyOrders(@RequestHeader("X-User-Id") Long clientId) {
        List<Order> myOrders = sr.getOrderRepository().findAll().stream()
                .filter(order -> order.getClient() != null && order.getClient().getId().equals(clientId))
                .collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(myOrders);
    }

    // =========================================================================
    // --- ЭНДПОИНТЫ СОТРУДНИКА (EMPLOYEE) ---
    // =========================================================================

    // Получение всех заказов для панели управления сотрудника
    @GetMapping("/rest-api/employee/orders")
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(sr.getOrderRepository().findAll());
    }

    // Выполнение заказа сотрудником
    @PostMapping("/rest-api/employee/orders/{orderId}/complete")
    public ResponseEntity<Map<String, String>> completeOrder(
            @PathVariable Long orderId,
            @RequestParam Long employeeId) {
        try {
            sr.completeOrder(orderId, employeeId);
            return ResponseEntity.ok(Map.of("message", "Заказ успешно выполнен!"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Ошибка при выполнении заказа: " + e.getMessage()));
        }
    }

    // Добавление новой книги сотрудником
    @PostMapping("/rest-api/employee/books")
    public ResponseEntity<Book> addBook(@RequestBody Book book) {
        if (book.getStatus() == null || book.getStatus().isEmpty()) {
            book.setStatus("Доступно");
        }
        Book savedBook = sr.getBookRepository().save(book);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedBook);
    }

    // Удаление книги из базы данных сотрудником
    @DeleteMapping("/rest-api/employee/books/{id}")
    public ResponseEntity<Map<String, String>> deleteBook(@PathVariable Long id) {
        var bookOpt = sr.getBookRepository().findById(id);
        if (bookOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Ошибка: Книга не найдена в базе данных."));
        }

        Book book = bookOpt.get();
        book.setStatus("Удалено"); // Смена статуса вместо физического удаления
        book.setNumber(0);         // Обнуляем на складе, чтобы нельзя было купить
        sr.getBookRepository().save(book);

        return ResponseEntity.ok(Map.of("message", "Книга успешно переведена в архив."));
    }

    // =========================================================================
    // --- ЭНДПОИНТЫ АДМИНИСТРАТОРА (ADMIN) ---
    // =========================================================================

    @PostMapping("/rest-api/admin/employees")
    public ResponseEntity<Employee> createEmployee(@RequestBody Employee employee) {
        return ResponseEntity.ok(sr.addEmployee(employee));
    }

    @DeleteMapping("/rest-api/admin/employees/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        sr.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/rest-api/admin/employees")
    public ResponseEntity<List<Employee>> getEmployees() {
        return ResponseEntity.ok(sr.getAllEmployees());
    }

    @GetMapping("/rest-api/admin/clients")
    public ResponseEntity<Page<Clients>> getClients(
            @RequestParam(required = false) String surname,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);

        if (surname != null && !surname.trim().isEmpty()) {
            return ResponseEntity.ok(sr.getClientRepository().findBySecondNameContainingIgnoreCaseOrderBySecondNameAsc(surname, pageable));
        }
        return ResponseEntity.ok(sr.getClientRepository().findAllByOrderBySecondNameAsc(pageable));
    }
    // =========================================================================
    // --- ВСПОМОГАТЕЛЬНЫЕ КЛАССЫ ---
    // =========================================================================

    @Getter
    @Setter
    public static class LoginRequest {
        private String login;
        private String password;
    }
}
