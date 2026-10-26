---
trigger: model_decision
description: When i working on zooTycoonSimulation minecraft mod
---
use C:\Users\Zevaldo\Documents\my mods\zooTycoonSimulation\.agents\skills\explore-index

commit & push Setiap fitur baru commit layaknya profesional
no bold font, no emoji. jangan over info jangan terlalu banyak info duplikats di dialog atau toast. no over colored font. Jangan ubah dialog kecuali memang di minta
Wajib membaca docs/hologram/RULES.md dan docs/hologram/PROGRESS.md sebelum mengerjakan fitur hologram.

C:\Users\Zevaldo\AppData\Roaming\ModrinthApp\profiles\Fresh & Smooth\mods pindah mod ke sini kalo sudah beres build akhir

mod source yg dipakai untuk mencari tau isi kode atau mixin:
https://github.com/Codx-org/AlexsMobsContinued
https://github.com/crispytwig/Naturalist
https://github.com/axperty/moredelight
https://github.com/MehVahdJukaar/FarmersDelightRefabricated
https://github.com/Let-s-Do-Collection/FarmAndCharm

data hewan:
https://docs.google.com/document/d/1jJZwJqmeuR9pmZL68d9drwBwZZVqxTd8_w1RZepzcl4

fungsi yang sudah ada jangan sampe rusak

Scope guard yang ketat: Dilarang mengubah atau menyentuh file, class, dan komponen UI yang tidak disebutkan secara eksplisit dalam instruksi pengguna. Jika tugas hanya menyebut feeding trough, jangan pernah menyentuh file zookeeper, cashier, builder, atau file lainnya.

Larangan mengubah representasi visual/fitur: Dilarang mengganti render 3D entitas (InventoryScreen) menjadi item render 2D, atau memodifikasi tata letak UI yang sudah ada tanpa instruksi eksplisit.

Konfirmasi sebelum refactor: Saat melakukan bugfix, fokus hanya pada perbaikan bug yang dilaporkan. Dilarang melakukan optimasi atau refactoring tambahan atas inisiatif sendiri.

jangan pernah kasih zookeper awalan makanan, hanya player yang harus ngasih makanan ke zookeper setelah itu zookeper baru ngisi makanan ke feeding through

fungsi yg udah fix (tidak boleh sampe rusak karena mengubah kode):
1. Logic builder dan ui dialog nya
2. Logic pemilihan zookeper untuk hewan
3. dialog zookeper dan ui nya
3. Logic dan ui cashier
4. logic antri
5. pengunjung / visitor
6. hunter
7. ui feeding through       
8. builder notes & ui notes
9. logika magnifying glass, animasi hologram
10. Reaksi satwa terhadap environtment, suhu, musim, biome,
11. Khusus satwa yang butuh air dia akan auto jalan ke air
12. Khusus Satwa yang bisa mencari makan sendiri dia akan makan lewat feeding thrugh yang di isi oleh zookeper (berkaitan dengan sistem lapar)
fungsi yang belum fix atau belum ada atau masih cacat (Dilarang mengerjakan semua tahap ini sebelum diminta secara eksplisit oleh pengguna):
13. Khusus Satwa yang perlu di kasi makan langsung dia akan di beri pakan langsung oleh zookeper (berkaitan dengan sistem lapar)
14. ui dialog button action zookeper
15. fungsi zookeper membersihkan satwa dengan alat
15. ui hologram satwa
17. hologram progress bar watering can dan brush (tapi icon nya belum)
18. magnifying glass logic
19. Gudang. empty barrel

fungsi yg belum ada atau masih cacat (jangan eksekusi fitur ini sebelum secara eksplisit diminta)

- Sistem Rating Zoo Bangun formula perhitungan rating total 5 star dinamis berdasarkan rata-rata kesehatan satwa, kebersihan kandang, kelengkapan fasilitas, dan kepuasan pengunjung, serta tampilkan indikator rating di UI/HUD.
- tambahkan already builded biome di kategori "terrain" di menu bangunan yang dimana already builded biome adalah biome yg di ambil dari world generation asli dari mod apapun misal user pake world generation vanila atau moded world generation apapun seperti tectonic nah di kategori terrain itu sudah ada already builded biome yg otomatis di generate oleh mod ini dan yg di generate nya itu world generation dari mod world generation itu sendiri dengan size biome yg bisa dikustomisasi. 
- Kunjungan Pengunjung Mengikuti Rating dan Kualitas
Perbarui spawner pengunjung agar kapasitas maksimal dan frekuensi kedatangan berskala dinamis mengikuti rating kebun binatang dan jumlah fasilitas/satwa aktif.
- Finansial dan Penetapan Harga (Tiket, Makanan, Gaji Pegawai)
Sediakan pengaturan harga tiket dan makanan/minuman yang berdampak pada keputusan pengunjung, serta terapkan pemotongan gaji berkala (biaya operasional harian) untuk seluruh staf zoo.


- Alasan Kepuasan Pengunjung Ringkas (Single-Reason Visitor Feedback)
- kendaraan untuk menangkap hewan
- mob bag logic

Urutan eksekusi sederhana:


2. Finansial dan penetapan harga Alasan: Mengatur harga tiket, harga makanan, dan pemotongan gaji staf harian.

3. Logika Mob Bag dan mobil Alasan: Alat tangan untuk menangkap satwa liar dan memindahkannya ke mobil untuk membawa nya ke zoo.

4. Sistem rating zoo 5 star dan indikator UI Alasan: Menghitung nilai kebun binatang dari kesehatan satwa, kebersihan, dan kepuasan pengunjung.

5. Spawner pengunjung mengikuti rating Alasan: Jumlah dan frekuensi pengunjung otomatis bertambah jika rating zoo tinggi.

6. Already builded biome di kategori terrain Alasan: Menghasilkan biome asli dari world generation dengan ukuran kustom.

7. performance check TPS, thick