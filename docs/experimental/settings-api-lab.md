# XPL-EX NEXT 1.6.7 — Settings API Lab

Experimental **app-owned** SettingsProvider interoperability test.
This is not an anti-detection feature and does not change third-party apps.

## Architecture

The existing XPL-EX libxposed API 101 `SettingsProvider.call` hook
in `system_server` now invokes a lab-only callback for
`GET_secure / android_id`.

The callback captures `Binder.getCallingUid()` before executing the
provider code, validates the UID via PackageManager, and, only for the
bundled unshared application `eu.faircode.xlua.identitylab`, replaces
the returned `Bundle["value"]` with `1122334455667788`.

The normal result is preserved for every other package, system UID,
shared UID, failed provider request, or unknown response format.
It neither changes the database nor installs hooks into the lab
application's own process.

## Test steps

1. Install both signed APKs from this branch's Actions artifact.
2. Enable XPL-EX and **System Framework** in LSPosed.
3. Do NOT select the IdentityLab package in LSPosed scope.
4. Reboot after replacing the XPL-EX module, so the system process loads
   the experiment.
5. Open IdentityLab. Compare `Settings.Secure.getString`,
   `SettingsProvider.call`, and the control value `Build.MODEL`.
6. The expected lab Android ID is `1122334455667788`. The control
   Build value must remain the phone's original model.
7. Disable the XPL-EX module, reboot, and repeat to observe the real
   per-app SSAID.

If the direct provider call changes but the regular API does not,
investigate the Android NameValueCache/generation behavior. A cached
result must not be misreported as proof that the system hook failed.

## Safety and limitations

- This test is deliberately hard-restricted to IdentityLab.
- No general per-app identifier profile support is enabled.
- No IMEI, GAID, MediaDrm, Build, Native, or attestation changes.
- Never assume this can bypass a third-party app's protection.
- It changes a hook running in system_server; test on a spare device
  with a viable rollback path before installing on a daily driver.
- Original XPL-EX command bridge protocol and release tag are unchanged.
- This workflow produces **Actions artifacts only**, not a GitHub Release.
