-- a ; 1
INSERT INTO EMPLOYEE (first_name, second_name, role, login, password)
    VALUES ('Иван', 'Иван', 'ROLE_ADMIN', 'a', '$2a$10$ceObkQ96GHQrAmSb89DPCejVRGuRzIIWW8gw9ZjkP6PqT6u0dEdbO');

-- w ; 1
INSERT INTO EMPLOYEE (first_name, second_name, role, login, password)
    VALUES ('Петр', 'Первый', 'ROLE_EMPLOYEE', 'w', '$2a$10$ceObkQ96GHQrAmSb89DPCejVRGuRzIIWW8gw9ZjkP6PqT6u0dEdbO');



INSERT INTO Book(name, author, pages, published, genre, price, number, status)
    VALUES      ('Аля иногда кокетничает со мной по-русски. Том 1', 'Сансан SUN', 170, 2025, 'романтика', 1200, 0, '');

INSERT INTO Book(name, author, pages, published, genre, price, number, status)
    VALUES      ('Аля иногда кокетничает со мной по-русски. Том 2', 'Сансан SUN', 170, 2026, 'романтика', 1200, 3, '');

INSERT INTO Book(name, author, pages, published, genre, price, number, status)
    VALUES      ('Аля иногда кокетничает со мной по-русски. Том 3', 'Сансан SUN', 170, 2026, 'романтика', 1200, 12, '');

INSERT INTO Book(name, author, pages, published, genre, price, number, status)
    VALUES      ('Герой нашего времени', 'Михаил Лермонтов', 317, 2024, 'приключения', 199, 6, '');



INSERT INTO CLIENTS (first_name, second_name, login, password) VALUES
    ('Иван', 'Иванов', 'ivan', '$2a$10$ceObkQ96GHQrAmSb89DPCejVRGuRzIIWW8gw9ZjkP6PqT6u0dEdbO');

INSERT INTO CLIENTS (first_name, second_name, login, password) VALUES
    ('Петр', 'Петров', 'petr', '$2a$10$ceObkQ96GHQrAmSb89DPCejVRGuRzIIWW8gw9ZjkP6PqT6u0dEdbO');

INSERT INTO CLIENTS (first_name, second_name, login, password) VALUES
    ('Сергей', 'Сидоров', 'sergey', '$2a$10$ceObkQ96GHQrAmSb89DPCejVRGuRzIIWW8gw9ZjkP6PqT6u0dEdbO');

INSERT INTO CLIENTS (first_name, second_name, login, password) VALUES
    ('Анна', 'Смирнова', 'anna', '$2a$10$ceObkQ96GHQrAmSb89DPCejVRGuRzIIWW8gw9ZjkP6PqT6u0dEdbO');

INSERT INTO CLIENTS (first_name, second_name, login, password) VALUES
    ('Мария', 'Кузнецова', 'maria', '$2a$10$ceObkQ96GHQrAmSb89DPCejVRGuRzIIWW8gw9ZjkP6PqT6u0dEdbO');

INSERT INTO CLIENTS (first_name, second_name, login, password) VALUES
    ('Алексей', 'Попов', 'alexey', '$2a$10$ceObkQ96GHQrAmSb89DPCejVRGuRzIIWW8gw9ZjkP6PqT6u0dEdbO');

INSERT INTO CLIENTS (first_name, second_name, login, password) VALUES
    ('Дмитрий', 'Васильев', 'dmitry', '$2a$10$ceObkQ96GHQrAmSb89DPCejVRGuRzIIWW8gw9ZjkP6PqT6u0dEdbO');