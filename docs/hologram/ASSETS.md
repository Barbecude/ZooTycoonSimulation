# Dokumentasi Aset Visual Animal Inspection Hologram

Dokumen ini mencatat seluruh aset grafis dan tekstur yang digunakan oleh fitur Animal Inspection Hologram di Zoo Tycoon, beserta asal-usul, resolusi, format, dan status lisensi sesuai Aturan Proyek (RULES.md butir 10 & 02-audit-external.md bagian C).

---

## 1. Tekstur Ikon Status In-World (9x9 Pixels)

Seluruh ikon di bawah ini dibuat secara orisinal (**original pixel art**) khusus untuk mod Zoo Tycoon. Tidak ada aset yang disalin dari mod eksternal berlisensi copyleft (seperti ThirstWasTaken2 / GPL-3.0), sehingga aman di bawah lisensi MIT Zoo Tycoon.

| Berkas | Resolusi | Format | Deskripsi Visual | Lisensi | Status |
|---|---|---|---|---|---|
| `assets/zootycoon/textures/gui/inspect/thirst.png` | 9x9 | PNG (RGBA 32-bit) | Ikon tetesan air (*water droplet*) dengan outline gelap `#12325c`, gradasi biru laut `#2c8be8`, bayangan `#1a5ea6`, dan pantulan cahaya putih `#ffffff`. | MIT (Orisinal Zoo Tycoon) | SELESAI (Fase 5) |
| `assets/zootycoon/textures/gui/inspect/cleanliness.png` | 9x9 | PNG (RGBA 32-bit) | Ikon gelembung berkilau (*sparkling bubble*) dengan outline teal `#0f4a54`, warna dasar cyan `#39c2d1`, dan kilau putih `#ffffff`. | MIT (Orisinal Zoo Tycoon) | SELESAI (Fase 5) |
| `assets/zootycoon/textures/gui/inspect/hunger.png` | 9x9 | PNG (RGBA 32-bit) | Ikon paha ayam panggang (*roast drumstick*) dengan warna cokelat-oranye `#b35515` / `#e07f2b`, highlight `#faa955`, dan ujung tulang krem `#f0ebd8`. | MIT (Orisinal Zoo Tycoon) | SELESAI (Fase 5) |
| `assets/zootycoon/textures/gui/inspect/heart.png` | 9x9 | PNG (RGBA 32-bit) | Ikon hati bergaya Minecraft vanilla dengan outline crimson gelap `#330000`, warna merah cerah `#d91a1a`, highlight `#f54747`, dan specular glint `#ffffff`. | MIT (Orisinal Zoo Tycoon) | SELESAI (Fase 5) |

---

## 2. Catatan Kepatuhan Lisensi

1. **Resolusi Isu TODO-USER ThirstWasTaken2**:
   - Audit Fase 2 (`02-audit-external.md` Bagian C) mencatat bahwa tekstur HUD ThirstWasTaken2 berlisensi GPL-3.0.
   - Sesuai rekomendasi solusi teknis, dibuat tekstur orisinal 9x9 tetes air pixel art baru di `assets/zootycoon/textures/gui/inspect/thirst.png`.
   - Tidak ada kode atau gambar dari repositori eksternal yang diimpor.

