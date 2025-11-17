---
marp: true
theme: default
paginate: true
---

# Model-View-Controller в Java

---

# Слайд 1: Что такое MVC?

- **Model-View-Controller** (MVC) - это архитектурный шаблон проектирования
- Разделяет приложение на три взаимосвязанных компонента
- Обеспечивает разделение ответственности
- Способствует модульности и повторному использованию кода

---

# Слайд 2: Компоненты MVC

- **Model (Модель)**: Данные приложения и бизнес-логика
- **View (Представление)**: Отображение данных пользователю
- **Controller (Контроллер)**: Обработка действий пользователя

---

# Слайд 3: Преимущества MVC

- Четкое разделение ответственности
- Параллельная разработка (разные команды могут работать над моделью, представлением и контроллером)
- Повышенная гибкость и модульность
- Легче поддерживать и тестировать код
- Повторное использование компонентов

---

# Слайд 4: Модель (Model)

- Содержит основные данные приложения
- Реализует бизнес-логику
- Не зависит от представления и контроллера
- Уведомляет представления об изменениях (паттерн Observer)

```java
public class UserModel {
  private String name;
  private String email;

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }

  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }
}
```

---

# Слайд 5: Представление (View)

- Отображает данные пользователю
- Получает данные от модели
- Отправляет действия пользователя контроллеру
- В Java может быть реализовано с помощью Swing, JavaFX или HTML

```java
public class UserView {
  public void displayUserDetails(String userName, String userEmail) {
    System.out.println("User Details:");
    System.out.println("Name: " + userName);
    System.out.println("Email: " + userEmail);
  }

  public String getUserInputName() {
    // Код для получения ввода от пользователя
    return "John Doe"; // Упрощенный пример
  }
}
```

---

# Слайд 6: Контроллер (Controller)

- Обрабатывает события от представления
- Обновляет модель в соответствии с действиями пользователя
- Выбирает подходящее представление для отображения
- Обеспечивает связь между Model и View

```java
public class UserController {
  private UserModel model;
  private UserView view;

  public UserController(UserModel model, UserView view) {
    this.model = model;
    this.view = view;
  }

  public void updateUserName(String name) {
    model.setName(name);
  }

  public void updateView() {
    view.displayUserDetails(model.getName(), model.getEmail());
  }
}
```

---

# Слайд 7: Взаимодействие компонентов MVC

1. Пользователь выполняет действие (например, нажимает кнопку)
2. Контроллер обрабатывает запрос пользователя
3. Контроллер обновляет модель
4. Модель уведомляет представление об изменениях
5. Представление обновляет интерфейс

---

# Слайд 8: Реализация MVC в Java

Простой пример реализации MVC:

```java
public class MVCPatternDemo {
   public static void main(String[] args) {
    // Получение данных модели из базы данных
    UserModel model = retrieveUserFromDatabase();

    // Создание представления для отображения данных
    UserView view = new UserView();

    // Создание контроллера
    UserController controller = new UserController(model, view);

    // Обновить представление с данными из модели
    controller.updateView();

    // Обновить данные модели через контроллер
    controller.updateUserName("Alice");

    // Отобразить обновленные данные
    controller.updateView();
   }

   private static UserModel retrieveUserFromDatabase() {
    UserModel user = new UserModel();
    user.setName("Bob");
    user.setEmail("bob@example.com");
    return user;
   }
}
```

---

# Слайд 9: MVC в веб-приложениях Java

В веб-приложениях Java MVC часто реализуется с использованием:

- **Модель**: Java-классы (POJO)
- **Представление**: JSP, HTML
- **Контроллер**: Сервлеты или классы контроллеров Spring MVC

```java
@Controller
public class UserController {
  @Autowired
  private UserService userService;

  @GetMapping("/user/{id}")
  public String getUser(@PathVariable Long id, Model model) {
    User user = userService.getUserById(id);
    model.addAttribute("user", user);
    return "user-details"; // имя представления
  }
}
```

---

# Слайд 10: MVC в JavaFX

JavaFX предлагает встроенную поддержку для MVC:

- **Модель**: Java-классы с данными
- **Представление**: FXML-файлы (определяют UI)
- **Контроллер**: Классы, реализующие логику обработки событий

```java
public class UserController {
  @FXML
  private TextField nameField;

  @FXML
  private TextField emailField;

  private UserModel user;

  public void initialize() {
    user = new UserModel();

    nameField.textProperty().bindBidirectional(user.nameProperty());
    emailField.textProperty().bindBidirectional(user.emailProperty());
  }
}
```

---

# Слайд 11: Применение MVC в Swing

```java
// Model
public class Calculator {
  public int add(int a, int b) {
    return a + b;
  }
}

// View
public class CalculatorView extends JFrame {
  private JTextField num1 = new JTextField(10);
  private JTextField num2 = new JTextField(10);
  private JButton addButton = new JButton("Add");
  private JTextField result = new JTextField(10);

  // Геттеры, конструктор и настройка UI
}

// Controller
public class CalculatorController {
  private Calculator model;
  private CalculatorView view;

  // Логика обработки нажатий на кнопку
}
```

---

# Слайд 12: Вариации MVC

1. **Классический MVC**: Модель уведомляет представления
2. **Passive Model**: Контроллер обновляет и модель, и представление
3. **MVP (Model-View-Presenter)**: Presenter заменяет контроллер
4. **MVVM (Model-View-ViewModel)**: ViewModel связывает модель и представление

---

# Слайд 13: Чистая архитектура с MVC

MVC часто является частью более крупной архитектуры:

- **Внешний слой**: View + Controller
- **Внутренний слой**: Model (Domain Model, Service Layer, Repository)

Преимущества:

- Чистые границы между слоями
- Более изолированное тестирование
- Улучшенная структура кода

---

# Слайд 14: Паттерн Observer в MVC

Для реализации связи между моделью и представлением:

```java
// Observable (Subject)
public class UserModel extends Observable {
  private String name;

  public void setName(String name) {
    this.name = name;
    setChanged();
    notifyObservers();
  }
}

// Observer
public class UserView implements Observer {
  @Override
  public void update(Observable o, Object arg) {
    UserModel model = (UserModel)o;
    // Обновление представления
  }
}
```

---

# Слайд 15: MVC и Spring Framework

Spring MVC - популярная реализация MVC в Java:

```java
@Controller
public class HomeController {
  @Autowired
  private UserService userService;

  @GetMapping("/")
  public String home(Model model) {
    List<User> users = userService.getAllUsers();
    model.addAttribute("users", users);
    return "home";
  }

  @PostMapping("/user/add")
  public String addUser(@ModelAttribute User user) {
    userService.addUser(user);
    return "redirect:/";
  }
}
```

---

# Слайд 16: Тестирование MVC

Модульное тестирование компонентов MVC:

```java
public class UserControllerTest {
  @Test
  public void testUpdateView() {
    // Arrange
    UserModel model = new UserModel();
    model.setName("Test");
    model.setEmail("test@example.com");

    MockUserView view = new MockUserView();
    UserController controller = new UserController(model, view);

    // Act
    controller.updateView();

    // Assert
    assertEquals("Test", view.getLastDisplayedName());
    assertEquals("test@example.com", view.getLastDisplayedEmail());
  }
}
```

---

# Слайд 17: Проблемы MVC

- **Тесная связь**: Контроллеры могут становиться тесно связанными с представлениями
- **Сложность**: Для небольших приложений может быть избыточным
- **Распределение логики**: Иногда сложно решить, где должна быть логика
- **"Толстые" контроллеры**: Контроллеры могут накапливать слишком много ответственности

---

# Слайд 18: MVC vs другие архитектуры

| Архитектура            | Преимущества                                   | Недостатки                         |
| ---------------------- | ---------------------------------------------- | ---------------------------------- |
| **MVC**                | Разделение ответственности, проверена временем | Сложность для маленьких приложений |
| **MVP**                | Лучшая тестируемость представления             | Презентеры часто дублируют код     |
| **MVVM**               | Двустороннее связывание данных                 | Сложность отладки связываний       |
| **Clean Architecture** | Сильная изоляция слоев                         | Дополнительные абстракции          |

---

# Слайд 19: Лучшие практики MVC

- Делайте модели независимыми от UI
- Контроллеры должны быть "тонкими"
- Бизнес-логика должна быть в моделях или сервисах
- Используйте интерфейсы для ослабления связей
- Избегайте прямой связи между моделью и представлением
- Контроллер должен знать о модели и представлении, но не наоборот

---

# Слайд 20: Заключение

- MVC - фундаментальный шаблон проектирования для структурирования кода
- Способствует хорошим практикам программирования
- Применяется в десктоп, веб и мобильных приложениях
- Имеет различные вариации и реализации
- Важен для понимания других архитектурных шаблонов

**Вопросы для изучения:** Как бы вы реализовали MVC в простом приложении для управления списком задач?
