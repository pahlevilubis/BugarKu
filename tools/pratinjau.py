"""Gambar lembar pratinjau animasi (beberapa frame per gerakan) untuk dicek mata."""
import sys
from PIL import Image, ImageDraw, ImageFont
from pose import proses, IDX, KEPALA
import gerakan as G

W = 170
H = 150


def gambar(d, fr, ax, ay, skala, data):
    def p(pt):
        return (ax + (pt[0] - data["kotak"][0]) * skala, ay + H - 12 - (pt[1]) * skala)
    # lantai
    d.line([(ax + 4, p((0, 0))[1]), (ax + W - 4, p((0, 0))[1])], fill=(120, 120, 120), width=1)
    for g in data["props"]:
        d.line([p((g[0], g[1])), p((g[2], g[3]))], fill=(140, 110, 80), width=3)
    lebar = max(2, int(5 * skala))
    jauh = (150, 170, 200)
    dekat = (30, 60, 120)
    def seg(a, b, warna):
        d.line([p(fr[IDX[a]]), p(fr[IDX[b]])], fill=warna, width=lebar)
    for a, b in [("bahu2", "siku2"), ("siku2", "tangan2"), ("pg2", "lutut2"), ("lutut2", "mata2"), ("mata2", "jari2")]:
        seg(a, b, jauh)
    if data["tampak"] == "depan":
        seg("bahu1", "bahu2", dekat)
        seg("pg1", "pg2", dekat)
    seg("pinggul", "leher", dekat)
    for a, b in [("bahu1", "siku1"), ("siku1", "tangan1"), ("pg1", "lutut1"), ("lutut1", "mata1"), ("mata1", "jari1")]:
        seg(a, b, (230, 100, 40))
    c = p(fr[0])
    r = KEPALA * skala
    d.ellipse([c[0] - r, c[1] - r, c[0] + r, c[1] + r], fill=dekat)


def main():
    pilih = sys.argv[1] if len(sys.argv) > 1 else None
    daftar = [g for g in G.G if not pilih or g["kat"] == pilih or g["id"] == pilih]
    kolom = int(sys.argv[2]) if len(sys.argv) > 2 else 5
    img = Image.new("RGB", (W * kolom + 150, H * len(daftar)), "white")
    d = ImageDraw.Draw(img)
    for baris, g in enumerate(daftar):
        data = proses(g)
        fr = data["_frames"]
        kt = data["kotak"]
        skala = min((W - 10) / (kt[2] - kt[0]), (H - 20) / (kt[3] - 0 + 2))
        d.text((4, baris * H + 4), g["id"], fill="black")
        d.text((4, baris * H + 18), f"nf={data['nf']} ikon={data['ikon']}", fill="gray")
        n = len(fr)
        pilihan = [0, n // 4, n // 2, (3 * n) // 4, data["ikon"]] if kolom == 5 else [i * n // kolom for i in range(kolom)]
        for k, i in enumerate(pilihan):
            gambar(d, fr[i], 150 + k * W, baris * H, skala, data)
    keluar = f"/tmp/pratinjau_{pilih or 'semua'}.png"
    img.save(keluar)
    print(keluar)


if __name__ == "__main__":
    main()
