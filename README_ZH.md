<div align="center">

<img src="https://github.com/user-attachments/assets/a8ba4b6d-7ba0-4371-9f27-578ae5a16234" alt="XPL-EX" width="720">

# XPL-EX-NEXT

面向 Android 与 LSPosed 的高级隐私保护与一致性设备档案伪装模块。

[![CI](https://img.shields.io/github/actions/workflow/status/Mobilelegends74/XPL-EX/app-on-push.yml?label=CI&style=flat-square)](https://github.com/Mobilelegends74/XPL-EX/actions/workflows/app-on-push.yml)
[![Release](https://img.shields.io/github/v/release/Mobilelegends74/XPL-EX?label=release&style=flat-square)](https://github.com/Mobilelegends74/XPL-EX/releases/latest)
[![Downloads](https://img.shields.io/github/downloads/Mobilelegends74/XPL-EX/total?label=downloads&style=flat-square)](https://github.com/Mobilelegends74/XPL-EX/releases)
[![License](https://img.shields.io/github/license/Mobilelegends74/XPL-EX?label=license&style=flat-square)](LICENSE)
[![Android](https://img.shields.io/badge/Android-8.1%2B-3DDC84?logo=android&logoColor=white&style=flat-square)](#系统要求)
[![LSPosed API](https://img.shields.io/badge/libxposed-API%20101-167C80?style=flat-square)](https://github.com/libxposed/api)

[Русская версия](README.md) · [English version](README_EN.md) · **简体中文**

[下载最新版本](https://github.com/Mobilelegends74/XPL-EX/releases/latest) · [报告问题](https://github.com/Mobilelegends74/XPL-EX/issues) · [版本历史](https://github.com/Mobilelegends74/XPL-EX/releases)

</div>

## 关于本项目

XPL-EX-NEXT 控制 Android 应用能获取到哪些设备信息。它 Hook 选定的 Java API，并按照为每个应用单独配置的档案返回相应数值，涵盖标识符、设备属性、网络参数、SIM 卡数据以及其他信息。

本项目延续 [ObbedCode 的 XPL-EX](https://github.com/0bbedCode/XPL-EX)，而后者基于 [M66B 的 XPrivacyLua](https://github.com/M66B/XPrivacyLua)。我们保留上游项目的开源性质、署名与许可证。

XPL-EX-NEXT 不含广告、统计分析或追踪器。Hook 本身无需联网；仅在可选的版本更新检查时会访问 GitHub。

## 功能

- 为每个选定的应用单独配置伪装档案。
- 型号、厂商、Build 属性与 Android 版本的一致性伪装。
- Android ID、广告 ID、GSF ID、IMEI/MEID、ICCID、IMSI、序列号等标识符。
- SIM 与运营商参数：名称、MCC、MNC、数字代码、电话号码与网络状态。
- CPU、GPU、内存、显示屏、存储、相机、传感器与硬件能力。
- Wi-Fi、MAC/BSSID/SSID、IP 地址、网络接口、VPN 与网络属性。
- 应用列表、时间戳、文件、shell 命令、properties、Binder 与 ContentResolver 过滤。
- 保存相关设置时自动分配所需的 Hook。
- 请求将选定应用加入 LSPosed 作用域，并在禁用最后一个 Hook 后移除。
- 配置导入导出、俄文本地化，以及内置的版本更新检查。
- 采用 Modern Xposed API 101，兼容桥协议保持为 `1.5.5`。

## 系统要求

- Android 8.1 或更高版本。
- Root 权限，以及支持 Modern Xposed API 的 LSPosed。
- 允许所选浏览器或文件管理器安装 APK。

本项目面向较新的 Android 版本。单个 Hook 的兼容性取决于 ROM、Android 版本以及目标应用的 API 实现。

## 安装

1. 安装并配置 [Magisk](https://github.com/topjohnwu/Magisk) 与 [LSPosed](https://github.com/LSPosed/LSPosed)。
2. 从[最新版本页面](https://github.com/Mobilelegends74/XPL-EX/releases/latest)下载 APK。
3. 安装 APK，在 LSPosed 中启用 XPL-EX-NEXT，并在作用域中勾选所需的目标应用。
4. 打开 XPL-EX-NEXT，选择应用，只配置你需要的 Hook 组。
5. 重启目标应用。更新模块自身后建议重启设备。

自 1.6.7 起，分配 Hook 时会请求将目标应用加入 LSPosed 作用域。如出现请求请确认；请保持 System Framework 启用。若请求被阻止，请在 LSPosed 模块列表中长按 XPL-EX，然后选择 **Reset scope request setting**。其他 Android 用户档案需单独配置。

### 迁移到固定签名证书

`1.6.0` 之前的版本由 GitHub Actions 使用临时证书构建。Android 不允许用不同证书签名的 APK 覆盖安装。

首次从 `1.5.12` 或更早版本迁移时：

1. 导出 XPL-EX 设置。
2. 卸载旧版本。
3. 安装 `1.6.0` 或更新版本，并导入设置。

自 `1.6.0` 起，所有官方版本使用同一个固定证书，可直接覆盖安装上一版本。

## 更新

XPL-EX-NEXT 启动时会检查[本仓库的最新版本](https://github.com/Mobilelegends74/XPL-EX/releases/latest)，频率不超过每 12 小时一次。若存在更新版本，应用会提示下载已签名的 APK。

LSPosed 会从其官方模块仓库显示更新。其中的 `eu.faircode.xlua` 条目属于原版 XPrivacyLua，因此本分支的更新由 XPL-EX-NEXT 自行检查。

## 重要限制

- 本模块修改的是本地 API 的返回结果，**无法让设备真正匿名**。
- 服务端校验与原生代码仍可通过其他途径获取信息。
- 不要无故同时启用全部 Hook；不兼容的组合可能导致目标应用异常。
- 重大更新前请备份配置。
- 请仅在你拥有管理权限的设备与应用上使用本项目。

## 界面截图

<details>
<summary>展开界面截图</summary>

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

## 文档与反馈

- [版本与更新日志](https://github.com/Mobilelegends74/XPL-EX/releases)
- [问题反馈与功能建议](https://github.com/Mobilelegends74/XPL-EX/issues)
- [上游常见问题](FAQ.md)
- [API 参考](APIHELP.md)
- [GitHub Actions 构建](https://github.com/Mobilelegends74/XPL-EX/actions)

提交 issue 时请注明 XPL-EX-NEXT、Android 与 LSPosed 的版本，受影响的 API 或 Hook 名称，并附上脱敏后的复现日志。

## 作者与致谢

- [M66B](https://github.com/M66B) —— 原版 [XPrivacyLua](https://github.com/M66B/XPrivacyLua) 作者。
- [ObbedCode / 0bbedCode](https://github.com/0bbedCode) —— 本分支所基于的扩展版 [XPL-EX](https://github.com/0bbedCode/XPL-EX) 作者。
- [VD-8](https://github.com/VD171/VD-Infos) —— VD-Infos 及相关工作。
- [HUBERTH](https://t.me/HubertHub) —— 检测方法研究与项目协助。
- [LSPosed](https://github.com/LSPosed/LSPosed) 与 [libxposed](https://github.com/libxposed) —— Xposed API 基础设施。
- [Mobilelegends74](https://github.com/Mobilelegends74) —— XPL-EX-NEXT 维护、修复与当前版本发布。

## 支持开发

如果本项目对你有帮助，你可以支持其后续开发。转账前请务必确认所选网络。

### XPL-EX-NEXT / Mobilelegends74

**BEP-20**

```text
0xafe2e8f8eb4792885026dd0296aae72311f00777
```

**TRC-20**

```text
TJREgqMhwGa2z47nJN4dRG5VEYM1P7sHx3
```

### 上游 XPL-EX / ObbedCode

**TRX (TRON)**

```text
TRk5a1C4U5fTgMbZQBi7wRM1hjvguPnqBb
```

## 许可证

本项目依据 [GNU General Public License v3.0](LICENSE) 分发。衍生作品必须保留许可证条款与上游署名声明。
