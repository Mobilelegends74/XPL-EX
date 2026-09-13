<div align="center">

<img src="https://github.com/user-attachments/assets/a8ba4b6d-7ba0-4371-9f27-578ae5a16234" alt="XPL-EX" width="720">

# XPL-EX-NEXT

An advanced privacy and coherent device-profile spoofing module for Android and LSPosed.

[![CI](https://img.shields.io/github/actions/workflow/status/Mobilelegends74/XPL-EX/app-on-push.yml?label=CI&style=flat-square)](https://github.com/Mobilelegends74/XPL-EX/actions/workflows/app-on-push.yml)
[![Release](https://img.shields.io/github/v/release/Mobilelegends74/XPL-EX?label=release&style=flat-square)](https://github.com/Mobilelegends74/XPL-EX/releases/latest)
[![Downloads](https://img.shields.io/github/downloads/Mobilelegends74/XPL-EX/total?label=downloads&style=flat-square)](https://github.com/Mobilelegends74/XPL-EX/releases)
[![License](https://img.shields.io/github/license/Mobilelegends74/XPL-EX?label=license&style=flat-square)](LICENSE)
[![Android](https://img.shields.io/badge/Android-8.1%2B-3DDC84?logo=android&logoColor=white&style=flat-square)](#requirements)
[![LSPosed API](https://img.shields.io/badge/libxposed-API%20101-167C80?style=flat-square)](https://github.com/libxposed/api)

[Русская версия](README.md) · **English version**

[Download latest release](https://github.com/Mobilelegends74/XPL-EX/releases/latest) · [Report an issue](https://github.com/Mobilelegends74/XPL-EX/issues) · [Release history](https://github.com/Mobilelegends74/XPL-EX/releases)

</div>

## About

XPL-EX-NEXT controls what information Android applications receive about a device. It hooks selected Java APIs and returns values from a per-application profile, including identifiers, device properties, network parameters, SIM data, and other information.

This project continues [XPL-EX by ObbedCode](https://github.com/0bbedCode/XPL-EX), which is based on the original [XPrivacyLua by M66B](https://github.com/M66B/XPrivacyLua). We preserve the open-source nature, attribution, and license of the upstream projects.

XPL-EX-NEXT contains no advertising, analytics, or trackers. Hooks work without an internet connection; GitHub access is used only for the optional release update check.

## Features

- Separate spoofing profiles for each selected application.
- Coherent model, manufacturer, Build property, and Android version spoofing.
- Android ID, Advertising ID, GSF ID, IMEI/MEID, ICCID, IMSI, serials, and other identifiers.
- SIM and carrier parameters: name, MCC, MNC, numeric ID, phone numbers, and network state.
- CPU, GPU, memory, display, storage, camera, sensors, and hardware capabilities.
- Wi-Fi, MAC/BSSID/SSID, IP addresses, network interfaces, VPN, and network properties.
- Application-list, timestamp, file, shell, property, Binder, and ContentResolver filtering.
- Automatic assignment of required hooks when related settings are saved.
- Configuration import/export, Russian localization, and a built-in release update check.
- Modern Xposed API 101 with the compatibility bridge protocol kept at `1.5.5`.

## Requirements

- Android 8.1 or newer.
- Root access and LSPosed with Modern Xposed API support.
- Permission to install APK files from the selected browser or file manager.

The project targets current Android versions. Individual hook compatibility depends on the ROM, Android version, and the target application's API implementation.

## Installation

1. Install and configure [Magisk](https://github.com/topjohnwu/Magisk) and [LSPosed](https://github.com/LSPosed/LSPosed).
2. Download the APK from the [latest release](https://github.com/Mobilelegends74/XPL-EX/releases/latest).
3. Install the APK, enable XPL-EX-NEXT in LSPosed, and select the required target applications in its scope.
4. Open XPL-EX-NEXT, select an application, and configure only the hook groups you need.
5. Restart the target application. Rebooting the device is recommended after updating the module itself.

### Migration to the permanent signing certificate

Releases before `1.6.0` were built by GitHub Actions with temporary certificates. Android cannot install an APK signed by a different certificate over an existing installation.

When migrating from `1.5.12` or an earlier build for the first time:

1. Export your XPL-EX settings.
2. Uninstall the old build.
3. Install `1.6.0` or newer and import your settings.

Starting with `1.6.0`, every official release uses one permanent certificate and can be installed directly over the previous version.

## Updates

When opened, XPL-EX-NEXT checks the [latest release in this repository](https://github.com/Mobilelegends74/XPL-EX/releases/latest) no more than once every 12 hours. If a newer version exists, the app offers the signed APK download.

LSPosed displays updates from its official module repository. The `eu.faircode.xlua` entry there belongs to the original XPrivacyLua, so updates for this fork are checked by XPL-EX-NEXT itself.

## Important limitations

- The module modifies local API responses but cannot make a device completely anonymous.
- Server-side checks and native code can obtain information through other channels.
- Do not enable every hook at once without a reason; incompatible combinations can break a target application.
- Back up your configuration before major updates.
- Use the project only on devices and applications you are authorized to control.

## Screenshots

<details>
<summary>Open the interface gallery</summary>

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

## Documentation and feedback

- [Releases and changelogs](https://github.com/Mobilelegends74/XPL-EX/releases)
- [Bug reports and feature requests](https://github.com/Mobilelegends74/XPL-EX/issues)
- [Upstream FAQ](FAQ.md)
- [API reference](APIHELP.md)
- [GitHub Actions builds](https://github.com/Mobilelegends74/XPL-EX/actions)

When opening an issue, include the XPL-EX-NEXT, Android, and LSPosed versions, the affected API or hook name, and a sanitized reproduction log.

## Authors and acknowledgements

- [M66B](https://github.com/M66B) — author of the original [XPrivacyLua](https://github.com/M66B/XPrivacyLua).
- [ObbedCode / 0bbedCode](https://github.com/0bbedCode) — author of the extended [XPL-EX](https://github.com/0bbedCode/XPL-EX) on which this branch is based.
- [VD-8](https://github.com/VD171/VD-Infos) — VD-Infos and related work.
- [HUBERTH](https://t.me/HubertHub) — detection research and project assistance.
- [LSPosed](https://github.com/LSPosed/LSPosed) and [libxposed](https://github.com/libxposed) — Xposed API infrastructure.
- [Mobilelegends74](https://github.com/Mobilelegends74) — XPL-EX-NEXT maintenance, fixes, and current releases.

## Support development

If this project is useful to you, you can support its continued development. Always verify the selected network before sending funds.

### XPL-EX-NEXT / Mobilelegends74

**BEP-20**

```text
0xafe2e8f8eb4792885026dd0296aae72311f00777
```

**TRC-20**

```text
TJREgqMhwGa2z47nJN4dRG5VEYM1P7sHx3
```

### Upstream XPL-EX / ObbedCode

**TRX (TRON)**

```text
TRk5a1C4U5fTgMbZQBi7wRM1hjvguPnqBb
```

## License

This project is distributed under the [GNU General Public License v3.0](LICENSE). Derivative works must preserve the license terms and upstream attribution notices.
