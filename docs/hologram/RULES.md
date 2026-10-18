# Aturan kerja fitur Animal Inspection Hologram
1. Satu sesi = satu fase. Jangan mengerjakan fase lain walau terlihat mudah.
2. Awal sesi: baca RULES.md, PROGRESS.md, dan dokumen fase sebelumnya. Akhir sesi: update PROGRESS.md (apa selesai, file diubah, keputusan, blocker, langkah berikutnya) dan commit.
3. Dilarang mengasumsikan nama class, method, field, tag, atau path. Buktikan dengan membaca kode/jar, dan cantumkan path:baris sebagai bukti di dokumen.
4. Dilarang memalsukan data. Jika sumber data indikator tidak ada, tampilkan status tidak tersedia (misalnya "-") dan catat di PROGRESS.md. Jangan hardcode nilai dan jangan membuat angka acak.
5. Scope: hanya ubah file yang tercantum di 04-plan.md untuk fase berjalan. Jika perlu file lain, tulis di "Butuh izin user" di PROGRESS.md dan berhenti pada bagian itu.
6. Jangan refactor di luar tugas. Jangan hapus fitur lama. Jangan ubah nama/paket yang sudah dipakai.
7. Mixin ke mod lain adalah opsi TERAKHIR dan hanya boleh di Fase 11, dengan bukti dari 10-feeding-audit.md.
8. Jangan menyalin kode mod referensi. Pelajari polanya, tulis implementasi sendiri. Cek lisensi sebelum memakai aset (tekstur/ikon). Jika lisensi tidak jelas atau All Rights Reserved: jangan commit aset itu, gambar ikon sendiri atau pakai placeholder dan catat TODO-USER.
9. Build harus dijalankan setelah tiap sub-langkah besar. Jika gagal 3 kali dengan akar masalah yang sama: berhenti, tulis analisis di PROGRESS.md "Blocker", jangan lanjut menebak.
10. Jangan menyatakan sesuatu selesai hanya karena UI terlihat. Selesai = data yang tampil terbukti berasal dari sumber aktual (log, test, atau pengujian in-game yang dijelaskan langkahnya).
11. Test: tulis unit test untuk logika murni (state visibilitas, format persen, kalkulasi) jika framework test ada. Jangan menambah framework test baru tanpa izin. Jika tidak ada test otomatis, catat jujur.
12. UI: gaya Minecraft vanilla. Tanpa emoji dekoratif, tanpa teks berwarna berlebihan (maks 2 warna aksen), hanya teks + ikon. Satu-satunya pengecualian: ikon mood.
13. Teks UI lewat file lang (id_id dan en_us). Komentar kode dalam bahasa Inggris.
14. Laporan di akhir sesi harus jujur: tulis apa yang BELUM terverifikasi.
