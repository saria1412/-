#!/usr/bin/env bash
set -u

cat build-input/part03-gz.* | base64 -d > build-input/baynana-v2.part.03.gz
echo 'e9b07f6d1aca39a22810d27da0a2c9f362d4d0cc8f3c472f66ed413bc5607ba8  build-input/baynana-v2.part.03.gz' | sha256sum -c -
gunzip -c build-input/baynana-v2.part.03.gz > build-input/baynana-v2.part.03
echo 'a75c13d2b600fad085d9844443e9ad8b6d98bc441dc2338a97e18d1dd36be6d9  build-input/baynana-v2.part.03' | sha256sum -c -
cat \
  build-input/baynana-v2.part.00 \
  build-input/baynana-v2.part.01 \
  build-input/baynana-v2.part.02 \
  build-input/baynana-v2.part.03 \
  | base64 -d > Baynana_Android_v2_Source.truncated.zip
echo 'cd6428d92299601820b777ab50cc6d5a1c3941fd13fdce5e9e3c676b807becf5  Baynana_Android_v2_Source.truncated.zip' | sha256sum -c -

python3 - <<'PY'
import binascii
import pathlib
import struct
import zlib

archive = pathlib.Path('Baynana_Android_v2_Source.truncated.zip').read_bytes()
position = 0
extracted = 0
while position + 30 <= len(archive):
    signature = struct.unpack_from('<I', archive, position)[0]
    if signature != 0x04034B50:
        break
    (
        _, _version, flags, method, _time, _date,
        expected_crc, compressed_size, uncompressed_size,
        name_length, extra_length,
    ) = struct.unpack_from('<IHHHHHIIIHH', archive, position)
    if flags & 0x08:
        raise RuntimeError('Unsupported data-descriptor ZIP entry')
    header_end = position + 30
    name_end = header_end + name_length
    data_start = name_end + extra_length
    data_end = data_start + compressed_size
    if data_end > len(archive):
        raise RuntimeError('Truncated file data')
    name = archive[header_end:name_end].decode('utf-8')
    target = pathlib.Path(name)
    if target.is_absolute() or '..' in target.parts:
        raise RuntimeError(f'Unsafe path: {name}')
    if name.endswith('/'):
        target.mkdir(parents=True, exist_ok=True)
    else:
        compressed = archive[data_start:data_end]
        if method == 0:
            content = compressed
        elif method == 8:
            content = zlib.decompress(compressed, -15)
        else:
            raise RuntimeError(f'Unsupported compression method {method}')
        if len(content) != uncompressed_size:
            raise RuntimeError(f'Size mismatch: {name}')
        if (binascii.crc32(content) & 0xFFFFFFFF) != expected_crc:
            raise RuntimeError(f'CRC mismatch: {name}')
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_bytes(content)
    extracted += 1
    position = data_end
print(f'Extracted {extracted} ZIP entries')
if extracted < 50:
    raise RuntimeError('Project extraction incomplete')
PY

python3 - <<'PY'
from pathlib import Path
path = Path('BaynanaAndroid/app/build.gradle.kts')
text = path.read_text()
if 'import org.jetbrains.kotlin.gradle.dsl.JvmTarget' not in text:
    text = 'import org.jetbrains.kotlin.gradle.dsl.JvmTarget\n\n' + text
text = text.replace('    kotlinOptions {\n        jvmTarget = "17"\n    }\n\n', '')
if '\nkotlin {\n' not in text:
    text += '\n\nkotlin {\n    compilerOptions {\n        jvmTarget.set(JvmTarget.JVM_17)\n    }\n}\n'
path.write_text(text)
PY

test -f BaynanaAndroid/app/build.gradle.kts
test -f BaynanaAndroid/app/src/main/java/com/baynana/couplesgame/ui/BaynanaApp.kt

set +e
(
  cd BaynanaAndroid
  gradle --no-daemon :app:assembleDebug --stacktrace
) 2>&1 | tee gradle-build.log
build_status=${PIPESTATUS[0]}
set -e

if [ "$build_status" -eq 0 ]; then
  cp BaynanaAndroid/app/build/outputs/apk/debug/app-debug.apk Baynana-v2.0-debug.apk
  sha256sum Baynana-v2.0-debug.apk > Baynana-v2.0-debug.apk.sha256
fi

exit "$build_status"
