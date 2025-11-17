<!-- marp: true -->
<!-- theme: default -->
<!-- paginate: true -->
<!-- backgroundColor: white -->

# Рефлексия и аннотации в Java

## Метаданные классов, динамическое создание объектов, аннотации, проксирование

---

## Содержание

1. Введение в рефлексию
2. Метаданные классов и их получение
3. Динамическое создание и использование объектов
4. Введение в аннотации
5. Создание и обработка собственных аннотаций
6. Проксирование и динамические прокси
7. Примеры использования и практические кейсы

---

## Введение в рефлексию

**Рефлексия** - механизм исследования данных о программе во время её выполнения.

Возможности рефлексии:

- Исследование классов, полей, методов, конструкторов
- Создание экземпляров классов во время выполнения
- Вызов методов и доступ к полям во время выполнения
- Модификация значений полей, включая приватные

Пакет: `java.lang.reflect`

---

## Преимущества и недостатки рефлексии

**Преимущества:**

- Гибкость в разработке фреймворков и библиотек
- Динамическое взаимодействие с кодом
- Поддержка плагинов и расширений
- Тестирование и мокирование

**Недостатки:**

- Потеря проверки типов на этапе компиляции
- Снижение производительности
- Нарушение инкапсуляции
- Усложнение поддержки кода

---

## Класс Class<T>

Центральный класс для работы с рефлексией - `Class<T>`:

```java
// Получение объекта Class
Class<?> stringClass = String.class;
Class<?> listClass = new ArrayList<String>().getClass();
Class<?> mapClass = Class.forName("java.util.HashMap");

// Информация о классе
String className = stringClass.getName(); // "java.lang.String"
boolean isInterface = listClass.isInterface(); // false
boolean isAbstract = Modifier.isAbstract(mapClass.getModifiers());
```

---

## Метаданные классов

Типы метаданных, доступных через рефлексию:

1. **Основная информация о классе:**

- Имя, пакет, модификаторы, родительский класс
- Реализуемые интерфейсы, вложенные классы

2. **Члены класса:**

- Поля (Fields)
- Методы (Methods)
- Конструкторы (Constructors)
- Аннотации (Annotations)

---

## Получение полей класса

```java
public class Person {
   public String name;
   private int age;
   protected boolean active;
}

// Получение полей
Class<?> personClass = Person.class;

// Все публичные поля
Field[] publicFields = personClass.getFields();

// Все поля, включая приватные
Field[] allFields = personClass.getDeclaredFields();

// Конкретное поле
Field ageField = personClass.getDeclaredField("age");
ageField.setAccessible(true); // Для доступа к приватному полю
```

---

## Получение методов и конструкторов

```java
// Получение методов
Method[] publicMethods = personClass.getMethods(); // + унаследованные
Method[] declaredMethods = personClass.getDeclaredMethods(); // только свои
Method method = personClass.getDeclaredMethod("setAge", int.class);

// Получение конструкторов
Constructor<?>[] constructors = personClass.getConstructors();
Constructor<?> constructor = personClass.getConstructor(String.class, int.class);
```

---

## Анализ модификаторов

```java
Field field = personClass.getDeclaredField("age");
int modifiers = field.getModifiers();

boolean isPublic = Modifier.isPublic(modifiers);
boolean isPrivate = Modifier.isPrivate(modifiers);
boolean isFinal = Modifier.isFinal(modifiers);
boolean isStatic = Modifier.isStatic(modifiers);

String modText = Modifier.toString(modifiers); // "private"
```

---

## Динамическое создание объектов

Два основных способа:

**1. Через конструктор:**

```java
Constructor<Person> constructor = Person.class.getConstructor(String.class);
Person person = constructor.newInstance("John");
```

**2. Через метод Class.newInstance() (устаревший):**

```java
// Требует публичного конструктора без параметров
Person person = Person.class.newInstance(); // Deprecated
```

---

## Работа с полями и методами

```java
// Работа с полями
Person person = new Person();
Field nameField = Person.class.getField("name");
nameField.set(person, "Alice");
String name = (String) nameField.get(person);

// Работа с методами
Method setAgeMethod = Person.class.getDeclaredMethod("setAge", int.class);
setAgeMethod.setAccessible(true);
setAgeMethod.invoke(person, 25);

Method getAgeMethod = Person.class.getDeclaredMethod("getAge");
getAgeMethod.setAccessible(true);
int age = (int) getAgeMethod.invoke(person);
```

---

## Примеры использования рефлексии

1. **ORM-системы**: Hibernate, JPA - отображение объектов в БД
2. **DI-контейнеры**: Spring - внедрение зависимостей
3. **Сериализация/десериализация**: Jackson, GSON
4. **Тестовые фреймворки**: JUnit
5. **Плагины и расширения**

---

## Введение в аннотации

**Аннотации** - специальные метки, добавляемые к элементам кода для предоставления дополнительной информации.

```java
@Override // Стандартная аннотация
public String toString() {
   return "Hello";
}

@Deprecated // Пометка устаревшего API
public void oldMethod() {}

@SuppressWarnings("unchecked") // Подавление предупреждений
List list = new ArrayList();
```

---

## Типы аннотаций по времени действия

Определяются meta-аннотацией `@Retention`:

1. **SOURCE** - присутствуют только в исходном коде

```java
@Retention(RetentionPolicy.SOURCE)
```

2. **CLASS** - сохраняются в class-файлах, но недоступны в runtime

```java
@Retention(RetentionPolicy.CLASS)
```

3. **RUNTIME** - доступны во время выполнения через рефлексию

```java
@Retention(RetentionPolicy.RUNTIME)
```

---

## Области применения аннотаций

Определяются meta-аннотацией `@Target`:

```java
@Target(ElementType.TYPE) // Для классов, интерфейсов, перечислений
@Target(ElementType.FIELD) // Для полей
@Target(ElementType.METHOD) // Для методов
@Target(ElementType.CONSTRUCTOR) // Для конструкторов
@Target(ElementType.PARAMETER) // Для параметров методов
@Target({ElementType.FIELD, ElementType.METHOD}) // Несколько целей
```

---

## Создание собственных аннотаций

```java
// Определение аннотации
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Test {
   String description() default "No description";
   boolean enabled() default true;
   Class<? extends Exception>[] expected() default {};
}

// Использование
@Test(description = "Тест метода вычитания", enabled = true)
public void testSubtract() {
   // код теста
}
```

---

## Обработка аннотаций через рефлексию

```java
// Проверка наличия аннотации
Method method = obj.getClass().getMethod("testSubtract");
if (method.isAnnotationPresent(Test.class)) {
   // Получение аннотации
   Test testAnnotation = method.getAnnotation(Test.class);

   // Получение атрибутов
   String description = testAnnotation.description();
   boolean enabled = testAnnotation.enabled();

   if (enabled) {
      System.out.println("Running test: " + description);
      method.invoke(obj);
   }
}
```

---

## Пример использования: собственный тестовый фреймворк

```java
public class SimpleTestRunner {
   public static void runTests(Class<?> testClass) throws Exception {
      Object instance = testClass.getDeclaredConstructor().newInstance();

      for (Method method : testClass.getDeclaredMethods()) {
        if (method.isAnnotationPresent(Test.class)) {
           Test test = method.getAnnotation(Test.class);

           if (test.enabled()) {
              try {
                method.invoke(instance);
                System.out.println("✓ " + method.getName() + " passed");
              } catch (Exception e) {
                System.out.println("✗ " + method.getName() + " failed: " + e.getCause());
              }
           }
        }
      }
   }
}
```

---

## Проксирование

**Прокси** - объект, который действует как посредник между клиентом и реальным объектом.

**Применение:**

- Отложенная инициализация
- Логирование, кэширование
- Контроль доступа
- Реализация транзакций
- AOP (Аспектно-ориентированное программирование)

---

## Виды прокси в Java

1. **Статические прокси:**

- Реализация того же интерфейса
- Композиция с реальным объектом
- Код пишется вручную

2. **Динамические прокси:**

- Создаются во время выполнения
- Используют `java.lang.reflect.Proxy`
- Работают только с интерфейсами

3. **Библиотеки генерации байткода:**

- CGLIB, Javassist, ByteBuddy
- Могут создавать прокси для классов

---

## Динамические прокси в Java

```java
// Интерфейс
public interface UserService {
   User findById(long id);
   void save(User user);
}

// Обработчик вызовов
public class LoggingInvocationHandler implements InvocationHandler {
   private final Object target;

   public LoggingInvocationHandler(Object target) {
      this.target = target;
   }

   @Override
   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      System.out.println("Before method: " + method.getName());
      Object result = method.invoke(target, args);
      System.out.println("After method: " + method.getName());
      return result;
   }
}
```

---

## Создание и использование динамического прокси

```java
// Создание прокси
UserService userService = new UserServiceImpl();
InvocationHandler handler = new LoggingInvocationHandler(userService);

UserService proxy = (UserService) Proxy.newProxyInstance(
   UserService.class.getClassLoader(),
   new Class<?>[] { UserService.class },
   handler
);

// Использование прокси
User user = proxy.findById(42); // Вызов перехватывается обработчиком
```

---

## Пример AOP с динамическими прокси

```java
public class TransactionInvocationHandler implements InvocationHandler {
   private final Object target;
   private final TransactionManager txManager;

   @Override
   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
      if (method.isAnnotationPresent(Transactional.class)) {
        Transaction tx = txManager.begin();
        try {
           Object result = method.invoke(target, args);
           tx.commit();
           return result;
        } catch (Exception e) {
           tx.rollback();
           throw e;
        }
      }
      return method.invoke(target, args);
   }
}
```

---

## Заключение

**Рефлексия и аннотации** - мощные инструменты для:

- Создания гибких фреймворков
- Динамического анализа и модификации кода
- Метапрограммирования
- Внедрения зависимостей
- Аспектно-ориентированного программирования

**Основной принцип:** использовать с осторожностью, только когда это действительно необходимо.

---

## Ресурсы для изучения

- [Java Reflection API Tutorial](https://www.oracle.com/technical-resources/articles/java/javareflection.html)
- [Java Annotations Tutorial](https://docs.oracle.com/javase/tutorial/java/annotations/)
- [Dynamic Proxy Classes](https://docs.oracle.com/javase/8/docs/technotes/guides/reflection/proxy.html)
- Книга: "Java Reflection in Action" by Ira Forman and Nate Forman
- Фреймворки, активно использующие рефлексию: Spring, Hibernate
