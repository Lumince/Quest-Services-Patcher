# Quest Services Patcher (Vector module)
Bypasses protected-package and protected-permission-prefix checks in `services.jar` (system_server) on Meta Quest headsets.

Hooks into `PackageManagerService` and `ProtectedPermissionPrefixList` at runtime, so any package Meta marks as "protected" can be disabled or modified like a normal app — and any permission with a protected prefix is no longer blocked.

## Requirements
* Rooted Meta Quest Headset (Pre-August 4th 2026 firmware)
* Magisk w/ Zygisk: Yes
* LSPosed/Vector installed and active in Magisk

## How to use
1. If your Quest headset isn't rooted and is on a supported firmware, root it with [Singularity](https://github.com/Lumince/singularity/releases/)
2. Open Magisk Manager and install the latest stable [Vector](https://github.com/JingMatrix/Vector/releases/) magisk module
3. Check that Magisk shows `Zygisk: Yes` on the main page — if it does, reboot and go to step 5
4. If `Zygisk: Yes` isn't shown, go into Magisk settings and toggle Zygisk off then on. Then open Singularity → AIO Tweaks → Utils → Fix Magisk Zygisk → Apply
5. Install `ServicesPatcher.apk`
6. Open Vector, enable Services Patcher, and scope it to **System Framework** (`android`)
7. Soft reboot when prompted

## Version support
This module hooks at the class/method level (`ProtectedPackages.isPackageStateProtected` and `ProtectedPermissionPrefixList.isInProtectedPrefix`) rather than patching bytecode offsets, so it is **version-agnostic** and should work across any firmware as long as those class and method names remain stable in `services.jar`.

## Logging

```
adb logcat -s ServicesPatcher:D
```
