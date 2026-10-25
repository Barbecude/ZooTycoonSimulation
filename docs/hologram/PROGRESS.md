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
| 13 | Diet & Metode Pakan Satwa | SELESAI | 2026-10-24 | Kategori diet & metode pakan di hologram serta AI makan palung vs pakan dilempar |
| 14 | Pemaksaan feeding trough & AI jangkauan makan | SELESAI | 2026-10-24 | AI Goal AnimalEatFromTroughGoal & perbaikan jangkauan makan/minum satwa berleher tinggi / ramping (ostrich, jerapah, unta, burung, dll) |
| 15 | Kondisi Dialog 2-Tombol & Perbaikan Toast Pakan | SELESAI | 2026-10-24 | Rework dialog zookeeper menjadi tepat 2 tombol sesuai kebutuhan (Alat Mandi vs Pakan) dan perbaikan toast pakan |
| 16 | Zookeeper Lempar Pakan, Animasi Mandi & Brush First-Person | SELESAI | 2026-10-24 | AI lempar pakan zookeeper untuk karnivora, dialog dinamis pakan lempar vs palung, animasi model penyiram & sikat zookeeper, dan animasi first-person brush player |
| 17 | Detail Jenis Pohon Habitat (Bahasa Inggris) | SELESAI | 2026-10-25 | Mengganti label generic pepohonan dan istilah terjemahan menjadi nama spesifik jenis pohon dalam bahasa Inggris (Pohon Jungle, Pohon Oak, Pohon Acacia, Pohon Spruce, dll) |
| 18 | Perbaikan Mandi Satwa Zookeeper & Kebersihan 100% | SELESAI | 2026-10-25 | Ticking per-tick aksi mandi zookeeper, jarak adaptif, pemulihan kebersihan 100%, dan filter dialog alat mandi hanya saat satwa kotor |
| 19 | Indikator Tembus Blok & Filter Peringatan Zookeeper | SELESAI | 2026-10-25 | Indikator billboard (emoji pengunjung, tanda seru staf) menembus blok via textSeeThrough dan penyembunyian tanda seru untuk zookeeper yang sedang bersih-bersih atau belum punya satwa binaan |
| 20 | Subtitle Ungu, Indikator Overhead Komprehensif & Evaluasi Habitat | SELESAI | 2026-10-25 | Subtitle kategori diet ungu di hologram, indikator overhead 5 stat (health, hunger, thirst, clean, thermal LSO api & es getar berjejer), dan perbaikan komprehensif scanner bunga, dasar akuatik, & terumbu karang |
| 21 | Efek Negatif Stat Satwa & In-World Hologram Progress Bar Mandi Player | SELESAI | 2026-10-25 | Efek Minecraft negatif saat stat 0 / tidak nyaman (Poison, Hunger, Slowness, Weakness, Freezing/Heat), in-world hologram progress bar 2 tahap saat watering can & brush ditekan sampai full, dan penghapusan teks (Basah) pada status kebersihan satwa |
| 22 | Format Kebutuhan Habitat Hologram & Scrolling Panel Pengelolaan Satwa | SELESAI | 2026-10-25 | Format teks kebutuhan habitat dibuat 100% sama dengan hologram (Kebutuhan Habitat: Terpenuhi / Kebutuhan Habitat: dengan rincian merah), sistem scrolling dinamis dan scissor clipping pada panel detail kanan agar seluruh makanan yang disukai dan statistik tetap dapat dilihat secara lengkap |
| 23 | Tampilan Persentase Statistik Satwa UI Pengelolaan | SELESAI | 2026-10-25 | Menambahkan persentase di samping kanan deretan icon (HP, Lapar, Haus, Kebersihan) dengan pewarnaan dinamis persis seperti hologram (HologramBarRenderer.getStatusColor: putih >50%, kuning <=50%, merah <=15%) |
| 24 | Ikon Brush Hologram, Penyesuaian Laju Mandi & Tampilan Persentase HP UI | SELESAI | 2026-10-25 | Tampilan persentase HP murni di UI kelola satwa (menghapus 97/160 HP), penggantian ikon sikat hologram dengan ikon item Brush Minecraft asli, perlambatan progress mandi (60 tick), toast penolakan siram berulang ("Satwa sudah disiram, segera bersihkan dengan brush!"), serta perbaikan fallback resolusi translation key zookeeper_assigned_toast |
| 25 | Penindasan Notifikasi Butuh Air Saat Satwa Sudah Bersih | SELESAI | 2026-10-25 | Menghapus dan menekan notifikasi 'Wet the animal first with a watering can!' / 'need_water' pada aksi sikat player saat satwa sudah selesai dimandikan atau kebersihannya sudah mencapai 100% |
| 26 | Hologram Icon Multi-Row (Kelola Satwa Style Tanpa Persen) | SELESAI | 2026-10-25 | Mengganti icon tunggal + persentase pada in-world hologram dengan deretan 10 icon persis seperti Kelola Satwa (10 hati, 10 paha pakan, 10 tetes air, 10 spons pembersih) tanpa teks persen, serta mempertahankan format suhu (termometer + °C + status) |
| 27 | Perbaikan Kenaikan Kebersihan Mandi & Do-Nothing Saat 100% Bersih | SELESAI | 2026-10-25 | Memperbaiki kenaikan kebersihan 100% pada CompletePlayerBathingPayload server-side (menghilangkan ketergantungan isWet saat paket tahap 2 tiba), mengirim snapshot & sync instan, serta menerapkan perilaku do-nothing (tanpa hologram progress bar, tanpa notifikasi need_water/toast, hanya partikel/animasi siram lokal) saat kebersihan satwa sudah 100% |

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

- Penyempurnaan Hologram & Perbaikan AI Minum Air (7 Poin):
  1. Suhu/Temperature dihapus dari daftar Kebutuhan Habitat. Status suhu dipindahkan langsung ke baris Suhu ("Nyaman" warna hijau 0xFF55FF55, "Butuh Heater" / "Butuh Cooler Box" warna merah 0xFFFF5555).
  2. Background hologram menggunakan styling kotak tooltip vanilla Minecraft lengkap dengan border gradient ungu-biru (0x505000FF -> 0x5028007F) dan background gelap (0xF0100010).
  3. Kebutuhan Habitat & Bioma kini hanya menampilkan item yang tidak terpenuhi (merah). Bila semua kebutuhan terpenuhi, hanya menampilkan satu baris hijau "Kebutuhan Habitat: Terpenuhi".
  4. Seluruh ikon stat (Heart, Hunger, Thirst, Cleanliness, Thermometer) diperbaiki dengan rendering dua sisi (two-sided quad) sehingga tidak lagi ter-cull oleh GPU, dan seluruh label stat menggunakan bahasa Indonesia murni (Kesehatan, Lapar, Haus, Kebersihan, Suhu).
  5. Nama bioma disajikan dalam bahasa Inggris Title Case dan menyebutkan seluruh bioma yang cocok untuk spesies tersebut jika bioma belum cocok.
  6. Perbaikan AI AnimalDrinkWaterGoal: hanya dipasang pada satwa yang habitatnya membutuhkan air (requiresWater), dilengkapi batas waktu navigasi (timeout 10 detik), cooldown minum 5 menit, dan deteksi instan saat berada di dalam air agar satwa tidak stuck.
  7. Heater dan Cooler Box hanya berstatus dibutuhkan saat suhu musiman/lingkungan berada di luar rentang nyaman satwa.
  8. Jangkauan deteksi air diperluas dari 8 blok menjadi 24 blok (radius 24, vertikal -6..6) dan mencakup Feeding Trough yang terisi air, serta radius AI mencari air diperluas menjadi 24 blok agar tidak mudah "missing water".
  9. Syarat pasangan (Companion) kini dikhususkan murni untuk hewan sosial/kawanan (singa, gajah, serigala, lumba-lumba, hewan ternak, dll). Hewan soliter (harimau, beruang, buaya, alligator snapping turtle, ular, platipus, komodo, dll) tidak lagi membutuhkan pasangan ("NA") dan tidak memunculkan indikator merah.

- Perbaikan Hologram UI, Oklusi, Termal Musim, dan Model Blok 3D:
  1. Ikon temperatur diganti dengan dial termometer resmi dari Legendary Survival Overhaul (16x16 pixel art circular thermometer).
  2. Nama satwa diperbesar (skala 1.25f), ikon makanan favorit diperbesar (skala 12.0f, spasi 16.0f), dan seluruh teks/elemen hologram di-align ke kiri (leftX = minX + 8.0f).
  3. Bioma yang cocok diformat sebagai bullet points menurun ke bawah saat bioma tidak terpenuhi.
  4. Posisi hologram di-offset ke depan ke arah kamera (forward offset hingga bbWidth * 0.55) dan ketinggian dinaikkan sehingga tidak terpotong atau tertutup badan, belalai, maupun tanduk mob.
  5. Perhitungan suhu musiman Fabric Seasons diintegrasikan secara presisi: satwa savana/jungle nyaman di musim panas dan membutuhkan Heater saat suhu anjlok di musim dingin.
  6. Model 3D Heater dan Cooler Box diadopsi dari LSO Blockbench lengkap dengan pelat atas, dan didaftarkan ke BlockRenderLayerMap sebagai cutout sehingga transparansi kisi dan rongga blok tampil sempurna.

- Perbaikan Komprehensif Kebutuhan Habitat & AI Minum Satwa (4 Poin):
  1. Kebutuhan Pohon dan Air Satwa: `HabitatEvaluator` kini secara aktif mengevaluasi `profile.requiredBlocks()` (seperti Pepohonan, Air, Bambu, Pasir, Salju) dan menyinkronkan daftar kebutuhan yang belum terpenuhi (`unmet`) ke client melalui payload network `InspectionSnapshotPayload`. Hologram kini menampilkan baris merah (contoh: "• Butuh Pepohonan", "• Butuh Air", "• Butuh Bambu") saat kandang belum memiliki blok tersebut, dan otomatis hilang begitu blok diletakkan.
  2. Eliminasi Teks Merah Bioma "Any": `HabitatProfileLoader` kini memiliki sistem klasifikasi fallback dinamis `getDynamicFallbackProfile` berbasis kata kunci ID entitas sehingga `getProfile(entity)` tidak pernah lagi bernilai `null`. Hewan yang cocok dengan semua bioma atau memiliki fallback universal otomatis berstatus `biomeOk = true`, dan teks "• Bioma yang cocok: - Any" tidak akan pernah lagi ditampilkan dalam warna merah.
  3. Pemenuhan Tanah Alami ("Butuh Tanah Alami"): Pemindaian blok di `HabitatEvaluator.scanSurroundingBlocks` diperluas untuk mencakup tag `#zootycoon:natural_ground`, `#minecraft:dirt`, `#minecraft:sand`, `#minecraft:terracotta`, serta blok langsung seperti `dirt`, `podzol`, `coarse_dirt`, `grass_block`, `gravel`, `mud`, `clay`, dan `snow`. Ambang batas pemenuhan disesuaikan menjadi minimal 4 blok di dalam radius kandang.
  4. AI Mandiri Satwa Mencari & Meminum Air: Batasan sepihak `requiresWater` pada `AnimalDrinkWaterGoal` dicabut sehingga seluruh satwa darat terdaftar (`PathfinderMob` non-akuatik) dapat secara mandiri mencari air ke kolam atau feeding trough berisi air saat tingkat kehausan turun (thirst <= 60). Radius pencarian 24 blok, jarak toleransi minum 2.5 blok, batas waktu gerak 10 detik, dan cooldown minum 5 menit mencegah satwa mengalami stuck atau loop navigasi.

- Penyesuaian Ketinggian Hologram & Animasi Zoom Ease Screen Lock:
  1. Penyesuaian Posisi Hologram: Ketinggian hologram in-world diturunkan dari yang sebelumnya melayang 2+ blok di atas kepala satwa menjadi sejajar ketinggian pandang mata alami (`yOffset = Math.max(0.65, eyeHeight + 0.15)`), serta batas vertikal kartu (`startY` dan `maxY`) kini simetris di sekitar titik tengah (`-halfH` ke `+halfH`). Hologram kini tepat berada di depan pandangan satwa tanpa membuat pemain mendongak ke langit.
  2. Animasi Zoom Ease Smooth & Screen Lock: Ketika pemain menahan klik kanan dengan Magnifying Glass pada satwa terdaftar, target dikunci (`InspectionTargetTracker`) dan hologram bertransisi mulus menggunakan kurva interpolasi cubic ease-in-out (`smoothEase`) dari posisi dunia menuju tepat di tengah layar kamera (`0, 0, -1.35f`). Hologram terkunci stabil di tengah layar tanpa bergoyang atau terpengaruh gerakan mob. Saat klik kanan dilepas, animasi ease kembali memutar transisi secara halus mengembalikan posisi hologram atau menghilangkannya jika pemain tidak lagi membidik satwa tersebut.

- Animasi Fade-Out Hologram, Pemulihan Toast Notifikasi & Perbaikan Zoom Kaca Pembesar:
  1. Animasi Fade Singkat Saat Hilang: Ditambahkan state `fadeAlpha` pada `ZooAnimalHologramRenderer` dan dukungan `alphaFactor` pada `HologramBarRenderer` (`applyAlpha`, `drawVanillaTooltipBackground`, `drawTexturedIcon`, `drawAtlasSprite`, dan ikon-ikon stat). Saat bidikan crosshair terlepas atau kunci klik kanan selesai, hologram tidak lagi langsung lenyap mendadak (pop), melainkan memudar secara mulus dan responsif dalam durasi singkat (~0.18 detik / speed 5.5f).
  2. Pemulihan Toast Notifikasi Pindai & Pendaftaran: Toast notifikasi dipulihkan sesuai permintaan pengguna. Pemindaian satwa terdaftar memunculkan toast "Berhasil memindai %s!" via `MagnifyingGlassItem`, dan pendaftaran satwa baru memunculkan toast "%s berhasil didaftarkan ke kebun binatang!" via `ZooAnimalHungerManager`.
  3. Eliminasi Animasi Pukul/Swing Mob & Aktivasi Langsung Zoom Scoping: Diperbaiki bug di mana klik kanan pada mob memicu animasi pukul/ayun lengan (`player.swing(hand)`) dan membatalkan scoping:
     - `UseEntityCallback` di `ZooTycoonMod` kini mengembalikan `InteractionResult.PASS` untuk item pemindai sehingga klik kanan tidak ditahan atau dipukul oleh callback entitas.
     - `MagnifyingGlassItem.interactLivingEntity` memanggil `player.startUsingItem(hand)` secara langsung dan mengembalikan `InteractionResult.CONSUME` (`shouldSwing() == false`), menghilangkan ayunan tangan dan langsung mengaktifkan pose spyglass.
      - Ditambahkan `markRegisteredLocally` pada `InspectionTargetTracker` dan fallback `mc.crosshairPickEntity` pada `ZooAnimalHologramRenderer` sehingga penguncian target dan animasi zoom ease ke tengah layar berjalan instan tanpa jeda jaringan.

- Perbaikan Oklusi Hologram Berbasis Z-Index (Tanpa Menggeser Posisi):
  1. Eliminasi Pergeseran Posisi Koordinat (`forwardOffset`): Blok komputasi `forwardOffset` (yang sebelumnya menggeser koordinat X dan Z ke depan ke arah kamera) dicabut sepenuhnya dari `ZooAnimalHologramRenderer`. Posisi 3D hologram tetap terpasang alami di atas kepala satwa tanpa melayang ke depan atau bergeser saat dipandang dari sudut berbeda.
  2. Lapisan Render See-Through (`NO_DEPTH_TEST`): Seluruh elemen rendering hologram dialihkan ke render type see-through vanilla yang menonaktifkan depth test OpenGL (`NO_DEPTH_TEST`):
     - Background tooltip vanilla: `RenderType.textBackgroundSeeThrough()`
     - Ikon stat & tekstur: `RenderType.textSeeThrough(...)`
     - Seluruh teks & label: `Font.DisplayMode.SEE_THROUGH`
  3. Jaminan Layering (Z-Index): Karena OpenGL depth testing dinonaktifkan, urutan render (`draw order`) menjamin kartu hologram selalu tampil di lapisan paling depan (di atas tubuh, belalai, moncong, maupun tanduk satwa) tanpa ada bagian kartu yang terpotong atau tertutup geometri mob.

- Penghapusan Toast Pindai, Partikel, dan Efek Suara Kaca Pembesar:
  1. Penghapusan Toast Pemindaian: Pemanggilan toast `zootycoon.animal.scan_toast` ("Berhasil memindai %s!") pada `MagnifyingGlassItem.scanAndRegister` telah dihapus.
  2. Penghapusan Efek Partikel & Suara: Suara `SoundEvents.EXPERIENCE_ORB_PICKUP` dan partikel `ParticleTypes.HAPPY_VILLAGER` saat memindai satwa telah dihapus.
  3. Scope Terisolasi Ketat: Perubahan diisolasi murni pada `MagnifyingGlassItem.java` tanpa mengubah fungsionalitas lain.

- Pendaftaran Otomatis Field Guide & Pemulihan Depth Testing Hologram (Bottom-Anchored):
  1. Pendaftaran Otomatis Field Guide ke Zoo:
     - Mengubah injeksi paket `FieldGuideScanUnlockMixin.handleServer` dari `@At("TAIL")` ke `@At("HEAD")`, memastikan pendaftaran satwa ke kebun binatang dieksekusi seketika paket tiba di server tanpa terhambat verifikasi scan eksternal.
     - Meningkatkan `FieldGuideScanManagerMixin.completeScan` di sisi client untuk segera mengirimkan `ScanAnimalPayload`, menambahkan tag `zoo_registered`, dan menandai target lokal pada `InspectionTargetTracker`.
     - Kode `MagnifyingGlassItem.java` sama sekali tidak disentuh untuk menjaga pemisahan fungsi.
  2. Pemulihan Border Ungu Gradien Tooltip & Depth Testing:
     - Memulihkan warna border tooltip vanilla pada `HologramBarRenderer` dengan gradien ungu-ke-nila yang tegas (`TOOLTIP_BORDER_TOP = 0xF05000FF` dan `TOOLTIP_BORDER_BOTTOM = 0xF028007F`).
     - Mengembalikan render type dan font display mode dari `SEE_THROUGH` kembali ke `NORMAL` dan `RenderType.textBackground()` / `RenderType.text(...)`. Dengan ini, hologram tidak lagi menembus langit, dinding/balok, maupun mob lain di sekitarnya.
  3. Eliminasi Oklusi Tubuh Mob dengan Bottom-Anchored Hologram:
     - Mengubah titik jangkar vertikal kartu hologram dari semula centered (`-halfH` ke `+halfH`) menjadi bottom-anchored (`startY = -totalBoxHeight`, `maxY = 0.0f`).
     - Mengatur ketinggian jangkar dasar (`yOffset = Math.max(entity.getBbHeight() + 0.08, eyeHeight + 0.15)`). Bagian bawah kartu berada tepat 8 cm di atas titik tertinggi bounding box satwa dan seluruh badan kartu menjulang ke atas ke ruang kosong.
     - Geometri tubuh, leher, dada, maupun moncong satwa secara matematis tidak pernah memotong bidang kartu hologram, dan posisinya tetap dekat dan pas tepat di atas satwa ("tidak terlalu atas").
     - Penyesuaian animasi zoom ease: saat klik kanan ditahan (`lockProgress` menuju 1.0f), kartu secara otomatis menginterpolasi translasi vertikal (`transY`) ke `-halfHWorld`, menempatkan kartu tepat di tengah layar kamera.

- Ikon Makanan 2D Flat Unshaded & Penyesuaian Kecerahan Border Ungu:
  1. Ikon Makanan 2D Flat (Tanpa Shading & Tidak Terpotong):
     - Menghapus rendering 3D `ItemRenderer.renderStatic` yang sebelumnya memicu shading bayangan 3D dan pemotongan kedalaman (clipping) akibat geometri model 3D menembus pelat background.
     - Menambahkan helper `HologramBarRenderer.drawItemIcon2D` yang mengambil sprite partikel 2D murni (`model.getParticleIcon()`) dan merendernya sebagai quad 2D datar murni pada `RenderType.text(sprite.atlasLocation())` di koordinat `z = -0.005f`.
     - Ikon makanan kini tampil datar 2D, terang penuh (unshaded) tanpa bayangan, tidak terpotong (uncropped), dan memudar secara mulus bersama `fadeAlpha`.
  2. Penyesuaian Kecerahan Border Ungu:
     - Mengubah nilai alpha border pada `HologramBarRenderer` dari `0xF0` (terlalu terang) kembali ke nilai vanilla `0x50` (`TOOLTIP_BORDER_TOP = 0x505000FF` dan `TOOLTIP_BORDER_BOTTOM = 0x5028007F`).
     - Border ungu gradien kini tampil halus, semi-transparan (~31% opacity), dan nyaman dipandang mengikuti estetika tooltip vanilla Minecraft.

- Optimasi Performa Render Client Hologram:
  1. Eliminasi Pemanggilan Redundan Raycast:
     - Menghapus pemanggilan `InspectionTargetTracker.tick(mc)` dari dalam `ZooAnimalHologramRenderer.render`, karena deteksi target raycast dan collision test sudah berjalan setiap tick di `ClientTickEvents.END_CLIENT_TICK`.
     - Mengoptimalkan `InspectionTargetTracker.isZooRegisteredAnimal` dengan pencarian O(1) pada `clientAnimalMap` sebelum fallback ke list `clientAnimals`.
  2. Konsolidasi Buffer Flush:
     - Menghapus pemanggilan berulang `bs.endBatch()` di tengah-tengah render kartu (setelah tooltip background, setelah ikon makanan, dan setelah ikon stat).
     - Menyatukan seluruh buffer draw call (background, teks, ikon stat, ikon makanan) ke dalam satu panggilan flush tunggal `bs.endBatch()` di akhir rendering kartu, mengeliminasi GPU pipeline stalls dan draw call splitting.
  3. Eliminasi Refleksi Musim & Kalkulasi Temperatur:
     - Menghapus pemanggilan `ZooClimate.getEffectiveTemperature(entity)` dan refleksi runtime ke `FabricSeasonsCompat` di setiap frame render.
     - Membaca langsung musim dan temperatur dari paket `InspectionSnapshotPayload` (`InspectionPayloads.getLatestClimate`) yang dikirim dari server, memetakan kategori temperatur secara instan tanpa refleksi.
  4. Caching Lokal Parsing Habitat & Bioma Per Snapshot:
     - Menerapkan `SNAPSHOT_RENDER_CACHE` untuk menyimpan hasil parsing `HabitatParsedInfo`, teks temperatur, label status, dan itemized checklist `redNeeds`. Selama data snapshot belum berubah antar frame, tidak ada regex split (`split(";")`, `split("=")`, `split("\\|")`) maupun alokasi objek baru yang dieksekusi.
     - Menerapkan `BIOME_LIST_CACHE` untuk menyimpan format nama bioma bahasa Inggris per `HabitatProfile` agar tidak memformat ulang string bioma setiap frame.
     - Membuat label stat ("Kesehatan:", "Lapar:", "Haus:", "Kebersihan:", "Suhu:") sebagai konstanta `static final Component` untuk menghindari alokasi `Component.literal` setiap frame.

- Optimasi Client Rendering Hologram dan Target Tracker (Prompt 2):
  1. Caching Metrik Teks dan Diet Makanan:
     - Memindahkan seluruh komputasi `font.width` (label stat, nilai stat HP/Hunger/Thirst/Cleanliness/Suhu, judul entitas, status temperatur, dan checklist kebutuhan habitat) ke dalam `CachedSnapshotRenderData.build(...)` yang hanya dipanggil saat data snapshot baru diterima dari server atau terjadi perubahan status satwa.
     - Memindahkan pemanggilan `AnimalDietRegistry.getFoodsForAnimal(entity)` ke dalam `CachedSnapshotRenderData`, menghilangkan pemindaian berat item registry di setiap frame render (~100 FPS).
     - Loop render per frame murni membaca nilai dimensi, lebar teks, dan list item pakan yang sudah tercache tanpa melakukan alokasi memori baru (zero-allocation rendering).
     - Tata letak visual, ukuran ikon 2D, warna font, padding, dan billboard tooltip background vanilla Minecraft dipertahankan 100% identik tanpa perubahan.
  2. Optimasi Raycast Target Tracker:
     - Pada `InspectionTargetTracker.java`, menambahkan guard check di awal `findTarget` sehingga `level.clip` (raycast blok padat) dan `ProjectileUtil.getEntityHitResult` hanya dieksekusi apabila pemain sedang memegang item `Magnifying Glass` di main hand atau offhand.
     - Jika pemain tidak memegang `Magnifying Glass`, tracker langsung mengembalikan `null` seketika tanpa melakukan komputasi raycast apa pun.
     - Toleransi grace buffer 15 tick (`targetRetentionTicks = 15`) tetap dipertahankan agar kartu hologram stabil dan tidak berkedip saat crosshair membidik sedikit meleset dari model satwa.

- Pemulihan Sprite Heart Vanilla 2D & Rendering 3D Block Makanan (Hay Bale):
  1. Sprite Heart Vanilla 2D:
     - Mengubah `HologramBarRenderer.drawVanillaHeartIcon` untuk menggunakan sprite resmi GUI vanilla Minecraft `minecraft:hud/heart/full` via `drawAtlasSprite` dengan alpha blending, menggantikan font glyph text "❤".
     - Ikon stat kini seragam dan konsisten sebagai pixel art resmi Minecraft vanilla 9x9.
  2. Rendering Makanan 3D Vanilla untuk Blok & 2D Flat untuk Item:
     - Pada `ZooAnimalHologramRenderer.java`, menambahkan pengecekan `model.isGui3d()` pada setiap item pakan di baris makanan favorit.
     - Untuk makanan tipe blok (seperti Hay Bale pada kuda, melon, labu), digunakan `HologramBarRenderer.drawBlockIcon3D` yang memanfaatkan `ItemRenderer.renderStatic(..., ItemDisplayContext.GUI, ...)` vanilla dengan offset kedalaman Z (+8.0f) agar model 3D tidak menembus atau terpotong oleh pelat background tooltip.
     - Untuk makanan tipe item biasa (seperti apel, gandum, wortel, daging/ikan mentah), tetap menggunakan `HologramBarRenderer.drawItemIcon2D` berbasis quad flat partikel 2D sehingga sangat ringan dan hemat kinerja GPU.

- Penambahan Cerobong Asap Heater & Format Durasi Bahasa Indonesia (Menit/Detik):
  1. Model 3D Cerobong Asap Terintegrasi 1-Blok:
     - Menggabungkan 6 elemen cerobong asap (chimney tube Y:16..24, 4 pilar sudut Y:24..28, dan topi pelat Y:28..30) langsung ke dalam model 1-blok `heater_off.json` dan `heater_on.json` tanpa memerlukan blok atas tambahan.
     - Menyesuaikan transformasi display GUI (`translation: [0, -3.5, 0]`, `scale: [0.45, 0.45, 0.45]`) dan ground agar model utuh beserta cerobongnya tampil proporsional di slot inventory.
     - Memperbarui VoxelShape di `ZooHeaterBlock.java` dengan penggabungan bentuk cerobong (`Block.box(3, 16, 3, 13, 30, 13)`) sehingga hitbox dan seleksi blok akurat hingga ujung cerobong.
     - Menambahkan partikel asap cerobong `CAMPFIRE_COSY_SMOKE` pada ketinggian cerobong `pos.getY() + 1.9` saat heater menyala (LIT).
  2. Format Durasi Bahasa Indonesia (Menit & Detik):
     - Mengimplementasikan helper `formatDuration(int totalSeconds)` pada `ZooHeaterBlock.java` dan `ZooCoolerBlock.java`.
     - Teks actionbar / popup saat pengisian bahan bakar atau pengecekan sisa waktu kini diformat ramah pengguna dalam satuan menit dan detik (contoh: "2 menit", "4 menit 30 detik", atau "45 detik") alih-alih angka detik mentah.
     - Menyesuaikan string terjemahan `id_id.json` dan `en_us.json` agar placeholder `%s` langsung menampilkan teks durasi lengkap.


- Integrasi Kategori Diet Satwa & Metode Pakan (Hologram & AI Lapar):
  1. Klasifikasi Kategori Diet & Metode Pakan di AnimalDietRegistry:
     - Menambahkan label resmi metode pakan: `TROUGH` ("Perlu Palung Makanan") dan `THROWN_MEAT` ("Lempar Makanan").
     - Menambahkan klasifikasi kategori diet: `Herbivora`, `Karnivora`, `Omnivora`, `Frugivora`, `Granivora`, `Insektivora`, `Piscivora`, `Pemakan Jamur`, `Pemakan Mineral`, dan `Nektarivora`.
     - Memetakan 40 spesies satwa dari Dokumen Google Doc Bagian 1, 2, dan 3 (Naturalist, Alex's Mobs, Wilder Nature, Vanilla) ke registry lengkap dengan kategori, jenis pakan, dan metode pakan.
     - Menyediakan helper client-safe `AnimalDietRegistry.getDietCategorySubtitle(LivingEntity)` yang menghasilkan format teks e.g. "Herbivora (Perlu Palung Makanan)" atau "Karnivora (Lempar Makanan)".
  2. Tampilan Subtitle di Bawah Nama Satwa pada Hologram:
     - Menambahkan baris subtitle tepat di bawah nama satwa pada `ZooAnimalHologramRenderer` (posisi Y = curY + 13.0f, warna abu-abu lembut 0xFFAAAAAA, font NORMAL, full bright).
     - Menyesuaikan `HEADER_HEIGHT` menjadi 25.0f dan lebar kartu `boxWidth` secara dinamis menghitung lebar subtitle pakan agar tidak terpotong.
  3. Pemisahan AI Lapar Satwa (Palung Pakan vs Pakan Dilempar):
     - Satwa bertipe `FeedingType.TROUGH` hanya mencari dan memakan pakan dari `ZooFeedingTroughBlock`.
     - Satwa predator / karnivora bertipe `FeedingType.THROWN_MEAT` tidak lagi menuju ke palung pakan kayu, melainkan secara aktif mendeteksi dan mendekati item pakan yang dilempar/terjatuh di tanah (`ItemEntity`) dalam radius 16 blok, memakannya hingga kenyang (+35 rasa lapar), memainkan partikel hati dan suara mengunyah.


- Perbaikan AI Memakan Pakan Dilempar (Dropped Food Consumption Fix):
  1. Penghapusan Pembatas Status Kenyang pada Pakan Dilempar:
     - Mencabut kondisi `if (feeder == null && current >= MAX_HUNGER) return false;` pada `ZooAnimalHungerManager.feedAnimal`. Satwa kini dapat menikmati camilan atau pakan yang dilempar pemain di tanah sekalipun tingkat kelaparan masih penuh (100%), memulihkan 2 HP bila terluka dan memunculkan partikel hati serta efek suara makan.
  2. Eliminasi Modulo Lock (% 25) pada handleUniversalAnimalNeeds:
     - Menghapus pengecekan modulo `(mob.getId() + level.getGameTime()) % 25 != 0` yang sebelumnya menyebabkan 80% entitas terkunci secara matematis karena berjalan di dalam blok tick kelipatan 20.
  3. Pembuatan AnimalEatDroppedFoodGoal (AI Goal Dedicated):
     - Membangun AI Goal prioritas tinggi (`AnimalEatDroppedFoodGoal`) pada `net.zootycoon.animal.ai` yang terpasang pada seluruh `PathfinderMob` terdaftar. AI Goal ini secara aktif memindai pakan jatuh dalam radius 16 blok, menavigasikan mob menuju pakan dengan kecepatan 1.25x, dan melahap pakan saat tiba di dekatnya dengan jeda cooldown 1.5 detik.
  4. Deteksi Pakan Dekat Instan (Proximity Pickup):
     - Menambahkan deteksi instan pakan di kaki satwa (`mob.getBoundingBox().inflate(1.6D, 1.0D, 1.6D)`) sehingga pakan yang dilempar langsung ke arah satwa langsung dimakan tanpa jeda.

- Perbaikan Komprehensif Feeding Trough AI & Reach Distance (Ostrich & Seluruh Satwa):
  1. Akar Masalah Terpecahkan:
     - Formula jangkauan lama `(bbWidth * 0.5) + 1.2` menyebabkan mob bertubuh ramping seperti Ostrich (lebar 0.7m) hanya memiliki jangkauan 1.55m (`1.55^2 = 2.40`), sedangkan jarak berhenti navigasi Minecraft ke feeding trough di blok sebelah adalah ~1.6m - 1.8m (`jarak^2 = 2.56 - 3.24`), sehingga kondisi makan tidak pernah terpenuhi. Berbeda dengan Zebra (lebar 1.4m) yang memiliki toleransi 1.9m (`1.9^2 = 3.61`) sehingga Zebra berhasil makan. Seluruh mob dengan lebar < 1.2m (burung, kelinci, bebek, kura-kura, rusa, kasuari, emu, kangguru, unta, penguin) mengalami masalah serupa.
     - Satwa bertubuh tinggi (Ostrich tinggi 2.4m, Jerapah 4.5m, Unta 2.8m, Gajah 3.2m) melihat ke palung pakan di tanah dengan sudut pitch menunduk 50-70 derajat. Pengecekan lama `dot >= 0.5` gagal total karena `cos(pitch) <= 0.5`, sehingga dot product vektor pandang horizontal tidak pernah mencapai 0.5 walau satwa menatap persis ke palung.
  2. Implementasi AnimalEatFromTroughGoal (AI Goal Mandiri):
     - Membangun kelas AI `AnimalEatFromTroughGoal` pada `net.zootycoon.animal.ai` yang meng-extend `MoveToBlockGoal`.
     - Toleransi jarak adaptif `acceptedDistance = Math.max(2.6D, (mob.getBbWidth() * 0.5D) + 1.6D)` dengan verifikasi ganda `closerThan` dan `horizDistSq <= maxDist * maxDist && vertDist <= 3.0D`.
     - Integrasi instan ke cache koordinat palung `ZooFeedingTroughBlock.KNOWN_TROUGHS` untuk pathfinding tanpa lag.
     - Menghapus ketergantungan kalkulasi pitch vector yang cacat.
  3. Penyelarasan ZooAnimalHungerManager:
     - Memperbaiki perhitungan jangkauan makan palung, minum palung air, dan minum air alami pada `handleUniversalAnimalNeeds`.
     - Mendaftarkan `AnimalEatFromTroughGoal.attach(pMob)` pada `tickServer` dan `setTagged`.
  4. Perluasan Ragam Makanan Palung pada AnimalDietRegistry:
     - Menambahkan variasi benih (pumpkin, beetroot, torchflower), jerami (hay block), gandum, dan apel untuk burung unta, emu, kasuari, jerapah, gajah, rusa, bebek, kura-kura, unta, dan kangguru.

- Spesifikasi Jenis Pohon Habitat Berbahasa Inggris (Fase 17):
  1. Penggantian Istilah Generic & Bahasa Daerah:
     - Mengubah kebutuhan template `jungle.json` dari "Pepohonan" menjadi "Pohon Jungle" (`jungle_wood`).
     - Mengubah kebutuhan template `forest.json` dari "Pepohonan" menjadi "Pohon Oak" (`oak`).
     - Mengubah kebutuhan spesies `giraffe.json` dari "Pohon Akasia" menjadi "Pohon Acacia" (`acacia`).
     - Mengubah `HabitatProfileLoader.java` untuk menghapus "Pohon Rimba", "Pohon Akasia", dan "Pohon Spruce / Cemara" menjadi "Pohon Jungle", "Pohon Acacia", dan "Pohon Spruce".
     - Mengubah `AnimalDietRegistry.java` dari "Predator Rimba" menjadi "Predator Jungle".
  2. Evaluasi & Scanning Blok Kayu Spesifik pada HabitatEvaluator:
     - Menambahkan pemindaian blok dan daun spesifik untuk oak, birch, dark_oak, mangrove, dan cherry di `scanSurroundingBlocks`.
     - Mendukung tipe kebutuhan `jungle` / `jungle_wood`, `oak`, `birch`, `dark_oak`, `mangrove`, dan `cherry` di `evaluate`.
     - Sanitasi dinamis pada label kebutuhan pohon yang belum terpenuhi (`unmet`) agar bila terdapat sisa template atau fallback bertuliskan "Pepohonan", "Rimba", "Akasia", atau "Cemara", label otomatis terformat rapi menjadi "Pohon Jungle", "Pohon Oak", "Pohon Acacia", atau "Pohon Spruce".

- Perbaikan Mandi Satwa Zookeeper & Kenaikan Kebersihan 100% (Fase 18):
  1. Akar Masalah Bersih-Bersih Zookeeper Sebelumnya:
     - Logika mandi sebelumnya hanya berjalan di dalam `handleStaffDuties` yang hanya dipanggil setiap 25 tick (`tickCount % 25 == 0`), sehingga `bathingStepTicks` hanya bertambah 1 setiap 1.25 detik. Dibutuhkan 87.5 detik durasi kedekatan tanpa henti untuk menyelesaikan tahap siram air dan sikat. Di Minecraft mob selalu bergerak setiap 2-5 detik sehingga zookeeper terus terinterupsi dan kebersihan tidak pernah naik.
     - Jarak jangkauan 2.8m center-to-center terlalu sempit untuk satwa dengan hitbox lebar (Zebra, Jerapah, Gajah, Beruang) sehingga zookeeper terjebak di luar jarak toleransi.
  2. Implementasi Ticking Aktif & Jarak Adaptif:
     - `tickBathingDuty` kini dijalankan setiap tick (20 TPS) saat `currentDutyState` adalah `"BATHING"`. Tahap siram air berlangsung mulus 30 tick (1.5 detik) dan tahap sikat berlangsung 35 tick (1.75 detik). Total pemandian hanya membutuhkan 3.25 detik.
     - Jarak jangkauan dihitung adaptif `Math.max(3.2, (assignedAnimal.getBbWidth() * 0.5) + 2.0)`.
     - Kebersihan satwa dipulihkan penuh ke 100% (`ZooAnimalHungerManager.MAX_CLEANLINESS`), durability sikat berkurang 1, dan pembaruan dikirimkan secara instan ke hologram dan sync client.
  3. Dialog & Proteksi Penyerahan Alat Mandi:
     - Zookeeper hanya meminta 2 alat mandi (penyiram air dan sikat) jika satwa kotor (`cleanliness <= 60`) dan zookeeper belum memiliki alat mandi.
     - Jika satwa bersih (`cleanliness > 60`), zookeeper tidak meminta alat mandi, dan `handleBathingToolsTransfer` menolak penyerahan alat jika satwa tidak kotor.
     - Saat sikat rusak/habis, inventaris zookeeper direset bersih sehingga zookeeper tidak memiliki alat awal apa pun ("jangan kasih zookeeper apapun") hingga satwa kotor kembali.

- Indikator In-World Tembus Blok & Filter Peringatan Zookeeper (Fase 19):
  1. Rendering Indikator Tembus Blok (X-Ray / See-Through):
     - Memperbarui `renderBillboard` pada `ZooWorldIndicatorRenderer` untuk menggunakan `RenderType.textSeeThrough(texture)`.
     - Tipe render ini menonaktifkan depth test (mirip nametag vanilla), memungkinkan ikon emoji mood pengunjung, tanda seru kebutuhan staf, dan indikator lapar terlihat menembus blok/dinding dengan tingkat kecerahan penuh (`0xF000F0`).
     - Menghadirkan two-sided quad (sisi depan dan belakang) dengan format verteks `DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP` dan pemanggilan `bs.endBatch()` di akhir pass untuk memastikan visual ter-flush instan di fase rendering in-world.
  2. Filter Tanda Seru Zookeeper:
     - Menambahkan sinkronisasi status hewan binaan `HAS_ASSIGNED_ANIMAL` (`SynchedEntityData<Boolean>`) pada `ZooZookeeperEntity` sehingga sisi client dapat mengetahui apakah zookeeper memiliki satwa asuh.
     - Menyembunyikan tanda seru di atas kepala zookeeper jika zookeeper belum memiliki satwa binaan (`!keeper.hasAssignedAnimal()`).
     - Menyembunyikan tanda seru jika zookeeper sedang melakukan tugas kebersihan atau mandi (`keeper.isSweeping()`, `keeper.getBathingAction() != 0`, memegang sikat, atau memegang penyiram air).

- Subtitle Ungu, Indikator Overhead Komprehensif & Evaluasi Habitat (Fase 20):
  1. Subtitle Kategori Pakan Berwarna Ungu Lavender di Hologram:
     - Memperbarui `AnimalDietRegistry.getDietCategorySubtitleComponent` untuk menyusun `Component` terformat: bagian kategori hewan (`Herbivora`, `Karnivora`, dll) diberi warna ungu lavender lembut (`0xFFBD6DFF`), sedangkan metode makan (` (Perlu Palung Makanan)` atau ` (Lempar Makanan)`) tetap abu-abu lembut (`0xFFAAAAAA`).
     - `ZooAnimalHologramRenderer` merender teks bertingkat ini dengan pelestarian warna gaya component dan alpha fade smooth.
  2. Indikator Overhead Komprehensif di Atas Kepala Satwa (ZooWorldIndicatorRenderer):
     - Memperluas indikator visual overhead satwa terdaftar: Kesehatan (Heart), Lapar (Hunger Indicator), Haus (`thirst.png`), dan Kebersihan (`cleanliness.png`) muncul saat nilai status <= 50%.
     - Menambahkan indikator termal musiman LSO: Suhu Panas (ikon api `temp_fire.png` dengan denyut lembut) dan Suhu Dingin (ikon kristal es `temp_ice.png` dengan getaran menggigil horizontal `shiverX/shiverY`).
     - Seluruh alert aktif ditata berjejer secara horizontal simetris di atas kepala satwa tanpa tumpang tindih.
  3. Pemenuhan Komprehensif Habitat (Bunga, Dasar Kerikil/Pasir, & Karang di HabitatEvaluator):
     - Tanaman Bunga: Memperluas deteksi scanner mencakup tag konvensi mod `#c:flowers`, bunga kecil dan tinggi vanilla, `flowering_azalea`, `flowering_azalea_leaves`, `pink_petals`, `spore_blossom`, `torchflower`, `pitcher_plant`, `chorus_flower`, dan pot bunga.
     - Terumbu Karang: Memperbaiki bug di mana blok kubus karang terlewat dengan menambahkan `BlockTags.CORAL_BLOCKS`, varian dead coral blocks, tall seagrass, kelp, dan sea lantern.
     - Dasar Lautan / Kerikil: Memperluas jangkauan pemindaian vertikal ke bawah dari minDy -6 menjadi -16 sehingga dasar akuarium/kolam dalam tetap terdeteksi oleh satwa yang berenang di permukaan, serta memasukkan lantai oseanarium seperti prismarine, dark prismarine, prismarine bricks, dan sea lantern ke dalam `natural_ground`.

## Bukti Selesai
- Seluruh kode terkompilasi bersih tanpa error dengan `compileJava` dan `build`.
- File jar `zootycoon-1.0.0.jar` telah tersalin ke direktori mods profil Modrinth.
- Kategori diet pada subtitle hologram satwa tampil berwarna ungu lavender lembut (`0xFFBD6DFF`) dan metode pakan tetap abu-abu (`0xFFAAAAAA`).
- Indikator overhead satwa kini mendukung 5 stat (Health, Hunger, Thirst, Cleanliness, Suhu Api/Es getar) berjejer horizontal.
- Scanner blok habitat kini mengenali bunga mod/variatif, blok kubus karang, dan dasar kerikil/pasir/prismarine di kedalaman air hingga 16 blok.

## Blocker
- Tidak ada blocker.

## File yang diubah
- src/main/java/net/zootycoon/animal/AnimalDietRegistry.java
- src/main/java/net/zootycoon/client/render/ZooAnimalHologramRenderer.java
- src/main/java/net/zootycoon/client/render/ZooWorldIndicatorRenderer.java
- src/main/java/net/zootycoon/animal/habitat/HabitatEvaluator.java
- src/main/resources/assets/zootycoon/textures/gui/icons/temp_fire.png
- src/main/resources/assets/zootycoon/textures/gui/icons/temp_ice.png
- docs/hologram/ASSETS.md
- docs/hologram/PROGRESS.md

## Butuh izin user
- Tidak ada.

## Belum terverifikasi
- Pengujian visual in-game oleh pengguna saat satwa berada di kandang berbunga, kandang berdasar kerikil, dan oseanarium berkarang.
