# Progress Pelaksanaan Fitur Animal Inspection Hologram

| Fase | Nama | Status | Tanggal | Catatan |
|---|---|---|---|---|
| 0 | Setup folder kerja | SELESAI | 2026-10-18 | Setup folder kerja hologram dan pembuatan CONTEXT.md, RULES.md, PROGRESS.md |
| 1 | Audit Zoo Tycoon | SELESAI | 2026-10-18 | Audit lengkap inspeksi satwa, sistem kebutuhan, sinkronisasi network, dan lang keys (01-audit-zoo.md) |
| 2 | Audit referensi eksternal | SELESAI | 2026-10-18 | Audit Farm & Charm (overlay 3D, billboarding, sync), Fabric Seasons (API musim & suhu), ThirstWasTaken2 (lisensi GPL, TODO-USER aset), Vanilla Watering Cans (resource pack, soft tag), tag makanan konvensi c:foods/raw_meat & raw_fish (02-audit-external.md) |
| 3 | Deteksi target & Billboarding Hologram | SELESAI | 2026-10-18 | Adopsi pola Farm & Charm: InspectionTargetTracker dengan crosshair pick + raycast collider 16 blok, zero-delay release, billboard world-space di ZooAnimalHologramRenderer dengan Font.DisplayMode.NORMAL |
| 4 | Sinkronisasi data | SELESAI | 2026-10-18 | Adopsi pola Farm & Charm: InspectionPayloads (InspectStart, InspectStop, InspectionSnapshot), ZooInspectionService (server manager, snapshot tiap 10 tick, validasi kaca pembesar/jarak/kandidat), InspectionClientCache (timeout 40 tick), ZooAnimalHologramRenderer membaca cache aktual dan menampilkan "-" untuk hewan belum terdaftar tanpa angka palsu |
| 5 | Tampilan Hologram In-World & Bar Indikator | BELUM | - | |
| 6 | Makanan favorit (Item Rendering 3D) | BELUM | - | |
| 7 | Musim dan suhu (Fabric Seasons) | BELUM | - | |
| 8 | Habitat dan ekosistem | BELUM | - | |
| 9 | Mandi hewan oleh player | BELUM | - | |
| 10 | Mandi hewan oleh zookeeper | BELUM | - | |
| 11 | Mood hewan | BELUM | - | |
| 12 | Validasi akhir hologram | BELUM | - | |
| 13 | Audit feeding trough | BELUM | - | |
| 14 | Pemaksaan feeding trough | BELUM | - | |

## Keputusan
- Koreksi CONTEXT.md poin T5: Sync periodik sebenarnya ada di server tick setiap 40 tick (2 detik) melalui `ZooNetworkPayloads.sendAnimalSync(player)`. Namun, method ini HANYA mengirimkan hewan yang sudah terdaftar (`isTagged`) dan dimiliki pemain (`isOwnedBy`). Hewan yang belum terdaftar tidak pernah masuk payload sehingga client renderer selalu menerima null dan jatuh ke nilai palsu (80/80/100). Mekanisme request inspeksi on-demand per target belum ada.
- Verifikasi temuan T1, T2, T3, T4, T6, T7, T8, T9 seluruhnya terkonfirmasi BENAR dengan bukti path:baris di kode sumber.
- Seluruh teks hologram saat ini hardcoded literal dengan emoji dan format § tanpa translation key.
- Integrasi musim rusak karena kode mengecek "sereneseasons", padahal modpack menggunakan "fabric-seasons".
- Arsitektur in-world hologram 3D Farm & Charm diverifikasi: mengadopsi `mc.crosshairPickEntity`, offset vertikal `yOffset = getBbHeight() + 0.6` (+ 0.3 untuk nama kustom), billboarding penuh `cameraOrientation()`, penskalaan 0.4f, dan rendering model 3D item `renderStatic(..., ItemDisplayContext.FIXED)`.
- Fabric Seasons terbukti memodifikasi suhu bioma runtime melalui `WorldViewMixin.getBiome` -> `FabricSeasons.injectBiomeTemperature`. Suhu efektif dapat dibaca langsung melalui `level.getBiome(pos).value().getTemperature(pos)` sebagai nilai float murni Minecraft vanilla.
- Aset ThirstWasTaken2 berlisensi GPL-3.0 dan tidak kompatibel dengan lisensi MIT Zoo Tycoon. Solusi: membuat tekstur orisinal 9x9 tetes air gaya Minecraft vanilla di bawah lisensi Zoo Tycoon.
- Vanilla Watering Cans terbukti murni Resource Pack (bukan mod Fabric 1.21.1) yang me-retexture ember vanilla. Integrasi disiapkan sebagai soft dependency melalui tag `zootycoon:watering_cans` berisi `minecraft:water_bucket` dan ID opsional mod lain dengan `"required": false`.
- Tag konvensi resmi Fabric API v2 di 1.21.1 adalah `#c:foods/raw_meat` dan `#c:foods/raw_fish`. Mod FarmersDelight, Farm & Charm, dan Naturalist secara konsisten mengimplementasikan tag tersebut, sedangkan item olahan/masakan dipisahkan agar tidak menjadi pakan satwa liar.
- Fase 3: Deteksi target dirombak total menggunakan `InspectionTargetTracker` yang memadukan `mc.crosshairPickEntity` dengan raycast berbatas `ClipContext.Block.COLLIDER` maks 16 blok. Timer lama `inspectExpireTime` (8-10 detik) dan trigger scan lama dihapus total sehingga target terlepas secara instan (zero-delay) saat crosshair bergeser, membuka GUI, atau menekan F1.
- Fase 3: `ZooAnimalHologramRenderer` diubah menjadi world-space in-world billboarded hologram melayang di atas kepala satwa menggunakan rotasi kamera (`poseStack.mulPose(camera.rotation())`) dan depth test aktif (`Font.DisplayMode.NORMAL`), tanpa emoji dan tanpa formatting § hardcoded.
- Fase 4: Sinkronisasi data inspeksi mengadopsi pola Farm & Charm (`SyncSaturationPacket`): client mengirim `InspectStartPayload(entityId)` saat crosshair membidik target baru dan `InspectStopPayload()` saat crosshair lepas atau berganti target. Server-side manager `ZooInspectionService` memvalidasi kelayakan kandidat, status hidup entitas, batas jarak <= 20 blok, dan status pemain memegang kaca pembesar, lalu mengirimkan `InspectionSnapshotPayload` tiap 10 tick. Nilai kebutuhan satwa yang belum terdaftar dikirim eksplisit sebagai `-1` dan ditampilkan `-` di hologram tanpa angka rekayasa (80/80/100).

## Bukti Selesai (Fase 4)
- Buka AnimalManagementTab, bandingkan nilai hewan terdaftar: nilai di hologram (hunger, thirst, cleanliness) bersumber dari `ZooAnimalHungerManager` aktual melalui `InspectionSnapshotPayload` dan sama persis dengan yang ada di tab manajemen.
- Amati saat hunger berkurang 1: bar dan teks persentase di hologram ikut ter-update live tiap 10 tick tanpa perlu klik kanan atau membuka GUI menu.
- Arahkan ke hewan liar belum terdaftar: hologram menampilkan status 'Belum terdaftar' dan nilai kebutuhan '-' secara jujur tanpa nilai default palsu (80/80/100).
- Payload lama `ZooNetworkPayloads.java` tidak disentuh sama sekali sehingga integritas `AnimalSyncData`, GUI manajemen, dan dialog zookeeper tetap aman dan utuh.
- Build gradle berhasil tanpa kesalahan dan file jar otomatis terpasang ke profil Modrinth.

## Blocker
- Tidak ada.

## File yang diubah
- src/main/java/net/zootycoon/network/InspectionPayloads.java (BARU)
- src/main/java/net/zootycoon/animal/ZooInspectionService.java (BARU)
- src/main/java/net/zootycoon/client/inspect/InspectionClientCache.java (BARU)
- src/main/java/net/zootycoon/client/inspect/InspectionTargetTracker.java
- src/main/java/net/zootycoon/client/render/ZooAnimalHologramRenderer.java
- src/main/java/net/zootycoon/ZooTycoonMod.java
- src/main/java/net/zootycoon/client/ZooTycoonClient.java
- docs/hologram/PROGRESS.md

## Butuh izin user
- TODO-USER: izin aset - Tekstur ikon thirst dari ThirstWasTaken2 berlisensi GPL-3.0 sehingga dilarang disalin langsung ke Zoo Tycoon (MIT). Solusi default adalah merancang ikon 9x9 pixel art tetes air orisinal gaya vanilla. Jika pengguna menginginkan aset spesifik lain, mohon berikan konfirmasi.

## Belum terverifikasi
- Uji interaktif koneksi jaringan remote multiplayer multi-client untuk memverifikasi reliabilitas rate 10-tick snapshot pada kondisi latensi tinggi.
