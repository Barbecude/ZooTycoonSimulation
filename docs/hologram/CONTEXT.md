# Konteks tetap fitur Animal Inspection Hologram (Pola Farm & Charm)
Platform: Minecraft 1.21.1, Fabric Loader 0.16.10, Fabric API 0.116.0+1.21.1, Loom 1.7.4, Mojang mappings, Java 21. Package net.zootycoon, mod id zootycoon.
Build: .\gradlew.bat build (task build otomatis menjalankan copyToModrinth ke folder mods profil).
Test otomatis: tidak ada (src/test kosong, tidak ada JUnit).
Folder mods profil: C:\Users\Zevaldo\AppData\Roaming\ModrinthApp\profiles\Fresh & Smooth\mods
Mod terpasang yang relevan: alexsmobs-2.2.2-fabric+1.21.1, naturalist-2.0.5-fabric-1.21.1, letsdo-wildernature-fabric-1.1.8, letsdo-farm_and_charm-fabric-1.1.29, fabric-seasons-2.4.2-BETA+1.21, FarmersDelight-1.21.1-3.3.6+refabricated, fieldguide-1.21.4+1.21.1-fabric.
Tidak terpasang: Serene Seasons, ThirstWasTaken2, Vanilla Watering Cans.

File terkait yang sudah ada (src/main/java/net/zootycoon/...):
- client/render/ZooAnimalHologramRenderer.java: renderer lama world-space, dipanggil di WorldRenderEvents.AFTER_ENTITIES (client/ZooTycoonClient.java sekitar baris 498-503).
- item/MagnifyingGlassItem.java: use() animasi spyglass + registrasi hewan; interactLivingEntity memanggil setInspectedAnimal (client) dan registrasi (server).
- animal/ZooAnimalHungerManager.java: hunger/thirst/cleanliness disimpan di Map + entity tag zoo_hunger_/zoo_thirst_/zoo_cleanliness_; tickServer; isZooAnimalCandidate; isTagged; isOwnedBy; registerAnimalFromSpyglass; cleanAnimal; handleUniversalAnimalNeeds (jalan ke trough/air).
- animal/AnimalDietRegistry.java: diet per entity type id (FeedingType TROUGH / THROWN_MEAT, acceptedFoods) + fallback diet.
- compat/SereneSeasonsCompat.java: refleksi ke mod id "sereneseasons"; evaluateEnvironment memakai heuristik nama entity.
- network/ZooNetworkPayloads.java: AnimalSyncData; sendAnimalSync(player) hanya dikirim saat event tertentu dan hanya untuk hewan tagged milik player.
- client/ZooTycoonClient.java: getAnimalData(entityId), registrasi render dan tick.
- ZooTycoonMod.java sekitar baris 97-124: UseEntityCallback; brush/broom langsung membersihkan hewan lewat cleanAnimal.

Temuan awal (diverifikasi di Fase 1):
T1 Raycast ProjectileUtil.getEntityHitResult tanpa cek blok padat, tembus tembok; render Font.DisplayMode.SEE_THROUGH membuat teks bocor lewat dinding.
T2 Target lengket: inspectExpireTime 8-10 detik membuat hologram tertinggal saat crosshair berpindah.
T3 Jangkauan 36-48 blok terlalu jauh; tidak cek layar UI terbuka atau F1.
T4 Nilai palsu hunger 80, thirst 80, cleanliness 100 saat sync kosong.
T5 Sync tidak periodik; hewan belum terdaftar tidak punya data yang akurat.
T6 Musim: terpasang Fabric Seasons, bukan Serene Seasons, jadi getCurrentSeason selalu SPRING (juga memengaruhi laju thirst di tickServer).
T7 Kenyamanan suhu berdasar nama entity, bukan data spesies.
T8 Teks memakai emoji dan kode warna §.
T9 Belum ada sistem mood hewan dan sistem habitat/ekosistem.

Keputusan desain final terinspirasi mod Farm & Charm (jangan diubah tanpa izin user):
D1 Tampilan: In-World Hologram 3D melayang tepat di atas kepala hewan target (world-space billboarded overlay mengikuti pola SaturationOverlayRenderer di Farm & Charm). Bukan 2D HUD di samping crosshair, melainkan hologram fisik di dunia Minecraft dengan billboarding rotasi kamera.
D2 Deteksi: Mengadopsi mekanisme target Farm & Charm (LivingEntityRendererMixin): menggunakan crosshair pick entity (mc.crosshairPickEntity) dan raycast dengan batas collision blok padat. Jangkauan wajar maks 16 blok. Tanpa timer lengket (langsung hilang seketika saat crosshair lepas atau terhalang blok).
D3 Syarat tampil: Magnifying Glass di main hand atau off hand, mc.screen == null, !mc.options.hideGui, target hidup, isZooAnimalCandidate, tidak terhalang tembok, jarak <= 16 blok.
D4 Data server: client kirim InspectStart(entityId) / InspectStop. Server validasi lalu kirim snapshot tiap 10 tick selama valid. State dibersihkan saat stop, logout, atau hewan mati.
D5 Hewan belum terdaftar (isTagged false): tampil nama + health + teks "Belum terdaftar". Indikator lain "-".
D6 Arti bar: 100 = baik (kenyang, tidak haus, bersih). Bar penuh selalu berarti baik.
D7 Hanya teks + ikon. Teks putih/abu-abu, maks 2 warna aksen (merah = kritis, kuning = peringatan). Emoji hanya untuk ikon mood (PNG Apple emoji).
D8 Semua teks lewat lang id_id.json dan en_us.json.
D9 Git: git add <path spesifik> saja.
D10 Kebersihan hanya naik lewat penyiram (watering can) + brush. Pembersihan pasif hujan/air dihapus. Broom tidak lagi membersihkan hewan.
D11 Hewan THROWN_MEAT tetap diberi makan dengan dilempar; pemaksaan feeding trough hanya untuk hewan TROUGH.
