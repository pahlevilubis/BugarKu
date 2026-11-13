"""Pratinjau gaya karakter 'manusia' (bukan garis) dari data pose yang sama."""
import math
import sys
from PIL import Image, ImageDraw, ImageFilter
from pose import proses, IDX
import gerakan as G

S = 3  # supersampling

KULIT = (198, 139, 89)
KULIT_G = (168, 113, 70)
RAMBUT = (35, 30, 30)
BAJU = (44, 50, 64)
BAJU_G = (30, 34, 45)
CELANA = (28, 31, 38)
CELANA_G = (18, 20, 25)
SEPATU = (245, 245, 245)
SOL = (240, 110, 50)
SOROT = (255, 96, 20, 150)
LATAR = (236, 239, 243)
MATRAS = (70, 76, 88)


def tabung(d, a, b, ra, rb, warna):
    dx, dy = b[0] - a[0], b[1] - a[1]
    L = math.hypot(dx, dy) or 1e-6
    nx, ny = -dy / L, dx / L
    d.polygon([(a[0] + nx * ra, a[1] + ny * ra), (b[0] + nx * rb, b[1] + ny * rb),
               (b[0] - nx * rb, b[1] - ny * rb), (a[0] - nx * ra, a[1] - ny * ra)], fill=warna)
    d.ellipse([a[0] - ra, a[1] - ra, a[0] + ra, a[1] + ra], fill=warna)
    d.ellipse([b[0] - rb, b[1] - rb, b[0] + rb, b[1] + rb], fill=warna)


def lerp(a, b, t):
    return (a[0] + (b[0] - a[0]) * t, a[1] + (b[1] - a[1]) * t)


def kartu(g, ukuran=230, frame=None):
    data = proses(g)
    frs = data["_frames"]
    fr = frs[data["ikon"] if frame is None else frame]
    depan = data["tampak"] == "depan"
    W = ukuran * S
    img = Image.new("RGBA", (W, W), (0, 0, 0, 0))
    d = ImageDraw.Draw(img, "RGBA")
    d.rounded_rectangle([0, 0, W - 1, W - 1], radius=28 * S, fill=LATAR)
    kt = data["kotak"]
    lebar = kt[2] - kt[0]
    tinggi = kt[3] - kt[1]
    pad = 0.12 * W
    sk = min((W - 2 * pad) / lebar, (W - 2 * pad) / tinggi)
    ox = (W - lebar * sk) / 2 - kt[0] * sk
    lantai = W - pad * 0.9
    def P(pt):
        return (ox + pt[0] * sk, lantai - pt[1] * sk)
    u = sk  # satu satuan dunia
    # matras untuk gerakan lantai
    rendah = min(fr[IDX["leher"]][1], fr[IDX["pinggul"]][1]) < 25
    if rendah:
        d.rounded_rectangle([P((kt[0] - 4, 0))[0], lantai - 1.2 * u, P((kt[2] + 4, 0))[0], lantai + 1.6 * u],
                            radius=1.2 * u, fill=MATRAS)
    # bayangan
    xs = [P(p)[0] for p in fr]
    d.ellipse([min(xs) - 2 * u, lantai - 1.2 * u, max(xs) + 2 * u, lantai + 1.8 * u], fill=(0, 0, 0, 28))
    for gp in data["props"]:
        tabung(d, P((gp[0], gp[1])), P((gp[2], gp[3])), 1.3 * u, 1.3 * u, (150, 120, 90))

    def titik(n):
        return P(fr[IDX[n]])

    def lengan(n, gelap):
        b, s, t = titik("bahu" + n), titik("siku" + n), titik("tangan" + n)
        kul = KULIT_G if gelap else KULIT
        tabung(d, s, t, 1.9 * u, 1.5 * u, kul)
        tabung(d, b, s, 2.3 * u, 1.9 * u, kul)
        tabung(d, b, lerp(b, s, 0.45), 2.8 * u, 2.5 * u, BAJU_G if gelap else BAJU)
        d.ellipse([t[0] - 1.8 * u, t[1] - 1.8 * u, t[0] + 1.8 * u, t[1] + 1.8 * u], fill=kul)

    def kaki(n, gelap):
        g_, l, m, j = titik("pg" + n), titik("lutut" + n), titik("mata" + n), titik("jari" + n)
        c = CELANA_G if gelap else CELANA
        tabung(d, l, m, 2.9 * u, 2.1 * u, c)
        tabung(d, g_, l, 3.9 * u, 2.9 * u, c)
        tabung(d, m, j, 1.9 * u, 1.6 * u, SEPATU)
        tabung(d, lerp(m, j, 0.1), j, 0.9 * u, 0.8 * u, SOL)

    def sorot(a, b, ra):
        tabung(d, a, b, ra, ra * 0.85, SOROT)

    kaki("2", True)
    lengan("2", True)
    # badan
    pg, lh = titik("pinggul"), titik("leher")
    if depan:
        b1, b2, g1, g2 = titik("bahu1"), titik("bahu2"), titik("pg1"), titik("pg2")
        tabung(d, g1, g2, 3.6 * u, 3.6 * u, CELANA)
        d.polygon([b1, b2, lerp(g2, b2, 0.12), lerp(g1, b1, 0.12)], fill=BAJU)
        tabung(d, b1, b2, 2.6 * u, 2.6 * u, BAJU)
        tabung(d, lerp(g1, b1, 0.1), lerp(g2, b2, 0.1), 3.2 * u, 3.2 * u, BAJU)
    else:
        tabung(d, pg, lerp(pg, lh, 0.15), 4.6 * u, 4.4 * u, CELANA)
        tabung(d, lerp(pg, lh, 0.12), lerp(pg, lh, 0.85), 4.3 * u, 4.9 * u, BAJU)
        tabung(d, lerp(pg, lh, 0.8), lh, 4.6 * u, 3.4 * u, BAJU)
    kaki("1", False)
    # leher & kepala
    kp = titik("kepala")
    tabung(d, lh, lerp(lh, kp, 0.6), 1.9 * u, 1.9 * u, KULIT_G)
    rk = 5.4 * u
    d.ellipse([kp[0] - rk, kp[1] - rk, kp[0] + rk, kp[1] + rk], fill=KULIT)
    # rambut: tutup bagian atas-belakang kepala
    arah_atas = math.atan2(-(kp[1] - lh[1]), kp[0] - lh[0])  # dalam koordinat layar dibalik
    awal = math.degrees(arah_atas)
    if depan:
        d.pieslice([kp[0] - rk * 1.05, kp[1] - rk * 1.08, kp[0] + rk * 1.05, kp[1] + rk * 1.02],
                   start=-awal - 95, end=-awal + 95, fill=RAMBUT)
    else:
        d.pieslice([kp[0] - rk * 1.06, kp[1] - rk * 1.06, kp[0] + rk * 1.06, kp[1] + rk * 1.06],
                   start=-awal - 40, end=-awal + 140 - 180 + 180, fill=RAMBUT)
        # telinga
        e = (kp[0] - math.cos(arah_atas + math.pi / 2) * 0.5 * u, kp[1] + math.sin(arah_atas + math.pi / 2) * 0.5 * u)
        d.ellipse([e[0] - 1.2 * u, e[1] - 1.4 * u, e[0] + 1.2 * u, e[1] + 1.4 * u], fill=KULIT_G)
    lengan("1", False)
    # sorotan otot
    for s in g.get("sorot", []):
        if s == "lengan":
            sorot(lerp(titik("bahu1"), titik("siku1"), 0.15), titik("siku1"), 1.7 * u)
        elif s in ("dada", "bahu", "punggung"):
            sorot(lerp(pg, lh, 0.62), lerp(pg, lh, 0.9), 2.8 * u)
        elif s == "perut":
            sorot(lerp(pg, lh, 0.2), lerp(pg, lh, 0.55), 2.8 * u)
        elif s == "kaki":
            sorot(lerp(titik("pg1"), titik("lutut1"), 0.15), lerp(titik("pg1"), titik("lutut1"), 0.85), 2.4 * u)
        elif s == "bokong":
            sorot(pg, lerp(pg, titik("lutut1"), 0.25), 3.0 * u)
    img = img.resize((ukuran, ukuran), Image.LANCZOS)
    return img


def main():
    ids = sys.argv[1].split(",")
    kolom = 4
    ukuran = 230
    gs = {g["id"]: g for g in G.G}
    baris = (len(ids) + kolom - 1) // kolom
    lembar = Image.new("RGB", (kolom * (ukuran + 16) + 16, baris * (ukuran + 16) + 16), "white")
    for i, gid in enumerate(ids):
        k = kartu(gs[gid], ukuran)
        lembar.paste(k, (16 + (i % kolom) * (ukuran + 16), 16 + (i // kolom) * (ukuran + 16)), k)
    lembar.save("/tmp/karakter.png")
    print("/tmp/karakter.png")


if __name__ == "__main__":
    main()
