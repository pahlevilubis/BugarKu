"""
Pratinjau peraga BugarKu (gaya sama dengan Figur.kt di app).
  python3 tools/karakter.py push_up,squat,crunch wanita
"""
import math
import sys
from PIL import Image, ImageDraw
from pose import proses, IDX
import gerakan as G

S = 3  # supersampling


def hx(h, a=255):
    return ((h >> 16) & 255, (h >> 8) & 255, h & 255, a)


PERAGA = {
    "wanita": dict(
        kulit=hx(0xEDBE99), kulitG=hx(0xD09E7A), rambut=hx(0x2A1D19), atasan=hx(0x283047), atasanG=hx(0x1C2233),
        bawahan=hx(0x283047), bawahanG=hx(0x1C2233), sepatu=hx(0x2BC0C4), sepatuG=hx(0x21999C), sol=hx(0xFF7A45),
        lengan_baju=0.0, celana_pendek=0.42, kuncir=True, pipi=True, ramping=0.92,
    ),
    "pria": dict(
        kulit=hx(0xC68B59), kulitG=hx(0xA87146), rambut=hx(0x231E1E), atasan=hx(0x2C3240), atasanG=hx(0x1E222D),
        bawahan=hx(0x1C1F26), bawahanG=hx(0x121419), sepatu=hx(0xF5F5F5), sepatuG=hx(0xD9D9D9), sol=hx(0xFF6A3D),
        lengan_baju=0.45, celana_pendek=1.0, kuncir=False, pipi=False, ramping=1.0,
    ),
}
MATA = hx(0x2B2020)
BIBIR = hx(0xB4584A)
MERAH_PIPI = (255, 120, 120, 70)
JAM = hx(0x22252C)
SOROT = (255, 70, 10, 125)
LATAR = (236, 239, 243, 255)
MATRAS = (74, 80, 92, 255)


def arah(d):
    r = math.radians(d)
    return (math.cos(r), math.sin(r))


def tambah(a, b, k=1.0):
    return (a[0] + b[0] * k, a[1] + b[1] * k)


def antara(a, b, t):
    return (a[0] + (b[0] - a[0]) * t, a[1] + (b[1] - a[1]) * t)


def tabung(d, a, b, ra, rb, warna):
    dx, dy = b[0] - a[0], b[1] - a[1]
    L = math.hypot(dx, dy) or 1e-6
    nx, ny = -dy / L, dx / L
    d.polygon([(a[0] + nx * ra, a[1] + ny * ra), (b[0] + nx * rb, b[1] + ny * rb),
               (b[0] - nx * rb, b[1] - ny * rb), (a[0] - nx * ra, a[1] - ny * ra)], fill=warna)
    d.ellipse([a[0] - ra, a[1] - ra, a[0] + ra, a[1] + ra], fill=warna)
    d.ellipse([b[0] - rb, b[1] - rb, b[0] + rb, b[1] + rb], fill=warna)


def bulat(d, c, r, warna):
    d.ellipse([c[0] - r, c[1] - r, c[0] + r, c[1] + r], fill=warna)


def garis(d, a, b, w, warna):
    tabung(d, a, b, w / 2, w / 2, warna)


def busur(d, c, r, mulai, sapuan, warna):
    # sudut layar (y ke bawah), searah jarum jam = positif, sama seperti drawArc Compose
    d.pieslice([c[0] - r, c[1] - r, c[0] + r, c[1] + r], start=mulai, end=mulai + sapuan, fill=warna)


def kartu(g, jenis="wanita", ukuran=230, frame=None, waktu=0.0):
    w = PERAGA[jenis]
    data = proses(g)
    frs = data["_frames"]
    fr = frs[data["ikon"] if frame is None else frame]
    depan = data["tampak"] == "depan"
    W = ukuran * S
    img = Image.new("RGBA", (W, W), (0, 0, 0, 0))
    d = ImageDraw.Draw(img, "RGBA")
    d.rounded_rectangle([0, 0, W - 1, W - 1], radius=28 * S, fill=LATAR)
    kt = data["kotak"]
    lebar, tinggi = kt[2] - kt[0], kt[3] - kt[1]
    pad = 0.1 * W
    sk = min((W - 2 * pad) / lebar, (W - 2 * pad) / tinggi)
    ox = (W - lebar * sk) / 2 - kt[0] * sk
    bawah = W - pad * 0.8

    def P(pt):
        return (ox + pt[0] * sk, bawah - (pt[1] - kt[1]) * sk)

    u = sk
    lantai = P((0, 0))[1]
    rendah = min(fr[IDX["leher"]][1], frs[0][IDX["leher"]][1]) < 32
    if rendah:
        d.rounded_rectangle([P((kt[0] - 4, 0))[0], lantai - 1.2 * u, P((kt[2] + 4, 0))[0], lantai + 1.6 * u],
                            radius=1.2 * u, fill=MATRAS)
    xs = [P(p)[0] for p in fr]
    d.ellipse([min(xs) - 2 * u, lantai - 1.2 * u, max(xs) + 2 * u, lantai + 1.8 * u], fill=(0, 0, 0, 26))
    for gp in data["props"]:
        tabung(d, P((gp[0], gp[1])), P((gp[2], gp[3])), 1.3 * u, 1.3 * u, (154, 122, 90, 255))

    def t(n):
        return P(fr[IDX[n]])

    rp = w["ramping"]

    def lengan(n, gelap, jam):
        b, s, tg = t("bahu" + n), t("siku" + n), t("tangan" + n)
        kul = w["kulitG"] if gelap else w["kulit"]
        tabung(d, s, tg, 1.75 * u * rp, 1.35 * u * rp, kul)
        tabung(d, b, s, 2.2 * u * rp, 1.75 * u * rp, kul)
        if w["lengan_baju"] > 0:
            tabung(d, b, antara(b, s, w["lengan_baju"]), 2.75 * u, 2.45 * u, w["atasanG"] if gelap else w["atasan"])
        bulat(d, tg, 1.65 * u * rp, kul)
        if jam:
            bulat(d, antara(s, tg, 0.8), 1.05 * u, JAM)

    def kaki(n, gelap):
        g_, l, m, j = t("pg" + n), t("lutut" + n), t("mata" + n), t("jari" + n)
        kul = w["kulitG"] if gelap else w["kulit"]
        cel = w["bawahanG"] if gelap else w["bawahan"]
        pendek = w["celana_pendek"] < 1.0
        tabung(d, l, m, 2.6 * u * rp, 1.75 * u * rp, kul if pendek else cel)
        tabung(d, g_, l, 3.7 * u * rp, 2.6 * u * rp, kul if pendek else cel)
        if pendek:
            tabung(d, g_, antara(g_, l, w["celana_pendek"]), 4.1 * u * rp, 3.5 * u * rp, cel)
        tabung(d, m, j, 1.95 * u, 1.65 * u, w["sepatuG"] if gelap else w["sepatu"])
        tabung(d, antara(m, j, 0.05), j, 0.85 * u, 0.75 * u, w["sol"])

    bayang = not depan
    kaki("2", bayang)
    lengan("2", bayang, False)
    pg, lh = t("pinggul"), t("leher")
    kp = t("kepala")
    rk = 5.4 * u
    atas = math.degrees(math.atan2(kp[1] - lh[1], kp[0] - lh[0]))
    up = arah(atas)
    muka = arah(atas + 90)
    blk = arah(atas - 90)

    # kuncir di belakang kepala (digambar sebelum badan & kepala)
    if w["kuncir"]:
        akar = tambah(kp, arah(atas - 40), rk * 0.8) if not depan else tambah(kp, up, rk * 0.75)
        if depan:
            samping = arah(atas + 90)
            arah1 = (samping[0] * 0.5 + 0.0, samping[1] * 0.5 + 0.9)
        else:
            arah1 = (blk[0] * 0.6, blk[1] * 0.6 + 0.8)
        L = math.hypot(*arah1) or 1
        a1 = math.degrees(math.atan2(arah1[1] / L, arah1[0] / L)) + math.sin(waktu * 7) * 9
        tengah = tambah(akar, arah(a1), rk * 1.05)
        a2 = a1 + (15 if math.cos(math.radians(a1)) < 0 else -15) + math.sin(waktu * 7 + 0.8) * 12
        ujung = tambah(tengah, arah(a2), rk * 1.0)
        tabung(d, akar, tengah, rk * 0.36, rk * 0.27, w["rambut"])
        tabung(d, tengah, ujung, rk * 0.27, rk * 0.07, w["rambut"])

    # badan
    if depan:
        b1, b2, g1, g2 = t("bahu1"), t("bahu2"), t("pg1"), t("pg2")
        c = antara(pg, lh, 0.5)
        pinggang1 = antara(antara(g1, b1, 0.42), c, 0.14 if w["kuncir"] else 0.05)
        pinggang2 = antara(antara(g2, b2, 0.42), c, 0.14 if w["kuncir"] else 0.05)
        tabung(d, g1, g2, 3.7 * u, 3.7 * u, w["bawahan"])
        d.polygon([b1, b2, pinggang2, antara(g2, b2, 0.12), antara(g1, b1, 0.12), pinggang1], fill=w["atasan"])
        tabung(d, b1, b2, 2.3 * u, 2.3 * u, w["atasan"])
        tabung(d, antara(g1, b1, 0.1), antara(g2, b2, 0.1), 3.3 * u, 3.3 * u, w["atasan"])
    else:
        tabung(d, pg, antara(pg, lh, 0.16), 4.5 * u * rp, 4.2 * u * rp, w["bawahan"])
        tabung(d, antara(pg, lh, 0.12), antara(pg, lh, 0.5), 4.2 * u * rp, 3.9 * u * rp, w["atasan"])
        tabung(d, antara(pg, lh, 0.48), antara(pg, lh, 0.84), 3.9 * u * rp, 4.8 * u * rp, w["atasan"])
        tabung(d, antara(pg, lh, 0.8), lh, 4.5 * u * rp, 3.1 * u * rp, w["atasan"])
    kaki("1", False)

    # leher & kepala
    tabung(d, lh, antara(lh, kp, 0.62), 1.75 * u, 1.75 * u, w["kulitG"])
    bulat(d, kp, rk, w["kulit"])
    rr = rk * 1.07
    if depan:
        busur(d, kp, rr, atas - 98, 196, w["rambut"])
        if w["kuncir"]:
            sp = arah(atas + 90)
            for s in (1, -1):
                garis(d, tambah(tambah(kp, sp, s * rk * 0.88), up, rk * 0.35),
                      tambah(tambah(kp, sp, s * rk * 0.93), up, -rk * 0.75), rk * 0.42, w["rambut"])
        # wajah depan
        sp = arah(atas + 90)
        for s in (1, -1):
            mata = tambah(tambah(kp, sp, s * rk * 0.36), up, -rk * 0.05)
            bulat(d, mata, rk * 0.13, MATA)
            bulat(d, tambah(tambah(mata, sp, rk * 0.04), up, rk * 0.05), rk * 0.045, (255, 255, 255, 255))
            garis(d, tambah(tambah(kp, sp, s * rk * 0.22), up, rk * 0.24),
                  tambah(tambah(kp, sp, s * rk * 0.5), up, rk * 0.22), rk * 0.08, w["rambut"])
            if w["pipi"]:
                bulat(d, tambah(tambah(kp, sp, s * rk * 0.55), up, -rk * 0.3), rk * 0.16, MERAH_PIPI)
        garis(d, tambah(tambah(kp, sp, -rk * 0.14), up, -rk * 0.5), tambah(tambah(kp, sp, rk * 0.14), up, -rk * 0.5),
              rk * 0.08, BIBIR)
    else:
        awal = atas - 150 if w["kuncir"] else atas - 140
        busur(d, kp, rr, awal, atas + 48 - awal, w["rambut"])
        # telinga, hidung, mata, alis, mulut
        bulat(d, tambah(kp, muka, -rk * 0.12), rk * 0.2, w["kulitG"])
        bulat(d, tambah(tambah(kp, muka, rk * 0.95), up, -rk * 0.08), rk * 0.15, w["kulit"])
        mata = tambah(tambah(kp, muka, rk * 0.55), up, -rk * 0.02)
        bulat(d, mata, rk * 0.13, MATA)
        bulat(d, tambah(tambah(mata, muka, rk * 0.04), up, rk * 0.05), rk * 0.045, (255, 255, 255, 255))
        garis(d, tambah(tambah(kp, muka, rk * 0.42), up, rk * 0.27), tambah(tambah(kp, muka, rk * 0.72), up, rk * 0.25),
              rk * 0.08, w["rambut"])
        garis(d, tambah(tambah(kp, muka, rk * 0.6), up, -rk * 0.5), tambah(tambah(kp, muka, rk * 0.8), up, -rk * 0.45),
              rk * 0.07, BIBIR)
        if w["pipi"]:
            bulat(d, tambah(tambah(kp, muka, rk * 0.4), up, -rk * 0.3), rk * 0.17, MERAH_PIPI)
    lengan("1", False, True)
    if depan:
        lengan("2", False, False)

    for s in g.get("sorot", []):
        if s == "lengan":
            garis(d, antara(t("bahu1"), t("siku1"), 0.2), t("siku1"), 3.2 * u, SOROT)
        elif s in ("dada", "bahu", "punggung"):
            garis(d, antara(pg, lh, 0.62), antara(pg, lh, 0.9), 5.4 * u, SOROT)
        elif s == "perut":
            garis(d, antara(pg, lh, 0.2), antara(pg, lh, 0.55), 5.4 * u, SOROT)
        elif s == "kaki":
            garis(d, antara(t("pg1"), t("lutut1"), 0.15), antara(t("pg1"), t("lutut1"), 0.85), 4.6 * u, SOROT)
        elif s == "bokong":
            garis(d, pg, antara(pg, t("lutut1"), 0.25), 5.8 * u, SOROT)
    return img.resize((ukuran, ukuran), Image.LANCZOS)


def main():
    ids = sys.argv[1].split(",")
    jenis = sys.argv[2] if len(sys.argv) > 2 else "wanita"
    kolom = 4
    ukuran = 260
    gs = {g["id"]: g for g in G.G}
    baris = (len(ids) + kolom - 1) // kolom
    lembar = Image.new("RGB", (kolom * (ukuran + 16) + 16, baris * (ukuran + 16) + 16), "white")
    for i, gid in enumerate(ids):
        k = kartu(gs[gid], jenis, ukuran)
        lembar.paste(k, (16 + (i % kolom) * (ukuran + 16), 16 + (i // kolom) * (ukuran + 16)), k)
    keluar = f"/tmp/peraga_{jenis}.png"
    lembar.save(keluar)
    print(keluar)


if __name__ == "__main__":
    main()
