"""
Daftar gerakan BugarKu: teks panduan + keyframe animasi.
Semua gerakan tanpa alat (paling banyak kursi, dinding, atau botol air).
"""
import math
from pose import maju, TORSO

KATEGORI = [
    ("atas", "Dada & Bahu"),
    ("lengan", "Lengan"),
    ("punggung", "Punggung"),
    ("perut", "Perut"),
    ("kaki", "Kaki & Bokong"),
    ("kardio", "Kardio"),
    ("regang", "Peregangan"),
]


def badan_lurus(pangkal, panjang_bawah, tinggi_bahu):
    """Badan lurus dari titik tumpu (pergelangan kaki/lutut) sampai bahu di ketinggian tertentu."""
    total = panjang_bawah + TORSO
    th = math.degrees(math.asin((tinggi_bahu - pangkal[1]) / total))
    pinggul = maju(pangkal, th, panjang_bawah)
    leher = maju(pinggul, th, TORSO)
    return pinggul, th, leher


def kursi(x_depan, x_belakang, tinggi=22.0, sandaran=None):
    garis = [[x_belakang, tinggi, x_depan, tinggi],
             [x_depan - 1, tinggi, x_depan - 1, 0.0],
             [x_belakang + 1, tinggi, x_belakang + 1, 0.0]]
    if sandaran:
        xs = x_belakang + 1 if x_belakang < x_depan else x_belakang - 1
        garis.append([xs, tinggi, xs, sandaran])
    return garis


G = []


def gerakan(**kw):
    G.append(kw)


# ───────────────────────────── DADA & BAHU ─────────────────────────────

def _pushup(ankle=(0.0, 6.0), dada_x=0.0):
    hip0, th0, n0 = badan_lurus(ankle, 41, 29)
    hip1, th1, n1 = badan_lurus(ankle, 41, 9)
    tangan = (n0[0] + dada_x, 0.0)
    k0 = dict(x=hip0[0], y=hip0[1], t=th0, k=-12, h1=tangan, h2=(tangan[0] - 2, 0.0),
              f1=ankle, f2=(ankle[0] - 1, ankle[1] + 0.5), fo1=70, fo2=70, dur=0.9, tahan=0.2)
    k1 = dict(x=hip1[0], y=hip1[1], t=th1, k=-6, h1=tangan, h2=(tangan[0] - 2, 0.0),
              f1=ankle, f2=(ankle[0] - 1, ankle[1] + 0.5), fo1=70, fo2=70, dur=1.1, tahan=0.2)
    return [k0, k1]


gerakan(
    id="push_up", ikon_awal=True, nama="Push Up", kat="atas", otot="Dada, bahu, lengan belakang (trisep), perut",
    level=2, tipe="rep", n=10, met=8.0, sorot=["dada", "lengan"],
    kf=_pushup(), tanam=["tangan1", "tangan2", "kaki1", "kaki2"], poros=(0.0, 6.0),
    langkah=[
        "Posisi plank tinggi: telapak tangan di lantai sedikit lebih lebar dari bahu, kaki lurus ke belakang bertumpu pada ujung jari kaki.",
        "Kencangkan perut dan bokong supaya badan lurus dari kepala sampai tumit.",
        "Tekuk siku perlahan, turunkan dada sampai hampir menyentuh lantai. Siku mengarah serong ke belakang, bukan melebar ke samping.",
        "Dorong lantai kuat-kuat sampai lengan lurus kembali.",
    ],
    tips=["Tarik napas saat turun, buang napas saat mendorong naik.", "Lebih baik 5 kali dengan bentuk benar daripada 15 kali asal-asalan."],
    salah=["Pinggul turun (melengkung) atau terlalu tinggi.", "Kepala menunduk/mendongak; pandangan ke lantai sedikit di depan tangan."],
    mudah="Push up lutut atau push up dinding.",
)

gerakan(
    id="push_up_lutut", ikon_awal=True, nama="Push Up Lutut", kat="atas", otot="Dada, bahu, trisep",
    level=1, tipe="rep", n=12, met=5.0, sorot=["dada", "lengan"],
    kf=(lambda: [
        dict(zip(["x", "y"], badan_lurus((20.0, 3.0), 21, 29)[0]),
             t=badan_lurus((20.0, 3.0), 21, 29)[1], k=-12,
             h1=(badan_lurus((20.0, 3.0), 21, 29)[2][0], 0.0), h2=(badan_lurus((20.0, 3.0), 21, 29)[2][0] - 2, 0.0),
             p1=badan_lurus((20.0, 3.0), 21, 29)[1] + 180, s1=170, p2=badan_lurus((20.0, 3.0), 21, 29)[1] + 180, s2=168,
             fo1=60, fo2=60, dur=0.9, tahan=0.2),
        dict(zip(["x", "y"], badan_lurus((20.0, 3.0), 21, 9)[0]),
             t=badan_lurus((20.0, 3.0), 21, 9)[1], k=-6,
             p1=badan_lurus((20.0, 3.0), 21, 9)[1] + 180, s1=170, p2=badan_lurus((20.0, 3.0), 21, 9)[1] + 180, s2=168,
             fo1=60, fo2=60, dur=1.1, tahan=0.2),
    ])(), tanam=["tangan1", "tangan2"], poros=(20.0, 3.0),
    langkah=[
        "Mulai dari posisi merangkak, lalu jalankan tangan ke depan sampai badan lurus dari kepala sampai lutut.",
        "Silangkan atau angkat sedikit telapak kaki, tangan sedikit lebih lebar dari bahu.",
        "Turunkan dada perlahan dengan menekuk siku, lalu dorong kembali ke atas.",
    ],
    tips=["Pakai alas (matras/handuk terlipat) di bawah lutut supaya tidak sakit.", "Kalau sudah bisa 3 set × 15, coba push up biasa."],
    salah=["Bokong menungging ke atas — badan harus tetap lurus dari kepala ke lutut."],
    mudah="Push up dinding.",
)


def _pushup_dinding():
    dinding = 0.0
    # badan lurus dari pergelangan kaki, tangan di dinding setinggi bahu
    th0 = 70.0
    ankle = (0.0, 0.0)
    leher0 = maju(ankle, th0, 71)
    dinding = leher0[0] + 30.0
    hip0 = maju(ankle, th0, 41)
    leher_x1 = dinding - 9
    th1 = math.degrees(math.acos(leher_x1 / 71))
    hip1 = maju(ankle, th1, 41)
    tangan = (dinding - 2.0, leher0[1] - 0.5)
    k0 = dict(x=hip0[0], y=hip0[1], t=th0, k=-5, h1=tangan, h2=(tangan[0], tangan[1] - 2),
              f1=ankle, f2=(ankle[0] - 1.5, 0.0), dur=0.9, tahan=0.2)
    k1 = dict(x=hip1[0], y=hip1[1], t=th1, k=-5, a1=-60, b1=60, a2=-62, b2=58,
              f1=ankle, f2=(ankle[0] - 1.5, 0.0), dur=1.1, tahan=0.2)
    return [k0, k1], [[dinding, 0.0, dinding, 100.0]]


_kf, _pr = _pushup_dinding()
gerakan(
    id="push_up_dinding", nama="Push Up Dinding", kat="atas", otot="Dada, bahu, trisep",
    level=1, tipe="rep", n=15, met=3.8, ringan=True, sorot=["dada", "lengan"],
    kf=_kf, props=_pr, tanam=["tangan1", "tangan2", "kaki1", "kaki2"], poros=(0.0, 0.0),
    langkah=[
        "Berdiri menghadap dinding sejauh kira-kira satu lengan.",
        "Letakkan telapak tangan di dinding setinggi bahu, sedikit lebih lebar dari bahu.",
        "Tekuk siku dan condongkan badan ke dinding sampai dada mendekat, badan tetap lurus.",
        "Dorong dinding sampai lengan lurus kembali.",
    ],
    tips=["Makin jauh kaki dari dinding, makin berat latihannya.", "Cocok untuk pemula, berat badan berlebih, atau yang lututnya sakit."],
    salah=["Pinggul maju duluan — dorong dengan dada, bukan pinggul."],
    mudah="Berdiri lebih dekat ke dinding.",
)


def _incline():
    meja = 22.0
    ankle = (0.0, 6.0)
    hip0, th0, n0 = badan_lurus(ankle, 41, meja + 29)
    hip1, th1, n1 = badan_lurus(ankle, 41, meja + 9)
    tangan = (n0[0], meja)
    k0 = dict(x=hip0[0], y=hip0[1], t=th0, k=-10, h1=tangan, h2=(tangan[0] - 2, meja),
              f1=ankle, f2=(ankle[0] - 1, ankle[1] + 0.5), fo1=70, fo2=70, dur=0.9, tahan=0.2)
    k1 = dict(x=hip1[0], y=hip1[1], t=th1, k=-5, f1=ankle, f2=(ankle[0] - 1, ankle[1] + 0.5),
              fo1=70, fo2=70, dur=1.1, tahan=0.2)
    return [k0, k1], kursi(tangan[0] + 12, tangan[0] - 12, meja)


_kf, _pr = _incline()
gerakan(
    id="incline_push_up", nama="Push Up Kursi (Incline)", kat="atas", otot="Dada bagian bawah, bahu, trisep",
    level=1, tipe="rep", n=12, met=4.5, sorot=["dada", "lengan"],
    kf=_kf, props=_pr, tanam=["tangan1", "tangan2", "kaki1", "kaki2"], poros=(0.0, 6.0),
    langkah=[
        "Letakkan tangan di tepi kursi yang kokoh, meja, atau anak tangga. Pastikan kursi tidak bisa bergeser (sandarkan ke dinding).",
        "Langkahkan kaki ke belakang sampai badan lurus.",
        "Turunkan dada ke tepi kursi dengan menekuk siku, lalu dorong kembali.",
    ],
    tips=["Ini jembatan dari push up dinding ke push up lantai.", "Makin rendah tumpuan tangan, makin berat."],
    salah=["Memakai kursi beroda atau yang mudah tergelincir."],
    mudah="Push up dinding.",
)


def _pike():
    ankle = (0.0, 4.0)
    k0 = dict(x=13, y=43, t=-31, k=-15, h1=(49.0, 0.0), h2=(47.0, 0.0), f1=ankle, f2=(-1.0, 4.5),
              fo1=60, fo2=60, dur=0.9, tahan=0.2)
    k1 = dict(x=24, y=36, t=-48, k=-12, f1=ankle, f2=(-1.0, 4.5), fo1=60, fo2=60, dur=1.1, tahan=0.2)
    return [k0, k1]


gerakan(
    id="pike_push_up", nama="Pike Push Up", kat="atas", otot="Bahu, trisep, dada atas",
    level=3, tipe="rep", n=8, met=8.0, sorot=["bahu", "lengan"],
    kf=_pike(), tanam=["tangan1", "tangan2", "kaki1", "kaki2"],
    langkah=[
        "Mulai dari posisi push up, lalu angkat pinggul tinggi-tinggi sampai badan membentuk huruf V terbalik.",
        "Tekuk siku dan turunkan kepala ke arah lantai di antara kedua tangan.",
        "Dorong kembali sampai lengan lurus. Pinggul tetap tinggi.",
    ],
    tips=["Makin dekat kaki ke tangan, makin berat untuk bahu."],
    salah=["Siku melebar terlalu jauh ke samping.", "Menjatuhkan kepala dengan cepat — turun perlahan."],
    mudah="Tahan posisi V terbalik saja tanpa menekuk siku, atau ganti dengan push up lutut.",
)

gerakan(
    id="putar_lengan", nama="Putar Lengan", kat="atas", otot="Bahu, lengan atas",
    level=1, tipe="detik", n=30, met=3.0, ringan=True, sorot=["bahu", "lengan"],
    kf=[
        dict(a1=-90, b1=-90, a2=-95, b2=-95, dur=0.45),
        dict(a1=0, b1=0, a2=-5, b2=-5, dur=0.45),
        dict(a1=90, b1=90, a2=85, b2=85, dur=0.45),
        dict(a1=180, b1=180, a2=175, b2=175, dur=0.45),
    ],
    langkah=[
        "Berdiri tegak, kaki selebar bahu, lengan lurus.",
        "Putar kedua lengan membentuk lingkaran besar ke depan dengan tenang.",
        "Setelah setengah waktu, ganti putaran ke arah belakang.",
    ],
    tips=["Bagus sebagai pemanasan sebelum latihan badan atas.", "Bahu tetap rileks, jangan diangkat ke telinga."],
    salah=["Memutar terlalu cepat sampai badan ikut bergoyang."],
    mudah="Buat lingkaran lebih kecil.",
)

# ───────────────────────────── LENGAN ─────────────────────────────


def _dips():
    tinggi = 22.0
    tepi = 0.0
    k0 = dict(x=3, y=21, t=90, k=0, h1=(tepi - 1.5, tinggi), h2=(tepi - 3.5, tinggi),
              f1=(26, 0), f2=(25, 0.5), fo1=90, fo2=90, dur=0.9, tahan=0.2)
    k1 = dict(x=3, y=9, t=96, k=0, a1=-147, b1=-39, a2=-149, b2=-41,
              f1=(26, 0), f2=(25, 0.5), fo1=90, fo2=90, dur=1.1, tahan=0.2)
    return [k0, k1], kursi(tepi, tepi - 24, tinggi, sandaran=48)


_kf, _pr = _dips()
gerakan(
    id="dips_kursi", nama="Dips Kursi", kat="lengan", otot="Lengan belakang (trisep), bahu, dada",
    level=2, tipe="rep", n=10, met=5.0, sorot=["lengan"],
    kf=_kf, props=_pr, tanam=["tangan1", "tangan2", "kaki1", "kaki2"],
    langkah=[
        "Duduk di tepi kursi yang kokoh (sandarkan ke dinding). Pegang tepi kursi di samping pinggul, jari menghadap ke depan.",
        "Geser pinggul ke depan sampai lepas dari kursi, lutut ditekuk, telapak kaki rata di lantai.",
        "Tekuk siku ke belakang dan turunkan pinggul sampai lengan atas hampir sejajar lantai.",
        "Dorong dengan telapak tangan sampai lengan lurus kembali.",
    ],
    tips=["Punggung tetap dekat dengan kursi.", "Kaki diluruskan = lebih berat; kaki makin dekat = lebih ringan."],
    salah=["Turun terlalu dalam sampai bahu terasa nyeri.", "Siku membuka ke samping."],
    mudah="Turun setengah saja, atau tekuk lutut lebih banyak.",
)

gerakan(
    id="push_up_diamond", ikon_awal=True, nama="Push Up Diamond", kat="lengan", otot="Trisep, dada tengah, bahu",
    level=3, tipe="rep", n=8, met=8.0, sorot=["lengan", "dada"],
    kf=_pushup(dada_x=-4.0), tanam=["tangan1", "tangan2", "kaki1", "kaki2"], poros=(0.0, 6.0),
    langkah=[
        "Posisi push up, tetapi kedua tangan berdekatan di bawah dada sampai ibu jari dan telunjuk membentuk segitiga/berlian.",
        "Badan lurus, perut kencang.",
        "Turunkan dada ke arah tangan dengan siku tetap dekat badan, lalu dorong kembali.",
    ],
    tips=["Gerakan ini berat — kalau belum kuat, lakukan dengan bertumpu pada lutut."],
    salah=["Siku melebar ke samping; harus menyusuri sisi badan."],
    mudah="Push up diamond dengan lutut di lantai.",
)


def _plank_naik_turun():
    kaki = (0.0, 6.0)
    def badan(tinggi):
        return badan_lurus(kaki, 41, tinggi)
    h0, t0, n0 = badan(17)
    h2, t2, n2 = badan(31)
    h1, t1, n1 = badan(24)
    k0 = dict(x=h0[0], y=h0[1], t=t0, k=-10, a1=-90, b1=0, a2=-92, b2=-2, f1=kaki, f2=(-1, 6.5),
              fo1=70, fo2=70, dur=0.7, tahan=0.15)
    k1 = dict(x=h1[0], y=h1[1], t=t1, k=-10, h1=(n1[0], 0.0), a2=-90, b2=-30, f1=kaki, f2=(-1, 6.5),
              fo1=70, fo2=70, dur=0.6)
    k2 = dict(x=h2[0], y=h2[1], t=t2, k=-12, h1=(n2[0], 0.0), h2=(n2[0] - 2, 0.0), f1=kaki, f2=(-1, 6.5),
              fo1=70, fo2=70, dur=0.6, tahan=0.15)
    k3 = dict(x=h1[0], y=h1[1], t=t1, k=-10, a1=-90, b1=-30, h2=(n1[0] - 2, 0.0), f1=kaki, f2=(-1, 6.5),
              fo1=70, fo2=70, dur=0.6)
    return [k0, k1, k2, k3]


gerakan(
    id="plank_naik_turun", nama="Plank Naik Turun", kat="lengan", otot="Trisep, bahu, perut",
    level=2, tipe="rep", n=10, met=6.0, sorot=["lengan", "perut"],
    kf=_plank_naik_turun(), tanam=["kaki1", "kaki2"], poros=(0.0, 6.0),
    langkah=[
        "Mulai dari plank siku: lengan bawah di lantai, siku di bawah bahu, badan lurus.",
        "Letakkan satu telapak tangan di lantai lalu dorong sampai lengan lurus, susul tangan lainnya hingga posisi plank tinggi.",
        "Turun kembali ke siku satu per satu. Hitung 1 kali setiap naik-turun.",
        "Gantian tangan yang memulai.",
    ],
    tips=["Buka kaki sedikit lebih lebar supaya pinggul tidak bergoyang."],
    salah=["Pinggul berayun kiri-kanan."],
    mudah="Lakukan dengan lutut di lantai.",
)

gerakan(
    id="curl_botol", nama="Curl Botol Air", kat="lengan", otot="Lengan depan (bisep), lengan bawah",
    level=1, tipe="rep", n=15, met=3.5, ringan=True, sorot=["lengan"],
    kf=[
        dict(a1=-92, b1=-80, a2=-88, b2=-76, dur=1.0, tahan=0.1),
        dict(a1=-92, b1=95, a2=-88, b2=99, dur=0.9, tahan=0.2),
    ],
    langkah=[
        "Berdiri tegak, pegang botol air (misalnya 1,5 liter) di masing-masing tangan, telapak menghadap ke depan.",
        "Siku menempel di samping badan. Tekuk siku dan angkat botol ke arah bahu.",
        "Turunkan perlahan sampai lengan hampir lurus.",
    ],
    tips=["Turunkan lebih lambat daripada saat mengangkat — ototnya bekerja lebih keras.", "Bisa juga pakai tas berisi buku."],
    salah=["Badan mengayun ke belakang untuk membantu mengangkat."],
    mudah="Pakai botol yang lebih kecil.",
)

gerakan(
    id="tinju", nama="Tinju di Tempat", kat="lengan", otot="Bahu, lengan, perut samping; juga kardio",
    level=1, tipe="detik", n=30, met=6.0, ringan=True, sorot=["lengan", "bahu"],
    kf=[
        dict(y=40.5, a1=-60, b1=75, a2=-64, b2=72, p1=-82, s1=-96, p2=-100, s2=-88, dur=0.25),
        dict(y=40.5, t=86, a1=-2, b1=0, a2=-64, b2=72, p1=-82, s1=-96, p2=-100, s2=-88, dur=0.22),
        dict(y=40.5, a1=-60, b1=75, a2=-64, b2=72, p1=-82, s1=-96, p2=-100, s2=-88, dur=0.22),
        dict(y=40.5, t=86, a1=-60, b1=75, a2=-4, b2=-2, p1=-82, s1=-96, p2=-100, s2=-88, dur=0.22),
    ],
    langkah=[
        "Berdiri dengan satu kaki sedikit di depan, lutut agak ditekuk, kepalan tangan di depan dagu.",
        "Pukul lurus ke depan bergantian kiri-kanan, putar sedikit pinggang setiap pukulan.",
        "Tarik tangan kembali ke depan dagu setelah setiap pukulan.",
    ],
    tips=["Jangan mengunci siku saat lengan lurus.", "Tambah kecepatan untuk kardio, perlambat untuk pemanasan."],
    salah=["Bahu tegang terangkat ke telinga."],
    mudah="Pukul pelan sambil tetap bernapas teratur.",
)

# ───────────────────────────── PUNGGUNG ─────────────────────────────

_TENGKURAP = dict(x=0, y=3, t=0, k=18, a1=4, b1=2, a2=2, b2=0, p1=180, s1=180, p2=178, s2=178, fo1=15, fo2=15)

gerakan(
    id="superman", nama="Superman", kat="punggung", otot="Punggung bawah, bokong, bahu",
    level=1, tipe="rep", n=12, met=3.5, ringan=True, sorot=["punggung"],
    kf=[
        dict(_TENGKURAP, dur=0.9, tahan=0.2),
        dict(_TENGKURAP, t=9, k=14, a1=22, b1=20, a2=20, b2=18, p1=172, s1=168, p2=170, s2=166, dur=0.9, tahan=1.2),
    ],
    langkah=[
        "Tengkurap di matras, lengan lurus ke depan di atas kepala.",
        "Kencangkan bokong, lalu angkat lengan, dada, dan kaki sedikit dari lantai bersamaan.",
        "Tahan 2 detik, pandangan tetap ke lantai, lalu turunkan perlahan.",
    ],
    tips=["Angkat secukupnya saja — tidak perlu tinggi.", "Bagus untuk yang sering duduk lama."],
    salah=["Mendongakkan kepala terlalu tinggi sehingga leher tegang."],
    mudah="Angkat lengan saja, lalu kaki saja, bergantian.",
)

_MERANGKAK = dict(x=0, y=22, t=15.5, k=-18, h1=(28.9, 0.5), h2=(27.4, 0.5), p1=-90, s1=180, p2=-92, s2=178,
                  fo1=15, fo2=15)
gerakan(
    id="bird_dog", nama="Bird Dog", kat="punggung", otot="Punggung bawah, perut, bokong, bahu",
    level=1, tipe="rep", n=10, sisi=True, met=3.0, ringan=True, sorot=["punggung", "perut"],
    kf=[
        dict(_MERANGKAK, dur=0.8),
        dict(_MERANGKAK, a1=8, b1=8, p2=174, s2=174, fo2=30, dur=1.0, tahan=0.8),
        dict(_MERANGKAK, dur=0.8),
        dict(_MERANGKAK, a2=6, b2=6, p1=176, s1=176, fo1=30, dur=1.0, tahan=0.8),
    ],
    langkah=[
        "Posisi merangkak: tangan di bawah bahu, lutut di bawah pinggul, punggung datar.",
        "Luruskan tangan kanan ke depan dan kaki kiri ke belakang bersamaan sampai sejajar lantai.",
        "Tahan 2 detik tanpa memutar pinggul, lalu kembali. Ganti tangan kiri dan kaki kanan.",
    ],
    tips=["Bayangkan ada gelas air di punggung — jangan sampai tumpah.", "Sangat aman untuk punggung yang sering pegal."],
    salah=["Mengangkat kaki terlalu tinggi sampai punggung melengkung."],
    mudah="Angkat tangan saja atau kaki saja.",
)

gerakan(
    id="good_morning", nama="Good Morning", kat="punggung", otot="Punggung bawah, paha belakang, bokong",
    level=1, tipe="rep", n=12, met=3.5, ringan=True, sorot=["punggung", "kaki"],
    kf=[
        dict(a1=100, b1=-150, a2=102, b2=-148, f1=(0, 0), f2=(-1.5, 0), dur=1.0, tahan=0.1),
        dict(x=-10, y=39, t=12, k=-5, a1=20, b1=150, a2=22, b2=152, f1=(0, 0), f2=(-1.5, 0), sk1=1, sk2=1,
             dur=1.1, tahan=0.3),
    ],
    tanam=["kaki1", "kaki2"],
    langkah=[
        "Berdiri, kaki selebar pinggul, tangan di belakang kepala (atau disilangkan di dada).",
        "Lutut sedikit ditekuk. Dorong pinggul ke belakang dan bungkukkan badan dengan punggung tetap lurus.",
        "Turun sampai badan hampir sejajar lantai atau sampai paha belakang terasa tertarik.",
        "Kencangkan bokong dan kembali berdiri tegak.",
    ],
    tips=["Gerak dari engsel pinggul, bukan dengan membungkukkan punggung."],
    salah=["Punggung membulat seperti kucing."],
    mudah="Bungkuk setengah saja.",
)

# ───────────────────────────── PERUT ─────────────────────────────

_TERLENTANG = dict(x=0, y=3, t=180, k=-20, a1=0, b1=0, a2=-2, b2=-2, p1=8, s1=8, p2=6, s2=6)
_LUTUT_TEKUK = dict(_TERLENTANG, f1=(21, 1.5), f2=(20, 1.5), fo1=62, fo2=62)

gerakan(
    id="crunch", nama="Crunch", kat="perut", otot="Perut depan",
    level=1, tipe="rep", n=15, met=3.8, sorot=["perut"],
    kf=[
        dict(_LUTUT_TEKUK, a1=32, b1=26, a2=30, b2=24, dur=0.9, tahan=0.1),
        dict(_LUTUT_TEKUK, t=152, k=-28, a1=34, b1=26, a2=32, b2=24, dur=0.8, tahan=0.5),
    ],
    tanam=["kaki1", "kaki2"],
    langkah=[
        "Berbaring telentang, lutut ditekuk, telapak kaki rata di lantai.",
        "Letakkan tangan di samping kepala atau lurus menunjuk lutut.",
        "Buang napas, kencangkan perut, dan angkat bahu dari lantai (punggung bawah tetap menempel).",
        "Tahan sebentar, lalu turun perlahan.",
    ],
    tips=["Gerakannya kecil saja — yang penting perut terasa mengencang.", "Ada ruang sekepalan tangan antara dagu dan dada."],
    salah=["Menarik leher/kepala dengan tangan.", "Naik terlalu tinggi sampai jadi sit-up dengan sentakan."],
    mudah="Tangan lurus ke arah lutut dan naik sedikit saja.",
)


def _plank_siku():
    kaki = (0.0, 6.0)
    h, t, n = badan_lurus(kaki, 41, 17)
    k0 = dict(x=h[0], y=h[1], t=t, k=-10, a1=-90, b1=0, a2=-92, b2=-2, f1=kaki, f2=(-1, 6.5),
              fo1=70, fo2=70, dur=1.5)
    k1 = dict(k0, y=h[1] + 0.8, t=t - 1, dur=1.5)
    return [k0, k1]


gerakan(
    id="plank", nama="Plank", kat="perut", otot="Seluruh otot perut, punggung, bahu",
    level=1, tipe="detik", n=30, met=3.8, sorot=["perut"],
    kf=_plank_siku(), tanam=["kaki1", "kaki2"], poros=(0.0, 6.0),
    langkah=[
        "Tumpukan lengan bawah di lantai, siku tepat di bawah bahu.",
        "Luruskan kaki ke belakang, bertumpu pada ujung jari kaki.",
        "Kencangkan perut dan bokong sehingga badan lurus seperti papan. Tahan.",
    ],
    tips=["Bernapas biasa — jangan menahan napas.", "Kalau gemetar, itu wajar; berhenti kalau bentuk badan sudah rusak."],
    salah=["Pinggul turun ke lantai.", "Bokong terlalu tinggi."],
    mudah="Plank dengan lutut di lantai.",
)

gerakan(
    id="mountain_climber", nama="Mountain Climber", kat="perut", otot="Perut, bahu, kaki; juga kardio",
    level=2, tipe="detik", n=30, met=8.0, sorot=["perut", "kaki"],
    kf=(lambda h, t, n: [
        dict(x=h[0], y=h[1], t=t, k=-12, h1=(n[0], 0.0), h2=(n[0] - 2, 0.0),
             p1=-30, s1=-160, p2=t + 180, s2=t + 180, fo1=60, fo2=70, dur=0.3),
        dict(x=h[0], y=h[1], t=t, k=-12, h1=(n[0], 0.0), h2=(n[0] - 2, 0.0),
             p1=t + 180, s1=t + 180, p2=-32, s2=-162, fo1=70, fo2=60, dur=0.3),
    ])(*badan_lurus((0.0, 6.0), 41, 29)),
    tanam=["tangan1", "tangan2"],
    langkah=[
        "Posisi plank tinggi: tangan di bawah bahu, badan lurus.",
        "Tarik satu lutut ke arah dada, lalu kembalikan sambil menarik lutut lainnya.",
        "Lakukan bergantian dengan cepat seperti berlari di tempat.",
    ],
    tips=["Bahu tetap di atas tangan.", "Pemula: lakukan perlahan, satu kaki bergantian."],
    salah=["Pinggul naik-turun terlalu banyak."],
    mudah="Lakukan lambat dengan tangan di kursi.",
)

gerakan(
    id="leg_raise", nama="Angkat Kaki (Leg Raise)", kat="perut", otot="Perut bagian bawah",
    level=2, tipe="rep", n=12, met=3.8, sorot=["perut"],
    kf=[
        dict(_TERLENTANG, p1=8, s1=8, p2=6, s2=6, dur=1.2, tahan=0.1),
        dict(_TERLENTANG, p1=82, s1=84, p2=80, s2=82, dur=1.0, tahan=0.2),
    ],
    langkah=[
        "Berbaring telentang, kaki lurus, tangan di samping badan (atau di bawah bokong).",
        "Tekan punggung bawah ke lantai dan angkat kedua kaki sampai tegak lurus.",
        "Turunkan perlahan sampai kaki hampir menyentuh lantai, lalu angkat lagi.",
    ],
    tips=["Semakin pelan saat turun, semakin terasa di perut bawah."],
    salah=["Punggung bawah terangkat melengkung dari lantai."],
    mudah="Tekuk lutut saat mengangkat kaki.",
)

gerakan(
    id="bicycle_crunch", nama="Bicycle Crunch", kat="perut", otot="Perut depan & samping",
    level=2, tipe="rep", n=20, met=5.0, sorot=["perut"],
    kf=[
        dict(_TERLENTANG, t=155, k=-30, a1=108, b1=200, a2=104, b2=196, p1=70, s1=-15, p2=25, s2=25, dur=0.6),
        dict(_TERLENTANG, t=155, k=-30, a1=108, b1=200, a2=104, b2=196, p1=25, s1=25, p2=70, s2=-15, dur=0.6),
    ],
    langkah=[
        "Berbaring telentang, tangan di belakang kepala, angkat bahu sedikit dari lantai.",
        "Tarik lutut kanan ke dada sambil memutar badan sehingga siku kiri mendekati lutut kanan; kaki kiri lurus.",
        "Ganti sisi seperti mengayuh sepeda. Hitung setiap sisi sebagai 1 kali.",
    ],
    tips=["Putar dari perut, bukan dari siku.", "Pelan dan terkontrol lebih efektif daripada cepat."],
    salah=["Menarik kepala dengan tangan."],
    mudah="Kaki yang lurus diangkat lebih tinggi.",
)

gerakan(
    id="dead_bug", nama="Dead Bug", kat="perut", otot="Perut dalam, punggung bawah",
    level=1, tipe="rep", n=10, sisi=True, met=3.0, ringan=True, sorot=["perut"],
    kf=[
        dict(_TERLENTANG, a1=90, b1=90, a2=88, b2=88, p1=90, s1=0, p2=88, s2=-2, dur=0.8),
        dict(_TERLENTANG, a1=172, b1=172, a2=88, b2=88, p1=90, s1=0, p2=12, s2=12, dur=1.0, tahan=0.4),
        dict(_TERLENTANG, a1=90, b1=90, a2=88, b2=88, p1=90, s1=0, p2=88, s2=-2, dur=0.8),
        dict(_TERLENTANG, a1=90, b1=90, a2=170, b2=170, p1=14, s1=14, p2=88, s2=-2, dur=1.0, tahan=0.4),
    ],
    langkah=[
        "Berbaring telentang, kedua lengan lurus ke langit-langit, lutut ditekuk 90° di atas pinggul.",
        "Tekan punggung bawah ke lantai. Turunkan lengan kanan ke belakang kepala dan luruskan kaki kiri mendekati lantai.",
        "Kembali ke tengah, lalu ganti lengan kiri dan kaki kanan.",
    ],
    tips=["Buang napas panjang saat meluruskan.", "Aman untuk punggung — pilihan bagus pengganti sit-up."],
    salah=["Punggung bawah terangkat dari lantai."],
    mudah="Gerakkan kaki saja, lengan tetap di atas.",
)


def _plank_samping():
    kaki = (0.0, 3.0)
    k0 = dict(x=41 * math.cos(math.radians(27.5)), y=3 + 41 * math.sin(math.radians(27.5)), t=27.5, k=0,
              a2=60, b2=120, f1=kaki, f2=(2.0, 5.5), fo1=90, fo2=-90, dur=1.2, tahan=0.6)
    k1 = dict(k0, y=k0["y"] - 6, t=36, dur=1.0, tahan=0.2)
    k0["h1"] = (0.0, 0.0)  # diganti di bawah setelah tahu posisi bahu
    return [k0, k1]


def _siapkan_plank_samping():
    from pose import selesaikan, fk, IDX
    kf = _plank_samping()
    p = dict(kf[0])
    p.pop("h1")
    pose = fk(selesaikan(p, True), True)
    bahu = pose[IDX["bahu1"]]
    kf[0]["h1"] = (bahu[0] + 1.0, 0.0)
    kf[0]["a2"] = 90
    kf[0]["b2"] = 90
    kf[1]["a2"] = 90
    kf[1]["b2"] = 90
    return kf


gerakan(
    id="plank_samping", nama="Plank Samping", kat="perut", otot="Perut samping (oblik), pinggang, bahu",
    level=2, tipe="detik", n=20, sisi=True, met=3.8, tampak="depan", sorot=["perut"],
    kf=_siapkan_plank_samping(), tanam=["tangan1", "kaki1", "kaki2"],
    langkah=[
        "Berbaring miring, bertumpu pada satu telapak tangan (atau siku) tepat di bawah bahu.",
        "Tumpuk kedua kaki lurus, lalu angkat pinggul sampai badan lurus dari kepala ke kaki.",
        "Tahan. Tangan atas boleh di pinggang atau lurus ke atas. Ganti sisi.",
    ],
    tips=["Jangan biarkan pinggul turun ke lantai.", "Versi di animasi: turun-naikkan pinggul pelan untuk melatih pinggang."],
    salah=["Badan condong ke depan atau belakang."],
    mudah="Tekuk kedua lutut dan bertumpu pada lutut.",
)

gerakan(
    id="flutter_kick", nama="Flutter Kick", kat="perut", otot="Perut bawah, paha depan",
    level=2, tipe="detik", n=30, met=4.0, sorot=["perut"],
    kf=[
        dict(_TERLENTANG, k=-30, t=172, p1=26, s1=26, p2=12, s2=12, dur=0.35),
        dict(_TERLENTANG, k=-30, t=172, p1=12, s1=12, p2=26, s2=26, dur=0.35),
    ],
    langkah=[
        "Berbaring telentang, tangan di samping atau di bawah bokong.",
        "Angkat kedua kaki lurus sedikit dari lantai, kepala dan bahu boleh sedikit terangkat.",
        "Ayunkan kaki naik-turun bergantian dengan gerakan kecil seperti berenang.",
    ],
    tips=["Jaga punggung bawah tetap menempel di lantai."],
    salah=["Kaki terlalu rendah sehingga punggung melengkung."],
    mudah="Angkat kaki lebih tinggi atau tekuk sedikit lutut.",
)

_D_TANGAN_KEPALA = dict(a1=38, b1=158, a2=142, b2=22)
gerakan(
    id="side_crunch_berdiri", nama="Crunch Samping Berdiri", kat="perut", otot="Perut samping, pinggang",
    level=1, tipe="rep", n=12, sisi=True, met=4.0, ringan=True, tampak="depan", sorot=["perut"],
    kf=[
        dict(_D_TANGAN_KEPALA, x=0, y=41, p1=-84, s1=-90, p2=-96, s2=-90, dur=0.6),
        dict(_D_TANGAN_KEPALA, x=-1, y=41, t=76, a1=-5, b1=150, p1=-15, s1=-92, p2=-94, s2=-90, dur=0.6, tahan=0.2),
        dict(_D_TANGAN_KEPALA, x=0, y=41, p1=-84, s1=-90, p2=-96, s2=-90, dur=0.6),
        dict(_D_TANGAN_KEPALA, x=1, y=41, t=104, a2=185, b2=30, p1=-86, s1=-90, p2=-165, s2=-88, dur=0.6, tahan=0.2),
    ],
    langkah=[
        "Berdiri tegak, kaki selebar bahu, tangan di belakang kepala dengan siku membuka.",
        "Angkat lutut kanan ke samping sambil membengkokkan badan ke kanan sehingga siku kanan mendekati lutut.",
        "Kembali berdiri, lalu ulangi ke sisi kiri.",
    ],
    tips=["Pilihan tepat untuk yang belum nyaman latihan di lantai.", "Kencangkan perut setiap kali siku turun."],
    salah=["Membungkuk ke depan — gerakan ke samping, bukan ke depan."],
    mudah="Angkat lutut lebih rendah, pegang kursi untuk keseimbangan.",
)

# ───────────────────────────── KAKI & BOKONG ─────────────────────────────

gerakan(
    id="squat", nama="Squat", kat="kaki", otot="Paha depan, bokong, paha belakang",
    level=1, tipe="rep", n=15, met=5.0, sorot=["kaki", "bokong"],
    kf=[
        dict(f1=(0, 0), f2=(-1.5, 0), dur=1.0, tahan=0.1),
        dict(x=-13, y=23.5, t=56, k=-20, a1=-2, b1=0, a2=-4, b2=-2, f1=(0, 0), f2=(-1.5, 0), dur=1.1, tahan=0.2),
    ],
    tanam=["kaki1", "kaki2"],
    langkah=[
        "Berdiri, kaki selebar bahu, ujung jari kaki sedikit menghadap keluar.",
        "Dorong pinggul ke belakang seperti mau duduk di kursi, tekuk lutut, lengan lurus ke depan untuk keseimbangan.",
        "Turun sampai paha hampir sejajar lantai (atau senyamannya), dada tetap tegak.",
        "Dorong lantai dengan tumit untuk berdiri kembali.",
    ],
    tips=["Lutut searah dengan ujung jari kaki.", "Berat badan di tumit dan tengah telapak kaki."],
    salah=["Tumit terangkat.", "Lutut masuk ke dalam saat naik."],
    mudah="Squat ke kursi: duduk sebentar lalu berdiri.",
)

gerakan(
    id="lunge", nama="Lunge di Tempat", kat="kaki", otot="Paha depan, bokong, keseimbangan",
    level=2, tipe="rep", n=10, sisi=True, met=5.0, sorot=["kaki", "bokong"],
    kf=[
        dict(x=0, y=38.5, t=90, a1=-125, b1=-20, a2=-122, b2=-18, f1=(17, 0), f2=(-17, 3), fo2=60, datar1=True, dur=1.0, tahan=0.1),
        dict(x=0, y=22.5, t=88, a1=-125, b1=-20, a2=-122, b2=-18, f1=(17, 0), f2=(-17, 3), fo2=60, dur=1.1, tahan=0.2),
    ],
    tanam=["kaki1", "kaki2"],
    langkah=[
        "Berdiri dengan satu kaki melangkah lebar ke depan dan kaki lainnya di belakang (bertumpu ujung jari).",
        "Turunkan badan lurus ke bawah sampai kedua lutut membentuk sudut sekitar 90°.",
        "Dorong dengan kaki depan untuk naik kembali. Setelah selesai, ganti kaki depan.",
    ],
    tips=["Badan tetap tegak, tangan di pinggang.", "Pegang sandaran kursi kalau sulit menjaga keseimbangan."],
    salah=["Lutut depan maju jauh melewati ujung jari kaki.", "Lutut belakang membentur lantai."],
    mudah="Turun setengah saja sambil berpegangan.",
)

gerakan(
    id="glute_bridge", nama="Glute Bridge", kat="kaki", otot="Bokong, paha belakang, punggung bawah",
    level=1, tipe="rep", n=15, met=3.5, ringan=True, sorot=["bokong"],
    kf=[
        dict(_LUTUT_TEKUK, a1=-4, b1=-2, a2=-2, b2=0, h1=(-1.8, 0.5), h2=(-3.8, 0.5), sh1=1, sh2=1, dur=1.0, tahan=0.1),
        dict(_LUTUT_TEKUK, x=-30 + 30 * math.cos(math.radians(30)), y=3 + 30 * math.sin(math.radians(30)), t=210, k=-50,
             dur=0.9, tahan=0.8),
    ],
    tanam=["kaki1", "kaki2", "tangan1", "tangan2"], poros=(-30.0, 3.0),
    langkah=[
        "Berbaring telentang, lutut ditekuk, telapak kaki rata di lantai selebar pinggul.",
        "Kencangkan bokong lalu angkat pinggul sampai badan lurus dari bahu ke lutut.",
        "Tahan 2 detik di atas, lalu turunkan perlahan.",
    ],
    tips=["Dorong dengan tumit.", "Bagus untuk yang banyak duduk dan sering pegal punggung bawah."],
    salah=["Mengangkat pinggul terlalu tinggi sampai punggung melengkung."],
    mudah="Angkat setengah saja.",
)

gerakan(
    id="wall_sit", nama="Wall Sit", kat="kaki", otot="Paha depan, bokong",
    level=2, tipe="detik", n=30, met=3.5, ringan=True, sorot=["kaki"],
    kf=[
        dict(x=0, y=21, t=90, k=0, a1=-60, b1=-8, a2=-62, b2=-10, p1=0, s1=-90, p2=-2, s2=-92, dur=1.5),
        dict(x=0, y=21.4, t=90, k=2, a1=-60, b1=-8, a2=-62, b2=-10, p1=0, s1=-90, p2=-2, s2=-92, dur=1.5),
    ],
    props=[[-3.6, 0.0, -3.6, 68.0]],
    langkah=[
        "Sandarkan punggung ke dinding, kaki melangkah sekitar dua telapak ke depan.",
        "Geser badan turun sampai paha sejajar lantai seperti duduk di kursi tak terlihat.",
        "Lutut tepat di atas pergelangan kaki. Tahan selama waktu yang ditentukan.",
    ],
    tips=["Tidak ada lompatan — aman untuk lutut bila sudutnya benar."],
    salah=["Lutut maju melewati ujung jari kaki."],
    mudah="Jangan turun terlalu dalam (sudut lutut lebih besar).",
)

gerakan(
    id="jinjit", nama="Jinjit (Calf Raise)", kat="kaki", otot="Betis",
    level=1, tipe="rep", n=20, met=3.0, ringan=True, sorot=["kaki"],
    kf=[
        dict(a1=-125, b1=-20, a2=-122, b2=-18, p2=-90, s2=-90, dur=0.8, tahan=0.1),
        dict(x=2.6, y=45.9, a1=-125, b1=-20, a2=-122, b2=-18, p2=-90, s2=-90, fo1=35, fo2=35, dur=0.7, tahan=0.5),
    ],
    langkah=[
        "Berdiri tegak, kaki selebar pinggul. Boleh berpegangan ke dinding atau sandaran kursi.",
        "Angkat tumit setinggi mungkin sampai bertumpu pada ujung telapak kaki.",
        "Tahan sebentar, lalu turunkan tumit perlahan.",
    ],
    tips=["Bisa dilakukan sambil menunggu atau di sela kerja."],
    salah=["Turun terlalu cepat."],
    mudah="Pegangan lebih kuat dan angkat lebih rendah.",
)

gerakan(
    id="side_lunge", nama="Side Lunge", kat="kaki", otot="Paha dalam & luar, bokong",
    level=2, tipe="rep", n=10, sisi=True, met=5.0, tampak="depan", sorot=["kaki"],
    kf=[
        dict(x=0, y=38, a1=-120, b1=150, a2=-60, b2=30, sh1=1, f1=(22, 0), f2=(-22, 0), dur=0.9, tahan=0.1),
        dict(x=11, y=23, a1=-120, b1=150, a2=-60, b2=30, f1=(22, 0), f2=(-22, 0), dur=1.0, tahan=0.2),
        dict(x=0, y=38, a1=-120, b1=150, a2=-60, b2=30, f1=(22, 0), f2=(-22, 0), dur=0.9, tahan=0.1),
        dict(x=-11, y=23, a1=-120, b1=150, a2=-60, b2=30, f1=(22, 0), f2=(-22, 0), sk2=-1, dur=1.0, tahan=0.2),
    ],
    tanam=["kaki1", "kaki2"],
    langkah=[
        "Berdiri dengan kaki dibuka lebar, ujung jari kaki menghadap depan, tangan di depan dada.",
        "Geser berat badan ke kanan sambil menekuk lutut kanan dan mendorong pinggul ke belakang; kaki kiri tetap lurus.",
        "Dorong kembali ke tengah, lalu ulangi ke kiri.",
    ],
    tips=["Telapak kaki tetap rata di lantai.", "Dada tetap tegak menghadap depan."],
    salah=["Lutut yang ditekuk masuk ke dalam."],
    mudah="Geser lebih sedikit, jangan turun terlalu dalam.",
)

_MRK = dict(_MERANGKAK)
gerakan(
    id="donkey_kick", nama="Donkey Kick", kat="kaki", otot="Bokong",
    level=1, tipe="rep", n=12, sisi=True, met=3.5, ringan=True, sorot=["bokong"],
    kf=[
        dict(_MRK, dur=0.8),
        dict(_MRK, p1=168, s1=92, fo1=90, dur=0.8, tahan=0.4),
    ],
    langkah=[
        "Posisi merangkak, tangan di bawah bahu, lutut di bawah pinggul.",
        "Dengan lutut tetap ditekuk 90°, angkat satu kaki ke belakang-atas sampai telapak kaki menghadap langit-langit.",
        "Kencangkan bokong di atas, lalu turunkan tanpa menyentuh lantai. Selesaikan satu sisi lalu ganti.",
    ],
    tips=["Perut tetap kencang supaya punggung tidak melengkung."],
    salah=["Mengayun kaki dengan cepat memakai momentum."],
    mudah="Angkat lebih rendah.",
)

gerakan(
    id="squat_jump", nama="Squat Lompat", kat="kaki", otot="Kaki, bokong; kardio intens",
    level=3, tipe="rep", n=10, met=8.0, sorot=["kaki", "bokong"],
    kf=[
        dict(x=-12, y=24, t=56, k=-20, a1=-150, b1=-140, a2=-152, b2=-142, f1=(0, 0), f2=(-1.5, 0), datar=True,
             dur=0.5, tahan=0.15),
        dict(x=0, y=49, t=90, k=0, a1=70, b1=80, a2=74, b2=84, p1=-90, s1=-92, p2=-92, s2=-94, fo1=40, fo2=40, dur=0.4, tahan=0.05),
        dict(x=-4, y=40, t=80, k=-5, a1=-100, b1=-80, a2=-102, b2=-82, f1=(0, 0), f2=(-1.5, 0), datar=True, dur=0.3),
    ],
    langkah=[
        "Mulai dari posisi squat, lengan di belakang.",
        "Lompat ke atas dengan kuat sambil mengayunkan lengan ke atas.",
        "Mendarat dengan lembut di ujung telapak lalu tumit, langsung turun ke squat berikutnya.",
    ],
    tips=["Mendarat pelan dengan lutut sedikit ditekuk untuk melindungi sendi."],
    salah=["Mendarat dengan kaki lurus dan berbunyi keras."],
    mudah="Squat biasa lalu jinjit di atas (tanpa lompat).",
)

# ───────────────────────────── KARDIO ─────────────────────────────

gerakan(
    id="jumping_jack", nama="Jumping Jack", kat="kardio", otot="Seluruh tubuh; jantung & paru",
    level=2, tipe="detik", n=30, met=8.0, tampak="depan", sorot=["kaki", "bahu"],
    kf=[
        dict(p1=-89, s1=-90, p2=-91, s2=-90, dur=0.28),
        dict(y=41, a1=-60, b1=-60, a2=-120, b2=-120, p1=-80, s1=-80, p2=-100, s2=-100, dur=0.14),
        dict(y=39.5, a1=70, b1=80, a2=110, b2=100, p1=-70, s1=-70, p2=-110, s2=-110, dur=0.14),
        dict(y=41, a1=-60, b1=-60, a2=-120, b2=-120, p1=-80, s1=-80, p2=-100, s2=-100, dur=0.14),
    ],
    langkah=[
        "Berdiri tegak, kaki rapat, tangan di samping badan.",
        "Lompat sambil membuka kaki selebar bahu lebih dan mengangkat kedua tangan ke atas kepala.",
        "Lompat lagi kembali ke posisi awal. Ulangi dengan ritme stabil.",
    ],
    tips=["Mendarat di ujung telapak kaki, lutut sedikit lentur."],
    salah=["Mendarat dengan tumit keras."],
    mudah="Step jack (tanpa lompat).",
)

gerakan(
    id="step_jack", nama="Step Jack (Tanpa Lompat)", kat="kardio", otot="Seluruh tubuh; aman untuk lutut",
    level=1, tipe="detik", n=30, met=4.5, ringan=True, tampak="depan", sorot=["kaki", "bahu"],
    kf=[
        dict(p1=-89, s1=-90, p2=-91, s2=-90, dur=0.4),
        dict(x=2, y=40, a1=60, b1=75, a2=120, b2=105, p1=-66, s1=-66, p2=-92, s2=-90, dur=0.45, tahan=0.1),
        dict(p1=-89, s1=-90, p2=-91, s2=-90, dur=0.4),
        dict(x=-2, y=40, a1=60, b1=75, a2=120, b2=105, p1=-88, s1=-90, p2=-114, s2=-114, dur=0.45, tahan=0.1),
    ],
    langkah=[
        "Berdiri tegak, tangan di samping.",
        "Langkahkan kaki kanan ke samping sambil mengangkat kedua tangan ke atas kepala.",
        "Kembali ke tengah, lalu langkahkan kaki kiri ke samping. Lakukan bergantian dengan cepat.",
    ],
    tips=["Pilihan kardio untuk pemula, berat badan berlebih, atau tinggal di lantai atas (tidak berisik)."],
    salah=["Lengan setengah-setengah — angkat penuh supaya jantung bekerja."],
    mudah="Angkat tangan setinggi bahu saja.",
)

gerakan(
    id="jalan_di_tempat", nama="Jalan di Tempat", kat="kardio", otot="Kaki, jantung; pemanasan",
    level=1, tipe="detik", n=45, met=3.5, ringan=True, sorot=["kaki"],
    kf=[
        dict(a1=-60, b1=10, a2=-125, b2=-60, p1=-35, s1=-105, p2=-90, s2=-90, fo1=95, dur=0.45),
        dict(a1=-92, b1=-60, a2=-90, b2=-58, p1=-90, s1=-90, p2=-90, s2=-90, dur=0.35),
        dict(a1=-125, b1=-60, a2=-58, b2=12, p1=-90, s1=-90, p2=-37, s2=-107, fo2=95, dur=0.45),
        dict(a1=-92, b1=-60, a2=-90, b2=-58, p1=-90, s1=-90, p2=-90, s2=-90, dur=0.35),
    ],
    langkah=[
        "Berdiri tegak dan mulai berjalan di tempat.",
        "Angkat lutut setinggi pinggang kalau sanggup, ayunkan lengan berlawanan dengan kaki.",
        "Atur napas teratur. Percepat untuk menaikkan detak jantung.",
    ],
    tips=["Pemanasan terbaik sebelum semua latihan.", "Bisa sambil menonton TV."],
    salah=["Badan membungkuk."],
    mudah="Angkat lutut lebih rendah.",
)

gerakan(
    id="high_knees", nama="Lari Angkat Lutut", kat="kardio", otot="Kaki, perut; kardio intens",
    level=2, tipe="detik", n=30, met=8.0, sorot=["kaki", "perut"],
    kf=[
        dict(y=42, a1=-35, b1=50, a2=-140, b2=-55, p1=2, s1=-92, p2=-92, s2=-92, fo1=110, fo2=60, dur=0.22),
        dict(y=42, a1=-140, b1=-55, a2=-37, b2=48, p1=-92, s1=-92, p2=0, s2=-94, fo1=60, fo2=110, dur=0.22),
    ],
    langkah=[
        "Berlari di tempat sambil mengangkat lutut setinggi pinggang.",
        "Ayunkan lengan dengan siku ditekuk 90°.",
        "Mendarat di ujung telapak kaki dengan ringan.",
    ],
    tips=["Badan tegak, perut kencang."],
    salah=["Badan condong ke belakang."],
    mudah="Jalan di tempat dengan lutut tinggi.",
)

gerakan(
    id="butt_kicks", nama="Lari Tendang Bokong", kat="kardio", otot="Paha belakang, kardio",
    level=2, tipe="detik", n=30, met=7.0, sorot=["kaki"],
    kf=[
        dict(y=41.5, a1=-40, b1=45, a2=-135, b2=-55, p1=-100, s1=105, p2=-90, s2=-90, fo1=40, dur=0.22),
        dict(y=41.5, a1=-135, b1=-55, a2=-42, b2=43, p1=-90, s1=-90, p2=-100, s2=105, fo2=40, dur=0.22),
    ],
    langkah=[
        "Berlari di tempat sambil menendang tumit ke arah bokong.",
        "Paha tetap mengarah ke bawah, yang bergerak hanya lutut ke bawah.",
        "Ayunkan lengan mengikuti ritme.",
    ],
    tips=["Pendaratan ringan di ujung telapak kaki."],
    salah=["Badan membungkuk ke depan."],
    mudah="Lakukan sambil berjalan di tempat.",
)


def _burpee():
    berdiri = dict(f1=(0, 0), f2=(-1.5, 0), datar=True, dur=0.45)
    jongkok = dict(x=-8, y=17, t=28, k=-25, h1=(20, 0.5), h2=(18, 0.5), f1=(0, 0), f2=(-1.5, 0), datar=True, dur=0.45)
    h, t, n = badan_lurus((-52.0, 6.0), 41, 29)
    hip = (20 - n[0] + h[0], h[1])
    plank = dict(x=hip[0], y=hip[1], t=t, k=-12, h1=(20, 0.5), h2=(18, 0.5), f1=(hip[0] - 41 * math.cos(math.radians(t)), 6.0),
                 f2=(hip[0] - 41 * math.cos(math.radians(t)) - 1, 6.5), fo1=70, fo2=70, dur=0.35, tahan=0.1)
    jongkok2 = dict(jongkok, dur=0.35)
    lompat = dict(x=0, y=49, t=90, k=5, a1=75, b1=85, a2=78, b2=88, p1=-90, s1=-92, p2=-92, s2=-94, fo1=40, fo2=40, dur=0.4)
    return [berdiri, jongkok, plank, jongkok2, lompat]


gerakan(
    id="burpee", nama="Burpee", kat="kardio", otot="Seluruh tubuh; kardio paling intens",
    level=3, tipe="rep", n=8, met=8.0, sorot=["dada", "kaki", "perut"],
    kf=_burpee(),
    langkah=[
        "Berdiri, lalu jongkok dan letakkan tangan di lantai di depan kaki.",
        "Lompatkan (atau langkahkan) kedua kaki ke belakang ke posisi plank.",
        "Lompatkan kaki kembali ke dekat tangan.",
        "Berdiri dan lompat ke atas dengan tangan di atas kepala.",
    ],
    tips=["Kualitas lebih penting daripada kecepatan.", "Boleh tanpa lompatan di akhir."],
    salah=["Pinggul jatuh saat posisi plank."],
    mudah="Langkahkan kaki satu per satu ke belakang dan ke depan, tanpa lompat.",
)

# ───────────────────────────── PEREGANGAN ─────────────────────────────

gerakan(
    id="regang_leher", nama="Peregangan Leher", kat="regang", otot="Leher, bahu atas",
    level=1, tipe="detik", n=30, met=2.3, ringan=True, tampak="depan", sorot=["bahu"],
    kf=[
        dict(dur=1.2, tahan=0.3),
        dict(k=-28, a1=60, b1=170, dur=1.4, tahan=1.6),
        dict(dur=1.2, tahan=0.3),
        dict(k=28, a2=120, b2=10, dur=1.4, tahan=1.6),
    ],
    langkah=[
        "Duduk atau berdiri tegak, bahu rileks turun.",
        "Miringkan kepala perlahan ke kanan (telinga ke arah bahu). Tangan kanan boleh menekan ringan.",
        "Tahan 10–15 detik sambil bernapas, lalu ganti ke kiri.",
    ],
    tips=["Sangat cocok di sela kerja di depan komputer."],
    salah=["Memutar kepala dengan cepat atau menyentak."],
    mudah="Miringkan sedikit saja tanpa bantuan tangan.",
)

gerakan(
    id="regang_bahu", nama="Peregangan Bahu Silang", kat="regang", otot="Bahu belakang, punggung atas",
    level=1, tipe="detik", n=20, sisi=True, met=2.3, ringan=True, tampak="depan", sorot=["bahu"],
    kf=[
        dict(dur=1.0, tahan=0.2),
        dict(a1=178, b1=180, h2=(-1.0, 70.5), sh2=1, dur=1.2, tahan=2.0),
    ],
    langkah=[
        "Berdiri atau duduk tegak.",
        "Bawa lengan kanan lurus menyilang di depan dada.",
        "Tahan dengan lengan kiri di atas siku, tarik pelan. Tahan, lalu ganti sisi.",
    ],
    tips=["Bahu tetap turun, jangan diangkat."],
    salah=["Memutar badan mengikuti tarikan."],
    mudah="Tarik lebih ringan.",
)

gerakan(
    id="regang_trisep", nama="Peregangan Trisep", kat="regang", otot="Lengan belakang, bahu",
    level=1, tipe="detik", n=20, sisi=True, met=2.3, ringan=True, tampak="depan", sorot=["lengan"],
    kf=[
        dict(dur=1.0, tahan=0.2),
        dict(k=-8, a1=98, b1=-100, h2=(4.0, 87.0), sh2=-1, dur=1.4, tahan=2.0),
    ],
    langkah=[
        "Angkat lengan kanan ke atas, lalu tekuk siku sehingga telapak tangan jatuh di belakang kepala.",
        "Pegang siku kanan dengan tangan kiri dan dorong pelan ke arah kepala.",
        "Tahan, lalu ganti sisi.",
    ],
    tips=["Kepala tetap tegak, jangan menunduk."],
    salah=["Punggung melengkung ke belakang."],
    mudah="Tanpa dorongan tangan lainnya.",
)

gerakan(
    id="cobra", nama="Peregangan Cobra", kat="regang", otot="Perut, punggung bawah, dada",
    level=1, tipe="detik", n=20, met=2.3, ringan=True, sorot=["perut", "punggung"],
    kf=[
        dict(_TENGKURAP, k=18, h1=(22, 1.5), h2=(20, 1.5), sh1=-1, sh2=-1, dur=1.2, tahan=0.3),
        dict(_TENGKURAP, t=34, k=10, dur=1.4, tahan=1.8),
    ],
    tanam=["tangan1", "tangan2"],
    langkah=[
        "Tengkurap, telapak tangan di samping dada, kaki lurus ke belakang.",
        "Dorong lantai perlahan dan angkat dada; pinggul tetap menempel di lantai.",
        "Siku boleh sedikit ditekuk. Tahan sambil bernapas, lalu turun perlahan.",
    ],
    tips=["Angkat hanya sampai terasa nyaman di punggung bawah."],
    salah=["Bahu naik ke telinga."],
    mudah="Bertumpu pada lengan bawah (sphinx).",
)

gerakan(
    id="child_pose", nama="Child's Pose", kat="regang", otot="Punggung, pinggul, bahu",
    level=1, tipe="detik", n=30, met=2.3, ringan=True, sorot=["punggung"],
    kf=[
        dict(x=0, y=22.5, t=90, k=0, a1=-92, b1=-88, a2=-90, b2=-86, p1=-90, s1=180, p2=-92, s2=178, fo1=15, fo2=15,
             dur=1.4, tahan=0.3),
        dict(x=-17.5, y=10, t=-6, k=-25, h1=(42, 2.5), h2=(40, 2.5), p1=-22, s1=180, p2=-24, s2=178, fo1=15, fo2=15,
             dur=1.8, tahan=2.0),
    ],
    langkah=[
        "Berlutut, lalu duduk di atas tumit.",
        "Bungkukkan badan ke depan sampai dahi menyentuh lantai, lengan lurus ke depan.",
        "Rilekskan bahu dan bernapas dalam ke arah punggung. Tahan.",
    ],
    tips=["Buka lutut sedikit lebih lebar kalau perut terasa terhimpit."],
    salah=["Menahan napas."],
    mudah="Letakkan bantal di bawah dahi atau di antara betis dan paha.",
)

gerakan(
    id="regang_bungkuk", nama="Bungkuk Sentuh Kaki", kat="regang", otot="Paha belakang, punggung bawah, betis",
    level=1, tipe="detik", n=20, met=2.3, ringan=True, sorot=["kaki", "punggung"],
    kf=[
        dict(f1=(0, 0), f2=(-1.5, 0), dur=1.2, tahan=0.3),
        dict(x=-9, y=39.5, t=-62, k=-15, h1=(7, 6), h2=(5, 6), f1=(0, 0), f2=(-1.5, 0), dur=1.6, tahan=2.0),
    ],
    tanam=["kaki1", "kaki2"],
    langkah=[
        "Berdiri, kaki selebar pinggul, lutut sedikit lentur.",
        "Bungkukkan badan dari pinggul, biarkan tangan turun ke arah tulang kering atau ujung kaki.",
        "Biarkan kepala menggantung rileks. Tahan, lalu naik perlahan sambil meluruskan punggung.",
    ],
    tips=["Tidak harus menyentuh lantai — cukup sampai terasa tertarik."],
    salah=["Memantul-mantulkan badan."],
    mudah="Tangan di lutut atau di kursi.",
)

gerakan(
    id="regang_paha", nama="Peregangan Paha Depan", kat="regang", otot="Paha depan, pinggul depan",
    level=1, tipe="detik", n=20, sisi=True, met=2.3, ringan=True, sorot=["kaki"],
    kf=[
        dict(dur=1.0, tahan=0.2),
        dict(a2=-10, b2=0, p1=-96, s1=98, fo1=60, h1=(-6.5, 31), sh1=1, dur=1.4, tahan=2.0),
    ],
    langkah=[
        "Berdiri dengan satu tangan berpegangan pada dinding atau kursi.",
        "Tekuk lutut kanan dan pegang pergelangan kaki kanan, tarik tumit ke arah bokong.",
        "Lutut menunjuk ke lantai, badan tegak. Tahan, lalu ganti sisi.",
    ],
    tips=["Kencangkan bokong supaya peregangan lebih terasa."],
    salah=["Badan membungkuk ke depan."],
    mudah="Gunakan handuk untuk membantu menarik kaki.",
)

gerakan(
    id="regang_samping", nama="Peregangan Samping", kat="regang", otot="Pinggang, perut samping, punggung",
    level=1, tipe="detik", n=20, sisi=True, met=2.3, ringan=True, tampak="depan", sorot=["perut"],
    kf=[
        dict(dur=1.0, tahan=0.2),
        dict(t=104, k=8, a1=118, b1=152, a2=-112, b2=-104, dur=1.4, tahan=2.0),
        dict(dur=1.0, tahan=0.2),
        dict(t=76, k=-8, a1=-68, b1=-76, a2=62, b2=28, dur=1.4, tahan=2.0),
    ],
    langkah=[
        "Berdiri tegak, kaki selebar bahu.",
        "Angkat tangan kanan ke atas kepala lalu condongkan badan ke kiri.",
        "Rasakan tarikan di pinggang kanan. Tahan, lalu ganti sisi.",
    ],
    tips=["Bernapas panjang dan rileks."],
    salah=["Badan condong ke depan; tetap lurus ke samping."],
    mudah="Tangan di pinggang, condong sedikit saja.",
)

gerakan(
    id="peluk_lutut", nama="Peluk Lutut", kat="regang", otot="Punggung bawah, bokong, pinggul",
    level=1, tipe="detik", n=20, sisi=True, met=2.3, ringan=True, sorot=["punggung", "bokong"],
    kf=[
        dict(_TERLENTANG, a1=-4, b1=-2, a2=-2, b2=0, p1=6, s1=6, p2=4, s2=4, dur=1.2, tahan=0.2),
        dict(_TERLENTANG, p1=122, s1=-42, p2=4, s2=4, h1=(-8, 19), h2=(-9, 18), sh1=1, sh2=1, dur=1.4, tahan=2.0),
    ],
    langkah=[
        "Berbaring telentang, kaki lurus.",
        "Tarik lutut kanan ke arah dada dan peluk dengan kedua tangan.",
        "Kaki kiri tetap lurus di lantai. Tahan, lalu ganti sisi. Boleh juga kedua lutut sekaligus.",
    ],
    tips=["Bagus sebelum tidur atau setelah duduk lama."],
    salah=["Mengangkat kepala sampai leher tegang."],
    mudah="Pegang paha belakang, bukan lutut.",
)

gerakan(
    id="regang_dada", nama="Peregangan Dada", kat="regang", otot="Dada, bahu depan",
    level=1, tipe="detik", n=20, met=2.3, ringan=True, sorot=["dada"],
    kf=[
        dict(dur=1.0, tahan=0.2),
        dict(t=94, k=8, a1=-128, b1=-150, a2=-126, b2=-148, dur=1.4, tahan=2.0),
    ],
    langkah=[
        "Berdiri tegak, kaitkan kedua tangan di belakang punggung.",
        "Luruskan lengan dan angkat sedikit menjauh dari badan sambil membusungkan dada.",
        "Tarik bahu ke belakang-bawah. Tahan sambil bernapas.",
    ],
    tips=["Kebalikan dari posisi membungkuk di depan layar — lakukan sering."],
    salah=["Mendongak terlalu jauh."],
    mudah="Pegang handuk di belakang punggung kalau tangan tidak sampai.",
)
