package com.example.demo;

import com.example.demo.rep.ClientRep;
import com.example.demo.rep.EmployeeRep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UsersDetailService implements UserDetailsService {

    @Autowired
    private ClientRep clientRepository;
    @Autowired
    private EmployeeRep employeeRepository;

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        // ищем среди сотрудников
        Optional<Employee> employee = employeeRepository.findByLogin(login);
        if (employee.isPresent()) {
            return User.builder()
                    .username(employee.get().getLogin())
                    .password(employee.get().getPassword())
                    // Используем .authorities() для точного соответствия с SecurityConfig
                    .authorities(employee.get().getRole())
                    .build();
        }

        // ищем среди клиентов
        Optional<Clients> client = clientRepository.findByLogin(login);
        if (client.isPresent()) {
            return User.builder()
                    .username(client.get().getLogin())
                    .password(client.get().getPassword())
                    .roles("CUSTOMER")
                    .build();
        }

        throw new UsernameNotFoundException("Пользователь не найден: " + login);
    }
}
