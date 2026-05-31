# Аметистовые Равнины (Amethyst Plains)

## Описание
Новый кастомный биом для мода Caveborn с уникальными особенностями:

### Особенности биома:
- 🌿 **Синяя трава** - вся растительность имеет синий оттенок (RGB: 37, 87, 255)
- 🌅 **Красное небо** - небо окрашено в красный цвет для создания уникальной атмосферы
- 💎 **Аметистовые столбы** - высокие конусообразные структуры из аметистовых блоков (похожи на ледяные пики)
  - Высота: 15-35 блоков
  - Форма: конусообразная, сужающаяся к вершине
  - Материал: блоки аметиста
  - Частота: 3 столба на чанк

### Технические детали:
- **ID биома**: `caveborn:amethyst_plains`
- **Температура**: 0.8 (тёплый)
- **Влажность**: 0.4 (умеренная)
- **Осадки**: Да (дождь)

## Как найти биом

### Вариант 1: Команда телепортации
```
/locatebiome caveborn:amethyst_plains
```

### Вариант 2: Создание нового мира
Биом будет генерироваться естественным образом в обычном мире (Overworld).

### Вариант 3: Использование сида
Создайте новый мир и исследуйте его - биом будет встречаться среди других биомов.

## Файлы, которые были созданы/изменены:

### Java классы:
- `src/main/java/ru/purpir/world/ModBiomes.java` - регистрация биома
- `src/main/java/ru/purpir/world/AmethystSpikeFeature.java` - генератор аметистовых столбов
- `src/main/java/ru/purpir/world/BiomeModifications.java` - добавление фич в биом
- `src/client/java/ru/purpir/client/BiomeColorHandler.java` - кастомные цвета травы

### JSON файлы:
- `src/main/resources/data/caveborn/worldgen/biome/amethyst_plains.json` - конфигурация биома
- `src/main/resources/data/caveborn/worldgen/configured_feature/amethyst_spike.json` - настройка фичи
- `src/main/resources/data/caveborn/worldgen/placed_feature/amethyst_spike.json` - размещение фичи
- `src/main/resources/data/caveborn/tags/worldgen/biome/is_overworld.json` - тег для генерации
- `src/main/resources/data/minecraft/tags/worldgen/biome/is_overworld.json` - тег Minecraft

### Локализация:
- `src/main/resources/assets/caveborn/lang/ru_ru.json` - русское название
- `src/main/resources/assets/caveborn/lang/en_us.json` - английское название

## Сборка и запуск

1. Скомпилируйте мод:
```bash
./gradlew build
```

2. Запустите клиент для тестирования:
```bash
./gradlew runClient
```

3. Создайте новый мир и найдите биом с помощью команды `/locatebiome caveborn:amethyst_plains`

## Возможные улучшения

- Добавить уникальных мобов для биома
- Создать кастомные структуры (руины, храмы)
- Добавить уникальные растения
- Создать специальные руды, которые генерируются только в этом биоме
- Добавить партиклы для усиления атмосферы

## Примечания

- Биом будет генерироваться только в новых чанках
- Для просмотра биома в существующем мире нужно исследовать новые территории
- Цвета неба и травы применяются автоматически при входе в биом
