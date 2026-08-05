import os
import re
import shutil

ROOT = '.'

def replace_in_file(filepath, replacements):
    """Replace text in a file using exact string replacements."""
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
        original = content
        for old, new in replacements:
            content = content.replace(old, new)
        if content != original:
            with open(filepath, 'w', encoding='utf-8') as f:
                f.write(content)
            return True
    except Exception as e:
        print(f"  ERROR in {filepath}: {e}")
    return False

def replace_in_files(pattern, replacements):
    """Apply replacements to all files matching glob pattern."""
    import glob
    count = 0
    for filepath in glob.glob(pattern, recursive=True):
        if replace_in_file(filepath, replacements):
            count += 1
    return count

def rename_files(pattern, old_part, new_part):
    """Rename files/directories matching pattern."""
    import glob
    count = 0
    for filepath in glob.glob(pattern, recursive=False):
        if old_part in filepath:
            new_path = filepath.replace(old_part, new_part)
            shutil.move(filepath, new_path)
            print(f"  Renamed: {filepath} -> {new_path}")
            count += 1
    return count

# ============================================================
# STEP 1: Update settings.gradle.kts
# ============================================================
print("STEP 1: Updating settings.gradle.kts...")
replace_in_file('settings.gradle.kts', [
    ('rootProject.name = "BreezyWeather"', 'rootProject.name = "FreezyWeather"'),
])
print("  Done")

# ============================================================
# STEP 2: Update gradle.properties
# ============================================================
print("STEP 2: Updating gradle.properties...")
replace_in_file('gradle.properties', [
    ('breezy.report_issue=', 'freezy.report_issue='),
    ('breezy.source_code_link=', 'freezy.source_code_link='),
    ('breezy.releases_link=', 'freezy.releases_link='),
    ('breezy.install_instructions_link=', 'freezy.install_instructions_link='),
    ('breezy.privacy_policy_link=', 'freezy.privacy_policy_link='),
    ('breezy.icon_packs_link=', 'freezy.icon_packs_link='),
    ('breezy.matrix_link=', 'freezy.matrix_link='),
    ('breezy.github.org=', 'freezy.github.org='),
    ('breezy.github.repo=', 'freezy.github.repo='),
    ('breezy.github.release_prefix=', 'freezy.github.release_prefix='),
])
print("  Done")

# ============================================================
# STEP 3: Update buildSrc Config files
# ============================================================
print("STEP 3: Updating buildSrc...")

# Update BuildConfig.kt
replace_in_file('buildSrc/src/main/kotlin/breezy/buildlogic/BuildConfig.kt', [
    ('Breezy Weather', 'Freezy Weather'),
    ('package breezy.buildlogic', 'package freezy.buildlogic'),
    ('val isBreezy: Boolean', 'val isFreezy: Boolean'),
    ('override val isBreezy: Boolean = project.hasProperty("breezy")', 'override val isFreezy: Boolean = project.hasProperty("freezy")'),
])

# Update other buildSrc files
for pattern in ['buildSrc/src/main/kotlin/breezy/buildlogic/*.kt']:
    import glob
    for fp in glob.glob(pattern):
        replace_in_file(fp, [
            ('package breezy.buildlogic', 'package freezy.buildlogic'),
            ('Breezy Weather', 'Freezy Weather'),
        ])

# Rename buildSrc directory
old_dir = 'buildSrc/src/main/kotlin/breezy'
new_dir = 'buildSrc/src/main/kotlin/freezy'
if os.path.exists(old_dir):
    os.makedirs(os.path.dirname(new_dir), exist_ok=True)
    shutil.move(old_dir, new_dir)
    print(f"  Renamed directory: {old_dir} -> {new_dir}")

print("  Done")

# ============================================================
# STEP 4: Update ALL Kotlin files - package/import replacements
# ============================================================
print("STEP 4: Updating Kotlin files...")
kt_count = replace_in_files('**/*.kt', [
    ('package org.breezyweather', 'package org.freezyweather'),
    ('import org.breezyweather', 'import org.freezyweather'),
    ('package breezy.buildlogic', 'package freezy.buildlogic'),
    ('import breezy.buildlogic', 'import freezy.buildlogic'),
])
print(f"  Updated {kt_count} Kotlin files")

# ============================================================
# STEP 5: Update AndroidManifest.xml
# ============================================================
print("STEP 5: Updating AndroidManifest.xml...")
replace_in_file('app/src/main/AndroidManifest.xml', [
    ('android:name=".BreezyWeather"', 'android:name=".FreezyWeather"'),
    ('org.breezyweather.ICON_PROVIDER', 'org.freezyweather.ICON_PROVIDER'),
    ('org.breezyweather.PROVIDER_CONFIG', 'org.freezyweather.PROVIDER_CONFIG'),
    ('org.breezyweather.DRAWABLE_FILTER', 'org.freezyweather.DRAWABLE_FILTER'),
    ('org.breezyweather.ANIMATOR_FILTER', 'org.freezyweather.ANIMATOR_FILTER'),
    ('org.breezyweather.SHORTCUT_FILTER', 'org.freezyweather.SHORTCUT_FILTER'),
    ('org.breezyweather.SUN_MOON_FILTER', 'org.freezyweather.SUN_MOON_FILTER'),
])
print("  Done")

# ============================================================
# STEP 6: Update ALL XML files - org.breezyweather -> org.freezyweather
# ============================================================
print("STEP 6: Updating XML layout/widget files...")
xml_count = replace_in_files('app/src/main/res/**/*.xml', [
    ('org.breezyweather', 'org.freezyweather'),
])
print(f"  Updated {xml_count} XML resource files")

# ============================================================
# STEP 7: Update brand_name in res_breezy and res_fork
# ============================================================
print("STEP 7: Updating brand_name...")
if os.path.exists('app/src/res_breezy/values/strings.xml'):
    replace_in_file('app/src/res_breezy/values/strings.xml', [
        ('Breezy Weather', 'Freezy Weather'),
    ])
if os.path.exists('app/src/res_freezy/values/strings.xml'):
    replace_in_file('app/src/res_freezy/values/strings.xml', [
        ('Breezy Weather', 'Freezy Weather'),
    ])
if os.path.exists('app/src/res_fork/values/strings.xml'):
    replace_in_file('app/src/res_fork/values/strings.xml', [
        ('No Name Weather', 'Freezy Weather'),
    ])
print("  Done")

# ============================================================
# STEP 8: Update main strings.xml
# ============================================================
print("STEP 8: Updating app/src/main/res/values/strings.xml...")
replace_in_file('app/src/main/res/values/strings.xml', [
    ('<string name="breezy_weather" translatable="false">Breezy Weather</string>', '<string name="breezy_weather" translatable="false">Freezy Weather</string>'),
])
print("  Done")

# ============================================================
# STEP 9: Update all translation files for Breezy Weather text
# ============================================================
print("STEP 9: Updating translation files (Breezy Weather -> Freezy Weather)...")
trans_count = replace_in_files('app/src/main/res/values-*/strings.xml', [
    ('Breezy Weather', 'Freezy Weather'),
])
print(f"  Updated {trans_count} translation files")

# ============================================================
# STEP 10: Update other XML files at root of res
# ============================================================
print("STEP 10: Updating other XML files...")
# Widget config XML files in xml/ directory
widget_count = replace_in_files('app/src/main/res/xml/*.xml', [
    ('org.breezyweather', 'org.freezyweather'),
    ('Breezy Weather', 'Freezy Weather'),
])
print(f"  Updated {widget_count} widget XML files")

# ============================================================
# STEP 11: Rename res_breezy directory to res_freezy
# ============================================================
print("STEP 11: Renaming res_breezy to res_freezy...")
res_breezy = 'app/src/res_breezy'
res_freezy = 'app/src/res_freezy'
if os.path.exists(res_breezy) and not os.path.exists(res_freezy):
    shutil.move(res_breezy, res_freezy)
    print(f"  Renamed: {res_breezy} -> {res_freezy}")
else:
    print("  Already done or not found")

# ============================================================
# STEP 12: Update gradle libs.versions.toml
# ============================================================
print("STEP 12: Updating gradle/libs.versions.toml...")
replace_in_file('gradle/libs.versions.toml', [
    ('breezy-weather-data-sharing', 'freezy-weather-data-sharing'),
])
print("  Done")

# ============================================================
# STEP 13: Update other gradle scripts (data, domain, etc. build.gradle.kts)
# ============================================================
print("STEP 13: Updating submodule build.gradle.kts files...")
sub_count = replace_in_files('**/build.gradle.kts', [
    ('breezy.buildlogic', 'freezy.buildlogic'),
    ('"breezy."', '"freezy."'),
    ('id("breezy.', 'id("freezy.'),
    ('namespace = "org.breezyweather"', 'namespace = "org.freezyweather"'),
    ('libs.breezy.', 'libs.freezy.'),
])
print(f"  Updated {sub_count} submodule build files")

# ============================================================
# STEP 14: Update buildSrc build.gradle.kts
# ============================================================
print("STEP 14: Updating buildSrc build.gradle.kts...")
replace_in_file('buildSrc/build.gradle.kts', [
    ('breezy.buildlogic', 'freezy.buildlogic'),
])
print("  Done")

# ============================================================
# STEP 15: Check for any remaining "breezy" references in key files
# ============================================================
print("STEP 15: Checking for remaining references...")
# Just report, don't change
import subprocess
try:
    result = subprocess.run(['git', 'grep', '-l', 'org\\.breezyweather'], 
                          capture_output=True, text=True, cwd=ROOT)
    remaining = result.stdout.strip().split('\n') if result.stdout.strip() else []
    if remaining:
        print(f"  Found {len(remaining)} files still referencing org.breezyweather:")
        for f in remaining:
            print(f"    - {f}")
    else:
        print("  No remaining org.breezyweather references found!")
except:
    print("  Git grep not available, skipping check")

print("\n========================================")
print("ALL DONE! Please review the changes.")
print("========================================")