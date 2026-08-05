import os
import re
import shutil

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
            return True
    except Exception as e:
        print(f"  ERROR in {filepath}: {e}")
    return False

# ============================================================
# ALL Breezy -> Freezy class name replacements
# ============================================================
CLASS_REPLACEMENTS = [
    # Main classes/files (order matters - longest first to avoid partial matches)
    ('BreezyFloatingTextActionModeCallback', 'FreezyFloatingTextActionModeCallback'),
    ('BreezyPrimaryTextActionModeCallback', 'FreezyPrimaryTextActionModeCallback'),
    ('BreezyTextActionModeCallback', 'FreezyTextActionModeCallback'),
    ('BreezyUpdateNotifierService', 'FreezyUpdateNotifierService'),
    ('BreezyTimeZoneService', 'FreezyTimeZoneService'),
    ('BreezyWeatherTheme', 'FreezyWeatherTheme'),
    ('BreezyWeather', 'FreezyWeather'),
    ('BreezyActivity', 'FreezyActivity'),
    ('BreezyFragment', 'FreezyFragment'),
    ('BreezyViewModel', 'FreezyViewModel'),
    ('BreezyLineChart', 'FreezyLineChart'),
    ('BreezyTextToolbar', 'FreezyTextToolbar'),
    # Data sharing JSON classes
    ('BreezyAirQuality', 'FreezyAirQuality'),
    ('BreezyAlert', 'FreezyAlert'),
    ('BreezyBulletin', 'FreezyBulletin'),
    ('BreezyCurrent', 'FreezyCurrent'),
    ('BreezyDaily', 'FreezyDaily'),
    ('BreezyDailyUnit', 'FreezyDailyUnit'),
    ('BreezyDegreeDay', 'FreezyDegreeDay'),
    ('BreezyHalfDay', 'FreezyHalfDay'),
    ('BreezyHourly', 'FreezyHourly'),
    ('BreezyMinutely', 'FreezyMinutely'),
    ('BreezyNormals', 'FreezyNormals'),
    ('BreezyPollen', 'FreezyPollen'),
    ('BreezyPollutant', 'FreezyPollutant'),
    ('BreezyPrecipitation', 'FreezyPrecipitation'),
    ('BreezyPrecipitationDuration', 'FreezyPrecipitationDuration'),
    ('BreezyPrecipitationProbability', 'FreezyPrecipitationProbability'),
    ('BreezySource', 'FreezySource'),
    ('BreezyTemperature', 'FreezyTemperature'),
    ('BreezyUnit', 'FreezyUnit'),
    ('BreezyWind', 'FreezyWind'),
    # Variable/method name changes
    ('isSignedByBreezy', 'isSignedByFreezy'),
    ('isImpersonatingBreezyWeather', 'isImpersonatingFreezyWeather'),
    ('startBreezyActivity', 'startFreezyActivity'),
]

BRANDING_REPLACEMENTS = [
    # Brand string checks - "breezy" -> "freezy" in code logic
    ('contains("breezy"', 'contains("freezy"'),
    ('contains("breezy', 'contains("freezy'),
    ('contains("breezyWeather"', 'contains("freezyWeather"'),
    ('"Breezy Weather"', '"Freezy Weather"'),
]

DOMAIN_IMPORT_REPLACEMENTS = [
    # breezeWeather domain imports that were missed
    ('breezeWeather.domain', 'freezyweather.domain'),
    ('breezyweather.domain', 'freezyweather.domain'),
]

TAG_REPLACEMENTS = [
    ('"BreezyWeather"', '"FreezyWeather"'),
]

# ============================================================
# STEP 1: Rename all Breezy*.kt files to Freezy*.kt
# ============================================================
print("STEP 1: Renaming Breezy*.kt files to Freezy*.kt...")
count = 0
for root, dirs, files in os.walk('.'):
    if '.git' in root:
        continue
    for f in files[:]:
        if f.startswith('Breezy') and f.endswith('.kt'):
            old_path = os.path.join(root, f)
            new_name = f.replace('Breezy', 'Freezy', 1)
            new_path = os.path.join(root, new_name)
            os.rename(old_path, new_path)
            print(f"  Renamed: {old_path} -> {new_path}")
            count += 1
print(f"  Renamed {count} files")

# ============================================================
# STEP 2: Update all Kotlin files with class name replacements
# ============================================================
print("\nSTEP 2: Updating all Kotlin files...")
kt_count = 0
for root, dirs, files in os.walk('.'):
    if '.git' in root:
        continue
    for f in files:
        if f.endswith('.kt') or f.endswith('.kts'):
            fp = os.path.join(root, f)
            if replace_in_file(fp, CLASS_REPLACEMENTS):
                kt_count += 1
print(f"  Updated {kt_count} Kotlin files")

# ============================================================
# STEP 3: Update branding checks
# ============================================================
print("\nSTEP 3: Updating branding checks...")
b_count = 0
for root, dirs, files in os.walk('.'):
    if '.git' in root:
        continue
    for f in files:
        if f.endswith('.kt') or f.endswith('.kts'):
            fp = os.path.join(root, f)
            if replace_in_file(fp, BRANDING_REPLACEMENTS):
                b_count += 1
print(f"  Updated {b_count} files with branding changes")

# ============================================================
# STEP 4: Update domain imports
# ============================================================
print("\nSTEP 4: Updating domain imports...")
d_count = 0
for root, dirs, files in os.walk('.'):
    if '.git' in root:
        continue
    for f in files:
        if f.endswith('.kt') or f.endswith('.kts') or f.endswith('.xml'):
            fp = os.path.join(root, f)
            if replace_in_file(fp, DOMAIN_IMPORT_REPLACEMENTS):
                d_count += 1
print(f"  Updated {d_count} files with domain import changes")

# ============================================================
# STEP 5: Update TAG constants
# ============================================================
print("\nSTEP 5: Updating TAG constants...")
t_count = 0
for root, dirs, files in os.walk('.'):
    if '.git' in root:
        continue
    for f in files:
        if f.endswith('.kt'):
            fp = os.path.join(root, f)
            if replace_in_file(fp, TAG_REPLACEMENTS):
                t_count += 1
print(f"  Updated {t_count} files with TAG changes")

# ============================================================
# STEP 6: Update XML files for BreezyWeather references
# ============================================================
print("\nSTEP 6: Updating XML files...")
x_count = 0
for root, dirs, files in os.walk('app/src/main/res'):
    for f in files:
        if f.endswith('.xml'):
            fp = os.path.join(root, f)
            if replace_in_file(fp, [
                ('BreezyWeather', 'FreezyWeather'),
                ('Breezy Weather', 'Freezy Weather'),
            ]):
                x_count += 1
print(f"  Updated {x_count} XML files")

# ============================================================
# STEP 7: Update styles
# ============================================================
print("\nSTEP 7: Updating style files...")
for root, dirs, files in os.walk('app/src/main/res'):
    for f in files:
        if f == 'styles.xml' or f == 'themes.xml':
            fp = os.path.join(root, f)
            replace_in_file(fp, [
                ('BreezyWeatherTheme', 'FreezyWeatherTheme'),
            ])

# ============================================================
# STEP 8: Update buildSrc settings.gradle.kts
# ============================================================
print("\nSTEP 8: Updating buildSrc settings...")
replace_in_file('buildSrc/settings.gradle.kts', [
    ('breezy', 'freezy'),
])

# ============================================================
# STEP 9: Update Fastlane metadata
# ============================================================
print("\nSTEP 9: Updating fastlane metadata...")
for root, dirs, files in os.walk('fastlane'):
    for f in files:
        if f == 'title.txt' or f == 'short_description.txt' or f == 'full_description.txt':
            fp = os.path.join(root, f)
            try:
                with open(fp, 'r', encoding='utf-8') as file:
                    content = file.read()
                original = content
                content = content.replace('Breezy Weather', 'Freezy Weather')
                content = content.replace('BreezyWeather', 'FreezyWeather')
                if content != original:
                    with open(fp, 'w', encoding='utf-8') as file:
                        file.write(content)
                    print(f"  Updated: {fp}")
            except:
                pass
print("  Done")

# ============================================================
# STEP 10: Check for remaining issues
# ============================================================
print("\n========================================")
print("STEP 10: Final check...")
import subprocess
try:
    # Check for remaining BreezyWeather class references
    result = subprocess.run(['git', 'grep', '-c', 'class BreezyWeather'], 
                          capture_output=True, text=True, cwd='.')
    if result.stdout.strip():
        print("  REMAINING 'class BreezyWeather':")
        print(result.stdout.strip())
    
    # Check for remaining BreezyActivity
    result = subprocess.run(['git', 'grep', '-c', 'BreezyActivity'], 
                          capture_output=True, text=True, cwd='.')
    if result.stdout.strip():
        lines = result.stdout.strip().split('\n')
        # Filter out only lines where count > 0
        remaining_lines = [l for l in lines if not l.endswith(':0')]
        if remaining_lines:
            print("  REMAINING 'BreezyActivity':")
            print('\n'.join(remaining_lines))
        else:
            print("  No remaining 'BreezyActivity' references!")
    else:
        print("  No 'BreezyActivity' references!")
        
    # Check for remaining "breezy" (not freezy)
    result = subprocess.run(['git', 'grep', '-c', 'breezy'], 
                          capture_output=True, text=True, cwd='.')
    if result.stdout.strip():
        lines = [l for l in result.stdout.strip().split('\n') if not l.endswith(':0')]
        # Filter out lines that are comments, license headers, etc.
        important = [l for l in lines if not any(x in l for x in [
            '.git/', '_rename_', 'work/', '.psd', '.svg', '.png', 'fastlane/',
            'LICENSE', '3RD_PARTY', 'CHANGELOG', 'docs/'
        ])]
        if important:
            print(f"  {len(important)} files still reference 'breezy':")
            for line in important[:20]:
                print(f"    {line}")
        else:
            print("  Only non-source files reference 'breezy'")
    
except Exception as e:
    print(f"  Git grep error: {e}")

print("\n========================================")
print("ALL DONE!")
print("========================================")