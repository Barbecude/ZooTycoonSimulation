# Audit Referensi Eksternal Arsitektur Hologram, API Musim, dan Lisensi Aset

Dokumen ini merupakan hasil audit teknis mendalam terhadap kode sumber dan berkas mod referensi eksternal untuk perancangan fitur Animal Inspection Hologram pada Zoo Tycoon (Minecraft 1.21.1 Fabric). Seluruh temuan disertai bukti lokasi berkas dan nomor baris faktual.

---

## A. Farm & Charm (Let-s-Do-Collection/FarmAndCharm)

Audit dilakukan pada repositori Farm & Charm cabang 1.21.1. Mod ini mengimplementasikan in-world overlay 3D di atas kepala hewan ternak saat pemain memakai celana dungarees dan mengarahkan crosshair ke hewan.

### 1. Bukti Path dan Baris Komponen Kunci

#### a. net.satisfy.farm_and_charm.core.mixin.LivingEntityRendererMixin
Lokasi berkas: `common/src/main/java/net/satisfy/farm_and_charm/core/mixin/LivingEntityRendererMixin.java`
- Baris 19-20: Target mixin adalah `LivingEntityRenderer.class`.
- Baris 22-23: Injeksi pada akhir render entitas (`@Inject(method = "render(...)", at = @At("TAIL"))`).
- Baris 24-27: Filter entitas agar hanya memproses `Animal` dewasa dari jenis sapi, babi, domba, dan ayam.
- Baris 31-33: Deteksi crosshair vanilla secara langsung:
  `Minecraft mc = Minecraft.getInstance();`
  `if (mc.crosshairPickEntity != animal || mc.player == null) return;`
  Penggunaan `mc.crosshairPickEntity` memastikan deteksi mengikuti raycast vanilla yang otomatis terhalang blok padat dan langsung bernilai entitas lain/null saat crosshair beralih, tanpa menahan target.
- Baris 34-40: Pengecekan perlengkapan pemain (hand item / armor guard):
  Memeriksa apakah pemain memakai item dungarees (`ObjectRegistry.DUNGAREES.get()`) pada slot armor. Jika terpenuhi, method memanggil `SaturationOverlayRenderer.render(poseStack, buffer, animal, tracker.level(), tracker.foodCounter())`.

#### b. net.satisfy.farm_and_charm.client.renderer.entity.SaturationOverlayRenderer
Lokasi berkas: `common/src/main/java/net/satisfy/farm_and_charm/client/renderer/entity/SaturationOverlayRenderer.java`
- Baris 32-35: Perhitungan offset vertikal posisi hologram:
  `double yOffset = entity.getBbHeight() + 0.6;`
  `if (entity.hasCustomName()) { yOffset += 0.3; }`
  Offset ditempatkan 0.6 blok di atas bounding box hewan, dan dinaikkan tambahan 0.3 blok jika entitas memiliki nama kustom agar tidak bertumpukan dengan nameplate vanilla.
- Baris 37-41: Transformasi matriks, billboarding rotasi kamera, dan penskalaan:
  `poseStack.pushPose();`
  `poseStack.translate(0.0, yOffset, 0.0);`
  `poseStack.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());`
  `poseStack.scale(0.4f, 0.4f, 0.4f);`
  Rotasi `cameraOrientation()` memutar poseStack menghadap kamera pemain secara penuh (billboard murni di world space).
- Baris 45-69: Rendering 3D item ikon pakan secara statis di dunia:
  `itemRenderer.renderStatic(icon, ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY, poseStack, buffer, entity.level(), 1);`
  Ikon dirender sebanyak 3 buah secara horizontal (`poseStack.translate((i - 1) * 1.1, 0.0, 0.0)`) dengan tingkat kecerahan alpha sesuai level saturasi.

#### c. net.satisfy.farm_and_charm.core.network.packet.SyncSaturationPacket & SaturationTracker
Lokasi berkas:
- `common/src/main/java/net/satisfy/farm_and_charm/core/network/packet/SyncSaturationPacket.java`: Record payload dengan field `int entityId, int level, int foodCounter`.
- `common/src/main/java/net/satisfy/farm_and_charm/core/network/handler/SyncSaturationPacketClientHandler.java`: Baris 10-19 menerima packet di client dan memanggil `saturated.farm_and_charm$getSaturationTracker().clientSync(packet.level(), packet.foodCounter())`.
- `common/src/main/java/net/satisfy/farm_and_charm/core/util/SaturationTracker.java`: Menyimpan nilai saturasi, counter pakan, penghitungan penurunan berkala (decay), serta method `clientSync`.
- `common/src/main/java/net/satisfy/farm_and_charm/core/mixin/AnimalEntityMixin.java`:
  - Baris 64-86: Pada `aiStep` sisi server, mengecek apakah level saturasi berubah; jika berubah, membuat `SyncSaturationPacket` dan memanggil `PacketHandler.sendSaturationSync(packet, this)`.
  - Baris 88-116: Pada interaksi pemberian pakan (`mobInteract`), langsung memperbarui tracker dan mengirim paket sinkronisasi.
- `common/src/main/java/net/satisfy/farm_and_charm/core/network/PacketHandler.java`: Baris 54-78 menyaring pemain dalam radius 64 blok yang memakai dungarees sebelum mengirim paket melalui `NetworkManager.sendToPlayer(player, packet)`.

#### d. net.satisfy.farm_and_charm.client.gui.overlay.InfoTooltips & BlockInfoProvider
Lokasi berkas:
- `common/src/main/java/net/satisfy/farm_and_charm/client/gui/overlay/InfoTooltips.java`: Baris 13-22 menyediakan method `isHidden(boolean enabled)` yang memverifikasi konfigurasi dan mengecek item pemain:
  `Player player = Minecraft.getInstance().player;`
  `return player == null || !player.getItemBySlot(EquipmentSlot.LEGS).is(ObjectRegistry.DUNGAREES.get());`
- `common/src/main/java/net/satisfy/farm_and_charm/client/gui/overlay/WaterSprinklerInfoProvider.java`: Baris 19-22 mengimplementasikan antarmuka `BlockInfoProvider` dengan pola:
  `if (InfoTooltips.isHidden(PlatformHelper.showWaterSprinklerInfo())) return List.of();`
  Pola ini memastikan teks dan indikator informasi hanya diproses dan ditampilkan ketika pemain memegang atau mengenakan peralatan pengamatan yang valid.

### 2. Tabel Komparasi Pola Farm & Charm vs Adaptasi Zoo Tycoon

| Aspek Arsitektur | Pola Farm & Charm | Adaptasi pada Zoo Tycoon Magnifying Glass Hologram |
|---|---|---|
| Crosshair detection | `mc.crosshairPickEntity != animal` dicek langsung di dalam renderer entitas. Mengikuti target pick vanilla yang otomatis terhalang blok padat. | Membaca `mc.crosshairPickEntity` (atau raycast bounded dengan `Level.clip` batas 16 blok). Tanpa timer lengket (inspectExpireTime = 0), langsung hilang seketika saat crosshair beralih atau terhalang dinding. |
| Hand item guard | Memeriksa celana dungarees pada slot armor pemain (`getItemBySlot(EquipmentSlot.LEGS)`). | Memeriksa Magnifying Glass pada tangan utama atau tangan kedua (`player.getMainHandItem()` atau `player.getOffhandItem()`). Disertai guard `mc.screen == null` dan `!mc.options.hideGui`. |
| Posisi hologram | `poseStack.translate(0.0, getBbHeight() + 0.6, 0.0)` (+ 0.3 jika entitas memiliki nama kustom). | Mengadopsi rumus ketinggian yang sama: `yOffset = entity.getBbHeight() + 0.6` (+ 0.3 jika ada custom name). Titik ini menjadi titik jangkar (anchor) utama untuk render baris teks dan bar indikator. |
| Billboarding | `poseStack.mulPose(mc.getEntityRenderDispatcher().cameraOrientation())`. | Mengadopsi pemanggilan `cameraOrientation()` yang sama persis agar seluruh panel teks dan ikon menghadap ke mata kamera pemain secara presisi di ruang 3D dunia. |
| Item 3D rendering | `itemRenderer.renderStatic(...)` dengan `ItemDisplayContext.FIXED` dan kalkulasi alpha cahaya (`Mth.floor(240 * alpha)`). | Menggunakan `itemRenderer.renderStatic(...)` dengan konteks `ItemDisplayContext.FIXED` atau `GROUND` untuk menampilkan model 3D item makanan kesukaan satwa di samping bar indikator. |
| Packet sync | Broadcast berkala dari server di `aiStep` ke seluruh pemain dalam radius 64 blok yang memakai dungarees. | Model on-demand Request/Response: Client mengirim `InspectStart(entityId)` saat crosshair membidik target, server memvalidasi dan mengirim snapshot data tiap 10 tick selama inspeksi berlangsung, lalu client mengirim `InspectStop` saat crosshair lepas. Menghemat bandwidth dan mencegah broadcast berlebih. |

---

## B. Fabric Seasons (fabric-seasons-2.4.2-BETA+1.21.jar)

Audit dilakukan terhadap berkas JAR `fabric-seasons-2.4.2-BETA+1.21.jar` yang terpasang pada profil pengguna serta kode sumber dari repositori `lucaargolo/fabric-seasons` pada tag git `2.4.2-BETA+1.21`.

### 1. Mod ID
Tercantum di dalam berkas `fabric.mod.json`:
- Mod ID: `seasons`
- Versi: `2.4.2-BETA+1.21`

### 2. Class dan Method Publik Pembaca Musim
- Nama Kelas: `io.github.lucaargolo.seasons.FabricSeasons`
- Method Sisi Server / Umum (World/Level):
  - Nama persis: `getCurrentSeason(World world)` (di Mojang mappings: `getCurrentSeason(Level level)`)
  - Signature binary: `(Lnet/minecraft/world/World;)Lio/github/lucaargolo/seasons/utils/Season;`
  - Lokasi berkas sumber: `src/main/java/io/github/lucaargolo/seasons/FabricSeasons.java:218-258`
  - Tipe kembalian: `io.github.lucaargolo.seasons.utils.Season` (enum dengan nilai `SPRING`, `SUMMER`, `FALL`, `WINTER`).
- Method Sisi Client (Convenience):
  - Nama persis: `getCurrentSeason()` (tanpa argumen, membaca world dari pemain lokal)
  - Signature binary: `()Lio/github/lucaargolo/seasons/utils/Season;`
  - Lokasi berkas sumber: `src/main/java/io/github/lucaargolo/seasons/FabricSeasons.java:260-269`

### 3. Modifikasi Suhu Bioma oleh Fabric Seasons
Fabric Seasons secara aktif memodifikasi suhu bioma di memori runtime saat musim berganti.
- Bukti Path dan Baris:
  1. `src/main/java/io/github/lucaargolo/seasons/mixin/WorldViewMixin.java:25-32`:
     Mixin melakukan `@Overwrite` terhadap method `default RegistryEntry<Biome> getBiome(BlockPos pos)` pada interface `WorldView` (di Mojang mappings: `LevelReader`).
     Saat method ini dipanggil pada objek `World`, mixin memanggil:
     `FabricSeasons.injectBiomeTemperature(biomeEntry, (World) this);`
  2. `src/main/java/io/github/lucaargolo/seasons/FabricSeasons.java:344-364`:
     Method `injectBiomeTemperature(RegistryEntry<Biome> entry, World world)` memodifikasi nilai cuaca internal bioma:
     `currentWeather.temperature = modifiedWeather.getRight();`
     Nilai `modifiedWeather` dihitung oleh `getSeasonWeather(season, biomeId, originalWeather.hasPrecipitation, originalWeather.temperature)` (baris 366-413) yang menerapkan offset suhu spesifik musim (contohnya pada bioma sedang/temperate: Spring -0.16f, Summer +0.66f, Winter -0.80f, Fall -0.16f).

### 4. Kesimpulan Pembacaan Suhu Efektif di BlockPos
- Cara paling akurat membaca suhu efektif:
  Panggil `level.getBiome(pos)` terlebih dahulu. Pemanggilan ini secara otomatis memicu `WorldViewMixin` milik Fabric Seasons yang langsung menginjeksi offset suhu musim saat itu ke dalam objek `Biome`.
  Setelah itu panggil method vanilla:
  - `level.getBiome(pos).value().getTemperature(pos)` jika membutuhkan suhu efektif lokal yang sudah memperhitungkan penurunan suhu di dataran tinggi (altitude temperature drop).
  - Atau `level.getBiome(pos).value().getBaseTemperature()` jika membutuhkan suhu dasar bioma pada musim aktif.
- Bentuk data:
  Data berupa nilai `float` murni Minecraft vanilla (rentang umum antara -0.7f hingga 2.0f+). Nilai ini BUKAN derajat Celsius palsu.
  Rentang float ini yang nantinya dikonversi oleh Zoo Tycoon ke dalam kategori kenyamanan iklim satwa (misalnya: Dingin Ekstrem < 0.15f, Sejuk/Temperate 0.15f - 0.8f, Hangat/Panas > 0.8f).

---

## C. ThirstWasTaken2 (n1ght3r/ThirstWasTaken2)

Audit dilakukan pada repositori `n1ght3r/ThirstWasTaken2`.

### 1. Lisensi Berkas
- Berkas `LICENSE` di root repositori: Berlisensi penuh **GNU General Public License v3.0 (GPL-3.0)**.
- Terdapat folder `licenses/ThirstWasTaken-MIT.txt` yang memuat lisensi MIT khusus untuk proyek asli Thirst Was Taken karya ghen, namun proyek turunan ThirstWasTaken2 secara keseluruhan didistribusikan di bawah GPL-3.0.

### 2. Path Tekstur Ikon Thirst / Water Drop
- `src/main/resources/assets/thirstwastaken2/textures/gui/thirst_icons.png`: Sprite sheet berisi ikon tetesan air untuk HUD indikator kehausan.
- `src/main/resources/assets/thirstwastaken2/textures/font/droplets.png`: Tekstur font glyph tetesan air.

### 3. Keputusan Penggunaan Aset
- Status: **DILARANG DISALIN KE PROYEK**.
- Alasan: Lisensi GPL-3.0 bersifat copyleft kuat dan tidak kompatibel dengan lisensi MIT proyek Zoo Tycoon. Menyalin aset berkas gambar dari proyek GPL ke proyek MIT berpotensi menimbulkan pelanggaran lisensi.
- Tindakan: Dicatat sebagai **TODO-USER: izin aset**.
- Solusi teknis: Dibuat tekstur orisinal ikon 9x9 tetes air bergaya pixel art Minecraft vanilla khusus untuk Zoo Tycoon dan ditempatkan di `src/main/resources/assets/zootycoon/textures/gui/hud/thirst_drop.png`.

---

## D. Vanilla Watering Cans (coolbot100s/Vanilla-Watering-Cans)

Audit dilakukan pada repositori `coolbot100s/Vanilla-Watering-Cans` dan halaman proyek Modrinth.

### 1. Lisensi Berkas
- Lisensi di root repositori: **GNU General Public License v3.0 (GPL-3.0)**.
- Kode dan aset dari repositori ini dilarang disalin langsung ke proyek Zoo Tycoon.

### 2. Status di Modrinth dan Ketersediaan Fabric 1.21.1
- Halaman proyek: `https://modrinth.com/resourcepack/vanilla-watering-cans`
- Tipe proyek: **Resource Pack** (`project_type: resourcepack`), BUKAN mod kode Java.
- Versi yang tersedia:
  - Versi 1.1 (Target: Minecraft 1.21.4+, loader: `minecraft`, menggunakan format model item 1.21.4).
  - Versi 1.0 (Target: Minecraft 1.8 - 1.21.3, loader: `minecraft`, menggunakan OptiFine CIT).
- Kesimpulan: **Tidak ada build mod Fabric 1.21.1**. Proyek ini murni merupakan resource pack pengganti tekstur ember.
- (Catatan: Mod berbasis kode dari pembuat yang sama adalah "Watering Cans [Dew Drop]" dengan slug `dew-drop-watering-cans`. Namun pada Minecraft 1.21.1 mod tersebut hanya tersedia untuk loader NeoForge, tanpa build Fabric).

### 3. Daftar Item ID dan Data Component / NBT
- Berdasarkan `renames.txt` dan berkas model `assets/minecraft/models/item/`:
  Vanilla Watering Cans tidak memiliki ID item kustom maupun NBT/Data Component kustom untuk kapasitas air, karena hanya mengganti tampilan visual item ember vanilla:
  - Varian kosong: `minecraft:bucket` (dinamai "Copper Watering Can", "Gold Watering Can", "Diamond Watering Can", "Netherite Watering Can" melalui CIT).
  - Varian berisi air: `minecraft:water_bucket` (dinamai sesuai material di atas saat terisi air).
- Kondisi air bersifat biner: kosong (`minecraft:bucket`) atau penuh (`minecraft:water_bucket`), tanpa variabel level air di NBT.

### 4. Rekomendasi Integrasi pada Zoo Tycoon
- Menerapkan integrasi longgar (**soft dependency**) melalui tag item Fabric: `data/zootycoon/tags/item/watering_cans.json`.
- Isi tag menyertakan item dasar vanilla: `minecraft:water_bucket`.
- Item watering can dari mod eksternal (misalnya `dewdrop:copper_watering_can`, `dewdrop:iron_watering_can`, dsb.) didaftarkan dengan flag `"required": false`.
- Dengan pendekatan ini:
  - Pemain vanilla/profil tanpa mod watering can tetap dapat memandikan satwa menggunakan ember air (`water_bucket`) dan sikat (`brush`).
  - Jika pemain atau modpack menambahkan mod watering can di kemudian hari, item tersebut dapat langsung difungsikan dengan menambahkannya ke tag tanpa mengubah satu baris pun kode logika pembersihan.

---

## E. Tag Makanan Lintas Mod (Convention Tags v2)

Audit dilakukan pada berkas `META-INF/jars/fabric-convention-tags-v2-0.116.17.jar` di dalam `fabric-api-0.116.17+1.21.1.jar` serta berkas JAR mod terpasang.

### 1. Nama Tag Konvensi Resmi Fabric di Minecraft 1.21.1
- Daging mentah: `#c:foods/raw_meat` (memiliki tag alias `#c:foods/raw_meats`).
- Ikan mentah: `#c:foods/raw_fish` (memiliki tag alias `#c:foods/raw_fishes`).
- Daging matang: `#c:foods/cooked_meat` (alias `#c:foods/cooked_meats`).
- Ikan matang: `#c:foods/cooked_fish` (alias `#c:foods/cooked_fishes`).

### 2. Pemetaan Item per Mod

#### a. Minecraft Vanilla
- Masuk ke `#c:foods/raw_meat`:
  `minecraft:beef`, `minecraft:porkchop`, `minecraft:chicken`, `minecraft:rabbit`, `minecraft:mutton`.
- Masuk ke `#c:foods/raw_fish`:
  `minecraft:cod`, `minecraft:salmon`, `minecraft:tropical_fish`, `minecraft:pufferfish`.

#### b. FarmersDelight (FarmersDelight-1.21.1-3.3.6+refabricated.jar)
- Masuk ke `#c:foods/raw_meat` (melalui sub-tag konvensi):
  `farmersdelight:chicken_cuts`, `farmersdelight:minced_beef`, `farmersdelight:bacon`, `farmersdelight:mutton_chops`, `farmersdelight:ham`.
- Masuk ke `#c:foods/raw_fish` (melalui sub-tag konvensi):
  `farmersdelight:cod_slice`, `farmersdelight:salmon_slice`.
- Item olahan / masakan (DILARANG menjadi pakan satwa karnivora liar):
  `farmersdelight:beef_patty`, `farmersdelight:cooked_chicken_cuts`, `farmersdelight:cooked_bacon`, `farmersdelight:cooked_mutton_chops`, `farmersdelight:smoked_ham`, `farmersdelight:cooked_cod_slice`, `farmersdelight:cooked_salmon_slice`, `farmersdelight:dog_food`, aneka burger, stew, pasta, dan pie.

#### c. Farm & Charm (letsdo-farm_and_charm-fabric-1.1.29.jar)
- Masuk ke `#c:foods/raw_meat`:
  `farm_and_charm:minced_beef`, `farm_and_charm:chicken_parts`, `farm_and_charm:lamb_ham`, `farm_and_charm:bacon`.
- Masuk ke `#c:foods/raw_fish`:
  Menggunakan ikan vanilla `#c:foods/raw_fishes`.
- Item olahan / masakan (DILARANG menjadi pakan satwa):
  `farm_and_charm:cooked_bacon`, `farm_and_charm:cooked_lamb_ham`, `farm_and_charm:cooked_chicken_parts`, `farm_and_charm:sausage`, `farm_and_charm:dog_food`, aneka hidangan piring dan sup.

#### d. Naturalist (naturalist-2.0.5-fabric-1.21.1.jar)
- Masuk ke `#c:foods/raw_meat`:
  `naturalist:bushmeat`, `naturalist:clam_meat`, `naturalist:crab_meat`, `naturalist:drumstick`, `naturalist:duck`, `naturalist:lizard_tail`, `naturalist:mammoth_meat`, `naturalist:morsel`, `naturalist:venison`.
- Masuk ke `#c:foods/raw_fish`:
  `naturalist:anglerfish`, `naturalist:bass`, `naturalist:blobfish`, `naturalist:catfish`, `naturalist:piranha`.
- Item olahan / masakan (DILARANG menjadi pakan satwa):
  `naturalist:cooked_bushmeat`, `naturalist:cooked_venison`, `naturalist:cooked_drumstick`, `naturalist:cooked_duck`, `naturalist:cooked_mammoth_meat`, `naturalist:cooked_lizard_tail`, `naturalist:cooked_crab_meat`, `naturalist:cooked_clam_meat`, `naturalist:cooked_bass`, `naturalist:cooked_catfish`, `naturalist:cooked_anglerfish`, `naturalist:cooked_blobfish`, `naturalist:cooked_piranha`.

#### e. Alex's Mobs (alexsmobs-2.2.2-fabric+1.21.1.jar)
- Item daging mentah:
  `alexsmobs:kangaroo_meat`, `alexsmobs:moose_ribs`.
- Item ikan/hewan air mentah:
  `alexsmobs:raw_catfish`, `alexsmobs:flying_fish`, `alexsmobs:blobfish`, `alexsmobs:lobster_tail`.
- Item olahan / masakan (DILARANG menjadi pakan satwa):
  `alexsmobs:cooked_kangaroo_meat`, `alexsmobs:cooked_moose_ribs`, `alexsmobs:cooked_catfish`, `alexsmobs:cooked_lobster_tail`.

#### f. WilderNature (letsdo-wildernature-fabric-1.1.8.jar)
- Item daging mentah:
  `wildernature:bison_meat`, `wildernature:cassowary_meat`, `wildernature:turkey_meat`, `wildernature:venison`.
- Item olahan / masakan (DILARANG menjadi pakan satwa):
  `wildernature:cooked_bison_meat`, `wildernature:cooked_cassowary_meat`, `wildernature:cooked_turkey_meat`, `wildernature:cooked_venison`.

---

## F. Ringkasan Dependensi dan Keputusan Desain

### 1. Komponen yang Dapat Langsung Dipakai
1. **Arsitektur Rendering In-World Hologram 3D (Pola Farm & Charm)**:
   - Menggunakan deteksi crosshair vanilla `mc.crosshairPickEntity` tanpa sticky timer.
   - Posisi vertikal dihitung dari `yOffset = entity.getBbHeight() + 0.6` (+ 0.3 untuk nama kustom).
   - Orientasi billboarding penuh menggunakan `mc.getEntityRenderDispatcher().cameraOrientation()`.
   - Render 3D item pakan kesukaan menggunakan `itemRenderer.renderStatic(...)` dengan `ItemDisplayContext.FIXED`.
2. **Integrasi Musim dan Suhu (Fabric Seasons)**:
   - Menggunakan Mod ID `seasons`.
   - Membaca musim aktif melalui `io.github.lucaargolo.seasons.FabricSeasons.getCurrentSeason(world)`.
   - Membaca suhu efektif bioma melalui `level.getBiome(pos).value().getTemperature(pos)` yang otomatis mendapatkan injeksi suhu musim via `WorldViewMixin`.
3. **Standarisasi Tag Diet Karnivora**:
   - Mengadopsi tag konvensi Fabric v2 `#c:foods/raw_meat` dan `#c:foods/raw_fish` untuk seluruh satwa pemakan daging dan ikan, mencakup item mentah dari Vanilla, FarmersDelight, Farm & Charm, Naturalist, Alex's Mobs, dan WilderNature.
4. **Tag Soft Dependency Alat Pembersih**:
   - Tag item `zootycoon:watering_cans` yang secara default memuat `minecraft:water_bucket` dan entri mod eksternal dengan status `"required": false`.

### 2. Komponen yang Membutuhkan Keputusan Pengguna
1. **Aset Ikon Thirst (TODO-USER: izin aset)**:
   - Tekstur ikon dari ThirstWasTaken2 berlisensi GPL-3.0 sehingga tidak disalin ke proyek.
   - Solusi yang diterapkan adalah merancang aset pixel art orisinal 9x9 tetes air gaya vanilla berlisensi MIT di dalam direktori aset mod Zoo Tycoon. Jika pengguna ingin menggunakan aset visual spesifik dari mod tertentu, izin eksplisit atau re-lisensi diperlukan.
