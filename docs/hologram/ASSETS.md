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
| **Temperature** | `assets/zootycoon/textures/gui/inspect/thermometer.png` | 16x16 | PNG (RGBA 32-bit) | Ikon termometer dial bulat resmi dari Legendary Survival Overhaul (`textures/item/thermometer/thermometer_15.png`). | LGPL-3.0 (Legendary Survival Overhaul) | SELESAI |
| **Temp Hot Alert** | `assets/zootycoon/textures/gui/icons/temp_fire.png` | 16x16 | PNG (RGBA 32-bit) | Ikon api status panas dari Legendary Survival Overhaul (`textures/gui/overlay.png` Slot 4). | LGPL-3.0 (Legendary Survival Overhaul) | SELESAI |
| **Temp Cold Alert** | `assets/zootycoon/textures/gui/icons/temp_ice.png` | 16x16 | PNG (RGBA 32-bit) | Ikon kristal es status dingin dari Legendary Survival Overhaul (`textures/gui/overlay.png` Slot 5). | LGPL-3.0 (Legendary Survival Overhaul) | SELESAI |

---

## 2. Model 3D Blok Termal (Heater & Cooler Box)

| Blok | Berkas Model & Tekstur | Sumber Asli | Lisensi | Status |
|---|---|---|---|---|
| **Heater** | `models/block/heater_off.json`, `heater_on.json`, `textures/block/heater_off.png`, `heater_on.png` | Blockbench model & texture LSO (`heater_off_full_block.json`, `heater_base_on.json`) | LGPL-3.0 (Legendary Survival Overhaul) | SELESAI |
| **Cooler Box** | `models/block/cooler_box_off.json`, `cooler_box_on.json`, `textures/block/cooler.png` | Blockbench model & texture LSO (`cooler_off.json`, `cooler_on.json`) | LGPL-3.0 (Legendary Survival Overhaul) | SELESAI |

---

## 3. Catatan Kepatuhan Lisensi

1. **Resolusi Isu TODO-USER ThirstWasTaken2**:
   - Audit Fase 2 (`02-audit-external.md` Bagian C) mencatat bahwa tekstur HUD ThirstWasTaken2 berlisensi GPL-3.0.
   - Sesuai rekomendasi solusi teknis, dibuat tekstur orisinal 9x9 tetes air pixel art baru di `assets/zootycoon/textures/gui/inspect/thirst.png`.
   - Tidak ada kode atau gambar dari repositori eksternal yang diimpor.

2. **Atribusi Aset Legendary Survival Overhaul (LSO)**:
   - Ikon termometer dan model 3D blok Heater serta Cooler Box diadopsi dari proyek open source Legendary Survival Overhaul (oleh Alex-Hashtag / sfiomn) di bawah lisensi LGPL-3.0.
   - Model disesuaikan untuk format Fabric 1.21.1 Zoo Tycoon dengan layer rendering cutout.
