import zipfile
import struct
import hashlib

p = "app/src/main/assets/efficientdet_lite0.tflite"

with open(p, "rb") as f:
    data = f.read()

print("SIZE:", len(data))
print("SIGNATURE:", data[4:8])
print("SHA256:", hashlib.sha256(data).hexdigest())

if zipfile.is_zipfile(p):
    with zipfile.ZipFile(p) as z:
        print("ARCHIVE FILES:", z.namelist())
        for name in z.namelist():
            if name.endswith(".txt"):
                print("LABELS:", name)
                print(z.read(name).decode(errors="replace")[:3000])
