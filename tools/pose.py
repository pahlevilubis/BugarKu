"""
Mesin pose figur garis (stick figure) untuk animasi gerakan BugarKu.

Satuan: koordinat dunia, sumbu y ke atas, lantai di y = 0.
Sudut dalam derajat, mutlak (0 = ke kanan, 90 = ke atas).
Figur tampak samping menghadap ke kanan.

Setiap keyframe berisi:
  x, y      posisi pinggul (titik tengah)
  t         sudut badan (pinggul -> leher)
  k         tolehan kepala relatif terhadap badan
  a1,b1     lengan atas & bawah sisi 1 (sisi dekat / sisi kanan layar untuk tampak depan)
  a2,b2     lengan sisi 2 (sisi jauh / kiri)
  p1,s1     paha & betis sisi 1
  p2,s2     paha & betis sisi 2
  fo1,fo2   sudut telapak relatif terhadap betis
atau target: h1/h2 = posisi tangan, f1/f2 = posisi pergelangan kaki (diselesaikan dengan IK).
"""
import math

TORSO = 30.0
LEHER = 9.0
KEPALA = 6.5
L_ATAS = 15.0
L_BAWAH = 14.0
PAHA = 21.0
BETIS = 20.0
TELAPAK = 6.0
BAHU_W = 6.5
PINGGUL_W = 4.0
TEBAL = 5.0          # tebal garis badan (untuk jarak ke lantai)
FPS = 15

# Urutan titik dalam satu frame (17 titik):
TITIK = ["kepala", "leher", "pinggul",
         "bahu1", "siku1", "tangan1", "bahu2", "siku2", "tangan2",
         "pg1", "lutut1", "mata1", "jari1", "pg2", "lutut2", "mata2", "jari2"]
IDX = {n: i for i, n in enumerate(TITIK)}

SUDUT = ["t", "k", "a1", "b1", "a2", "b2", "p1", "s1", "p2", "s2", "fo1", "fo2"]


def arah(a):
    r = math.radians(a)
    return math.cos(r), math.sin(r)


def maju(p, a, panjang):
    dx, dy = arah(a)
    return (p[0] + dx * panjang, p[1] + dy * panjang)


def sudut_ke(a, b):
    return math.degrees(math.atan2(b[1] - a[1], b[0] - a[0]))


def silang(o, a, b):
    """Tanda posisi b terhadap garis o->a (positif = di kiri)."""
    return (a[0] - o[0]) * (b[1] - o[1]) - (a[1] - o[1]) * (b[0] - o[0])


def ik(akar, target, l1, l2, tanda):
    jarak = math.hypot(target[0] - akar[0], target[1] - akar[1])
    jarak = max(abs(l1 - l2) + 1e-3, min(l1 + l2 - 1e-3, jarak))
    th = sudut_ke(akar, target)
    c = (l1 * l1 + jarak * jarak - l2 * l2) / (2 * l1 * jarak)
    c = max(-1.0, min(1.0, c))
    al = math.degrees(math.acos(c))
    s1 = th + tanda * al
    sendi = maju(akar, s1, l1)
    s2 = sudut_ke(sendi, target)
    return s1, s2


def akar_anggota(p, depan):
    """Titik pangkal bahu & pinggul untuk sisi 1 dan 2."""
    pinggul = (p["x"], p["y"])
    leher = maju(pinggul, p["t"], TORSO)
    if depan:
        b1 = maju(leher, p["t"] - 90, BAHU_W)
        b2 = maju(leher, p["t"] + 90, BAHU_W)
        g1 = maju(pinggul, p["t"] - 90, PINGGUL_W)
        g2 = maju(pinggul, p["t"] + 90, PINGGUL_W)
    else:
        b1 = b2 = leher
        g1 = g2 = pinggul
    return pinggul, leher, b1, b2, g1, g2


DEFAULT = dict(x=0.0, y=41.0, t=90.0, k=0.0,
               a1=-98.0, b1=-82.0, a2=-84.0, b2=-72.0,
               p1=-90.0, s1=-90.0, p2=-92.0, s2=-92.0)


def selesaikan(spec, depan):
    """Ubah spesifikasi keyframe (boleh pakai target h1/h2/f1/f2) menjadi sudut lengkap."""
    p = dict(DEFAULT)
    if depan:
        p.update(a1=-80.0, b1=-86.0, a2=-100.0, b2=-94.0, p1=-88.0, s1=-90.0, p2=-92.0, s2=-90.0)
    p["fo1"] = 90.0
    p["fo2"] = -90.0 if depan else 90.0
    for kunci, nilai in spec.items():
        if kunci in ("h1", "h2", "f1", "f2", "sh1", "sh2", "sk1", "sk2", "dur", "tahan", "datar", "datar1", "datar2"):
            continue
        p[kunci] = float(nilai)
    _, _, b1, b2, g1, g2 = akar_anggota(p, depan)
    p["_T"] = {k: tuple(spec[k]) for k in ("h1", "h2", "f1", "f2") if k in spec}
    p["_S"] = {"h1": spec.get("sh1", -1), "h2": spec.get("sh2", -1), "f1": spec.get("sk1", 1), "f2": spec.get("sk2", 1)}
    if "h1" in spec:
        p["a1"], p["b1"] = ik(b1, spec["h1"], L_ATAS, L_BAWAH, spec.get("sh1", -1))
    if "h2" in spec:
        p["a2"], p["b2"] = ik(b2, spec["h2"], L_ATAS, L_BAWAH, spec.get("sh2", -1))
    if "f1" in spec:
        p["p1"], p["s1"] = ik(g1, spec["f1"], PAHA, BETIS, spec.get("sk1", 1))
    if "f2" in spec:
        p["p2"], p["s2"] = ik(g2, spec["f2"], PAHA, BETIS, spec.get("sk2", 1))
    # telapak rata di lantai (menghadap depan) apa pun sudut betisnya
    if spec.get("datar") or spec.get("datar1"):
        p["fo1"] = -p["s1"]
    if spec.get("datar") or spec.get("datar2"):
        p["fo2"] = -p["s2"]
    p["dur"] = float(spec.get("dur", 0.8))
    p["tahan"] = float(spec.get("tahan", 0.0))
    return p


def fk(p, depan):
    pinggul, leher, b1, b2, g1, g2 = akar_anggota(p, depan)
    kepala = maju(leher, p["t"] + p["k"], LEHER)
    s1 = maju(b1, p["a1"], L_ATAS)
    t1 = maju(s1, p["b1"], L_BAWAH)
    s2 = maju(b2, p["a2"], L_ATAS)
    t2 = maju(s2, p["b2"], L_BAWAH)
    tel = TELAPAK * (0.6 if depan else 1.0)
    l1 = maju(g1, p["p1"], PAHA)
    m1 = maju(l1, p["s1"], BETIS)
    j1 = maju(m1, p["s1"] + p["fo1"], tel)
    l2 = maju(g2, p["p2"], PAHA)
    m2 = maju(l2, p["s2"], BETIS)
    j2 = maju(m2, p["s2"] + p["fo2"], tel)
    return [kepala, leher, pinggul, b1, s1, t1, b2, s2, t2, g1, l1, m1, j1, g2, l2, m2, j2]


def beda_sudut(a, b):
    d = (b - a) % 360.0
    if d > 180:
        d -= 360
    return d


def halus(u):
    return u * u * (3 - 2 * u)


def campur(k0, k1, u, poros=None):
    hasil = {}
    if poros is not None:
        r0 = math.hypot(k0["x"] - poros[0], k0["y"] - poros[1])
        r1 = math.hypot(k1["x"] - poros[0], k1["y"] - poros[1])
        f0 = sudut_ke(poros, (k0["x"], k0["y"]))
        f1 = sudut_ke(poros, (k1["x"], k1["y"]))
        r = r0 + (r1 - r0) * u
        f = f0 + beda_sudut(f0, f1) * u
        hasil["x"], hasil["y"] = maju(poros, f, r)
    else:
        for kunci in ("x", "y"):
            hasil[kunci] = k0[kunci] + (k1[kunci] - k0[kunci]) * u
    t0, t1 = k0.get("_T", {}), k1.get("_T", {})
    hasil["_T"] = {k: (t0[k][0] + (t1[k][0] - t0[k][0]) * u, t0[k][1] + (t1[k][1] - t0[k][1]) * u)
                   for k in t0 if k in t1}
    hasil["_S"] = k0.get("_S", {})
    for kunci in SUDUT:
        if kunci in ("fo1", "fo2"):
            hasil[kunci] = k0[kunci] + (k1[kunci] - k0[kunci]) * u
        else:
            hasil[kunci] = k0[kunci] + beda_sudut(k0[kunci], k1[kunci]) * u
    return hasil


def pasang(titik, akar_i, tengah_i, ujung_i, target, l1, l2, bawaan, paksa=None):
    """Paksa ujung anggota tetap di target (IK). Arah tekuk: `paksa` kalau ada, kalau tidak ikut pose saat ini."""
    akar = titik[akar_i]
    if paksa is not None:
        tanda = paksa
    else:
        tanda = silang(akar, target, titik[tengah_i])
        if abs(tanda) < 1e-6:
            tanda = bawaan
    tanda = 1 if tanda > 0 else -1
    s1, s2 = ik(akar, target, l1, l2, tanda)
    sendi = maju(akar, s1, l1)
    titik[tengah_i] = sendi
    titik[ujung_i] = maju(sendi, s2, l2)


def kaki_ke(titik, n, target, fo, depan, tanda):
    pasang(titik, IDX["pg" + n], IDX["lutut" + n], IDX["mata" + n], target, PAHA, BETIS, 1, tanda)
    s = sudut_ke(titik[IDX["lutut" + n]], titik[IDX["mata" + n]])
    titik[IDX["jari" + n]] = maju(titik[IDX["mata" + n]], s + fo, TELAPAK * (0.6 if depan else 1.0))


def buat_frame(keyframes, depan, tanam, poros=None):
    ks = [selesaikan(k, depan) for k in keyframes]
    pose0 = fk(ks[0], depan)
    target = {}
    for nama in tanam:
        if nama.startswith("tangan"):
            target[nama] = pose0[IDX[nama]]
        else:  # kaki: pergelangan + ujung jari tetap
            n = nama[-1]
            target["mata" + n] = pose0[IDX["mata" + n]]
            target["jari" + n] = pose0[IDX["jari" + n]]
    # urutan segmen: transisi menuju keyframe i memakai dur milik keyframe i
    segmen = []
    n = len(ks)
    for i in range(n):
        a = ks[i]
        b = ks[(i + 1) % n]
        if a["tahan"] > 0:
            segmen.append((a, a, a["tahan"]))
        segmen.append((a, b, b["dur"]))
    total = sum(s[2] for s in segmen)
    jumlah = max(2, int(round(total * FPS)))
    frames = []
    for f in range(jumlah):
        waktu = f / jumlah * total
        for a, b, d in segmen:
            if waktu <= d or (a, b, d) == segmen[-1]:
                u = 0.0 if d <= 0 else min(1.0, waktu / d)
                pose = campur(a, b, halus(u), poros)
                break
            waktu -= d
        titik = fk(pose, depan)
        tg = pose["_T"]
        sg = pose["_S"]
        if "h1" in tg:
            pasang(titik, IDX["bahu1"], IDX["siku1"], IDX["tangan1"], tg["h1"], L_ATAS, L_BAWAH, -1, sg["h1"])
        if "h2" in tg:
            pasang(titik, IDX["bahu2"], IDX["siku2"], IDX["tangan2"], tg["h2"], L_ATAS, L_BAWAH, -1, sg["h2"])
        if "f1" in tg:
            kaki_ke(titik, "1", tg["f1"], pose["fo1"], depan, sg["f1"])
        if "f2" in tg:
            kaki_ke(titik, "2", tg["f2"], pose["fo2"], depan, sg["f2"])
        if "tangan1" in target:
            pasang(titik, IDX["bahu1"], IDX["siku1"], IDX["tangan1"], target["tangan1"], L_ATAS, L_BAWAH, -1)
        if "tangan2" in target:
            pasang(titik, IDX["bahu2"], IDX["siku2"], IDX["tangan2"], target["tangan2"], L_ATAS, L_BAWAH, -1)
        if "mata1" in target:
            pasang(titik, IDX["pg1"], IDX["lutut1"], IDX["mata1"], target["mata1"], PAHA, BETIS, 1)
            titik[IDX["jari1"]] = target["jari1"]
        if "mata2" in target:
            pasang(titik, IDX["pg2"], IDX["lutut2"], IDX["mata2"], target["mata2"], PAHA, BETIS, 1)
            titik[IDX["jari2"]] = target["jari2"]
        frames.append(titik)
    return frames, total


def angka(v):
    s = f"{v:.1f}"
    if s.endswith(".0"):
        s = s[:-2]
    return "0" if s == "-0" else s


def kotak(frames, props):
    xs, ys = [], []
    for fr in frames:
        for i, (x, y) in enumerate(fr):
            r = KEPALA if i == 0 else TEBAL / 2
            xs += [x - r, x + r]
            ys += [y - r, y + r]
    for g in props:
        xs += [g[0], g[2]]
        ys += [g[1], g[3]]
    ys.append(0.0)
    return [min(xs), min(ys), max(xs), max(ys)]


def proses(g):
    """g: dict gerakan dengan 'kf' (keyframes), 'tampak', 'tanam', 'props'. Hasil: data animasi."""
    depan = g.get("tampak", "samping") == "depan"
    frames, total = buat_frame(g["kf"], depan, g.get("tanam", []), g.get("poros"))
    # turunkan/naikkan figur supaya titik terendah di frame pertama menyentuh lantai
    terendah = min(
        (y - (KEPALA if i == 0 else TEBAL / 2)) for i, (x, y) in enumerate(frames[0])
    )
    geser_y = -terendah
    # props ikut bergeser, kecuali ujung yang memang di lantai (y = 0) tetap di lantai
    props = [[p[0], 0.0 if p[1] == 0 else p[1] + geser_y, p[2], 0.0 if p[3] == 0 else p[3] + geser_y]
             for p in g.get("props", [])]
    frames = [[(x, y + geser_y) for (x, y) in fr] for fr in frames]
    # titik tengah horizontal supaya rapi
    kt = kotak(frames, props)
    geser_x = -(kt[0] + kt[2]) / 2
    frames = [[(x + geser_x, y) for (x, y) in fr] for fr in frames]
    props = [[p[0] + geser_x, p[1], p[2] + geser_x, p[3]] for p in props]
    kt = kotak(frames, props)
    # frame ikon: paling jauh dari frame pertama
    def jarak(fr):
        return sum((a[0] - b[0]) ** 2 + (a[1] - b[1]) ** 2 for a, b in zip(fr, frames[0]))
    ikon = max(range(len(frames)), key=lambda i: jarak(frames[i])) if len(frames) > 1 else 0
    if jarak(frames[ikon]) < 4:
        ikon = 0
    teks = " ".join(angka(v) for fr in frames for pt in fr for v in pt)
    return dict(
        f=teks,
        nf=len(frames),
        fps=FPS,
        kotak=[round(v, 1) for v in kt],
        props=[[round(v, 1) for v in p] for p in props],
        tampak="depan" if depan else "samping",
        ikon=ikon,
        _frames=frames,
    )
