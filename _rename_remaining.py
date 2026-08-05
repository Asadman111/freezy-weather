import os
import re
import shutil
import glob as glob_mod

def replace_in_file(filepath, replacements):
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
        original = content
        for old, new in replacements:
            content = content.replace(old, new)
        if content != original:
            with open(filepath, 'w', encoding='utf-8') as f:
                f.write(content)
            print(f"  Updated: {filepath}")
            return True
    except Exception as e:
        print(f"  ERROR in {filepath}: {e}")
    return False

# STEP 1: Rename remaining breezyweather directories in source trees
print("STEP 1: Renaming remaining breezyweather directories...")
patterns_to_rename = [
    'app/src/src_nonfreenet/org',
    'app/src/src_freenet/org',
    'app/src/test/kotlin/org',
    'data/src/main/sqldelight',
    'data/src/test/kotlin/org',
    'domain/src/main/kotlin',
    'domain/src/test/kotlin/org',
    'maps-utils/src/test/kotlin/org',
    'ui-weather-view/src/test/kotlin/org',
    'weather-unit/src/test/kotlin/org',
]

for pattern in patterns_to_rename:
    old_dir = os.path.join(pattern, 'breezyweather')
    new_dir = os.path.join(pattern, 'freezyweather')
    if os.path.exists(old_dir):
        os.makedirs(os.path.dirname(new_dir), exist_ok=True)
        shutil.move(old_dir, new_dir)
        print(f"  Renamed: {old_dir} -> {new_dir}")

# Also check if there are breezyweather dirs inside domain/src/main/kotlin/org etc
for root, dirs, files in os.walk('.'):
    for d in dirs[:]:
        if d == 'breezyweather':
            old_path = os.path.join(root, d)
            new_path = os.path.join(root, 'freezyweather')
            print(f"  Found directory: {old_path}, renaming to {new_path}")
            shutil.move(old_path, new_path)
            dirs.remove(d)

print("  Done")

# STEP 2: Update any remaining Kotlin files with breezyweather references
print("\nSTEP 2: Updating remaining Kotlin files...")
for root, dirs, files in os.walk('.'):
    for f in files:
        if f.endswith('.kt') or f.endswith('.kts'):
            fp = os.path.join(root, f)
            replace_in_file(fp, [
                ('package org.breezyweather', 'package org.freezyweather'),
                ('import org.breezyweather', 'import org.freezyweather'),
                ('package breezy.buildlogic', 'package freezy.buildlogic'),
                ('import breezy.buildlogic', 'import freezy.buildlogic'),
                ('package breezyweather', 'package freezyweather'),
            ])
print("  Done")

# STEP 3: Update SQLDelight files
print("\nSTEP 3: Updating SQLDelight files...")
for root, dirs, files in os.walk('data/src/main/sqldelight'):
    for f in files:
        if f.endswith('.sq'):
            fp = os.path.join(root, f)
            print(f"  Checking: {fp}")
            # SQLDelight files reference the package name
            replace_in_file(fp, [
                ('breezyweather', 'freezyweather'),
            ])
print("  Done")

# STEP 4: Update remaining build.gradle.kts files that still have old refs
print("\nSTEP 4: Updating remaining build files...")
for root, dirs, files in os.walk('.'):
    if '.git' in root:
        continue
    for f in files:
        if f == 'build.gradle.kts':
            fp = os.path.join(root, f)
            replace_in_file(fp, [
                ('breezy.buildlogic', 'freezy.buildlogic'),
                ('namespace = "org.breezyweather"', 'namespace = "org.freezyweather"'),
                ('libs.breezy.', 'libs.freezy.'),
            ])
print("  Done")

# STEP 5: Update proguard files
print("\nSTEP 5: Updating proguard files...")
replace_in_file('app/proguard-rules.pro', [
    ('org.breezyweather', 'org.freezyweather'),
])
print("  Done")

# STEP 6: Update config-fork library files
print("\nSTEP 6: Updating config-fork files...")
for root, dirs, files in os.walk('config-fork'):
    for f in files:
        fp = os.path.join(root, f)
        replace_in_file(fp, [
            ('Breezy Weather', 'Freezy Weather'),
            ('breezy-weather', 'freezy-weather'),
            ('breezyweather', 'freezyweather'),
        ])
print("  Done")

# STEP 7: Update config library files
print("\nSTEP 7: Updating config files...")
for root, dirs, files in os.walk('config'):
    for f in files:
        fp = os.path.join(root, f)
        replace_in_file(fp, [
            ('Breezy Weather', 'Freezy Weather'),
            ('breezy-weather', 'freezy-weather'),
            ('breezyweather', 'freezyweather'),
        ])
print("  Done")

# STEP 8: Update markdown docs
print("\nSTEP 8: Updating markdown docs...")
for fp in ['CHANGELOG.md', 'INSTALL.md', 'README.md', 'docs/TECHNICAL.md']:
    replace_in_file(fp, [
        ('org.breezyweather', 'org.freezyweather'),
        ('Breezy Weather', 'Freezy Weather'),
        ('breezy-weather', 'freezy-weather'),
    ])
print("  Done")

# STEP 9: Check for BreezyWeather.kt and rename
print("\nSTEP 9: Finding and renaming BreezyWeather.kt...")
for root, dirs, files in os.walk('app/src/main/kotlin'):
    for f in files:
        if f == 'BreezyWeather.kt':
            old_path = os.path.join(root, f)
            new_path = os.path.join(root, 'FreezyWeather.kt')
            shutil.move(old_path, new_path)
            print(f"  Renamed: {old_path} -> {new_path}")
print("  Done")

# STEP 10: Final check
print("\n========================================")
print("STEP 10: Final check for remaining references...")
import subprocess
try:
    result = subprocess.run(['git', 'grep', '-n', 'org\\.breezyweather'], 
                          capture_output=True, text=True, cwd='.')
    remaining = result.stdout.strip()
    if remaining:
        print("  REMAINING REFERENCES:")
        print(remaining)
    else:
        print("  No remaining org.breezyweather references found!")

    result2 = subprocess.run(['git', 'grep', '-n', 'Breezy Weather'], 
                          capture_output=True, text=True, cwd='.')
    remaining2 = result2.stdout.strip()
    if remaining2:
        print("\n  REMAINING 'Breezy Weather' REFERENCES:")
        print(remaining2)
    else:
        print("  No remaining 'Breezy Weather' references found!")
        
except Exception as e:
    print(f"  Error: {e}")

print("\n========================================")
print("ALL DONE!")
print("========================================")