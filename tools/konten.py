"""Sesi latihan, program bertahap, artikel panduan, dan tips harian BugarKu."""

# n = jumlah repetisi atau detik untuk level Menengah (Pemula ×0,7; Lanjut ×1,3 — diatur di app)
SESI = [
    dict(id="pemula_7", judul="Full Body Pemula", ringkas="Pengenalan seluruh tubuh, tanpa lompatan. Cocok untuk hari pertama.",
         kat="kardio", level=1, putaran=1,
         item=[("jalan_di_tempat", 40), ("squat", 10), ("push_up_dinding", 10), ("glute_bridge", 12),
               ("bird_dog", 6), ("plank", 20), ("step_jack", 30)]),
    dict(id="full_body", judul="Full Body Menengah", ringkas="Latihan seluruh tubuh untuk membakar kalori dan menguatkan otot.",
         kat="kardio", level=2, putaran=2,
         item=[("jumping_jack", 30), ("squat", 15), ("push_up", 10), ("lunge", 8), ("superman", 12),
               ("plank", 40), ("mountain_climber", 30)]),
    dict(id="perut_pemula", judul="Perut Pemula", ringkas="Menguatkan otot perut dengan gerakan yang aman untuk punggung.",
         kat="perut", level=1, putaran=2,
         item=[("crunch", 12), ("dead_bug", 8), ("glute_bridge", 12), ("plank", 20), ("side_crunch_berdiri", 10),
               ("flutter_kick", 20)]),
    dict(id="perut_menengah", judul="Perut Kencang", ringkas="Perut depan, samping, dan bawah — ditambah kardio ringan.",
         kat="perut", level=2, putaran=2,
         item=[("crunch", 15), ("bicycle_crunch", 20), ("leg_raise", 12), ("mountain_climber", 30),
               ("plank", 40), ("plank_samping", 20), ("flutter_kick", 30)]),
    dict(id="badan_atas", judul="Badan Atas Kuat", ringkas="Dada, bahu, punggung, dan lengan dalam satu sesi.",
         kat="atas", level=2, putaran=2,
         item=[("putar_lengan", 30), ("push_up_lutut", 12), ("incline_push_up", 12), ("dips_kursi", 10),
               ("superman", 12), ("plank_naik_turun", 8), ("pike_push_up", 6)]),
    dict(id="dada_bahu_pemula", judul="Dada & Bahu Pemula", ringkas="Mulai dari dinding dan kursi sebelum ke lantai.",
         kat="atas", level=1, putaran=2,
         item=[("putar_lengan", 30), ("push_up_dinding", 12), ("incline_push_up", 10), ("tinju", 30),
               ("push_up_lutut", 8)]),
    dict(id="lengan", judul="Lengan Kencang", ringkas="Bisep, trisep, dan bahu supaya lengan lebih kencang.",
         kat="lengan", level=2, putaran=2,
         item=[("putar_lengan", 30), ("curl_botol", 15), ("dips_kursi", 10), ("tinju", 30),
               ("push_up_diamond", 6), ("plank_naik_turun", 8)]),
    dict(id="kaki_bokong", judul="Kaki & Bokong", ringkas="Paha, bokong, dan betis — otot besar yang paling banyak membakar energi.",
         kat="kaki", level=2, putaran=2,
         item=[("squat", 15), ("lunge", 10), ("glute_bridge", 15), ("side_lunge", 8), ("donkey_kick", 12),
               ("wall_sit", 30), ("jinjit", 20)]),
    dict(id="kardio_ringan", judul="Kardio Tanpa Lompat", ringkas="Bakar lemak tanpa lompatan — ramah lutut dan tidak berisik.",
         kat="kardio", level=1, putaran=2,
         item=[("jalan_di_tempat", 60), ("step_jack", 40), ("tinju", 40), ("side_crunch_berdiri", 12),
               ("squat", 12), ("jalan_di_tempat", 45)]),
    dict(id="hiit", judul="HIIT Bakar Lemak", ringkas="Interval intens untuk yang sudah terbiasa. Detak jantung naik tinggi.",
         kat="kardio", level=3, putaran=3,
         item=[("jumping_jack", 40), ("high_knees", 30), ("squat_jump", 10), ("mountain_climber", 30),
               ("burpee", 8), ("butt_kicks", 30)]),
    dict(id="punggung", judul="Punggung Sehat", ringkas="Untuk yang sering duduk lama dan punggung terasa pegal.",
         kat="punggung", level=1, putaran=1,
         item=[("bird_dog", 8), ("superman", 10), ("glute_bridge", 12), ("dead_bug", 8), ("good_morning", 10),
               ("cobra", 20), ("child_pose", 30)]),
    dict(id="regang_pagi", judul="Peregangan Pagi", ringkas="5 menit untuk membuka badan setelah bangun tidur.",
         kat="regang", level=1, putaran=1, istirahat=5,
         item=[("regang_leher", 30), ("regang_bahu", 20), ("regang_samping", 20), ("regang_bungkuk", 20),
               ("regang_paha", 20), ("regang_dada", 20)]),
    dict(id="regang_kantor", judul="Peregangan Kantor", ringkas="Semua sambil berdiri di dekat meja — tidak perlu ganti baju.",
         kat="regang", level=1, putaran=1, istirahat=5,
         item=[("regang_leher", 30), ("regang_bahu", 20), ("regang_trisep", 20), ("regang_dada", 20),
               ("regang_samping", 20), ("jinjit", 15)]),
    dict(id="regang_malam", judul="Peregangan Sebelum Tidur", ringkas="Rileks dan pelan supaya tidur lebih nyenyak.",
         kat="regang", level=1, putaran=1, istirahat=5,
         item=[("regang_leher", 30), ("child_pose", 30), ("cobra", 20), ("peluk_lutut", 20)]),
]

# Program bertahap: tiap hari berisi sesi + pengali (kali) atau istirahat.
ISTIRAHAT_AKTIF = "Istirahat aktif: jalan kaki santai 20–30 menit dan tidur cukup."
ISTIRAHAT = "Hari istirahat. Otot tumbuh saat beristirahat."


def minggu(pola, kali):
    hari = []
    for p in pola:
        if p is None:
            hari.append(dict(istirahat=ISTIRAHAT, sesi="regang_malam"))
        elif p == "jalan":
            hari.append(dict(istirahat=ISTIRAHAT_AKTIF))
        else:
            hari.append(dict(sesi=p, kali=kali))
    return hari


PROGRAM = [
    dict(id="perut_28", judul="Kecilkan Perut Buncit", ringkas="28 hari: kardio pembakar lemak + perut + seluruh tubuh, bertahap makin berat.",
         kat="perut",
         catatan="Lemak perut berkurang kalau lemak tubuh total turun. Jadi program ini menggabungkan kardio, latihan "
                 "otot besar, dan latihan perut. Hasil paling terasa bila dibarengi pola makan (lihat Panduan).",
         hari=minggu(["pemula_7", "perut_pemula", "jalan", "kardio_ringan", "perut_pemula", "kaki_bokong", None], 0.8)
         + minggu(["pemula_7", "perut_pemula", "jalan", "kardio_ringan", "perut_menengah", "kaki_bokong", None], 1.0)
         + minggu(["full_body", "perut_menengah", "jalan", "kardio_ringan", "perut_menengah", "kaki_bokong", None], 1.0)
         + minggu(["full_body", "perut_menengah", "jalan", "hiit", "perut_menengah", "kaki_bokong", None], 1.15)),
    dict(id="nol_28", judul="Mulai dari Nol", ringkas="28 hari untuk yang lama tidak olahraga. Ringan, tanpa lompatan di minggu awal.",
         kat="kardio",
         catatan="Tujuan utama: membangun kebiasaan. Kalau terasa terlalu berat, ulangi minggu yang sama sebelum lanjut.",
         hari=minggu(["pemula_7", "jalan", "regang_pagi", "pemula_7", "jalan", "punggung", None], 0.7)
         + minggu(["pemula_7", "jalan", "kardio_ringan", "pemula_7", "jalan", "punggung", None], 0.8)
         + minggu(["pemula_7", "dada_bahu_pemula", "kardio_ringan", "perut_pemula", "jalan", "kaki_bokong", None], 0.9)
         + minggu(["pemula_7", "dada_bahu_pemula", "kardio_ringan", "perut_pemula", "jalan", "kaki_bokong", None], 1.0)),
    dict(id="atas_28", judul="Badan Atas & Lengan", ringkas="28 hari untuk dada, bahu, punggung, dan lengan — tetap seimbang dengan kaki & perut.",
         kat="atas",
         catatan="Mulai dari push up dinding/lutut dan naik bertahap. Istirahat 1 hari di antara latihan badan atas yang berat.",
         hari=minggu(["dada_bahu_pemula", "kaki_bokong", "lengan", "jalan", "dada_bahu_pemula", "perut_pemula", None], 0.8)
         + minggu(["badan_atas", "kaki_bokong", "lengan", "jalan", "badan_atas", "perut_pemula", None], 0.9)
         + minggu(["badan_atas", "kaki_bokong", "lengan", "jalan", "badan_atas", "perut_menengah", None], 1.0)
         + minggu(["badan_atas", "kaki_bokong", "lengan", "kardio_ringan", "badan_atas", "perut_menengah", None], 1.15)),
]

# Artikel: daftar bagian. ("h", judul), ("p", paragraf), ("d", [poin...]), ("c", catatan berwarna)
ARTIKEL = [
    dict(id="perut_buncit", judul="Cara Mengecilkan Perut Buncit", ringkas="Langkah yang benar-benar bekerja, tanpa mitos.",
         ikon="perut", isi=[
             ("c", "Kunci utamanya: lemak perut ikut turun ketika lemak tubuh secara keseluruhan turun. Tidak ada gerakan "
                   "yang bisa membakar lemak hanya di perut saja."),
             ("h", "1. Mulai dari yang diminum"),
             ("p", "Minuman manis adalah sumber gula tersembunyi terbesar: es teh manis, kopi susu kekinian, minuman boba, "
                   "sirup, dan jus kemasan. Kementerian Kesehatan menganjurkan gula tidak lebih dari 50 gram (sekitar 4 "
                   "sendok makan) per orang per hari — termasuk gula di dalam minuman."),
             ("d", ["Ganti dengan air putih, teh atau kopi tanpa gula.", "Kalau belum bisa, kurangi bertahap: \"gula setengah\", lalu \"tanpa gula\".",
                    "Minum air putih sebelum makan supaya tidak kalap."]),
             ("h", "2. Atur isi piring, bukan menyiksa diri"),
             ("p", "Ikuti panduan Isi Piringku: setengah piring sayur dan buah, setengah lagi makanan pokok dan lauk-pauk. "
                   "Tidak perlu diet ekstrem — cukup makan sedikit lebih sedikit dari kebutuhan, secara konsisten."),
             ("d", ["Sayur lebih banyak dari buah, nasi lebih banyak dari lauk — tetapi nasi cukup secentong-dua centong, bukan menggunung.",
                    "Pilih lauk berprotein: telur, tempe, tahu, ikan, ayam tanpa kulit. Protein membuat kenyang lebih lama.",
                    "Batasi gorengan, santan kental, dan camilan manis. Batas lemak harian sekitar 5 sendok makan minyak.",
                    "Makan pelan-pelan, berhenti sebelum terlalu kenyang."]),
             ("h", "3. Gerak: kardio + latihan otot"),
             ("p", "WHO menganjurkan orang dewasa melakukan aktivitas sedang 150–300 menit per minggu (atau 75–150 menit "
                   "aktivitas berat), ditambah latihan penguatan otot minimal 2 hari per minggu. Program \"Kecilkan Perut "
                   "Buncit\" di BugarKu sudah menggabungkan keduanya."),
             ("d", ["Kardio (jalan cepat, kardio tanpa lompat, HIIT) membakar energi.",
                    "Latihan otot besar (squat, lunge, push up) menambah otot sehingga tubuh lebih banyak memakai energi.",
                    "Latihan perut (plank, crunch, dead bug) menguatkan dan merapikan otot perut di balik lemaknya."]),
             ("h", "4. Kurangi duduk lama"),
             ("p", "Duduk berjam-jam memperlambat pembakaran energi. Berdiri dan bergerak 2–3 menit setiap 30–60 menit. "
                   "Nyalakan \"Pengingat gerak saat kerja\" di menu Atur."),
             ("h", "5. Tidur dan stres"),
             ("p", "Kurang tidur dan stres berkepanjangan membuat lapar dan keinginan makan manis meningkat. Usahakan tidur "
                   "7–9 jam dan jadwal tidur yang teratur. Berhenti merokok juga membantu kesehatan perut dan jantung."),
             ("h", "6. Ukur dengan meteran, bukan hanya timbangan"),
             ("p", "Lingkar perut lebih tepat menggambarkan lemak perut. Menurut Kemenkes, obesitas sentral bila lingkar "
                   "perut ≥ 90 cm pada pria dan ≥ 80 cm pada wanita. Catat setiap 1–2 minggu di menu Progres."),
             ("c", "Sabar dan konsisten. Turun sekitar 0,5–1 kg per minggu sudah sangat baik dan lebih mudah dipertahankan."),
         ]),
    dict(id="mitos", judul="Mitos Seputar Perut Buncit", ringkas="Sit-up ratusan kali? Korset? Ini faktanya.", ikon="lampu", isi=[
        ("h", "\"Sit-up 100 kali sehari pasti mengecilkan perut\""),
        ("p", "Sit-up dan crunch menguatkan otot perut, tetapi tidak membakar lemak di atasnya secara khusus. Lemak "
              "berkurang dari seluruh tubuh. Gabungkan latihan perut dengan kardio, latihan otot besar, dan pola makan."),
        ("h", "\"Korset atau sabuk panas melelehkan lemak\""),
        ("p", "Yang keluar hanya keringat (air), dan akan kembali setelah minum. Lemak tidak berkurang."),
        ("h", "\"Tidak makan malam = perut langsing\""),
        ("p", "Yang menentukan adalah jumlah total makanan dalam sehari, bukan jamnya. Makan malam porsi wajar tetap boleh; "
              "yang perlu dihindari adalah ngemil berlebihan larut malam."),
        ("h", "\"Harus olahraga berat setiap hari\""),
        ("p", "Konsisten jauh lebih penting daripada berat. Latihan 20–30 menit, 4–5 kali seminggu yang dijalani terus "
              "lebih berhasil daripada latihan berat seminggu lalu berhenti."),
        ("h", "\"Keringat banyak = lemak banyak terbakar\""),
        ("p", "Keringat adalah cara tubuh mendinginkan diri. Banyaknya keringat dipengaruhi suhu ruangan dan bawaan "
              "tubuh, bukan jumlah lemak yang terbakar."),
    ]),
    dict(id="makan", judul="Pola Makan: Isi Piringku & Batas GGL", ringkas="Panduan Kemenkes yang mudah diterapkan.", ikon="piring", isi=[
        ("h", "Isi Piringku"),
        ("d", ["Setengah piring: sayur dan buah (sayur lebih banyak dari buah).",
               "Setengah piring lainnya: makanan pokok dan lauk-pauk (makanan pokok lebih banyak dari lauk).",
               "Lauk-pauk bisa hewani (ikan, telur, ayam, daging) dan nabati (tahu, tempe, kacang-kacangan)."]),
        ("h", "Batas gula, garam, lemak (GGL) per orang per hari"),
        ("d", ["Gula: 50 gram ≈ 4 sendok makan.", "Garam: 5 gram ≈ 1 sendok teh.", "Lemak/minyak: 67 gram ≈ 5 sendok makan."]),
        ("p", "Ingat, batas ini sudah termasuk gula, garam, dan minyak di dalam makanan jadi, gorengan, kecap, saus, dan minuman."),
        ("h", "Tips sehari-hari"),
        ("d", ["Biasakan sarapan supaya tidak kalap saat makan siang.",
               "Pilih direbus, dikukus, dipepes, atau dipanggang daripada digoreng.",
               "Baca label kemasan: lihat kandungan gula per sajian.",
               "Air putih yang cukup sepanjang hari — nyalakan pengingat minum di menu Atur."]),
    ]),
    dict(id="berapa_banyak", judul="Berapa Banyak Olahraga yang Dibutuhkan?", ringkas="Anjuran WHO untuk orang dewasa.", ikon="jam", isi=[
        ("d", ["Aktivitas aerobik intensitas sedang 150–300 menit per minggu, atau intensitas berat 75–150 menit, atau campuran keduanya.",
               "Latihan penguatan otot yang melibatkan otot-otot besar, minimal 2 hari per minggu.",
               "Kurangi waktu duduk; aktivitas apa pun, sekecil apa pun, lebih baik daripada tidak sama sekali."]),
        ("h", "Cara mengenali intensitas (tes bicara)"),
        ("d", ["Sedang: masih bisa berbicara, tetapi tidak bisa bernyanyi.",
               "Berat: hanya bisa mengucapkan beberapa kata sebelum menarik napas."]),
        ("p", "Kalau belum terbiasa, mulai dari sedikit — misalnya 10 menit sehari — lalu tambah bertahap. Grafik mingguan "
              "di Beranda membantu memantau target 150 menit."),
    ]),
    dict(id="aman", judul="Aman Berolahraga", ringkas="Pemanasan, pendinginan, dan kapan harus berhenti.", ikon="aman", isi=[
        ("c", "Segera berhenti dan cari pertolongan medis bila muncul nyeri atau rasa tertekan di dada, sesak napas yang "
              "tidak wajar, pusing hebat atau hampir pingsan, atau jantung berdebar tidak teratur."),
        ("h", "Tanyakan dokter dulu bila"),
        ("d", ["Punya penyakit jantung, tekanan darah tinggi yang belum terkontrol, atau diabetes.",
               "Sedang hamil, baru melahirkan, atau baru operasi.",
               "Punya cedera sendi, lutut, atau tulang belakang.",
               "Lama sekali tidak berolahraga dan ragu dengan kondisi tubuh."]),
        ("h", "Pemanasan 3–5 menit"),
        ("p", "Jalan di tempat, putar lengan, dan gerakan ringan membuat otot dan sendi siap. Sesi BugarKu biasanya dimulai "
              "dengan gerakan ringan."),
        ("h", "Pendinginan & peregangan"),
        ("p", "Setelah latihan, turunkan intensitas lalu lakukan peregangan 3–5 menit. Tahan setiap peregangan tanpa "
              "memantul-mantul."),
        ("h", "Nyeri otot vs cedera"),
        ("d", ["Pegal 1–2 hari setelah latihan baru itu wajar dan akan berkurang.",
               "Nyeri tajam, bengkak, atau nyeri di sendi saat bergerak bukan hal wajar — istirahatkan dan periksakan bila tidak membaik."]),
    ]),
    dict(id="kantor", judul="Untuk Pekerja Kantoran", ringkas="Lawan efek duduk seharian di depan komputer.", ikon="kantor", isi=[
        ("d", ["Berdiri dan bergerak 2–3 menit setiap 30–60 menit duduk. Aktifkan \"Pengingat gerak saat kerja\".",
               "Coba sesi \"Peregangan Kantor\" — semuanya bisa sambil berdiri di dekat meja.",
               "Atur layar setinggi mata, punggung bersandar, kaki menapak lantai.",
               "Jalan kaki saat menelepon, naik tangga daripada lift bila memungkinkan.",
               "Bawa botol air di meja supaya lebih sering minum (dan lebih sering berdiri ke toilet)."]),
        ("p", "Gerakan \"Bird Dog\", \"Glute Bridge\", dan \"Peregangan Dada\" sangat membantu punggung yang pegal dan bahu "
              "yang membungkuk karena lama di depan layar."),
    ]),
    dict(id="ukur", judul="Cara Mengukur Lingkar Perut", ringkas="Supaya catatan progres akurat.", ikon="meteran", isi=[
        ("d", ["Berdiri tegak, perut rileks, ukur pagi hari sebelum makan.",
               "Lingkarkan meteran jahit di pertengahan antara tulang rusuk terbawah dan tulang panggul atas — biasanya sedikit di atas pusar.",
               "Meteran sejajar lantai, menempel tetapi tidak menekan kulit.",
               "Baca angka di akhir embusan napas normal.",
               "Ukur di tempat dan cara yang sama setiap kali."]),
        ("h", "Batas menurut Kemenkes"),
        ("d", ["Pria: obesitas sentral bila ≥ 90 cm.", "Wanita: obesitas sentral bila ≥ 80 cm."]),
        ("h", "Indeks Massa Tubuh (IMT)"),
        ("p", "IMT = berat (kg) ÷ (tinggi (m) × tinggi (m)). Klasifikasi Kemenkes: kurus < 18,5; normal 18,5–25,0; gemuk "
              "25,1–27,0; obesitas > 27,0. IMT tidak membedakan otot dan lemak, jadi pakai bersama lingkar perut."),
    ]),
    dict(id="jalan_kaki", judul="Jalan Kaki: Olahraga Paling Murah", ringkas="Mudah, aman, dan ampuh membakar lemak.", ikon="jalan", isi=[
        ("d", ["Jalan cepat sampai napas sedikit terengah tetapi masih bisa bicara = intensitas sedang.",
               "30 menit × 5 hari = 150 menit, sudah memenuhi anjuran minimal WHO.",
               "Pecah jadi 3 × 10 menit kalau sibuk: setelah sarapan, makan siang, dan makan malam.",
               "Pakai sepatu yang nyaman, ayunkan lengan, badan tegak.",
               "Ajak keluarga atau teman supaya lebih konsisten."]),
        ("p", "Hari \"istirahat aktif\" di program BugarKu diisi dengan jalan kaki santai."),
    ]),
]

TIPS = [
    "Segelas es teh manis bisa mengandung beberapa sendok gula. Coba minta \"gula setengah\" dulu.",
    "Lemak perut turun bersama lemak tubuh lainnya — kombinasikan kardio, latihan otot, dan pola makan.",
    "Berdiri dan bergerak 2–3 menit setiap 30–60 menit duduk.",
    "Setengah piring isi dengan sayur dan buah (Isi Piringku).",
    "Tidur 7–9 jam membantu mengendalikan rasa lapar.",
    "Lebih baik 10 menit setiap hari daripada 1 jam seminggu sekali.",
    "Ukur lingkar perut setiap 1–2 minggu — lebih jujur daripada timbangan.",
    "Pilih makanan yang direbus, dikukus, atau dipanggang daripada digoreng.",
    "Minum air putih sebelum makan supaya tidak makan berlebihan.",
    "Protein (telur, tempe, tahu, ikan) membuat kenyang lebih lama.",
    "Pemanasan 3–5 menit sebelum latihan mengurangi risiko cedera.",
    "Bentuk gerakan yang benar lebih penting daripada jumlah repetisi.",
    "Jalan cepat 30 menit, 5 hari seminggu = 150 menit anjuran WHO.",
    "Latih otot besar (kaki & bokong) — makin banyak otot, makin banyak energi terpakai.",
    "Pegal setelah latihan baru itu wajar. Nyeri tajam di sendi bukan — istirahatkan.",
    "Batas garam sehari hanya sekitar 1 sendok teh, termasuk dari kecap dan saus.",
    "Naik tangga daripada lift adalah latihan kaki gratis.",
    "Kalau melewatkan satu hari, tidak apa-apa. Yang penting jangan melewatkan dua hari berturut-turut.",
    "Makan pelan-pelan; otak butuh waktu sekitar 20 menit untuk merasa kenyang.",
    "Catat progresmu. Melihat grafik turun itu memotivasi!",
]
