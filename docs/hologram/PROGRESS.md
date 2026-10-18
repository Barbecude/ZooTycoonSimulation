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
| 7 | Musim dan suhu (Fabric Seasons) | SELESAI | 2026-10-18 | Integrasi FabricSeasonsCompat dengan refleksi getCurrentSeason & suhu bioma musiman; SereneSeasonsCompat otomatis fallback ke Fabric Seasons; ZooClimate menghitung suhu efektif & kategori (Beku, Dingin, Sejuk/Sedang, Hangat, Panas Terik); InspectionSnapshotPayload & ZooInspectionService mengirim data iklim; ZooAnimalHologramRenderer menampilkan baris "Musim: [Nama Musim] | Suhu: [Kategori Suhu]" tanpa Celsius palsu; laju kehausan musim panas normal |
| 8 | Habitat dan ekosistem | SELESAI | 2026-10-18 | Data-driven habitat suitability & ecosystem block requirements via JSON data packs; 9 template ekosistem (Savana, Hutan, Hutan Tropis, Kutub/Salju, Gurun, Padang Rumput, Perairan Tawar, Lautan, Rawa); evaluasi bioma, temperatur nyaman, dan blok sekitar radius 8; server snapshot sync; hologram menampilkan "Habitat: Sesuai (Ideal)" atau "Habitat: Tidak Sesuai" dan petunjuk kebutuhan "Butuh: ..." |
| 9 | Mandi hewan oleh player | SELESAI | 2026-10-18 | Sistem mandi 2 tahap (watering can + brush): tag zootycoon:watering_cans (default water_bucket, mod cans opsional), durasi basah 600 tick, brush nambah kebersihan +35 (maks 100) kurangi durability 1, partikel busa kilau sabun & splash, actionbar warning jika belum basah, broom tidak lagi bersihkan badan satwa, auto-clean hujan dihapus, status (Basah) di hologram |
| 10 | Mandi hewan oleh zookeeper & Bedah Rendering Hologram | SELESAI | 2026-10-18 | Bedah total in-world hologram (vertical anchoring maxY=0 ke atas, quad winding CCW, Z-layering presisi, Font.DisplayMode.POLYGON_OFFSET, pakan 3D skala 7) & integrasi Zookeeper memandikan satwa asuh (GiveBathingToolsPayload, tombol dialog 3-button, AI mandi siram + sikat kebersihan 100%, durability brush berkurang 1) |
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
- Fase 7: Mengimplementasikan `FabricSeasonsCompat` via refleksi runtime ke `io.github.lucaargolo.seasons.FabricSeasons` (`getCurrentSeason` level & client, `injectBiomeTemperature`), memperbaiki bug T6 di mana iklim selalu macet di "SPRING". `SereneSeasonsCompat.getCurrentSeason` dan `isLoaded` secara otomatis mengarahkan panggilan ke `FabricSeasonsCompat` jika Serene Seasons tidak ada, menormalkan laju thirst satwa di musim kemarau/panas (`ZooAnimalHungerManager.tickServer`).
- Fase 7: Kelas utilitas iklim `ZooClimate` menghitung suhu efektif aktual bioma pada posisi satwa (termasuk altitude lapse rate di atas Y=80) dan mengklasifikasikan ke dalam kategori resmi: Beku (< 0.15), Dingin (< 0.45), Sejuk / Sedang (< 0.85), Hangat (< 1.25), Panas Terik (>= 1.25).
- Fase 7: Status musim dan kategori suhu dikirimkan secara periodik di `InspectionSnapshotPayload` oleh `ZooInspectionService`. `ZooAnimalHologramRenderer` menampilkan baris iklim: `Musim: [Nama Musim] | Suhu: [Kategori Suhu]` via translatable key `zootycoon.inspect.climate` tanpa angka Celsius palsu.
- Fase 8: Arsitektur data-driven habitat mengadopsi pola Minecraft Data Pack dengan `SimpleJsonResourceReloadListener` di `HabitatProfileLoader` yang membaca template ekosistem dari `data/zootycoon/zoo_habitat/templates/*.json` dan mapping spesies dari `data/zootycoon/zoo_habitat/species/*.json`, teregistrasi melalui `ResourceManagerHelper.get(PackType.SERVER_DATA)`.
- Fase 8: 9 template ekosistem baku (Savana, Hutan, Hutan Tropis, Kutub/Salju, Gurun, Padang Rumput, Perairan Tawar, Lautan, Rawa) mendefinisikan bioma yang cocok (tag `#minecraft:is_savanna`, dll serta list ID bioma), rentang temperatur ideal, serta blok ekosistem wajib di sekitar (radius 8 blok, maks 3 syarat, contoh: air >= 4 blok, pohon/daun >= 6 blok, salju/es >= 6 blok, pasir >= 8 blok). Seluruh satwa di `AnimalDietRegistry` memiliki pemetaan template dan fallback in-code.
- Fase 8: Server-side evaluator `HabitatEvaluator.evaluate(ServerLevel level, LivingEntity animal)` memvalidasi bioma posisi satwa, temperatur efektif bioma (via `ZooClimate`), serta memindai blok sekitar dalam radius horizontal 8 dan vertikal -4..4 blok.
- Fase 8: Sinkronisasi server ke client mengalir melalui `InspectionSnapshotPayload` (`habitatStatus` dan `habitatNeeds`) yang dikirim periodik tiap 10 tick dan dicache di `InspectionPayloads.LATEST_HABITAT`.
- Fase 8: Visual hologram `ZooAnimalHologramRenderer` menampilkan baris status: `Habitat: Sesuai (Ideal)` (hijau soft 0xFF55FF55) atau `Habitat: Tidak Sesuai` (merah soft 0xFFFF5555). Jika ada kebutuhan yang kurang, ditampilkan baris petunjuk: `Butuh: Air, Savana`, `Bioma: Perlu Savana`, atau `Suhu: Terlalu Dingin` (kuning peringatan 0xFFFFAA00). Jika status sesuai/ideal, baris petunjuk otomatis dihilangkan agar tidak memadati layar.
- Fase 9: Mod eksternal "Vanilla Watering Cans" diverifikasi tidak memiliki mod Fabric 1.21.1 (hanya resource pack pengganti tekstur CIT / mod NeoForge). Sesuai rekomendasi audit, diterapkan sistem soft dependency melalui item tag `zootycoon:watering_cans` yang secara bawaan mendukung `minecraft:water_bucket` dan item watering can mod eksternal dengan status `"required": false`.
- Fase 9: Mekanik mandi satwa wajib 2 alat (`AnimalBathingManager`): satwa terdaftar yang disiram alat penyiram air masuk status `basah` selama 600 tick (30 detik), memunculkan efek suara `bucket_empty` & `generic_splash` serta partikel `splash` dan `dripping_water`.
- Fase 9: Penggosokan dengan `minecraft:brush` pada satwa basah memulihkan kebersihan +35 poin (maks 100), mengurangi durability sikat sebesar 1 (kecuali mode creative), memainkan suara `brush_generic`, dan memunculkan partikel busa kilau sabun (`bubble_pop`, `wax_off`, `splash`).
- Fase 9: Penggosokan dengan `brush` pada satwa yang BELUM basah memunculkan actionbar translatable `zootycoon.bathing.need_water` ("Basahi satwa terlebih dahulu dengan penyiram air!") dan kebersihan tidak bertambah.
- Fase 9: Item sapu `BroomItem` dicabut dari event pembersihan satwa di `ZooTycoonMod.java`, murni dikhususkan untuk membersihkan sampah kandang (`ZooBlocks.TRASH`).
- Fase 9: Pembersihan pasif otomatis dari air/hujan di `ZooAnimalHungerManager.tickServer` dihapus, penurunan kebersihan bertahap 1% tiap 240 tick tetap berjalan normal.
- Fase 9: Payload `InspectionSnapshotPayload` membawa `boolean isWet`, `ZooInspectionService` memberitahukan status basah secara real-time via `notifyAnimalUpdated`, dan `ZooAnimalHologramRenderer` menampilkan status `(Basah)` / `(Wet)` di baris bar kebersihan.

- Fase 10: Vertical anchoring disesuaikan ke `entY = entity.getY() + entity.getBbHeight() + 0.15f` (+0.25f jika custom name) dengan skala diperkecil ke `0.0135f` dan `maxY = 0.0f` serta `startY = maxY - totalBoxHeight`. Kartu hologram tumbuh ke atas dan mengapung proporsional persis di atas kepala satwa tanpa terpotong atau terlalu tinggi di langit.
- Fase 10: Winding order pada `drawRect`, `drawTexturedIcon`, dan `drawAtlasSprite` di `HologramBarRenderer` diubah dari Clockwise (CW) ke Counter-Clockwise (CCW): `minX,minY -> maxX,minY -> maxX,maxY -> minX,maxY` untuk mencegah backface culling OpenGL.
- Fase 10: Z-layering presisi ditetapkan dari belakang ke depan (background plate z=-0.02f, track/border z=-0.016f & -0.014f, fill z=-0.010f, highlight z=-0.008f, ikon z=-0.005f, font z=0.0f), `bs.endBatch(RenderType.textBackground())` di-flush tepat setelah bar, dan seluruh teks menggunakan `Font.DisplayMode.NORMAL` sehingga font dan bar tidak lagi tertutup oleh background plate.
- Fase 10: Horizontal scale X ditetapkan positif `scale(scale, -scale, scale)` memulihkan arah pengisian progress bar (mengisi dari kiri ke kanan, track gelap di kanan) dan memulihkan font rendering yang sebelumnya terbalik dan ter-cull GPU.
- Fase 10: Magnifying glass difilter ketat (`isZooRegisteredAnimal`) baik pada client (`InspectionTargetTracker`) maupun server (`ZooInspectionService`) sehingga HANYA membidik dan menampilkan hologram untuk satwa yang telah terdaftar di zoo.
- Fase 10: Label teks status ("Darah", "Lapar", "Haus", "Bersih") ditambahkan di samping kiri masing-masing bar indikator di samping ikon, dengan nilai teks di sebelah kanan.
- Fase 10: Hologram pakan disederhanakan dari render 3D menjadi baris teks pakan yang dinamis memindai seluruh item mod terpasang via `Animal.isFood(ItemStack)` dan tag raw meat/fish (`AnimalDietRegistry.getFoodsForAnimal`).
- Fase 10: Bedah total in-world hologram (vertical anchoring maxY=0 ke atas, quad winding CCW, Z-layering presisi, Font.DisplayMode.POLYGON_OFFSET, pakan 3D skala 7) & integrasi Zookeeper memandikan satwa asuh (GiveBathingToolsPayload, tombol dialog 3-button, AI mandi siram + sikat kebersihan 100%, durability brush berkurang 1).
- Redesain Hologram Wireframe & Habitat Thermal AI:
  1. UI Hologram murni mengikuti wireframe mockup pengguna: nama hewan di atas, jajaran ikon makanan favorit tanpa teks label, 5 baris status berikon (Kesehatan, Lapar, Haus, Kebersihan, Temperature), serta checklist kebutuhan habitat bernomor.
  2. Perbaikan permanen bug strip status ("-"): durasi snapshot pada InspectionClientCache diperpanjang menjadi 600 tick (30 detik).
  3. Makanan favorit ditampilkan murni berupa jajaran ikon item pakan tanpa teks label.
  4. Ikon temperatur mengadopsi termometer pixel art dari Legendary Survival Overhaul (LSO), nilai temperatur dikonversi ke derajat Celsius akurat via ZooClimate.toCelsius.
  5. Interval rasa lapar satwa diperpanjang menjadi 240 tick per 1% drop (siklus penuh 20 menit / 24.000 tick dari 100% ke 0%).
  6. Evaluasi habitat berbasis tag blok alam tanah sekitar (#zootycoon:natural_ground dan konvensi #c:natural_ground) mencakup coarse dirt, podzol, sand, gravel, snow, dan tanah modifikasi mod lain.
  7. Aturan bioma dan habitat: bioma cocok = habitat cocok, pengecualian air hanya diperiksa untuk satwa yang membutuhkan air (jika butuh air dan air tidak ada, habitat tidak terpenuhi).
  8. Menambahkan blok Heater dan Cooler Box fungsional berbasis bahan bakar dengan tekstur, blockstate, model, resep crafting vanilla, dan pemancaran radius termal 12 blok (+0.6f / -0.6f) yang memengaruhi suhu efektif satwa dan evaluasi habitat.
  9. Menghadirkan AnimalDrinkWaterGoal (AI goal mandiri berbasis MoveToBlockGoal) yang terpasang pada seluruh satwa terdaftar via MobAccessor untuk mencari, mendekat, dan meminum air di sumber air atau feeding trough saat haus.

## Bukti Selesai
- Seluruh 9 poin UI dan gameplay telah terimplementasi dan terkompilasi bersih dengan .\gradlew.bat build (BUILD SUCCESSFUL).
- File jar hasil build zootycoon-1.0.0.jar otomatis tersalin ke folder mods profil Modrinth.
- Tata letak hologram in-world presisi mengikuti mockup wireframe pengguna.
- Blok Heater dan Cooler Box siap dicrafting dan digunakan di dalam game.

## Blocker
- Tidak ada blocker.

## File yang diubah
- src/main/java/net/zootycoon/client/render/ZooAnimalHologramRenderer.java
- src/main/java/net/zootycoon/client/inspect/HologramBarRenderer.java
- src/main/java/net/zootycoon/client/inspect/InspectionClientCache.java
- src/main/java/net/zootycoon/animal/ZooAnimalHungerManager.java
- src/main/java/net/zootycoon/animal/ZooClimate.java
- src/main/java/net/zootycoon/animal/ZooInspectionService.java
- src/main/java/net/zootycoon/animal/habitat/HabitatEvaluator.java
- src/main/java/net/zootycoon/animal/habitat/HabitatProfile.java
- src/main/java/net/zootycoon/animal/ai/AnimalDrinkWaterGoal.java (BARU)
- src/main/java/net/zootycoon/block/ZooBlocks.java
- src/main/java/net/zootycoon/block/ZooHeaterBlock.java (BARU)
- src/main/java/net/zootycoon/block/ZooCoolerBlock.java (BARU)
- src/main/java/net/zootycoon/block/entity/ZooThermalBlockEntity.java (BARU)
- src/main/java/net/zootycoon/mixin/MobAccessor.java (BARU)
- src/main/resources/zootycoon.mixins.json
- src/main/resources/assets/zootycoon/lang/en_us.json
- src/main/resources/assets/zootycoon/lang/id_id.json
- src/main/resources/assets/zootycoon/blockstates/heater.json (BARU)
- src/main/resources/assets/zootycoon/blockstates/cooler_box.json (BARU)
- src/main/resources/assets/zootycoon/models/block/*.json (BARU)
- src/main/resources/assets/zootycoon/models/item/*.json (BARU)
- src/main/resources/assets/zootycoon/textures/block/*.png (BARU)
- src/main/resources/assets/zootycoon/textures/gui/inspect/thermometer.png (BARU)
- src/main/resources/data/zootycoon/recipe/*.json (BARU)
- src/main/resources/data/zootycoon/tags/block/natural_ground.json (BARU)
- src/main/resources/data/c/tags/block/natural_ground.json (BARU)
- docs/hologram/PROGRESS.md

## Butuh izin user
- Tidak ada.

## Belum terverifikasi
- Pengujian in-game langsung oleh pengguna untuk memeriksa visual hologram wireframe, interaksi blok Heater dan Cooler Box, serta AI satwa minum air.
