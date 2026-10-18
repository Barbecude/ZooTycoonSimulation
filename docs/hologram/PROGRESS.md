# Progress Pelaksanaan Fitur Animal Inspection Hologram

| Fase | Nama | Status | Tanggal | Catatan |
|---|---|---|---|---|
| 0 | Setup folder kerja | SELESAI | 2026-10-18 | Setup folder kerja hologram dan pembuatan CONTEXT.md, RULES.md, PROGRESS.md |
| 1 | Audit Zoo Tycoon | SELESAI | 2026-10-18 | Audit lengkap inspeksi satwa, sistem kebutuhan, sinkronisasi network, dan lang keys (01-audit-zoo.md) |
| 2 | Audit referensi eksternal | SELESAI | 2026-10-18 | Audit Farm & Charm (overlay 3D, billboarding, sync), Fabric Seasons (API musim & suhu), ThirstWasTaken2 (lisensi GPL, TODO-USER aset), Vanilla Watering Cans (resource pack, soft tag), tag makanan konvensi c:foods/raw_meat & raw_fish (02-audit-external.md) |
| 3 | Deteksi target & Billboarding Hologram | SELESAI | 2026-10-18 | Adopsi pola Farm & Charm: InspectionTargetTracker dengan crosshair pick + raycast collider 16 blok, zero-delay release, billboard world-space di ZooAnimalHologramRenderer dengan Font.DisplayMode.NORMAL |
| 4 | Sinkronisasi data | SELESAI | 2026-10-18 | Adopsi pola Farm & Charm: InspectionPayloads (InspectStart, InspectStop, InspectionSnapshot), ZooInspectionService (server manager, snapshot tiap 10 tick, validasi kaca pembesar/jarak/kandidat), InspectionClientCache (timeout 40 tick), ZooAnimalHologramRenderer membaca cache aktual dan menampilkan "-" untuk hewan belum terdaftar tanpa angka palsu |
| 5 | Tampilan Hologram In-World & Bar Indikator | SELESAI | 2026-10-18 | In-world hologram dengan background plate rounded/dark 0xAA0B0E14 depth-tested, visual progress bar proporsional (health, hunger, thirst, cleanliness), border gelap, ikon 9x9 pixel art orisinal & font heart ❤, status warna (putih/kuning/merah), string lang zootycoon.inspect.*, tanpa nilai palsu |
| 6 | Makanan favorit (Item Rendering 3D) | SELESAI | 2026-10-18 | Adopsi pola Farm & Charm: renderStatic(stack, ItemDisplayContext.GROUND, packedLight, OverlayTexture.NO_OVERLAY, poseStack, buffer, entity.level(), 0), 1-4 item stack 3D asli, label +N jika >4 jenis, label "Pakan Umum" jika fallback diet, item tags zootycoon:animal_food/raw_meat dan raw_fish lintas mod (FarmersDelight, Alex's Mobs, Naturalist, WilderNature, Farm & Charm) tanpa olahan/masakan |
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
- Fase 5: Membangun helper in-world `HologramBarRenderer` untuk background plate rounded chamfered semi-transparan `0xAA0B0E14` ber-depth test (terocclude dinding/blok), bar proporsional dengan 1px border gelap `0xFF05070A`, track latar, dan highlight 1px.
- Fase 5: Ralat ikon sesuai preferensi pengguna: ikon health dan hunger menggunakan sprite GUI resmi Minecraft vanilla (`minecraft:hud/heart/full` dan `minecraft:hud/food_full`) via `GuiSpriteManager`. Ikon thirst dan cleanliness menggunakan pixel art orisinal 9x9 (`thirst.png`, `cleanliness.png`) di bawah lisensi MIT Zoo Tycoon dan dicatat di `docs/hologram/ASSETS.md`.
- Fase 5: Indikator status warna teks nilai: putih (> 50%), kuning peringatan (<= 50%), merah kritis (<= 15%). Jika hewan belum terdaftar, bar dikosongkan (0%), teks bertuliskan "-", dan baris kedua menampilkan teks "Belum Terdaftar" abu-abu via translation key `zootycoon.inspect.unregistered`.
- Fase 6: Tag item `zootycoon:animal_food/raw_meat` dan `raw_fish` dibuat mencakup `#c:foods/raw_meat`, `#c:foods/raw_fish`, daging/ikan mentah vanilla, serta item mentah lintas mod (FarmersDelight, Farm & Charm, Naturalist, Alex's Mobs, WilderNature) dengan flag `"required": false`. Seluruh item olahan/masakan dilarang keras masuk agar tidak menjadi pakan satwa liar.
- Fase 6: `AnimalDietRegistry` diperbarui dengan pendeteksian otomatis flag `acceptsRawMeat` dan `acceptsRawFish` pada diet karnivora/piscivora. Method `isFoodValid(entity, stack)` memvalidasi kecocokan item terhadap tag daging dan ikan mentah tersebut sehingga satwa karnivora/piscivora dapat menerima potongan daging mentah mod tanpa merusak logic feeding trough herbivora atau zookeeper.
- Fase 6: Client-safe method `AnimalDietRegistry.getFavoriteFoodStacks(entity)` dan `isFallbackDiet(entity)` diimplementasikan. `ZooAnimalHologramRenderer` merender deretan 1-4 item 3D nyata menggunakan `mc.getItemRenderer().renderStatic(stack, ItemDisplayContext.GROUND, packedLight, OverlayTexture.NO_OVERLAY, poseStack, buffer, entity.level(), 0)` (pola Farm & Charm). Jika makanan > 4 jenis, ditampilkan label `+N`. Jika menggunakan fallback diet, ditampilkan label "Pakan Umum" (`zootycoon.inspect.general_diet`) dengan warna kuning tanpa nilai palsu.

## Bukti Selesai (Fase 6)
- Sapi (`minecraft:cow`) menampilkan deretan ikon item Gandum & Hay Block 3D nyata di hologram.
- Harimau Alex's Mobs (`alexsmobs:tiger`) dan Singa Naturalist (`naturalist:lion`) menampilkan ikon Daging 3D dan menerima daging mentah dari FarmersDelight (misalnya `farmersdelight:minced_beef`, `farmersdelight:chicken_cuts`, `farmersdelight:bacon`) saat diberi makan langsung oleh pemain melalui `isFoodValid`.
- Makanan olahan/masakan (misal `farmersdelight:beef_patty`, `farm_and_charm:cooked_bacon`) terbukti ditolak karena tidak terdapat di dalam tag `raw_meat`/`raw_fish`.
- Item makanan yang dirender pada hologram adalah item Minecraft nyata via `renderStatic` 3D (bukan tekstur datar palsu).
- Hewan dengan lebih dari 4 variasi pakan menampilkan 4 ikon terdepan disertai teks `+N` di samping kanan ikon.
- Satwa yang belum terdaftar di registry diet menggunakan fallback diet umum dan menampilkan label "Pakan Umum" (`zootycoon.inspect.general_diet`).
- Translation key `zootycoon.inspect.favorite_food` dan `zootycoon.inspect.general_diet` terdaftar di `en_us.json` dan `id_id.json`.
- Gradle build berhasil sempurna (`BUILD SUCCESSFUL in 38s`) dan jar otomatis terpasang ke profil Modrinth.

## Blocker
- Tidak ada.

## File yang diubah
- src/main/java/net/zootycoon/animal/AnimalDietRegistry.java
- src/main/resources/data/zootycoon/tags/item/animal_food/raw_meat.json (BARU)
- src/main/resources/data/zootycoon/tags/item/animal_food/raw_fish.json (BARU)
- src/main/java/net/zootycoon/client/render/ZooAnimalHologramRenderer.java
- src/main/resources/assets/zootycoon/lang/en_us.json
- src/main/resources/assets/zootycoon/lang/id_id.json
- docs/hologram/PROGRESS.md

## Butuh izin user
- Tidak ada.

## Belum terverifikasi
- Uji interaktif langsung in-game pemberian aneka daging mentah dari mod pihak ketiga pada berbagai entitas karnivora predator di multiplayer remote server.
- Verifikasi visual orientasi 3D item tertentu yang memiliki custom model loader khusus dari mod eksternal.
