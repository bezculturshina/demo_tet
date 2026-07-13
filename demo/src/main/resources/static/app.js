const { createApp, ref, onMounted, computed } = Vue;

createApp({
    setup() {
        const books = ref([]);
        const orders = ref([]);
        const myOrders = ref([]);
        const currentYear = ref(new Date().getFullYear());
        const currentTab = ref('main');
        const cart = ref([]);

        const bookPage = ref(0);
        const bookTotalPages = ref(1);
        const pageSize = ref(10); // количество книг на одну страницу

        const employees = ref([]);
        const newEmployeeForm = ref({ firstName: '', secondName: '', login: '', password: '' });

        const showAuthForms = ref(false);
        const authMessage = ref('');
        const authError = ref('');

        // Внутри setup() в app.js:

        const currentUser = ref({ id: null, role: '', firstName: '', secondName: '' });

        const fetchCurrentUser = async () => {
            try {
                const res = await fetch('/rest-api/auth/me', {
                    method: 'GET',
                    credentials: 'include' // Это критически важно для кук (JSESSIONID)
                });

                if (res.ok) {
                    const data = await res.json();
                    currentUser.value = {
                        id: data.id || null,
                        firstName: data.firstName || '',
                        secondName: data.secondName || '',
                        role: data.role || '' // Берём напрямую из поля role
                    };
//                } else {
//                    currentUser.value = { id: null, role: '', firstName: '', secondName: '' };
                }
            } catch (e) {
                console.error("Ошибка проверки сессии", e);
                currentUser.value = { id: null, role: '', firstName: '', secondName: '' };
            }
        };

        // Вызывайте fetchCurrentUser при старте
        onMounted(() => {
            fetchCurrentUser();
        });

        const checkoutStatus = ref(null);

        const loginForm = ref({ login: '', password: '' });
        const regForm = ref({ firstName: '', secondName: '', login: '', password: '' });

        // Реактивная форма для создания новой книги сотрудником
        const newBookForm = ref({ name: '', author: '', genre: '', pages: '', published: '', price: '', number: '' });

        const formatDate = (dateValue) => {
            if (!dateValue) return new Date().toLocaleDateString('ru-RU');
            if (Array.isArray(dateValue)) {
                const [year, month, day] = dateValue;
                const d = day < 10 ? '0' + day : day;
                const m = month < 10 ? '0' + month : month;
                return `${d}.${m}.${year}`;
            }
            return new Date(dateValue).toLocaleDateString('ru-RU');
        };

        // ЗАГРУЗКА КНИГ (Customer URL)
//        const fetchBooks = async () => {
//            try {
//                const response = await fetch('http://localhost:8080/rest-api/customer/books');
//                const allBooks = await response.json();
//                // Показываем только те книги, у которых статус не "Удалено"
//                books.value = allBooks.filter(book => book.status !== 'Удалено');
//            } catch (error) { console.error("Ошибка при загрузке книг:", error); }
//        };
        const fetchBooks = async () => {
            try {
                const response = await fetch(`http://localhost:8080/rest-api/customer/books?page=${bookPage.value}&size=${pageSize.value}`);
                const data = await response.json();

                // Так как бэкенд возвращает объект Page, данные берем из content
                books.value = data.content.filter(book => book.status !== 'Удалено');
                bookTotalPages.value = data.totalPages;
            } catch (error) {
                console.error("Ошибка при загрузке книг:", error);
            }
        };

        const nextBookPage = () => { if (bookPage.value < bookTotalPages.value - 1) { bookPage.value++; fetchBooks(); } };
        const prevBookPage = () => { if (bookPage.value > 0) { bookPage.value--; fetchBooks(); } };

        // ЗАГРУЗКА ВСЕХ ЗАКАЗОВ (Employee URL)
        const fetchOrders = async () => {
            try {
                const response = await fetch('http://localhost:8080/rest-api/employee/orders');
                orders.value = await response.json();
            } catch (error) { console.error("Ошибка при загрузке всех заказов:", error); }
        };

        // ЗАГРУЗКА ЛИЧНЫХ ЗАКАЗОВ (Customer URL)
        const fetchMyOrders = async () => {
            try {
                const response = await fetch('http://localhost:8080/rest-api/customer/orders/my', {
                    method: 'GET',
                    headers: { 'X-User-Id': currentUser.value.id }
                });
                myOrders.value = await response.json();
            } catch (error) { console.error("Ошибка при загрузке личных заказов:", error); }
        };

        const goToEmployeeOrders = () => { currentTab.value = 'employee_orders'; fetchOrders(); };
        const goToEmployeeBooks = () => { currentTab.value = 'employee_books'; fetchBooks(); };
        const goToMyOrders = () => { currentTab.value = 'my_orders'; fetchMyOrders(); };

        // ВЫПОЛНЕНИЕ ЗАКАЗА (Employee URL)
        const handleCompleteOrder = async (orderId) => {
            try {
                const response = await fetch(`http://localhost:8080/rest-api/employee/orders/${orderId}/complete?employeeId=${currentUser.value.id}`, {
                    method: 'POST'
                });
                if (response.ok) { await fetchOrders(); }
                else { const data = await response.json(); alert(data.message); }
            } catch (e) { console.error("Ошибка при выполнении заказа:", e); }
        };

        // СОЗДАНИЕ КНИГИ (Employee URL)
        const handleCreateBook = async () => {
            if (!newBookForm.value.name || !newBookForm.value.author) {
                return;
            }
            try {
                const response = await fetch('http://localhost:8080/rest-api/employee/books', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(newBookForm.value)
                });
                if (response.ok) {
                    newBookForm.value = { name: '', author: '', genre: '', pages: '', published: '', price: '', number: '' };
                    await fetchBooks(); // Перезагружаем ассортимент
                }
            } catch (e) { console.error("Ошибка при добавлении книги:", e); }
        };

        // УДАЛЕНИЕ КНИГИ (Employee URL)
        const handleDeleteBook = async (bookId) => {
            try {
                const response = await fetch(`http://localhost:8080/rest-api/employee/books/${bookId}`, {
                    method: 'DELETE'
                });
                if (response.ok) { await fetchBooks(); }
            } catch (e) { console.error("Ошибка при удалении книги:", e); }
        };

        const getCountInCart = (bookId) => {
            const currentUserId = currentUser.value.id;
            return cart.value.filter(item => item.userId === currentUserId && item.book.id === bookId).length;
        };

        const addToCart = (book) => {
            const currentUserId = currentUser.value.id;
            if (getCountInCart(book.id) < book.number) {
                cart.value.push({ userId: currentUserId, book: JSON.parse(JSON.stringify(book)) });
            }
        };

        const removeFromCart = (bookId) => {
            const currentUserId = currentUser.value.id;
            const index = cart.value.findIndex(item => item.userId === currentUserId && item.book.id === bookId);
            if (index !== -1) { cart.value.splice(index, 1); }
        };

        const groupedCart = computed(() => {
            const currentUserId = currentUser.value.id;
            const groups = {};
            cart.value.filter(item => item.userId === currentUserId).forEach(item => {
                const book = item.book;
                if (!groups[book.id]) { groups[book.id] = { book: book, count: 0 }; }
                groups[book.id].count++;
            });
            return Object.values(groups);
        });

        const cartTotal = computed(() => {
            const currentUserId = currentUser.value.id;
            return cart.value.filter(item => item.userId === currentUserId).reduce((sum, item) => sum + item.book.price, 0);
        });

        const currentUserCartLength = computed(() => {
            const currentUserId = currentUser.value.id;
            return cart.value.filter(item => item.userId === currentUserId).length;
        });

        // ОФОРМЛЕНИЕ ЗАКАЗА (Customer URL)
        const handleCheckout = async () => {
            checkoutStatus.value = null;
            const currentUserId = currentUser.value.id;
            const bookIds = cart.value.filter(item => item.userId === currentUserId).map(item => item.book.id);

            try {
                const response = await fetch('http://localhost:8080/rest-api/customer/checkout', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json', 'X-User-Id': currentUserId },
                    body: JSON.stringify(bookIds)
                });
                const data = await response.json();
                if (response.ok) {
                    checkoutStatus.value = { success: true, text: "Заказ оформлен" };
                    cart.value = cart.value.filter(item => item.userId !== currentUserId);
                    await fetchBooks();
                    setTimeout(() => { checkoutStatus.value = null; }, 3000);
                } else { checkoutStatus.value = { success: false, text: data.message }; }
            } catch (e) { checkoutStatus.value = { success: false, text: "Ошибка соединения с сервером." }; }
        };

//        const handleLogin = async () => {
//            authError.value = '';
//            authMessage.value = '';
//
//            try {
//                const response = await fetch('http://localhost:8080/rest-api/auth/login', {
//                    method: 'POST',
//                    headers: { 'Content-Type': 'application/json' },
//                    credentials: 'include',
//                    body: JSON.stringify(loginForm.value)
//                });
//
//                const data = await response.json();
//
//                if (response.ok) {
//
//                    let role = data.role;
//                    if (role && role.startsWith('ROLE_')) {
//                        role = role.replace('ROLE_', '');
//                    }
//
//                    currentUser.value = {
//                        id: data.id,
//                        role: role,
//                        firstName: data.firstName || data.login,
//                        secondName: data.secondName || ''
//                    };
//
////                    localStorage.setItem('user', JSON.stringify(currentUser.value));
//
//                    authMessage.value = data.message;
//                    showAuthForms.value = false;
//                    loginForm.value = { login: '', password: '' };
//
//                    // Загружаем данные в зависимости от роли
//                    if(role === 'EMPLOYEE') {
//                        fetchOrders();
//                    } else if(role === 'CUSTOMER') {
//                        fetchMyOrders();
//                    }
//                } else {
//                    authError.value = data.message || "Ошибка входа";
//                }
//            } catch (e) {
//                authError.value = "Ошибка сервера";
//                console.error(e);
//            }
//        };
        const handleLogin = async () => {
            authError.value = '';
            authMessage.value = '';

            try {
                const response = await fetch('http://localhost:8080/rest-api/auth/login', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    credentials: 'include',
                    body: JSON.stringify(loginForm.value)
                });

                if (response.ok) {
                    authMessage.value = "Успешный вход!";
                    showAuthForms.value = false;
                    loginForm.value = { login: '', password: '' };

                    // Перезапрашиваем данные сессии, чтобы обновить currentUser и его роль
                    await fetchCurrentUser();

                    // Загружаем данные в зависимости от роли
                    if (currentUser.value.role === 'EMPLOYEE') {
                        fetchOrders();
                    } else if (currentUser.value.role === 'CUSTOMER') {
                        fetchMyOrders();
                    }
                } else {
                    const data = await response.json();
                    authError.value = data.message || "Ошибка входа";
                }
            } catch (e) {
                authError.value = "Ошибка сервера";
                console.error(e);
            }
        };

        const handleRegister = async () => {
            authError.value = ''; authMessage.value = '';
            try {
                const response = await fetch('http://localhost:8080/rest-api/auth/register', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(regForm.value)
                });
                const data = await response.json();
                if (response.ok) {
                    authMessage.value = data.message;
                    regForm.value = { firstName: '', secondName: '', login: '', password: '' };
                } else { authError.value = data.message; }
            } catch (e) { authError.value = "Ошибка сервера"; }
        };

        const logout = () => {
            currentUser.value = { id: null, role: '', firstName: '', secondName: '' };

//            localStorage.removeItem('user');

            currentTab.value = 'main'; authMessage.value = ''; authError.value = ''; checkoutStatus.value = null;
            orders.value = []; myOrders.value = [];
        };

        onMounted(() => { fetchBooks(); });


        const fetchEmployees = async () => {
            const res = await fetch('http://localhost:8080/rest-api/admin/employees');
            employees.value = await res.json();

//                console.log("Попытка загрузки сотрудников...");
//                try {
//                    const res = await fetch('http://localhost:8080/rest-api/admin/employees');
//                    if (!res.ok) throw new Error("Ошибка сервера");
//                    employees.value = await res.json();
//                    console.log("Сотрудники загружены:", employees.value);
//                } catch (e) {
//                    console.error("Ошибка при получении сотрудников:", e);
//                }

        };

        const handleCreateEmployee = async () => {
            await fetch('http://localhost:8080/rest-api/admin/employees', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify(newEmployeeForm.value)
            });
            newEmployeeForm.value = { firstName: '', secondName: '', login: '', password: '' };
            await fetchEmployees();
        };

        const handleDeleteEmployee = async (id) => {
            await fetch(`http://localhost:8080/rest-api/admin/employees/${id}`, { method: 'DELETE' });
            await fetchEmployees();
        };


        const clients = ref([]);
        const clientFilter = ref('');
        const clientPage = ref(0);
        const clientTotalPages = ref(1);
        const clientPageSize = ref(10);

//        const fetchClients = async () => {
//            const searchTrimmed = clientFilter.value ? clientFilter.value.trim() : '';
//
//            let url = `http://localhost:8080/rest-api/admin/clients?page=${clientPage.value}&size=${clientPageSize.value}`;
//            if (searchTrimmed) {
//                url += `&surname=${encodeURIComponent(searchTrimmed)}`;
//            }
//
//            try {
//                const res = await fetch(url);
//                if (res.ok) {
//                    const data = await res.json();
//                    // Так как бэкенд теперь возвращает Page, данные берем из content
//                    clients.value = data.content || [];
//                    clientTotalPages.value = data.totalPages || 1;
//                }
//            } catch (err) {
//                console.error("Ошибка при загрузке клиентов:", err);
//            }
//        };
const fetchClients = async () => {
    const searchTrimmed = clientFilter.value ? clientFilter.value.trim() : '';

    let url = `http://localhost:8080/rest-api/admin/clients?page=${clientPage.value}&size=${clientPageSize.value}`;
    if (searchTrimmed) {
        url += `&surname=${encodeURIComponent(searchTrimmed)}`;
    }

    try {
        const res = await fetch(url);
        if (res.ok) {
            const data = await res.json();
//            console.log("Ответ от бэкенда по клиентам:", data);

            clients.value = data.content || [];

            // Защита: вытаскиваем общее количество страниц из любого возможного поля
            if (data.totalPages !== undefined) {
                clientTotalPages.value = Number(data.totalPages);
            } else if (data.total_pages !== undefined) {
                clientTotalPages.value = Number(data.total_pages);
            } else {
                clientTotalPages.value = 1;
            }

//            console.log("Записано в Vue clientTotalPages:", clientTotalPages.value);
        }
    } catch (err) {
//        console.error("Ошибка при загрузке клиентов:", err);
    }
};

        // Функции переключения страниц
        const nextClientPage = () => { if (clientPage.value < clientTotalPages.value - 1) { clientPage.value++; fetchClients(); } };
        const prevClientPage = () => { if (clientPage.value > 0) { clientPage.value--; fetchClients(); } };


        const goToAdminClients = () => {
            currentTab.value = 'admin_clients';
            clientFilter.value = '';
            clientPage.value = 0; // Сброс на первую страницу
            fetchClients();
        };

        const checkAuth = async () => {
            try {
                const res = await fetch('/rest-api/auth/me', {
                    method: 'GET',
                    credentials: 'include' // Обязательно для передачи куки
                });
                if (res.ok) {
                    currentUser.value = await res.json();
                } else {
                    currentUser.value = { id: null, role: '', firstName: '', secondName: '' };
                }
            } catch (e) {
                console.error("Ошибка проверки сессии", e);
            }
        };

        onMounted(() => {
            checkAuth(); // Вызываем при старте
        });

        return {
            books, orders, myOrders, currentYear, currentTab, cart,
            showAuthForms, currentUser, authMessage, authError,
            loginForm, regForm, logout,
            addToCart, removeFromCart, getCountInCart, groupedCart, cartTotal,
            handleCheckout, checkoutStatus, currentUserCartLength,
            goToEmployeeOrders, goToEmployeeBooks, goToMyOrders, handleCompleteOrder,
            handleCreateBook, handleDeleteBook, formatDate,
            handleLogin, handleRegister, newBookForm,
            employees, fetchEmployees, handleCreateEmployee, handleDeleteEmployee, newEmployeeForm,

            clients, clientFilter, fetchClients,
            clientPage, clientTotalPages, clientPageSize,
            nextClientPage, prevClientPage,

            bookPage, bookTotalPages, nextBookPage, prevBookPage,
            goToAdminClients,
            checkAuth, fetchCurrentUser
        };
    }
}).mount('#app');
