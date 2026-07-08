package com.example.demo;

import com.example.demo.rep.ClientRep;
import com.example.demo.rep.EmployeeRep;
import com.example.demo.rep.OrderRep;
import com.example.demo.rep.Rep;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@AllArgsConstructor
@Getter
public class Service {

    private final Rep rep;
    private final OrderRep orderRepository;
    private final EmployeeRep employeeRepository;
    private final ClientRep clientRepository;

    // Объект для хеширования и проверки паролей
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // =========================================================================
    // --- СОВМЕСТИМОСТЬ С КОНТРОЛЛЕРОМ ---
    // =========================================================================

    /**
     * Возвращает репозиторий книг.
     */
    public Rep getBookRepository() {
        return this.rep;
    }

    // =========================================================================
    // --- ОБЩИЕ МЕТОДЫ / АВТОРИЗАЦИЯ ---
    // =========================================================================

    public List<Book> getBooks() {
        return rep.findAll();
    }

    public String registerClient(Clients client) {
        if (clientRepository.findByLogin(client.getLogin()).isPresent() ||
                employeeRepository.findByLogin(client.getLogin()).isPresent()) {
            return "Логин уже занят!";
        }

        String encodedPassword = passwordEncoder.encode(client.getPassword());
        client.setPassword(encodedPassword);

        clientRepository.save(client);
        return "Успешная регистрация!";
    }

    public String login(String login, String password) {
        Optional<Clients> clientOpt = clientRepository.findByLogin(login);
        if (clientOpt.isPresent()) {
            Clients client = clientOpt.get();
            if (passwordEncoder.matches(password, client.getPassword())) {
                return "CUSTOMER";
            } else {
                return "WRONG_PASSWORD";
            }
        }

        Optional<Employee> employeeOpt = employeeRepository.findByLogin(login);
        if (employeeOpt.isPresent()) {
            Employee employee = employeeOpt.get();
            if (passwordEncoder.matches(password, employee.getPassword())) {
                return employee.getRole().replace("ROLE_", "");
            } else {
                return "WRONG_PASSWORD";
            }
        }

        return "USER_NOT_FOUND";
    }

    // =========================================================================
    // --- МЕТОДЫ ПОКУПАТЕЛЯ (CUSTOMER) ---
    // =========================================================================

    public String checkoutBooksSafe(List<Long> bookIds, Long clientId) {
        // 1. Группируем и проверяем остатки "в уме"
        Map<Long, Long> requestedCounts = bookIds.stream()
                .collect(Collectors.groupingBy(id -> id, Collectors.counting()));

        List<Book> booksToOrder = new ArrayList<>();

        for (Map.Entry<Long, Long> entry : requestedCounts.entrySet()) {
            Long bookId = entry.getKey();
            Long requestedQty = entry.getValue();

            Book book = rep.findById(bookId).orElse(null);
            if (book == null) {
                return "ERROR_NOT_FOUND";
            }
            if (book.getNumber() < requestedQty) {
                return "ERROR_INSUFFICIENT_STOCK";
            }

            for (int i = 0; i < requestedQty; i++) {
                booksToOrder.add(book);
            }
        }

        // 2. Если проверка успешна — списываем со склада
        for (Map.Entry<Long, Long> entry : requestedCounts.entrySet()) {
            Long bookId = entry.getKey();
            Long requestedQty = entry.getValue();

            Book book = rep.findById(bookId).get();
            book.setNumber(book.getNumber() - requestedQty.intValue());
            rep.save(book);
        }

        // 3. Создаем новый заказ
        Order order = new Order();
        Clients client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Клиент не найден"));

        order.setClient(client);
        order.setBooks(booksToOrder);
        order.setStatus("PENDING");

        // Автоматически фиксируем дату заказа (без времени)
        order.setOrderDate(LocalDate.now());

        orderRepository.save(order);
        return "SUCCESS";
    }

    // =========================================================================
    // --- МЕТОДЫ СОТРУДНИКА (EMPLOYEE) ---
    // =========================================================================

    public Order completeOrder(Long orderId, Long employeeId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Заказ не найден"));
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Сотрудник не найден"));

        order.setEmployee(employee);
        order.setStatus("COMPLETED");
        return orderRepository.save(order);
    }

    // =========================================================================
    // --- МЕТОДЫ АДМИНИСТРАТОРА (ADMIN) ---
    // =========================================================================

    public Employee addEmployee(Employee employee) {
        if (employee.getRole() == null || employee.getRole().isEmpty()) {
            employee.setRole("ROLE_EMPLOYEE");
        }
        employee.setPassword(passwordEncoder.encode(employee.getPassword()));
        return employeeRepository.save(employee);
    }

    public void deleteEmployee(Long id) {
        employeeRepository.deleteById(id);
    }

    public List<Employee> getAllEmployees() {
        return employeeRepository.findByRole("ROLE_EMPLOYEE");
    }
}