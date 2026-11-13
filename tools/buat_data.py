"""
Bangun app/src/main/assets/bugarku.json dari gerakan.py + konten.py.
Jalankan:  python3 tools/buat_data.py
"""
import json
import os
import sys

sys.path.insert(0, os.path.dirname(__file__))
from pose import proses  # noqa: E402
import gerakan as G  # noqa: E402
import konten as K  # noqa: E402

AKAR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
KELUAR = os.path.join(AKAR, "app", "src", "main", "assets", "bugarku.json")


def main():
    ids = set()
    daftar = []
    for g in G.G:
        assert g["id"] not in ids, "id ganda: " + g["id"]
        ids.add(g["id"])
        assert g["kat"] in dict(G.KATEGORI), g["id"]
        assert g["tipe"] in ("rep", "detik"), g["id"]
        a = proses(g)
        ikon = 0 if g.get("ikon_awal") else a["ikon"]
        daftar.append(dict(
            id=g["id"], nama=g["nama"], kat=g["kat"], otot=g["otot"], level=g["level"], tipe=g["tipe"],
            n=g["n"], sisi=bool(g.get("sisi", False)), met=g["met"], ringan=bool(g.get("ringan", False)),
            sorot=g.get("sorot", []), langkah=g["langkah"], tips=g.get("tips", []), salah=g.get("salah", []),
            mudah=g.get("mudah", ""), tampak=a["tampak"], fps=a["fps"], nf=a["nf"], kotak=a["kotak"],
            props=a["props"], ikon=ikon, f=a["f"],
        ))
    sesi_ids = set()
    sesi = []
    for s in K.SESI:
        for gid, n in s["item"]:
            assert gid in ids, f"sesi {s['id']}: gerakan tidak ada {gid}"
        sesi_ids.add(s["id"])
        sesi.append(dict(id=s["id"], judul=s["judul"], ringkas=s["ringkas"], kat=s["kat"], level=s["level"],
                         putaran=s["putaran"], istirahat=s.get("istirahat", 0),
                         item=[dict(g=gid, n=n) for gid, n in s["item"]]))
    program = []
    for p in K.PROGRAM:
        assert len(p["hari"]) == 28, p["id"]
        for h in p["hari"]:
            if "sesi" in h:
                assert h["sesi"] in sesi_ids, f"program {p['id']}: sesi tidak ada {h['sesi']}"
        program.append(dict(id=p["id"], judul=p["judul"], ringkas=p["ringkas"], kat=p["kat"], catatan=p["catatan"],
                            hari=[dict(sesi=h.get("sesi", ""), kali=h.get("kali", 1.0), istirahat=h.get("istirahat", ""))
                                  for h in p["hari"]]))
    artikel = []
    for a in K.ARTIKEL:
        isi = []
        for jenis, nilai in a["isi"]:
            assert jenis in ("h", "p", "d", "c")
            isi.append(dict(t=jenis, v=nilai))
        artikel.append(dict(id=a["id"], judul=a["judul"], ringkas=a["ringkas"], ikon=a["ikon"], isi=isi))
    data = dict(
        versi=1,
        kategori=[dict(id=i, nama=n) for i, n in G.KATEGORI],
        gerakan=daftar, sesi=sesi, program=program, artikel=artikel, tips=K.TIPS,
    )
    os.makedirs(os.path.dirname(KELUAR), exist_ok=True)
    with open(KELUAR, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, separators=(",", ":"))
    print(f"{KELUAR}: {os.path.getsize(KELUAR) // 1024} KB, {len(daftar)} gerakan, {len(sesi)} sesi, "
          f"{len(program)} program, {len(artikel)} artikel")


if __name__ == "__main__":
    main()
