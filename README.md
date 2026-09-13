<div align="center">

<img src="https://github.com/user-attachments/assets/a8ba4b6d-7ba0-4371-9f27-578ae5a16234" alt="XPL-EX" width="720">

# XPL-EX-NEXT

Расширенный модуль конфиденциальности и согласованной подмены профиля устройства для Android и LSPosed.

[![CI](https://img.shields.io/github/actions/workflow/status/Mobilelegends74/XPL-EX/app-on-push.yml?label=CI&style=flat-square)](https://github.com/Mobilelegends74/XPL-EX/actions/workflows/app-on-push.yml)
[![Release](https://img.shields.io/github/v/release/Mobilelegends74/XPL-EX?label=release&style=flat-square)](https://github.com/Mobilelegends74/XPL-EX/releases/latest)
[![Downloads](https://img.shields.io/github/downloads/Mobilelegends74/XPL-EX/total?label=downloads&style=flat-square)](https://github.com/Mobilelegends74/XPL-EX/releases)
[![License](https://img.shields.io/github/license/Mobilelegends74/XPL-EX?label=license&style=flat-square)](LICENSE)
[![Android](https://img.shields.io/badge/Android-8.1%2B-3DDC84?logo=android&logoColor=white&style=flat-square)](#требования)
[![LSPosed API](https://img.shields.io/badge/libxposed-API%20101-167C80?style=flat-square)](https://github.com/libxposed/api)

**Русская версия** · [English version](README_EN.md)

[Скачать последнюю версию](https://github.com/Mobilelegends74/XPL-EX/releases/latest) · [Сообщить о проблеме](https://github.com/Mobilelegends74/XPL-EX/issues) · [История релизов](https://github.com/Mobilelegends74/XPL-EX/releases)

</div>

## О проекте

XPL-EX-NEXT позволяет управлять тем, какие сведения Android-приложения получают об устройстве. Модуль перехватывает выбранные Java API и возвращает значения из индивидуального профиля приложения: идентификаторы, характеристики устройства, параметры сети, SIM-карты и другие данные.

Проект является развитием [XPL-EX от ObbedCode](https://github.com/0bbedCode/XPL-EX), который, в свою очередь, основан на оригинальном [XPrivacyLua от M66B](https://github.com/M66B/XPrivacyLua). Мы сохраняем открытый исходный код, авторство и лицензию исходных проектов.

XPL-EX-NEXT не содержит рекламы, аналитики или трекеров. Для работы хуков интернет не требуется; подключение к GitHub используется только для необязательной проверки обновлений.

## Возможности

- Раздельные профили подмены для каждого выбранного приложения.
- Согласованная подмена модели, производителя, Build-параметров и версии Android.
- Android ID, Advertising ID, GSF ID, IMEI/MEID, ICCID, IMSI, серийные и другие идентификаторы.
- Параметры SIM и оператора: имя, MCC, MNC, numeric ID, номера и состояние сети.
- CPU, GPU, память, экран, хранилище, камера, сенсоры и аппаратные возможности.
- Wi-Fi, MAC/BSSID/SSID, IP-адреса, интерфейсы сети, VPN и сетевые свойства.
- Фильтрация списка приложений, временных меток, файлов, shell-команд, properties, Binder и ContentResolver.
- Автоматическое назначение необходимых хуков при сохранении связанных настроек.
- Импорт и экспорт конфигурации, русская локализация и встроенная проверка новых релизов.
- Modern Xposed API 101 при сохранённом bridge-протоколе совместимости `1.5.5`.

## Требования

- Android 8.1 или новее.
- Root-доступ и установленный LSPosed с поддержкой Modern Xposed API.
- Разрешение установки APK из выбранного браузера или файлового менеджера.

Проект ориентирован на актуальные версии Android. Совместимость конкретного хука зависит от прошивки, версии Android и реализации API в целевом приложении.

## Установка

1. Установите и настройте [Magisk](https://github.com/topjohnwu/Magisk) и [LSPosed](https://github.com/LSPosed/LSPosed).
2. Скачайте APK со страницы [последнего релиза](https://github.com/Mobilelegends74/XPL-EX/releases/latest).
3. Установите APK, включите XPL-EX-NEXT в LSPosed и выберите нужные приложения в области действия модуля.
4. Откройте XPL-EX-NEXT, выберите приложение и настройте только необходимые группы хуков.
5. Перезапустите целевое приложение. После обновления самого модуля рекомендуется перезагрузить устройство.

### Переход на единую подпись

Релизы до `1.6.0` собирались GitHub Actions с временной подписью. Android не разрешает установить APK с новым сертификатом поверх такой версии.

При первом переходе с `1.5.12` или более ранней сборки:

1. Экспортируйте настройки XPL-EX.
2. Удалите установленную старую сборку.
3. Установите `1.6.0` или более новую версию и импортируйте настройки.

Начиная с `1.6.0`, все официальные релизы подписываются одним постоянным сертификатом и устанавливаются поверх предыдущей версии.

## Обновления

При открытии XPL-EX-NEXT не чаще одного раза в 12 часов проверяет [последний релиз этого репозитория](https://github.com/Mobilelegends74/XPL-EX/releases/latest). Если опубликована более новая версия, приложение предложит скачать подписанный APK.

LSPosed показывает обновления из собственного официального каталога. Пакет `eu.faircode.xlua` в этом каталоге принадлежит оригинальному XPrivacyLua, поэтому обновления нашей ветки проверяются самим XPL-EX-NEXT.

## Важные ограничения

- Модуль изменяет ответы локальных API, но не делает устройство полностью анонимным.
- Серверные проверки и нативный код могут получать данные другими способами.
- Не включайте все хуки одновременно без необходимости: несовместимая комбинация может нарушить работу целевого приложения.
- Создавайте резервную копию конфигурации перед крупным обновлением.
- Используйте проект только на устройствах и в приложениях, которыми вы вправе управлять.

## Скриншоты

<details>
<summary>Открыть галерею интерфейса</summary>

<p align="center">
  <img src="https://github.com/user-attachments/assets/52bf2d05-05ae-42a7-aa41-be357c6e88b5" alt="XPL-EX screen 1" width="240">
  <img src="https://github.com/user-attachments/assets/ec717fb0-c1f4-47f9-ac36-b6c71bd6538c" alt="XPL-EX screen 2" width="240">
  <img src="https://github.com/user-attachments/assets/f06808fa-bb44-4c84-801b-8a0590c6f702" alt="XPL-EX screen 3" width="240">
</p>
<p align="center">
  <img src="https://github.com/user-attachments/assets/c4f7580b-d183-4baf-96e0-fc1ed5877b69" alt="XPL-EX screen 4" width="240">
  <img src="https://github.com/user-attachments/assets/06553558-1a92-4db2-b9ea-04fbeab48c7b" alt="XPL-EX screen 5" width="240">
  <img src="https://github.com/user-attachments/assets/e20caed3-87cc-4fb6-b65d-0656c9714321" alt="XPL-EX screen 6" width="240">
</p>

</details>

## Документация и обратная связь

- [Релизы и описания изменений](https://github.com/Mobilelegends74/XPL-EX/releases)
- [Сообщения об ошибках и предложения](https://github.com/Mobilelegends74/XPL-EX/issues)
- [FAQ исходного проекта](FAQ.md)
- [Справка по API](APIHELP.md)
- [Сборки GitHub Actions](https://github.com/Mobilelegends74/XPL-EX/actions)

При создании issue укажите версию XPL-EX-NEXT, версию Android и LSPosed, название проблемного API или хука и приложите обезличенный журнал воспроизведения.

## Авторы и благодарности

- [M66B](https://github.com/M66B) — автор оригинального [XPrivacyLua](https://github.com/M66B/XPrivacyLua).
- [ObbedCode / 0bbedCode](https://github.com/0bbedCode) — автор расширенного [XPL-EX](https://github.com/0bbedCode/XPL-EX), на котором основана эта ветка.
- [VD-8](https://github.com/VD171/VD-Infos) — проект VD-Infos и связанные наработки.
- [HUBERTH](https://t.me/HubertHub) — исследования методов обнаружения и помощь проекту.
- [LSPosed](https://github.com/LSPosed/LSPosed) и [libxposed](https://github.com/libxposed) — инфраструктура Xposed API.
- [Mobilelegends74](https://github.com/Mobilelegends74) — сопровождение XPL-EX-NEXT, исправления и выпуск актуальных сборок.

## Поддержать разработку

Если проект оказался полезен, вы можете поддержать его дальнейшую разработку. Перед отправкой обязательно проверьте выбранную сеть.

### XPL-EX-NEXT / Mobilelegends74

**BEP-20**

```text
0xafe2e8f8eb4792885026dd0296aae72311f00777
```

**TRC-20**

```text
TJREgqMhwGa2z47nJN4dRG5VEYM1P7sHx3
```

### Исходный XPL-EX / ObbedCode

**TRX (TRON)**

```text
TRk5a1C4U5fTgMbZQBi7wRM1hjvguPnqBb
```

## Лицензия

Проект распространяется по лицензии [GNU General Public License v3.0](LICENSE). Производные работы должны сохранять условия лицензии и уведомления об авторстве исходных проектов.
