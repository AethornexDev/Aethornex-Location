import struct

d = open("app/src/main/assets/efficientdet_lite0.tflite", "rb").read()

def u16(p): return struct.unpack_from("<H", d, p)[0]
def u32(p): return struct.unpack_from("<I", d, p)[0]
def i32(p): return struct.unpack_from("<i", d, p)[0]

def field(t, n):
    vt = t - i32(t)
    size = u16(vt)
    slot = 4 + 2*n
    return u16(vt+slot) if slot < size else 0

def target(t, n):
    f = field(t, n)
    if not f: return 0
    p = t + f
    return p + u32(p)

def tables(t, n):
    p = target(t, n)
    if not p: return []
    count = u32(p)
    return [p+4+4*i+u32(p+4+4*i) for i in range(count)]

def ints(t, n):
    p = target(t, n)
    if not p: return []
    return [i32(p+4+4*i) for i in range(u32(p))]

def string(t, n):
    p = target(t, n)
    if not p: return ""
    length = u32(p)
    return d[p+4:p+4+length].decode(errors="replace")

root = u32(0)
subgraphs = tables(root, 2)
print("MODEL VERSION:", u32(root+field(root, 0)))
print("SUBGRAPHS:", len(subgraphs))

sg = subgraphs[0]
tensors = tables(sg, 0)
inputs = ints(sg, 1)
outputs = ints(sg, 2)

types = {
    0:"FLOAT32", 1:"FLOAT16", 2:"INT32", 3:"UINT8",
    4:"INT64", 5:"STRING", 6:"BOOL", 7:"INT16",
    9:"INT8"
}

print("TENSOR COUNT:", len(tensors))

def show(i, label):
    t = tensors[i]
    shape = ints(t, 0)
    f = field(t, 1)
    dtype = d[t+f] if f else -1
    name = string(t, 3)
    print(label, "INDEX:", i, "NAME:", name,
          "SHAPE:", shape, "TYPE:", types.get(dtype, dtype))

print("\nINPUTS:")
for i in inputs:
    show(i, "INPUT")

print("\nOUTPUTS:")
for i in outputs:
    show(i, "OUTPUT")
