# ValGameWatcher
Простое приложение, которое я создал чтобы научиться делать запросы и немного работать с темами в kotlin compose

## Суть приложения
Она заключается в сравнении последних сыграных игр в на аккаунте Valorant с теми, что приложение записало ранее. Если обрануживается несовпадение, приложение уведомляет об этом пользователя.
Для получения списка последних игр используется traker.gg valorant api (которое не предполагает использование его в таких целях, из-за чего могут возникать проблемы)

## Демонстрация
Основной экран

<img src="/screenshots/Screenshot_main.png" alt="main" width="250"/>

Демонстрация работы

<img src="/screenshots/Screenshot_work.png" alt="work" width="250"/>

Параметры

<img src="/screenshots/Screenshot_params.png" alt="params" width="250"/>

Светлая тема

<img src="/screenshots/Screenshot_lighttheme.png" alt="light" width="250"/>

Уведомления

<img src="/screenshots/Screenshot_notification.png" alt="notification" width="250"/>

## Технологии
* Compose
* ktor
* Сервисы

## Проблемы
* У меня запросы к api через мобильный интернет не работают
* Кажется что сервис, ответственный за фоновую работу приложения, закрывается после первой попытки сделать запросы к api
