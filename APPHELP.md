# XPL-EX Documentation

---

## 📌 Introduction

Welcome! This documentation is designed to help you get the most out of **XPL-EX**. Whether you're a beginner or a seasoned power user, this guide covers all major areas clearly and effectively.

Over time, we've added help dialogs, examples, tooltips, and contextual menus. Your role as the user is to **read**, **explore**, and **experiment**. If you're asking questions that can be answered by a tooltip, the help menu, or a quick Google search (e.g., *"What is an MCC?"*), you’ll be redirected here.<br><br>
Selecting ALL Hooks at Once is a FOOLISH Choice and WILL be Ignored if you have Issues, Select what you Need or you think you need. Every app is Different, if the Hook Group Selected Causes a Crash you know now not to use it...<br>

Please be respectful of the time invested in building this tool — use the resources made available to you first before asking questions.

> 🧠 TL;DR: Click, read, debug, Google — then ask.

---

## 🔒 Laziness Policy (Randomization & Expectations)

XPL-EX is a **gatekeeper**, not a babysitter. While we provide tools such as one-click randomizers, we do **not** perform value validation, cross-checks against databases, or automatic configuration updates.

### ❗ Why?

Implementing a randomizer that outputs realistic values would require:

1. Building and maintaining a full database of valid device info
2. Cross-validating each setting against others
3. Handling edge cases like custom spoofing values

This is **outside the scope** of XPL-EX.

> Use `%random%` for quick outputs — but if you want realism, research it yourself.

* Want a Samsung Note 9 profile? Google:

  * `Samsung Note 9 build.prop`
  * `Samsung Note 9 SoC`
* Then input those values manually.

**We DO provide randomizers** for most common non-unique fields (e.g., Model, Build Date). However, we do not support:

* Complex cross-checked device configurations
* Requests to add specific devices
* Complaints about unrealistic outputs from randomization

> 📣 If it’s not used to uniquely identify you, stop obsessing over it. Focus on what matters.

### 🌀 How `%random%` Works

When you assign `%random%` as a value to a setting, this instructs XPL-EX to replace that placeholder with a randomly generated value **at runtime — before the target app starts**. Each time the app stops and starts again, this process is repeated.

However, this only works **if the setting has a valid randomizer assigned**. You can verify this by:

* Clicking the Randomize button — if it says **"No Randomizer Selected"**, there's no valid randomizer.
* Checking the current randomizer name — if it's set to **`[Select Randomizer]`**, then none has been assigned.

In these cases, `%random%` will do nothing.

### ❌ Why No Cross-Check Randomizers?

Creating cross-consistent random values would require:

* Linking one setting’s random output to others
* Having a backend database that maps valid combinations
* Continual maintenance as new devices and Android versions emerge

That’s a huge undertaking — and not our focus.

Instead, **we provide randomized outputs for all unique settings** and most general settings where applicable. For the rest:

* Use the provided **descriptions and examples** in the UI
* Explore outputs from existing randomizers for inspiration
* Manually fill in values using realistic examples

---

<br><br>
## 🎛️ Main UI Overview

Here's what you'll see when you launch XPL-EX:

<br>
<details>
  <summary>Screenshot with Layout Numbers</summary>
  
![photo_2025-05-09_20-38-33](https://github.com/user-attachments/assets/a5e6aa1b-4702-4b33-96f5-80dd407060af)

</details>

### App List Controls

1. **Filter**: Filter which apps are shown
2. **Search**: Quickly find your apps
3. **Help (❓)**: Understand icon meanings and functions
4. **Hamburger Menu (≡)**: Access core features:

   * \[4.1] Notify on new apps
   * \[4.2] Documentation
   * \[4.3] FAQ
   * \[4.4] Donate
   * \[4.5] Dark Mode toggle
   * \[4.6] Collections
   * \[4.7] Debug Logs (WIP)
   * \[4.8] Force English
   * \[4.9] Setting Values (Global Settings)
   * \[4.10] Hooks UI
   * \[4.11] Import Settings
   * \[4.12] Export Settings


<br>
<details>
  <summary>Side Menu Screenshot with Numbers</summary>
  
  ![photo_2025-05-09_20-38-45](https://github.com/user-attachments/assets/8173e804-0727-4517-8a90-cb3dfd127c8c)


</details>

### App Cards

Each app card contains:

* Icon & name
* UID
* **⚙️ Settings Icon**: Per-app hook values (e.g., Android ID override)
* **📥 Dropdown**: Group hook visibility
* **☑️ Checkbox**: Enable all hooks (⚠️ not recommended!)

Hooks are organized into **Collections**, then into **Groups** within each collection.

> Click the ❓ next to a group name for a brief description.

---

<br><br>
## ⚙️ Settings UI

This is where you control spoofed values used by hooks. Example: Android ID, DRM ID, Ad ID, etc.

Hooks read values from here. No value = no spoofing.

### Features:

<details>
  <summary>Screenshot of Layout with Numbers</summary>
  
![photo_2025-05-09_21-07-05](https://github.com/user-attachments/assets/6840b2a8-655b-4e21-ab93-81f3c8050b92)


</details>

1. **Filter Settings**
2. **Search Settings**
3. **Help Dialog**
4. **App Island Menu**:

   * \[4.1] Save Checked
   * \[4.2] Clear App Data
   * \[4.3] Reset Settings
   * \[4.4] Force Stop
   * \[4.5] Logs (Old vs New value shown)
   * \[4.6] Profiles (Coming soon)
   * \[4.7] Export Configs
   * \[4.8] Create Config
   * \[4.9] Auto Force Stop (Per action basis)

At the bottom-right, there’s a **Floating Action Button** that opens 5 quick tools. Refer to the Help Dialog for exact functionality.

> ⚠️ `%random%` only works with supported settings. Avoid using it on booleans — just pick True or False.

You can create and apply **configs** across apps or export individual setups.<br>
Note, for the buttons that Rest below the Setting, you Can horizontally Scroll, So if they are Cut off just Scroll...

---

<br><br>
## 🌐 Global vs App Context

In XPL-EX, every setting can exist in two contexts:

* **Global Context**: This applies universally across all apps. It provides default values for any setting that hasn't been overridden per app.
* **App Context**: These are per-app overrides. If you assign a value for a setting in an app’s context, it takes priority over the global value.

### 🔄 Priority Behavior

When an app accesses a value that a hook is intercepting:

1. XPL-EX checks if there is a **per-app value (App Context)** set for that setting.
2. If **none is found**, it falls back to the **Global Context**.
3. If neither has a value, the hook will effectively do nothing or return default behavior.

### 🧪 Example Scenario

Let’s say you have the `ANDROID_ID` hook enabled for App A:

* You assign `ANDROID_ID = 123abc` in Global Context.
* But you assign `ANDROID_ID = 789xyz` in App Context for App A.
* **Result**: When App A runs, it will use `789xyz` — the per-app value.

If you delete the per-app value:

* App A will now use `123abc`, the global value.

> ⚠️ If a spoofed value doesn’t seem to apply, check both contexts. The App Context always overrides Global.

### 🔧 Best Practices

* Use Global to define common baseline values
* Only use App Context when specific apps need unique identities
* Don’t duplicate values across both contexts unnecessarily

---

<br><br>
## 🧩 Hooks UI

Found under \[4.10] in the main menu.

Here you can:

* View all available hooks
* Use help dialogs
* Create or edit hooks

> This section is for advanced users. Proceed only if you know what you’re doing.

---

## 🎉 Final Thoughts

You’re in full control. XPL-EX gives you the tools, but how you use them is up to you.

* Don't blindly randomize — understand what you’re spoofing. (If its Unique Fuck it Randomize it all whenever)
* Use the help dialogs. They’re there for a reason.
* Share configs. Learn by experimenting.

> 🔐 Power comes with responsibility — and documentation.

---

Need visual references? Each `[X.X]` entry will later be linked to screenshots or UI elements.

---

<br><br>

## 🛠️ Reporting Errors & Obtaining Logs

When reporting an issue, proper logs are essential to diagnose the problem. Follow the steps below to ensure you're providing the correct information:

### ✅ What to Include When Reporting

* Name of the **target app** experiencing issues
* The **specific Hook Group** or Hook causing the problem
* A list of **Hooks enabled** for that app
* **Logcat logs** or **Settings UI logs** for the session

### 📄 Getting Logcat Logs

1. Install **Logcat Reader** (package name: `com.dp.logcatapp`) from your preferred app store.
2. Open the app and:

   * Tap the **three-dot menu** (top-right)
   * Tap **"Clear"** to start with a clean log
3. Use the **search icon** and search for: `xlua`
4. Reproduce the issue in the app
5. Return to Logcat Reader:

   * Tap the three-dot menu again
   * Tap **"Save"**
   * Share that saved log file along with your report

> 🔍 Use keywords like `xlua` to isolate relevant entries from XPL-EX within large logs.

### 🧪 Alternative Log Methods

If Logcat Reader does not work:

* Use **ADB logcat** via command line:

  ```bash
  adb logcat -v time > /sdcard/xplex_log.txt
  ```
or More Specific:
  ```bash
  logcat | grep -iE -B10 -A10 'XLua' &>> /sdcard/xplex_log.txt
  ```
Command will Vary and Can, You may need to exit aka with the Command use your Brain or Uninstall Android / Root. <br>

  Reproduce the issue, then stop the log and attach `xplex_log.txt` to your report.
* Try other apps such as **MatLog**, **Logcat Extreme**, or **Bug Report Reader* If your using Command Line use a Valid Path, Arguments, Perhaps use ROOT (SU) instead of ADB*. 

Do **not** assume a tool failure means you cannot report the issue. Use alternatives.

### 🧾 Settings UI Logs

XPL-EX includes non-error logs visible in the **Settings UI > App Island > Logs** section:

* These show changes made to settings
* Hook values replaced (old → new)
* Can be used for understanding behavior or confirming spoofing took place

---

## 🧱 Debugging, Behavior Limits, and Expected Results

### ⚙️ Native vs Java Hooking Limitations

As of **6/12/24**, XPL-EX **does not support native method hooking**. If an app retrieves values using native code (e.g., via `libc`, `fopen`, `__system_property_get`, or native `exec getprop`), XPL-EX will **not** intercept or spoof these.

#### 🧪 Common Signs of Native-Only Behavior

* You’ve set the correct hooks and spoofed values, but the app shows no change
* Known native-heavy apps like **Chrome**, **Brave**, or other Chromium browsers do not reflect spoofed user agents

> 🚫 If it happens in the native layer, XPL-EX cannot spoof it (yet).

Use logic and known app behavior to infer whether a detail is natively fetched. Due to time and scale, the XPL-EX team cannot analyze every app.

---

### 🔄 Wrong Value Being Spoofed? Try This

If a value appears spoofed but isn’t what you expected — test deliberately.

#### 🧪 Example Diagnostic

Assume you're trying to spoof device info:

1. Go into **XPL-EX > Settings**
2. Assign values like:

   * `device.manufacturer = dog`
   * `device.brand = cat`
   * `device.model = snake`
3. Open the target app and check how the information is displayed

> If your values appear, you’ve confirmed which setting was used. If not, the app may be pulling data differently.

---

### 🐛 LSPosed Settings Storage Checkbox

Sometimes in **LSPosed > XPL-EX scope**, you’ll see the `Settings Storage` module **unchecked** even though you selected it previously.

* This is likely a bug within LSPosed or a side effect of how XPL-EX interacts with it.
* If you’re unsure, **select `Settings Storage` again and reboot**.

> ✅ As long as it was checked at least once and you’ve rebooted, you should be fine — even if it appears unchecked later.

Keep this in mind to avoid confusion during setup.

---

## ⚠️ Deprecated Collections Warning

If your issue originates from the old `Privacy` collection or outdated builds:

* 🛑 No support will be provided
* Migrate to the updated `PrivacyEx` collection instead

If you’ve **manually modified** a hook and now face issues:

* We cannot help debug custom logic. Ensure your hook was not altered incorrectly.

### ⚠️ Missed Hook Warnings

Some Hook Groups display **Warning Messages** when selected. Always select Hook Groups **one by one** to ensure you:

* Trigger these warnings
* Understand potential side effects

Failure to read warnings or blindly selecting every Hook Group — then reporting crashes — will result in your report being ignored.

> 📣 TL;DR: Provide app name, hook info, logcat logs (via app or ADB), and pay attention to warnings!
