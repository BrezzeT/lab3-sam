# Годинник (Mobiki) - Android додаток

Android-додаток «Годинник» з функціоналом таймера, налаштувань та відображення часу.

## Функціонал

- **Splash-екран** - показується при запуску протягом 2 секунд
- **Екран годинника** - відображає поточні рік, дату, час та часовий пояс
- **Екран налаштувань** - дозволяє змінити часовий пояс
- **Екран таймера** - вимірювання проміжків часу з функціями Старт, Стоп, Пауза, Коло
- **Прогрес синхронізації** - відображення прогресу роботи таймера у відсотках

## Технічні особливості

### Архітектура
- **MVVM (Model-View-ViewModel)** - архітектурний патерн для розділення логіки та UI
- **ViewModel** - зберігає дані та логіку, пов'язану з UI
- **LiveData** - реактивні дані, які автоматично оновлюють UI при змінах
- **LifecycleObserver** - спостереження за життєвим циклом компонентів

### Робота з таймером
- **HandlerThread** - окремий потік для роботи таймера (замість Service)
- **Observer Pattern** - для отримання подій від таймера
- **Прогрес синхронізації** - відображення прогресу у відсотках з ProgressBar

### Інтерфейс
- **Fragments** - навігація побудована на базі фрагментів
- **Layout** - компонування на базі FrameLayout та LinearLayout
- **Material Design** - сучасний та привабливий дизайн

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
│   │   ├── viewmodel/
│   │   │   └── TimerViewModel.kt
│   │   └── service/
│   │       ├── TimerHandlerThread.kt
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
- AndroidX Libraries

## Встановлення

1. Клонуйте репозиторій:
```bash
git clone https://github.com/BrezzeT/lab2-mob.git
```

2. Відкрийте проект в Android Studio

3. Синхронізуйте Gradle (File → Sync Project with Gradle Files)

4. Запустіть на пристрої або емуляторі

## Особливості реалізації

- **TimerViewModel** - використовує LiveData для реактивного оновлення UI
- **TimerHandlerThread** - працює в окремому потоці, не блокує UI
- **Прогрес синхронізації** - автоматично розраховується на основі поточного часу
- **Lifecycle-aware** - коректна обробка життєвого циклу компонентів

## Автор

Іщенко Євгеній Романович



