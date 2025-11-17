---
marp: true
theme: default
paginate: true
---

# Разработка игр на Java

## Основные концепции и принципы

---

# Содержание

1. Введение в разработку игр на Java
2. Архитектура игровых приложений
3. Основной игровой цикл (Game Loop)
4. Система актёров (Actors)
5. Игровая сцена (Scene)
6. Обработка пользовательских событий
7. Практические примеры

---

# Введение в разработку игр на Java

- Java предлагает множество инструментов для разработки игр
- Некоторые библиотеки и фреймворки:
  - JavaFX
  - LWJGL (Lightweight Java Game Library)
- Кроссплатформенность и объектно-ориентированный подход
- Автоматическое управление памятью (сборщик мусора)

---

# Архитектура игровых приложений

Типичная игра на Java включает:

- **Game Engine** — ядро, управляющее всеми системами
- **Game Loop** — основной цикл обновления и отрисовки
- **Assets Manager** — управление ресурсами (изображения, звуки)
- **Entity System** — управление игровыми объектами
- **Renderer** — отрисовка игровых объектов
- **Input Handler** — обработка пользовательского ввода
- **Physics Engine** — обработка физики и коллизий

---

# Основной игровой цикл (Game Loop)

Сердце любой игры — цикл, который непрерывно выполняется, пока игра запущена:

```java
public void run() {
    long lastTime = System.nanoTime();
    double amountOfTicks = 60.0; // Частота обновлений (тиков) в секунду
    double ns = 1000000000 / amountOfTicks;
    double delta = 0;

    while (running) {
        long now = System.nanoTime();
        delta += (now - lastTime) / ns;
        lastTime = now;

        // Обновляем состояние игры с фиксированной частотой
        while (delta >= 1) {
            update(); // Обновляем логику игры
            delta--;
        }

        render(); // Отрисовываем текущее состояние
    }
}
```

# Основной игровой цикл (Game Loop - Fixed FPS)

Сердце любой игры — цикл, который непрерывно выполняется, пока игра запущена.
Вариант с фиксированной частотой кадров:

```java
public void run() {
    final int TARGET_FPS = 60;
    final long OPTIMAL_TIME = 1000000000 / TARGET_FPS;
    long lastUpdateTime = System.nanoTime();
    long lastRenderTime;

    // Счетчики для мониторинга производительности
    int fps = 0;
    long timer = System.currentTimeMillis();

    while (running) {
        long currentTime = System.nanoTime();
        long updateDelta = currentTime - lastUpdateTime;
        lastUpdateTime = currentTime;

        // Преобразуем наносекунды в секунды для более удобной работы
        float deltaTime = updateDelta / 1_000_000_000.0f;

        // Обновляем игровую логику с учетом прошедшего времени
        update(deltaTime);

        // Отрисовываем текущее состояние
        render();
        fps++;

        // Ожидаем, чтобы поддерживать постоянный FPS
        lastRenderTime = System.nanoTime() - currentTime;
        if (lastRenderTime < OPTIMAL_TIME) {
            try {
                Thread.sleep((OPTIMAL_TIME - lastRenderTime) / 1000000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Выводим частоту кадров каждую секунду
        if (System.currentTimeMillis() - timer > 1000) {
            System.out.println("FPS: " + fps);
            fps = 0;
            timer = System.currentTimeMillis();
        }
    }
}
```

Этот подход обеспечивает стабильную частоту кадров, что важно для:

- Плавности анимации
- Предсказуемого поведения игры на разных устройствах
- Снижения нагрузки на CPU

---

# Основной игровой цикл: подробнее

## Функции Game Loop:

1. **Обновление состояния игры (update):**

   - Обновление позиций объектов
   - Обработка пользовательского ввода
   - Обнаружение коллизий
   - Обновление AI и других систем

2. **Отрисовка (render):**
   - Отрисовка всех объектов в текущем состоянии

---

# Основной игровой цикл: реализация

```java
public class Game implements Runnable {
    private Scene currentScene;
    private boolean running = false;

    public void update() {
        // Обработка ввода
        inputHandler.processInput();

        // Обновление текущей сцены и всех её объектов
        currentScene.update();

        // Проверка коллизий
        collisionSystem.checkCollisions();
    }

    public void render() {
        // Очистка экрана
        renderer.clear();

        // Отрисовка текущей сцены
        currentScene.render(renderer);

        // Отображение результата
        renderer.display();
    }
}
```

---

# Система актёров (Actors)

**Актёр** — любой интерактивный объект в игре:

- Персонаж игрока
- Враги
- Препятствия
- Интерактивные элементы

```java
public abstract class Actor {
    protected Vector2 position;
    protected Vector2 velocity;
    protected float width, height;

    public abstract void update(float deltaTime);
    public abstract void render(Renderer renderer);
    public abstract void handleInput(InputHandler inputHandler);

    public Rectangle getBounds() {
        return new Rectangle(position.x, position.y, width, height);
    }
}
```

---

# Система актёров: иерархия

Распространенная иерархия актеров:

```
Actor (абстрактный)
  ├── Entity (базовый класс для физических объектов)
  │     ├── Player (игрок)
  │     ├── Enemy (враг)
  │     └── Item (предмет)
  ├── StaticObject (неподвижные объекты)
  │     ├── Platform (платформа)
  │     └── Obstacle (препятствие)
  └── UIElement (элементы интерфейса)
        ├── Button (кнопка)
        └── Label (метка)
```

---

# Пример реализации актёра: Player

```java
public class Player extends Actor {
    private int health = 100;
    private float speed = 5.0f;

    @Override
    public void update(float deltaTime) {
        // Обновляем позицию на основе скорости
        position.x += velocity.x * speed * deltaTime;
        position.y += velocity.y * speed * deltaTime;

        // Дополнительная логика обновления
    }

    @Override
    public void handleInput(InputHandler input) {
        // Устанавливаем скорость на основе нажатых клавиш
        velocity.x = 0;
        velocity.y = 0;

        if (input.isKeyPressed(KeyCode.RIGHT)) velocity.x = 1;
        if (input.isKeyPressed(KeyCode.LEFT)) velocity.x = -1;
        if (input.isKeyPressed(KeyCode.UP)) velocity.y = -1;
        if (input.isKeyPressed(KeyCode.DOWN)) velocity.y = 1;
    }

    @Override
    public void render(Renderer renderer) {
        renderer.drawSprite(sprite, position);
        // Дополнительная логика отрисовки
    }
}
```

---

# Игровая сцена (Scene)

**Сцена** — контейнер для актёров и других игровых объектов:

- Управляет всеми объектами в определенной части игры
- Хранит списки актеров и обновляет их состояние
- Может представлять разные уровни, меню или экраны игры

```java
public class Scene {
  private List<Actor> actors = new ArrayList<>();
  private Camera camera;

  public void addActor(Actor actor) {
    actors.add(actor);
  }

  public void update(float deltaTime) {
    for (Actor actor : actors) {
      actor.update(deltaTime);
    }
  }

  public void render(Renderer renderer) {
    renderer.setCamera(camera);
    for (Actor actor : actors) {
      actor.render(renderer);
    }
  }
}
```

---

# Сцены и состояния игры

Игра может содержать несколько сцен:

```java
public class GameStateManager {
    private Map<String, Scene> scenes = new HashMap<>();
    private Scene currentScene;

    public void addScene(String name, Scene scene) {
        scenes.put(name, scene);
    }

    public void switchScene(String name) {
        if (scenes.containsKey(name)) {
            currentScene = scenes.get(name);
            currentScene.init(); // Инициализация при переключении
        }
    }

    public void update(float deltaTime) {
        if (currentScene != null) {
            currentScene.update(deltaTime);
        }
    }

    public void render(Renderer renderer) {
        if (currentScene != null) {
            currentScene.render(renderer);
        }
    }
}
```

---

# Обработка пользовательских событий

Обработка ввода — важная часть любой игры:

```java
public class InputHandler {
    private Set<Integer> pressedKeys = new HashSet<>();
    private Set<Integer> justPressedKeys = new HashSet<>();
    private Vector2 mousePosition = new Vector2();
    private boolean mousePressed = false;

    public void keyPressed(int keyCode) {
        pressedKeys.add(keyCode);
        justPressedKeys.add(keyCode);
    }

    public void keyReleased(int keyCode) {
        pressedKeys.remove(keyCode);
    }

    public boolean isKeyPressed(int keyCode) {
        return pressedKeys.contains(keyCode);
    }

    public boolean isKeyJustPressed(int keyCode) {
        return justPressedKeys.contains(keyCode);
    }

    // Обработка мыши...

    public void update() {
        // Очищаем кнопки, нажатые в текущем кадре
        justPressedKeys.clear();
    }
}
```

---

# Шаблон наблюдателя для событий

Альтернативный подход с использованием шаблона Observer:

```java
public interface InputListener {
    void onKeyPressed(int keyCode);
    void onKeyReleased(int keyCode);
    void onMouseMoved(int x, int y);
    void onMousePressed(int button, int x, int y);
    void onMouseReleased(int button, int x, int y);
}

public class InputSystem {
    private List<InputListener> listeners = new ArrayList<>();

    public void addListener(InputListener listener) {
        listeners.add(listener);
    }

    public void keyPressed(int keyCode) {
        for (InputListener listener : listeners) {
            listener.onKeyPressed(keyCode);
        }
    }

    // Другие методы обработки...
}
```

---

# Практический пример: простая игра

Структура простой 2D-игры на Java:

```java
public class SimpleGame {
    private GameLoop gameLoop;
    private Scene gameScene;
    private Player player;
    private InputHandler inputHandler;
    private Renderer renderer;

    public SimpleGame() {
        // Инициализация компонентов
        renderer = new Renderer(800, 600);
        inputHandler = new InputHandler();
        gameScene = new Scene();

        // Создание игрока
        player = new Player(400, 300);
        gameScene.addActor(player);

        // Добавление врагов и других объектов
        for (int i = 0; i < 5; i++) {
            Enemy enemy = new Enemy(Math.random() * 800, Math.random() * 600);
            gameScene.addActor(enemy);
        }

        // Настройка игрового цикла
        gameLoop = new GameLoop(this);
    }

    public void start() {
        gameLoop.start();
    }

    public void update() {
        inputHandler.update();
        player.handleInput(inputHandler);
        gameScene.update();
    }

    public void render() {
        renderer.clear();
        gameScene.render(renderer);
        renderer.display();
    }
}
```

---

# Итоги

- **Game Loop** - основной цикл игры, который управляет обновлением и отрисовкой
- **Actors** - объекты игрового мира с собственным поведением
- **Scene** - контейнер для объектов, связанных с определенным уровнем или экраном
- **Input Handling** - обработка пользовательских событий для взаимодействия с игрой

**Ключевые принципы:**

- Разделение логики и отображения
- Компонентная архитектура
- Использование паттернов проектирования
- Фиксированный и переменный временной шаг

---

# Рекомендуемые ресурсы

- **Библиотеки:** LibGDX, LWJGL, JavaFX
- **Книги:**
  - "Game Programming Patterns" by Robert Nystrom
    https://gameprogrammingpatterns.com/
  - "Developing Games in Java" by David Brackeen
- **Онлайн-курсы:**
  - Java Game Development на Udemy
  - Java Programming Masterclass на Coursera

---

# Вопросы?

Спасибо за внимание!
