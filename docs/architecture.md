# Архитектура системы координации автоволонтеров «Приют»

**Проект:** `priyut-coordination-bot`  
**Версия документа:** 1.0  
**Статус:** Draft / Working baseline  
**Дата:** 17.10.2026

---

## 1. Назначение документа

Документ описывает архитектуру системы координации автоволонтеров для приюта «Приютино».

Система решает следующие задачи:

- сбор базы автоволононтеров через Telegram-бот;
- модерация волонтеров администратором;
- создание поездок администратором;
- отклик волонтеров на поездки;
- подтверждение или отклонение кандидатуры администратором;
- отмена и перенос поездок;
- напоминания за 24 часа и за 2 часа до поездки;
- двухэтапное завершение поездки;
- хранение истории поездок;
- экспорт истории в CSV;
- соблюдение требований 152-ФЗ: согласие, отзыв согласия, удаление персональных данных;
- мониторинг и метрики для эксплуатации.

Целевой показатель: сокращение времени организации поездки до 5 минут.

---

## 2. Границы проекта

### 2.1. Что входит в систему

- Telegram-бот для волонтеров и администраторов.
- Бэкенд на Java / Spring Boot.
- База данных PostgreSQL 15.
- Миграции базы данных через Flyway.
- Docker и Docker Compose для развертывания.
- Автоматические напоминания о поездках.
- Журналирование согласий и отзывов согласий на обработку ПДн.
- Метрики через Spring Boot Actuator и Prometheus.
- Резервное копирование базы данных.

### 2.2. Что не входит в систему

Согласно ТЗ, в первой версии нет:

- веб-админки;
- мобильного приложения;
- интеграции с картами;
- автоматического подбора волонтера;
- системы рейтингов и черных списков;
- приема платежей и донатов.

---

## 3. Общий контекст системы

```mermaid
flowchart LR
    Volunteer[Волонтёр] --> Telegram[Telegram]
    Admin[Администратор] --> Telegram

    Telegram --> Bot[Telegram Bot Adapter]

    subgraph Application[priyut-coordination-bot]
        Bot --> UseCases[Application Use Cases]
        UseCases --> Domain[Domain Model]
        UseCases --> Persistence[Persistence Adapter]
        Scheduler[Scheduling Adapter] --> UseCases
        Web[Web/Actuator Adapter] --> Metrics[Metrics]
    end

    Persistence --> PostgreSQL[(PostgreSQL 15)]

    Prometheus[Prometheus] --> Metrics
    Backups[Backup Job] --> PostgreSQL
```
## 4. Целевой технологический стек

| Компонент | Технология | Комментарий |
| :--- | :--- | :--- |
| **Язык** | Java 25 | ТЗ допускает Java 21+, проект использует более новую LTS-версию |
| **Фреймворк** | Spring Boot 4.x | При необходимости строгой совместимости возможен откат на Spring Boot 3.x |
| **Сборка** | Gradle Kotlin DSL | Используется version catalog и build-logic |
| **Доступ к БД** | Spring JDBC | Осознанный отказ от JPA/Hibernate ради прозрачного SQL и меньшего потребления памяти |
| **База данных** | PostgreSQL 15 | Хранение анкет, поездок, истории, согласий |
| **Миграции** | Flyway | Версионированные SQL-миграции |
| **Интерфейс** | Telegram Bot API | Только Telegram, без веб-сайта |
| **Контейнеризация** | Docker + Docker Compose | Развертывание на VPS |
| **Хостинг** | VPS FirstByte, Москва | Требования 152-ФЗ: сервер в РФ |
| **Метрики** | Micrometer + Prometheus + Spring Actuator | Наблюдаемость и эксплуатация |
| **Тесты** | JUnit 5, AssertJ, Mockito, Testcontainers | Юнит и интеграционные тесты |

## 5. Архитектурный стиль

Принят архитектурный стиль: **модульный монолит** с элементами **гексагональной архитектуры** (*ports and adapters*).

#### Причины выбора:
* **Небольшой размер команды** — минимизирует накладные расходы на коммуникацию и синхронизацию.
* **Умеренная нагрузка** — до 500 волонтеров и до 50 поездок в день не требуют сложного масштабирования.
* **Отсутствие избыточных требований** — в ТЗ нет жестких условий по использованию микросервисов.
* **Простота эксплуатации** — система легко разворачивается и администрируется на одном VPS.
* **Гибкость на будущее** — архитектура оставляет возможность при необходимости вынести отдельные модули в самостоятельные сервисы позже.

## 6. Модульная структура проекта

Проект организован по принципу модульного монолита. Ниже представлено назначение каждого модуля:

* **`app`** — точка входа приложения, конфигурация, композиция модулей, сборка executable jar.
* **`domain`** — ядро системы: бизнес-сущности, правила, статусы и доменные события.
* **`application`** — сценарии использования (use cases), входные и выходные порты приложения.
* **`infrastructure`** — внешние адаптеры и технические детали реализации:
    * **`/persistence`** — доступ к PostgreSQL через Spring JDBC, управление схемами и Flyway-миграции.
    * **`/telegram`** — интеграция с Telegram Bot API, обработка команд, кнопок и логика диалогов.
    * **`/scheduling`** — фоновые процессы, отправка напоминаний, архивация и регулярные задачи.
* **`tests`** — изолированное окружение для тестирования:
    * **`/test-fixtures`** — общие тестовые фикстуры, хелперы и билдеры объектов.
    * **`/integration-tests`** — сквозные интеграционные тесты с использованием Testcontainers.

## 7. Основные бизнес-объекты

### 7.1. Волонтёр
Хранит анкету волонтера:
* **Внутренний идентификатор**
* **Telegram ID**
* **Имя**
* **Телефон**
* **Район**
* **Автомобиль**
* **Статус анкеты**
* **Режим доступности**
* **Дата последней активности**
* **Признаки удаления/архивации**

### 7.2. Согласие на обработку ПДн
Хранит сведения о согласии:
* **Идентификатор волонтера**
* **Версия политики**
* **Дата и время согласия**
* **Дата и время отзыва согласия**
* **Источник действия**
* **Комментарий или техническая отметка**

### 7.3. Режим доступности
Хранит доступность волонтера:
* **Дни недели**
* **Временные интервалы**
* **Статус** (доступен / временно недоступен)

### 7.4. Поездка
Хранит заявку на поездку:
* **Идентификатор**
* **Маршрут**
* **Пункт назначения**
* **Дата и время**
* **Задача**
* **Тип** (обычная / срочная)
* **Комментарий**
* **Статус**
* **Назначенный волонтер**
* **Автор-администратор**
* **Версия записи** (для оптимистичной блокировки)

### 7.5. Отклик / назначение волонтера
Хранит взаимодействие волонтера с поездкой:
* **Поездка**
* **Волонтер**
* **Статус отклика**
* **Время отклика**
* **Решение администратора**
* **Причина отмены**

### 7.6. История поездки
Хранит события жизненного цикла поездки:
* **Создание**
* **Публикация**
* **Отклик**
* **Подтверждение**
* **Отклонение**
* **Отмена**
* **Перенос времени**
* **Напоминание**
* **Отметка о выполнении**
* **Закрытие поездки**

## 8. Роли и права доступа

| Роль | Права | Способ доступа |
| :--- | :--- | :--- |
| **Волонтёр** | Просмотр поездок, отклик, профиль, доступность, отмена своей поездки, завершение поездки, удаление профиля, отзыв согласия | `/start`, команды и инлайн-кнопки бота |
| **Администратор** | Создание поездок, подтверждение волонтеров, отмена и перенос поездок, просмотр анкет, модерация, закрытие поездок, экспорт истории | Внесение Telegram ID в белый список (через конфигурацию/БД) |
| **Разработчик / оператор эксплуатации** | Развертывание, настройка токена, белый список админов, бэкапы, сбор и просмотр метрик | SSH-доступ к серверу и доступ к Git-репозиторию |

## 9. Ключевые сценарии и потоки данных

### 9.1. Регистрация волонтера

```mermaid
sequenceDiagram
participant U as Пользователь
participant T as Telegram
participant B as Bot Adapter
participant A as Application
participant DB as PostgreSQL

    U->>T: /start
    T->>B: update
    B->>A: StartRegistration
    A->>B: Send policy PDF + consent text
    B->>U: Politika_PDN_Priyut.pdf + кнопка "Зарегистрироваться"
    U->>B: Нажал "Зарегистрироваться"
    B->>A: GrantConsent
    A->>DB: Save consent version/time
    A->>B: Start profile questionnaire
    B->>U: Вопросы анкеты
    U->>B: Ответы
    B->>A: RegisterVolunteer
    A->>DB: Save volunteer with status "На модерации"
    A->>B: Notify admin
    B->>Admin: Новая анкета
```

Важно: регистрация считается начатой только после явного нажатия кнопки «Зарегистрироваться».

### 9.2. Создание и публикация поездки

```mermaid
sequenceDiagram
participant Admin as Администратор
participant B as Bot Adapter
participant A as Application
participant DB as PostgreSQL
participant V as Волонтёры

    Admin->>B: /new_trip
    B->>Admin: Запрос параметров поездки
    Admin->>B: Дата, маршрут, задача, тип, комментарий
    B->>A: CreateTrip
    A->>DB: Save trip draft
    B->>Admin: Preview + buttons
    Admin->>B: Опубликовать
    B->>A: PublishTrip
    A->>DB: Update status = PUBLISHED
    A->>B: Broadcast trip
    B->>V: Сообщение с кнопкой "Я поеду"
```

### 9.3. Отклик и подтверждение волонтера

```mermaid
sequenceDiagram
participant V as Волонтёр
participant B as Bot Adapter
participant A as Application
participant DB as PostgreSQL
participant Admin as Администратор

    V->>B: Нажал "Я поеду"
    B->>A: RespondToTrip
    A->>DB: Trip status = VOLUNTEER_PENDING
    A->>B: Notify admin
    B->>Admin: Данные волонтера + подтвердить/отклонить
    Admin->>B: Подтвердить
    B->>A: ConfirmVolunteer
    A->>DB: Trip status = ASSIGNED
    A->>B: Notify assigned volunteer
    B->>V: Контакт админа, детали поездки
    A->>B: Notify other volunteers
    B->>V: Исполнитель найден
```

### 9.4. Напоминания

```mermaid
sequenceDiagram
participant S as Scheduler
participant A as Application
participant DB as PostgreSQL
participant B as Bot Adapter
participant V as Волонтёр

    S->>A: Check upcoming trips
    A->>DB: Find trips in next 24h / 2h
    A->>B: Send reminder
    B->>V: Напоминание + кнопки подтверждения
    V->>B: Подтверждаю / Не смогу
    B->>A: Handle reminder response
    A->>DB: Save reminder response
    A->>B: Notify admin if declined
```

## 10. Статусная модель поездки

### Таблица статусов и описаний

| Статус | Описание |
| :--- | :--- |
| **DRAFT** | Поездка создана администратором, но еще не опубликована |
| **PUBLISHED** | Поездка опубликована и доступна для откликов волонтеров |
| **VOLUNTEER_PENDING** | Есть отклик волонтера, ожидается подтверждение администратора |
| **ASSIGNED** | Администратор подтвердил волонтера, поездка закреплена за ним |
| **PENDING_COMPLETION** | Волонтёр отметил поездку выполненной, ожидается закрытие администратором |
| **COMPLETED** | Администратор закрыл поездку, данные сохранены в историю |
| **CANCELLED** | Поездка отменена администратором или волонтером по сценарию |
| **RESCHEDULED_PENDING** | Время поездки изменено, требуется повторное подтверждение волонтера |

#### Жизненный цикл поездки (State Diagram)

```mermaid
stateDiagram-v2
    [*] --> DRAFT : Создание админом
    
    DRAFT --> PUBLISHED : Публикация
    DRAFT --> CANCELLED : Отмена

    PUBLISHED --> VOLUNTEER_PENDING : Отклик волонтера
    PUBLISHED --> CANCELLED : Отмена админом

    VOLUNTEER_PENDING --> ASSIGNED : Админ подтвердил волонтера
    VOLUNTEER_PENDING --> PUBLISHED : Админ отклонил волонтера

    ASSIGNED --> PENDING_COMPLETION : Волонтер выполнил
    ASSIGNED --> RESCHEDULED_PENDING : Перенос времени админом
    ASSIGNED --> PUBLISHED : Волонтер отказался / Отмена админом

    RESCHEDULED_PENDING --> ASSIGNED : Волонтер согласен с новым временем
    RESCHEDULED_PENDING --> PUBLISHED : Волонтер отказался от нового времени

    PENDING_COMPLETION --> COMPLETED : Админ закрыл поездку
    PENDING_COMPLETION --> CANCELLED : Сбой / Отмена админом

    COMPLETED --> [*]
    CANCELLED --> [*]
```

#### Дополнительные признаки (Флаги напоминаний)
Для автоматизации процесса контроля поездок в модуле `infrastructure/scheduling` используются технические маркеры уведомлений:
* **`REMINDER_24_SENT`** — отправлено напоминание за 24 часа до выезда.
* **`REMINDER_2_SENT`** — отправлено финальное напоминание за 2 часа до выезда.
* **`REMINDER_CONFIRMED`** — волонтер подтвердил актуальность поездки через инлайн-кнопку в напоминании.
* **`REMINDER_DECLINED`** — волонтер нажал кнопку отмены в напоминании (триггер для возврата в `PUBLISHED` или `CANCELLED`).

## 11. Статусная модель волонтера

### Таблица статусов и описаний

| Статус | Описание |
| :--- | :--- |
| **PENDING_MODERATION** | Анкета заполнена и ожидает проверки администратора |
| **ACTIVE** | Волонтёр одобрен администратором и может откликаться на поездки |
| **REJECTED** | Анкета отклонена администратором (не пройдена модерация) |
| **TEMPORARILY_UNAVAILABLE** | Волонтёр временно отключил доступность (отпуск, личные обстоятельства) |
| **DELETION_REQUESTED** | Пользователь запросил удаление профиля и отзыв согласия на обработку ПДн |
| **DELETED** | Персональные данные полностью очищены/обезличены в соответствии с 152-ФЗ |
| **ARCHIVED** | Профиль архивирован автоматически по истечении 3 лет полной неактивности |

#### Жизненный цикл волонтера (State Diagram)

```mermaid
stateDiagram-v2
    [*] --> PENDING_MODERATION : Успешное анкетирование
    
    PENDING_MODERATION --> ACTIVE : Одобрение админом
    PENDING_MODERATION --> REJECTED : Отклонение админом
    PENDING_MODERATION --> DELETION_REQUESTED : Отзыв согласия / Отмена

    REJECTED --> PENDING_MODERATION : Повторная подача анкеты
    REJECTED --> DELETION_REQUESTED : Удаление данных

    ACTIVE --> TEMPORARILY_UNAVAILABLE : Смена режима доступности
    ACTIVE --> DELETION_REQUESTED : Запрос на удаление профиля
    ACTIVE --> ARCHIVED : 3 года неактивности (Cron)

    TEMPORARILY_UNAVAILABLE --> ACTIVE : Возврат в режим доступности
    TEMPORARILY_UNAVAILABLE --> DELETION_REQUESTED : Запрос на удаление профиля
    TEMPORARILY_UNAVAILABLE --> ARCHIVED : 3 года неактивности (Cron)

    ARCHIVED --> ACTIVE : Авторизация / Восстановление
    ARCHIVED --> DELETION_REQUESTED : Окончательное удаление

    DELETION_REQUESTED --> DELETED : Очистка ПДн (Модуль persistence)
    DELETED --> [*]
```

## 12. Модель данных верхнего уровня

Ниже приведена логическая модель. Физические таблицы описываются в Flyway-миграциях.

### Схема связей данных (ER-диаграмма)

```mermaid
erDiagram
    VOLUNTEERS {
        bigint id PK
        bigint telegram_id UK
        varchar full_name
        varchar phone
        varchar district
        varchar car_info
        varchar status
        timestamp created_at
        timestamp updated_at
        timestamp last_activity_at
        timestamp deleted_at
    }

    VOLUNTEER_AVAILABILITY {
        bigint id PK
        bigint volunteer_id FK
        integer day_of_week
        time time_from
        time time_to
        varchar availability_status
    }

    CONSENTS {
        bigint id PK
        bigint volunteer_id FK
        varchar consent_version
        varchar consent_document_name
        timestamp granted_at
        timestamp withdrawn_at
        varchar source
        text comment
    }

    TRIPS {
        bigint id PK
        bigint created_by_admin_id FK
        varchar route_from
        varchar route_to
        varchar destination
        timestamp trip_datetime
        text task_description
        varchar trip_type
        boolean is_urgent
        text comment
        varchar status
        timestamp published_at
        bigint assigned_volunteer_id FK
        timestamp cancelled_at
        text cancel_reason
        timestamp completed_at
        bigint closed_by_admin_id FK
        integer version
    }

    TRIP_RESPONSES {
        bigint id PK
        bigint trip_id FK
        bigint volunteer_id FK
        varchar status
        timestamp created_at
        timestamp admin_decision_at
        bigint admin_id
        text cancel_reason
    }

    TRIP_HISTORY {
        bigint id PK
        bigint trip_id FK
        varchar event_type
        varchar actor_type
        bigint actor_id
        jsonb payload
        timestamp created_at
    }

    REMINDERS {
        bigint id PK
        bigint trip_id FK
        varchar reminder_type
        timestamp sent_at
        varchar response
        timestamp responded_at
    }

    DELETION_REQUESTS {
        bigint id PK
        bigint volunteer_id FK
        timestamp requested_at
        timestamp scheduled_deadline
        varchar status
        timestamp processed_at
    }

    ADMIN_WHITELIST {
        bigint telegram_id PK
        varchar display_name
        timestamp added_at
        bigint added_by
    }

    %% Отношения и связи между таблицами
    VOLUNTEERS ||--o{ VOLUNTEER_AVAILABILITY : "имеет расписание"
    VOLUNTEERS ||--o{ CONSENTS : "дает / отзывает"
    VOLUNTEERS ||--o{ DELETION_REQUESTS : "запрашивает удаление"
    VOLUNTEERS ||--o{ TRIPS : "назначается на"
    VOLUNTEERS ||--o{ TRIP_RESPONSES : "оставляет отклик"

    TRIPS ||--o{ TRIP_RESPONSES : "содержит отклики"
    TRIPS ||--o{ TRIP_HISTORY : "логирует события"
    TRIPS ||--o{ REMINDERS : "генерирует уведомления"
    
    ADMIN_WHITELIST ||--o{ TRIPS : "создает / закрывает"
```

#### Краткое описание ключевых связей:
* **Обезличивание и 152-ФЗ:** Таблицы `consents` и `deletion_requests` жестко привязаны к `volunteers`. При переходе волонтера в статус `DELETED` персональные данные стираются из `volunteers`, но история в `trips` сохраняется (благодаря связи с сохранением ссылочной целостности или через каскадные правила, если это необходимо).
* **Аналитика и аудит:** Таблица `trip_history` содержит поле `payload` типа `jsonb` для гибкого логирования изменений полей поездки (например, старое/новое время при переносе).
* **Безопасность:** Таблица `admin_whitelist` выступает изолированным реестром Telegram ID, имеющих доступ к административным методам в модуле `infrastructure/telegram`.

## 13. Доступ к базе данных

В проекте используется **Spring JDBC** вместо JPA/Hibernate. Это осознанный выбор инженерного стека.

### Причины выбора:
* **Предсказуемый SQL** — полное исключение скрытых запросов (N+1, неявные Lazy Initialization-исключения).
* **Экономия памяти** — отсутствие тяжелого контекста постоянства (Persistence Context), кэшей первого/второго уровня и прокси-объектов.
* **Полный контроль** — легкая оптимизация сложных выборок для аналитики или истории.
* **Соответствие нагрузке** — возможностей Spring JDBC с запасом хватает для объема данных проекта.
* **Соответствие 152-ФЗ** — прямое управление SQL упрощает выполнение точечных процедур маскирования, архивации и физического удаления (`hard delete`) персональных данных волонтеров.

#### Инструментарий разработки:
* `JdbcTemplate` и `NamedParameterJdbcTemplate` — для выполнения всех SQL-запросов.
* `SimpleJdbcInsert` — для удобной вставки новых записей без написания громоздких выражений `INSERT INTO`.
* `RowMapper` — для явного и безопасного маппинга строк из ResultSet в иммутабельные доменные объекты (Java Records).
* **Управление транзакциями** — декларативное через аннотацию `@Transactional` строго на уровне `application`-слоя (Use Cases / Интеракторы).

---

## 14. Время и часовые пояса

Правила работы с временными метками для обеспечения консистентности данных:
* **Хранение в БД** — абсолютно все даты и время записываются в типе данных `TIMESTAMP WITH TIME ZONE` (или переводятся) строго в часовом поясе **UTC**.
* **Интерфейс пользователя** — ввод данных администратором и отображение дат волонтерам в Telegram происходит исключительно в локальном времени **Europe/Moscow** (MSK, UTC+3).
* **Логика уведомлений** — расчет интервалов отправки напоминаний (24 часа и 2 часа до поездки) привязан к московскому времени.
* **Тестирование** — в тестовой конфигурации (`tests/*`) через сборочный скрипт Gradle принудительно задается системный параметр `-Duser.timezone=UTC`. Это гарантирует стабильность и воспроизводимость тестов независимо от локальных настроек машин разработчиков или CI/CD серверов.

---

## 15. Интеграция с Telegram

### 15.1. Способ подключения
Для MVP выбран режим **Long Polling** (метод `getUpdates`).

#### Причины выбора:
* **Автономность** — не требует организации публичного доменного имени и настройки доверенного HTTPS-сертификата (SSL/TLS).
* **Простота инфраструктуры** — приложение само тянет данные из Telegram API, что минимизирует конфигурацию сети на VPS FirstByte.
* **Изолированность** — отсутствие необходимости открывать входящие порты в брандмауэре сервера.
* **Производительность** — пропускной способности Long Polling с запасом хватает для заявленной нагрузки (до 500 пользователей).

*В будущем архитектура модульного монолита позволит бесшовно заменить адаптер `infrastructure/telegram` на Webhook.*

### 15.2. Обработка обновлений (Идемпотентность)
Для предотвращения сетевых дублей и повторной обработки одних и тех же сообщений:
* Каждое входящее событие валидируется по `update_id`.
* Инлайн-кнопки (`InlineKeyboardMarkup`) генерируют уникальные идентификаторы в `callback_data`.
* Бизнес-логика проверяет текущий статус сущностей перед изменением (например, повторный клик на кнопку «Откликнуться» в статусе `VOLUNTEER_PENDING` или `ASSIGNED` просто игнорируется или выводит alert-уведомление, не создавая новую запись в `trip_responses`).

### 15.3. Ограничение доступа (RBAC / Права)
Доступ к функционалу жестко разграничен на уровне адаптера `infrastructure/telegram`:
* **Администраторы** — доступ к командам создания, переноса и модерации открывается только после успешной проверки `message.from.id` по внутренней таблице `admin_whitelist`.
* **Волонтеры** — доступ к просмотру активных поездок и откликам открывается только для пользователей, чьи анкеты в таблице `volunteers` находятся в статусе `ACTIVE`.
* **Гости (Неизвестные)** — при любом взаимодействии перенаправляются исключительно на корневую команду `/start` и стартовый сценарий регистрации с принятием политики персональных данных.
