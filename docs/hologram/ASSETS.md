# Dokumentasi Aset Visual Animal Inspection Hologram

Dokumen ini mencatat seluruh aset grafis dan tekstur yang digunakan oleh fitur Animal Inspection Hologram di Zoo Tycoon, beserta asal-usul, resolusi, format, dan status lisensi sesuai Aturan Proyek (RULES.md butir 10 & 02-audit-external.md bagian C).

---

## 1. Ikon Status In-World (Health & Hunger Vanilla, Thirst & Cleanliness Kustom)

Sesuai arahan desain visual Minecraft vanilla:
- **Health**: Menggunakan sprite resmi Minecraft vanilla `hud/heart/full` dari `GuiSpriteManager` (resolusi 9x9).
- **Hunger**: Menggunakan sprite resmi Minecraft vanilla `hud/food_full` dari `GuiSpriteManager` (resolusi 9x9).
- **Thirst & Cleanliness**: Karena mekanik ini tidak ada pada vanilla Minecraft, dibuat tekstur orisinal (**original pixel art**) 9x9 khusus untuk mod Zoo Tycoon di bawah lisensi MIT.

| Ikon | Sumber / Berkas | Resolusi | Format | Deskripsi Visual | Lisensi | Status |
|---|---|---|---|---|---|---|
| **Health** | `minecraft:hud/heart/full` | 9x9 | Vanilla Sprite Atlas | Ikon hati resmi Minecraft vanilla HUD dengan outline gelap, warna merah, dan specular dot. | Mojang / Minecraft Vanilla | SELESAI (Fase 5) |
| **Hunger** | `minecraft:hud/food_full` | 9x9 | Vanilla Sprite Atlas | Ikon paha ayam resmi Minecraft vanilla HUD (*food bar full*). | Mojang / Minecraft Vanilla | SELESAI (Fase 5) |
| **Thirst** | `assets/zootycoon/textures/gui/inspect/thirst.png` | 9x9 | PNG (RGBA 32-bit) | Ikon tetesan air (*water droplet*) dengan outline gelap `#12325c`, gradasi biru laut `#2c8be8`, bayangan `#1a5ea6`, dan pantulan cahaya putih `#ffffff`. | MIT (Orisinal Zoo Tycoon) | SELESAI (Fase 5) |
| **Cleanliness** | `assets/zootycoon/textures/gui/inspect/cleanliness.png` | 9x9 | PNG (RGBA 32-bit) | Ikon gelembung berkilau (*sparkling bubble*) dengan outline teal `#0f4a54`, warna dasar cyan `#39c2d1`, dan kilau putih `#ffffff`. | MIT (Orisinal Zoo Tycoon) | SELESAI (Fase 5) |

---

## 2. Catatan Kepatuhan Lisensi

1. **Resolusi Isu TODO-USER ThirstWasTaken2**:
   - Audit Fase 2 (`02-audit-external.md` Bagian C) mencatat bahwa tekstur HUD ThirstWasTaken2 berlisensi GPL-3.0.
   - Sesuai rekomendasi solusi teknis, dibuat tekstur orisinal 9x9 tetes air pixel art baru di `assets/zootycoon/textures/gui/inspect/thirst.png`.
   - Tidak ada kode atau gambar dari repositori eksternal yang diimpor.
