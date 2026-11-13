# BugarKu

Aplikasi Android latihan di rumah **tanpa alat**, by Pahlevi Lubis.

- **47 gerakan** dengan karakter yang bergerak (animasi): Dada & Bahu, Lengan, Punggung, Perut, Kaki & Bokong, Kardio, Peregangan.
- **14 sesi latihan siap pakai** dengan level Pemula / Menengah / Lanjut.
- **Program 28 hari**: *Kecilkan Perut Buncit*, *Mulai dari Nol*, *Badan Atas & Lengan*.
- **Mode latihan** dengan hitung mundur, istirahat otomatis, suara pemandu bahasa Indonesia, bip, dan getar.
- **Pengingat**: jadwal olahraga (hari & jam), gerak/peregangan saat kerja, dan minum air.
- **Panduan**: cara mengecilkan perut buncit, mitos, Isi Piringku & batas gula-garam-lemak, anjuran WHO, keamanan.
- **Progres**: riwayat latihan, target 150 menit/minggu, catatan berat & lingkar perut dengan grafik, IMT.
- Semua data tersimpan di HP saja. Tidak perlu internet.

## Pasang APK

Buka tab **Releases** di repo ini → build terbaru → unduh `BugarKu.apk` dari HP → pasang
(izinkan "pasang dari sumber tidak dikenal" bila diminta). Update cukup dipasang menimpa versi lama.

## Untuk pengembang

- Kode app: Kotlin + Jetpack Compose (`app/`). APK dibangun otomatis oleh GitHub Actions setiap push ke `main`.
- Data gerakan, animasi, sesi, program, dan artikel dibuat dari `tools/`:
  ```
  python3 tools/buat_data.py        # tulis app/src/main/assets/bugarku.json
  python3 tools/cek_data.py         # cek data
  python3 tools/pratinjau.py perut  # gambar pratinjau pose (butuh Pillow)
  python3 tools/karakter.py push_up,squat  # pratinjau gaya karakter
  ```
- Pose ditulis sebagai keyframe sudut sendi di `tools/gerakan.py`, lalu diubah jadi frame animasi oleh `tools/pose.py`.
