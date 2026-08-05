import os

print("=== Full scan for remaining 'breezy' text in source files ===")
found = []
for root, dirs, files in os.walk('.'):
    dirs[:] = [d for d in dirs if d not in ('.git', 'work', 'docs', 'fastlane')]
    for f in files:
        ext = f.split('.')[-1].lower()
        if ext in ('png','svg','jpg','jpeg','webp','jar','psd','lock'):
            continue
        fp = os.path.join(root, f)
        try:
            with open(fp, encoding='utf-8', errors='ignore') as fh:
                t = fh.read()
        except:
            continue
        if 'breezy' in t.lower():
            found.append(fp)

important = [fp for fp in found if not any(x in fp for x in [
    '3RD_PARTY.md', 'LICENSE', 'CHANGELOG', '_rename_',
    'fastlane', 'work/', '.psd', '.svg', '.png'
])]

if important:
    print(f"Found {len(important)} important files still with 'breezy':")
    for fp in important:
        print(f"  {fp}")
else:
    print("No important source files with 'breezy' found! (clean)")

if found:
    print(f"\nTotal {len(found)} files with 'breezy' (including docs/licenses):")
    for fp in found:
        print(f"  {fp}")

print("\n=== Verify FreezyWeather.kt exists ===")
if os.path.exists('app/src/main/kotlin/org/freezyweather/FreezyWeather.kt'):
    print("OK - FreezyWeather.kt exists")
else:
    print("ERROR - FreezyWeather.kt NOT FOUND!")

print("\n=== Check build files ===")
for fp in ['app/build.gradle.kts', 'settings.gradle.kts', 'gradle.properties']:
    try:
        with open(fp, encoding='utf-8') as fh:
            content = fh.read()
        if 'breezy' in content.lower():
            print(f"WARNING: {fp} still contains 'breezy'!")
        else:
            print(f"OK - {fp} is clean")
    except:
        print(f"ERROR reading {fp}")

print("\n=== Check key manifest references ===")
try:
    with open('app/src/main/AndroidManifest.xml', encoding='utf-8') as fh:
        manifest = fh.read()
    checks = ['BreezyWeather', 'BreezyActivity', 'BreezyFragment', 'BreezyTimeZone']
    for c in checks:
        if c in manifest:
            print(f"WARNING: Manifest still has {c}!")
        else:
            print(f"OK - No {c} in manifest")
except:
    print("ERROR reading manifest")

print("\n=== DONE ===")