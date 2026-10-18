# Aturan kerja fitur Animal Inspection Hologram
1. Satu sesi = satu fase. Jangan kerjakan fase lain walau terlihat mudah.
2. Awal sesi: baca CONTEXT.md, RULES.md, PROGRESS.md, dan dokumen yang disebut prompt. Akhir sesi: update PROGRESS.md lalu commit.
3. Dilarang mengasumsikan nama class, method, field, tag, atau path. Buktikan dengan membaca kode/jar, tulis path:baris sebagai bukti.
4. Dilarang memalsukan data. Jika sumber data tidak ada, tampilkan "-" dan catat di PROGRESS.md. Tanpa nilai default palsu, tanpa angka acak.
5. Scope: hanya ubah file yang tercantum di prompt fase. Jika butuh file lain, tulis di "Butuh izin user" dan berhenti pada bagian itu.
6. Jangan refactor di luar tugas, jangan hapus fitur lama, jangan ganti nama class/paket yang sudah dipakai.
7. Fitur yang sudah fix tidak boleh rusak: builder + dialog, pemilihan zookeeper, dialog zookeeper, cashier, antri, visitor, hunter, UI feeding trough, builder notes.
8. Zookeeper tidak pernah diberi makanan awal.
9. Mixin ke mod lain hanya di Fase 14, hanya untuk entity yang terbukti kategori C di 13-feeding-audit.md.
10. Jangan menyalin kode mod referensi secara mentah. Ambil arsitektur dan polanya, tulis implementasi sendiri. Cek lisensi sebelum memakai aset, catat sumber aset di docs/hologram/ASSETS.md.
11. Jalankan .\gradlew.bat build setelah tiap sub-langkah besar. Gagal 3 kali dengan akar masalah sama: berhenti, tulis analisis di "Blocker".
12. Selesai = data terbukti dari sumber aktual (log atau langkah uji in-game yang jelas), bukan karena UI terlihat.
13. Tidak ada framework test. Jangan tambah JUnit tanpa izin. Tulis langkah uji manual in-game.
14. UI gaya Minecraft: in-world hologram billboarded, teks + ikon, tanpa bold, tanpa emoji berlebihan kecuali ikon mood, maks 2 warna aksen. Komentar kode bahasa Inggris.
15. Git: hanya git add <path spesifik>. Dilarang git add -A, git add ., git commit -a. Pesan commit profesional (feat:, fix:, docs:). Push setelah commit.
16. Laporan akhir sesi jujur, tulis apa yang BELUM terverifikasi.
