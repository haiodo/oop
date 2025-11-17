---
marp: true
paginate: true
theme: default
# Введение в Java: Потоки, Мониторы и Примитивы Синхронизации
---

## Оглавление

<style>
li {
  font-size: 0.8em;
}
</style>

1. Введение в многопоточность
2. Создание потоков в Java
3. Жизненный цикл потока
4. Использование интерфейса Runnable и расширения класса Thread
5. Синхронизация потоков: основы
6. Проблемы многопоточности
7. Мониторы в Java
8. Устройство монитора в Java
9. Использование ключевого слова synchronized
10. Практическое применение synchronized методов и блоков

---

## Введение в многопоточность

- **Что такое многопоточность?**  
  Многопоточность — это возможность программы выполнять несколько задач параллельно, используя несколько ядер процессора, или конкурентно — чередуя выполнение на одном ядре.  
  Например: обработка пользовательского интерфейса и выполнение вычислений параллельно.

- **Преимущества:**
  - Эффективное использование ресурсов процессора.
  - Улучшение производительности приложений.
  - Возможность реализации реальных времени систем.

---

## Создание потоков в Java

### Через расширение класса Thread:

```java
class MyThread extends Thread {
    public void run() {
        System.out.println("Этот код выполняется в новом потоке.");
    }
}
MyThread thread = new MyThread();
thread.start(); // Поток начинает выполнение
```

---

### Через реализацию интерфейса Runnable:

```java
class MyRunnable implements Runnable {
    public void run() {
        System.out.println("Этот код также выполняется в новом потоке.");
    }
}

Thread thread = new Thread(new MyRunnable());
thread.start();
```

**Рекомендации:**

- Используйте `Runnable`, если класс уже наследует другие **классы**.
- `Thread` удобен для простых случаев.

---

### Cоздание через of методы

```java
class MyClass {
    public void run() {
        Thread.ofPlatform().unstarted(() -> {
            System.out.println("Hello World!");
        });

        Thread.ofPlatform().start(() -> {
            System.out.println("Hello World!");
        });
    }
}
```

---

### Виртуальные потоки и потоки платформы

- С Java 21 появились виртуальные потоки.
- Виртуальные потоки выполняются в кооперативном режиме с учетом ввода/вывода и операций синхронизации на мониторах/операторах ожидания, выполняются в пуле потоков операционной системы которые контролируются самой JVM.
- Практически бесплатные с точки зрения создания/удаления.

---

## Жизненный цикл потока

```mermaid
stateDiagram-v2
  [*] --> New : Создание объекта Thread
  New --> Runnable : Вызов start()
  Runnable --> Blocked : Ожидание ресурсов
  Runnable --> Waiting : Ожидание события (wait())
  Runnable --> TimedWaiting : Сон (sleep())
  Blocked --> Runnable : Ресурс освобожден
  Waiting --> Runnable : Уведомление (notify() или notifyAll())
  TimedWaiting --> Runnable : Проснулся по таймеру
  [*] <-- Terminated : Завершение потока
```

---

- **Новый (New):** Поток создан, но не запущен.
- **Готовый к выполнению (Runnable):** Поток может начать выполнение.
- **Блокированный (Blocked):** Поток ждет ресурсов.
- **Ожидание (Waiting) / Сон (Timed Waiting):** Поток ожидает события или таймера.
- **Завершенный (Terminated):** Поток завершил выполнение.

---

## Использование интерфейса Runnable и расширения класса Thread

### Интерфейс `Runnable`

```java
@FunctionalInterface
public interface Runnable {
    void run();
}
```

- **Преимущества:**
  - Может быть реализован любым классом.
  - Добавляет гибкость в проектировании.

---

### Класс `Thread`

```java
public class Thread implements Runnable {
    public void start() { ... }
    public void run() { ... }
}
```

- **Преимущества:**
  - Простое создание потоков.
  - Дополнительные методы (getName(), sleep(), etc.).

---

## Синхронизация потоков: основы

### Проблемы без синхронизации:

```java
public class Counter {
    private int count = 0;

    public void increment() {
        count++;
    }
}
```

- **Состояние гонки (Race Condition):**  
  Если два потока одновременно вызовут `increment()`, результат может быть некорректным.

### Решение:

Использование синхронизации для обеспечения атомарности операций.

---

## Проблемы многопоточности

1. **Состояние гонки (Race Condition):**

   - Несколько потоков изменяют одну переменную одновременно.

   ```java
   public class UnsafeCounter {
       private int count = 0;

       public void increment() {
           int temp = count;
           temp++;
           count = temp; // Возможна гонка здесь
       }
   }
   ```

2. **Неправильное опубликование данных (Publishing):**

   - Потоки могут видеть неполные или некорректные данные.

---

3. **Взаимоблокировка (Deadlock):**

   ```java
   class DeadlockExample {
       private final Object lock1 = new Object();
       private final Object lock2 = new Object();

       void methodA() {
           synchronized(lock1) { // Поток A блокирует lock1
               try { Thread.sleep(100); } catch (InterruptedException e) {}
               synchronized(lock2) { // Поток A ожидает lock2, занятый потоком B
                   // ...
               }
           }
       }

       void methodB() {
           synchronized(lock2) { // Поток B блокирует lock2
               try { Thread.sleep(100); } catch (InterruptedException e) {}
               synchronized(lock1) { // Поток B ожидает lock1, занятый потоком A
                   // ...
               }
           }
       }
   }
   ```

---

## Мониторы в Java

### Что такое монитор?

- Каждый объект в Java имеет ассоциированный монитор.
- Монитор управляет доступом к ресурсам, предотвращая одновременное изменение их несколькими потоками.

### Основные методы:

- `synchronized`: Обеспечивает синхронизацию методов или блоков кода.
- `wait()`, `notify()`, `notifyAll()`: Управление ожиданием и уведомлением потоков.

---

## Устройство монитора в Java

### Состояния:

1. **Свободен (Unlocked):** Никто не использует монитор.
2. **Заблокирован (Locked):** Один поток владеет монитором.

### Очередь ожидания:

- Потоки, пытающиеся получить доступ к заблокированному монитору, добавляются в очередь ожидания.
- Монитор поддерживает методы `wait()`, `notify()` и `notifyAll()`, которые управляют этой очередью.

---

## Использование ключевого слова synchronized

### Синхронизированный метод:

```java
public class Counter {
    private int count = 0;

    public synchronized void increment() {
        count++;
    }
}
```

---

### Синхронизированный блок:

```java
public class AnotherCounter {
    private int count = 0;
    private final Object lock = new Object();

    public void increment() {
        synchronized (lock) { // Используем lock в качестве монитора
            count++;
        }
    }
}
```

---

### Рекомендации:

- Используйте синхронизацию на уровне метода, если это возможно.
- Для сложных сценариев используйте блокировку объектов.

---

## Практическое применение synchronized

### Пример: Thread-safe счетчик

```java
public class SafeCounter {
    private int count = 0;

    public synchronized void increment() {
        count++;
    }

    public synchronized int getCount() {
        return count;
    }
}
```

---

### Как это работает:

- Только один поток может вызвать `increment()` или `getCount()` одновременно.
- Это предотвращает состояние гонки.

---

## Взаимоблокировки (Deadlock)

### Причины deadlock'а:

1. **Зависимость ресурсов:** Потоки ждут несколько ресурсов друг от друга.
2. **Неправильный порядок блокировок:** Потоки запрашивают ресурсы в разных порядках.

---

### Пример deadlock'а:

```java
public class DeadlockExample {
    private Object lock1 = new Object();
    private Object lock2 = new Object();

    public void methodA() {
        synchronized(lock1) { // Поток A блокирует lock1
            try { Thread.sleep(100); } catch (InterruptedException e) {}
            synchronized(lock2) { // Поток A ожидает lock2, занятый потоком B
                // ...
            }
        }
    }

    public void methodB() {
        synchronized(lock2) { // Поток B блокирует lock2
            try { Thread.sleep(100); } catch (InterruptedException e) {}
            synchronized(lock1) { // Поток B ожидает lock1, занятый потоком A
                // ...
            }
        }
    }
}
```

---

## Как избежать взаимоблокировок

### Рекомендации:

1. **Упорядочивайте доступ к ресурсам:** Все потоки должны запрашивать ресурсы в одном и том же порядке.
   ```java
   synchronized(lock1) {
       synchronized(lock2) {
           // ...
       }
   }
   ```
2. **Используйте таймауты при ожидании:**  
   Метод `wait(long timeout)` позволяет потоку проснуться, если ресурс не освободился в течение заданного времени.

---

3. **Минимизируйте время блокировки:**  
   Не выполняйте длительных операций внутри синхронизированных блоков.

4. **Используйте современные механизмы синхронизации:**  
   Например, `ReentrantLock` из пакета `java.util.concurrent`.

---

## Примитивы синхронизации: wait(), notify() и notifyAll()

### Методы монитора:

- `wait()`: Поток переходит в состояние ожидания до тех пор, пока не будет вызван `notify()` или `notifyAll()`.
- `notify()`: Разбуживает один поток из очереди ожидания.
- `notifyAll()`: Разбуживает все потоки из очереди ожидания.

---

### Пример использования:

```java
public class ProducerConsumer {
    private int product;
    private boolean available = false;

    public synchronized void produce(int product) {
        while (available) { // Проверка условия
            try {
                wait(); // Ожидаем, пока потребитель не заберет продукт
            } catch (InterruptedException e) {}
        }
        this.product = product;
        available = true;
        notifyAll();
    }

    public synchronized int consume() {
        while (!available) { // Проверка условия
            try {
                wait(); // Ожидаем, пока производитель не создаст продукт
            } catch (InterruptedException e) {}
        }
        available = false;
        return product;
    }
}
```

---

## Правила использования wait() и notify()

1. **Вызывайте только внутри синхронизированных блоков:**

   ```java
   synchronized(lock) {
       lock.wait();
   }
   ```

2. **Используйте цикл проверки условия после выхода из ожидания:**

   ```java
   synchronized(lock) {
       while (!condition) { // Проверка условия в цикле
           lock.wait();
       }
       // Логика при выполнении условия
   }
   ```

3. **Никогда не вызывайте `notify()` без проверки состояния:**  
   Убедитесь, что уведомление соответствует ожиданию.

---

## Разница между sleep() и yield()

### `sleep(long millis)`

- Приостанавливает текущий поток на заданное количество миллисекунд.
- Монитор не освобождается: другие потоки не могут использовать ресурс, если текущий поток заблокировал его.

```java
try {
    Thread.sleep(100); // Поток спит 100ms
} catch (InterruptedException e) {}
```

### `yield()`

- Рекомендует планировщику переключить контекст на другой поток.
- Не гарантирует переключение: зависит от реализации JVM.

```java
Thread.yield(); // Рекомендуемый сигнал для переключения
```

### Когда использовать:

- `sleep()`: Для задержки выполнения текущего потока.
- `yield()`: Для уступки времени другим потокам в ситуации, когда ваш поток может ждать.

---

## Использование Executor Framework для управления потоками

### Преимущества:

1. **Масштабируемость:** Легко изменять количество потоков.
2. **Упрощение кода:** Пул потоков автоматизирует управление ресурсами.
3. **Возможность отмены задач:** С помощью `Future`.

---

### Пример использования:

```java
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutorExample {
    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(2); // Пул с 2 потоками

        executor.submit(new RunnableTask()); // Добавляем задачу в пул
        executor.submit(new CallableTask());

        executor.shutdown(); // Завершаем пул после выполнения всех задач
    }
}
```

---

## Callable и Future в Java

### `Callable<V>` (интерфейс)

- Аналог `Runnable`, но может возвращать результат.
- Может выбрасывать исключения.

```java
import java.util.concurrent.Callable;

public class CallableTask implements Callable<Integer> {
    @Override
    public Integer call() throws Exception {
        return 42; // Возвращаемый результат
    }
}
```

---

### `Future<V>` (класс)

- Представляет асинхронный расчет.
- Может получить результат, отменить задачу или проверить статус завершения.

```java
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;

public class FutureExample {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Integer> future = executor.submit(new CallableTask());
        // Ожидаем и получаем результат
        Integer result = future.get();
        System.out.println("Результат: " + result);
        executor.shutdown();
    }
}
```

---

## Задачи, которые выполняются в пуле потоков

### Пример: Пул для обработки задач

```java
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPoolExample {
    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(4); // Пул из 4 потоков

        // Добавляем задачи в пул
        for (int i = 0; i < 10; i++) {
            executor.submit(() -> System.out.println("Задача выполнена в потоке " + Thread.currentThread().getName()));
        }

        executor.shutdown(); // Завершаем пул после выполнения всех задач
    }
}
```

---

### Рекомендации:

- Используйте `ExecutorService` для управления группами потоков.
- Для длинных задач используйте пулы с большим количеством потоков.
- Для коротких и легких задач используйте фиксированный размер пула.

---

## Пример многопоточного приложения

### Thread-safe счетчик

```java
public class SafeCounter {
    private int count = 0;
    public synchronized void increment() { count++;}
    public synchronized int getCount() { return count; }
}
public class CounterTest {
    public static void main(String[] args) throws InterruptedException {
        SafeCounter counter = new SafeCounter();
        Thread thread1 = new Thread(() -> {
          for (int i = 0; i < 1000; i++) { counter.increment(); }
        });
        Thread thread2 = new Thread(() -> {
          for (int i = 0; i < 1000; i++) { counter.increment();}
        });
        thread1.start(); thread2.start();
        thread1.join(); thread2.join(); // Дожидаемся завершения потоков
        System.out.println("Итоговое значение: " + counter.getCount());
        // Выведет 2000
    }
}
```

---

## Заключение

- **Многопоточность** позволяет эффективно использовать ресурсы и улучшать производительность приложений.
- **Синхронизация потоков** необходима для предотвращения состояний гонки, deadlock'ов и других проблем.
- Используйте современные механизмы синхронизации (`synchronized`, `wait()`, `notify()`), а также `Executor Framework` для управления группами потоков.

Спасибо за внимание! 😊
