"""Cek bugarku.json sebelum build: JSON valid, data animasi utuh, semua rujukan ada."""
import json
import sys

TITIK = 17

path = sys.argv[1] if len(sys.argv) > 1 else "app/src/main/assets/bugarku.json"
with open(path, encoding="utf-8") as f:
    d = json.load(f)

kat = {k["id"] for k in d["kategori"]}
gid = set()
for g in d["gerakan"]:
    for kunci in ("id", "nama", "kat", "otot", "tipe", "n", "langkah", "f", "nf", "kotak", "fps"):
        assert kunci in g, f"{g.get('id')}: kunci {kunci} tidak ada"
    assert g["kat"] in kat, g["id"]
    angka = g["f"].split(" ")
    assert len(angka) == g["nf"] * TITIK * 2, f"{g['id']}: jumlah angka frame tidak cocok"
    [float(a) for a in angka]
    assert 0 <= g["ikon"] < g["nf"], g["id"]
    gid.add(g["id"])

sid = set()
for s in d["sesi"]:
    for it in s["item"]:
        assert it["g"] in gid, f"sesi {s['id']}: {it['g']}"
    sid.add(s["id"])

for p in d["program"]:
    assert len(p["hari"]) == 28, p["id"]
    for h in p["hari"]:
        assert h["sesi"] == "" or h["sesi"] in sid, f"program {p['id']}: {h['sesi']}"

for a in d["artikel"]:
    for b in a["isi"]:
        assert b["t"] in ("h", "p", "d", "c"), a["id"]

print(f"OK: {len(gid)} gerakan, {len(sid)} sesi, {len(d['program'])} program, {len(d['artikel'])} artikel, {len(d['tips'])} tips")
