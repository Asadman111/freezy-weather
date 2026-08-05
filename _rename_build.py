import re

with open('app/build.gradle.kts', 'r', encoding='utf-8') as f:
    content = f.read()

replacements = [
    ('breezy.buildlogic', 'freezy.buildlogic'),
    ('"breezy.android.application"', '"freezy.android.application"'),
    ('"breezy.android.application.compose"', '"freezy.android.application.compose"'),
    ('namespace = "org.breezyweather"', 'namespace = "org.freezyweather"'),
    ('applicationId = "org.breezyweather"', 'applicationId = "org.freezyweather"'),
    ('versionCode = 60202', 'versionCode = 10000'),
    ('versionName = "6.2.2"', 'versionName = "1.0.0"'),
    ('"IS_BREEZY"', '"IS_FREEZY"'),
    ('Config.isBreezy', 'Config.isFreezy'),
    ('breezy.report_issue', 'freezy.report_issue'),
    ('breezy.source_code_link', 'freezy.source_code_link'),
    ('breezy.releases_link', 'freezy.releases_link'),
    ('breezy.install_instructions_link', 'freezy.install_instructions_link'),
    ('breezy.icon_packs_link', 'freezy.icon_packs_link'),
    ('breezy.privacy_policy_link', 'freezy.privacy_policy_link'),
    ('breezy.matrix_link', 'freezy.matrix_link'),
    ('breezy.github.org', 'freezy.github.org'),
    ('breezy.github.repo', 'freezy.github.repo'),
    ('breezy.github.release_prefix', 'freezy.github.release_prefix'),
    ('breezy.source.default_location', 'freezy.source.default_location'),
    ('breezy.source.default_location_search', 'freezy.source.default_location_search'),
    ('breezy.source.default_geocoding', 'freezy.source.default_geocoding'),
    ('breezy.source.default_weather', 'freezy.source.default_weather'),
    ('breezy.accu.key', 'freezy.accu.key'),
    ('breezy.aemet.key', 'freezy.aemet.key'),
    ('breezy.atmoaura.key', 'freezy.atmoaura.key'),
    ('breezy.atmofrance.key', 'freezy.atmofrance.key'),
    ('breezy.atmograndest.key', 'freezy.atmograndest.key'),
    ('breezy.atmohdf.key', 'freezy.atmohdf.key'),
    ('breezy.atmosud.key', 'freezy.atmosud.key'),
    ('breezy.baiduip.key', 'freezy.baiduip.key'),
    ('breezy.bmkg.key', 'freezy.bmkg.key'),
    ('breezy.cwa.key', 'freezy.cwa.key'),
    ('breezy.eccc.key', 'freezy.eccc.key'),
    ('breezy.geonames.key', 'freezy.geonames.key'),
    ('breezy.metie.key', 'freezy.metie.key'),
    ('breezy.metoffice.key', 'freezy.metoffice.key'),
    ('breezy.mf.jwtKey', 'freezy.mf.jwtKey'),
    ('breezy.mf.key', 'freezy.mf.key'),
    ('breezy.openweather.key', 'freezy.openweather.key'),
    ('breezy.pirateweather.key', 'freezy.pirateweather.key'),
    ('breezy.polleninfo.key', 'freezy.polleninfo.key'),
    ('breezy.infoplaza.key', 'freezy.infoplaza.key'),
    ('src/res_breezy', 'src/res_freezy'),
    ('libs.breezy.datasharing', 'libs.freezy.datasharing'),
    ('Missing breezy.', 'Missing freezy.'),
]

for old, new in replacements:
    content = content.replace(old, new)

with open('app/build.gradle.kts', 'w', encoding='utf-8') as f:
    f.write(content)
print('Done - app/build.gradle.kts updated')