# Годинник (Mobiki) - Android додаток

Android-додаток «Годинник» з функціоналом таймера, налаштувань та відображення часу.

## Функціонал

- **Splash-екран** - показується при запуску протягом 2 секунд
- **Екран годинника** - відображає поточні рік, дату, час та часовий пояс
- **Екран налаштувань** - дозволяє змінити часовий пояс
- **Екран таймера** - вимірювання проміжків часу з функціями Старт, Стоп, Пауза, Коло

## Технічні особливості

- **Service** - TimerService для роботи таймера у фоновому режимі
- **Binder** - механізм зв'язування для комунікації зі службою
- **Observer Pattern** - для отримання подій від служби
- **Fragments** - навігація побудована на базі фрагментів
- **Layout** - компонування на базі FrameLayout та LinearLayout

## Структура проекту

```
app/
├── src/main/
│   ├── java/com/example/mobiki/
│   │   ├── SplashActivity.kt
│   │   ├── MainActivity.kt
│   │   ├── fragments/
│   │   │   ├── ClockFragment.kt
│   │   │   ├── SettingsFragment.kt
│   │   │   └── TimerFragment.kt
│   │   └── service/
│   │       ├── TimerService.kt
│   │       └── TimerObserver.kt
│   ├── res/
│   │   └── layout/
│   │       ├── activity_splash.xml
│   │       ├── activity_main.xml
│   │       ├── fragment_clock.xml
│   │       ├── fragment_settings.xml
│   │       └── fragment_timer.xml
│   └── AndroidManifest.xml
```

## Вимоги

- Android Studio
- Min SDK: 26
- Target SDK: 36
- Kotlin

## Встановлення

1. Клонуйте репозиторій:
```bash
git clone https://github.com/ваш-username/Mobiki.git
```

2. Відкрийте проект в Android Studio

3. Синхронізуйте Gradle

4. Запустіть на пристрої або емуляторі

## Автор

Іщенко Євгеній Романович

## Ліцензія

MIT License

