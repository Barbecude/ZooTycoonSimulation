---
trigger: model_decision
description: When i working on zooTycoonSimulation minecraft mod
---

commit & push Setiap fitur baru commit layaknya profesional
no bold font, no emoji. jangan over info jangan terlalu banyak info duplikats di dialog atau toast. no over colored font. Jangan ubah dialog kecuali memang di minta
Wajib membaca docs/hologram/RULES.md dan docs/hologram/PROGRESS.md sebelum mengerjakan fitur hologram.

C:\Users\Zevaldo\AppData\Roaming\ModrinthApp\profiles\Fresh & Smooth\mods pindah mod ke sini kalo sudah beres build akhir

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

fungsi yang belum fix atau belum ada atau masih cacat (Dilarang mengerjakan semua tahap ini sebelum diminta secara eksplisit oleh pengguna):
- jika salah satu dari stats (haus, kebersihan, lapar, suhu) sampai 0 atau tidak nyaman maka hewan akan dapat effect minecraft negatif bisa berupa poison, hunger atau lainnya.
- Khusus Satwa yang bisa mencari makan sendiri dia akan makan lewat feeding thrugh yang di isi oleh zookeper (berkaitan dengan sistem lapar)
- Khusus Satwa yang perlu di kasi makan langsung dia akan di beri pakan langsung oleh zookeper (berkaitan dengan sistem lapar)
- Khusus satwa yang butuh air dia akan auto jalan ke air
- Sistem Rating Zoo dan Integrasi UI
Bangun formula perhitungan rating dinamis berdasarkan rata-rata kesehatan satwa, kebersihan kandang, kelengkapan fasilitas, dan kepuasan pengunjung, serta tampilkan indikator rating di UI/HUD.
- Kunjungan Pengunjung Mengikuti Rating dan Kualitas
Perbarui spawner pengunjung agar kapasitas maksimal dan frekuensi kedatangan berskala dinamis mengikuti rating kebun binatang dan jumlah fasilitas/satwa aktif.
- Finansial dan Penetapan Harga (Tiket, Makanan, Gaji Pegawai)
Sediakan pengaturan harga tiket dan makanan/minuman yang berdampak pada keputusan pengunjung, serta terapkan pemotongan gaji berkala (biaya operasional harian) untuk seluruh staf zoo.
- Gudang (Warehouse dan Rantai Pasok)
Bangun sistem gudang sentral untuk menyimpan stok pakan dan suplai grosir, serta integrasikan zookeeper, dan cashier untuk mengambil jatah pakan dari gudang tanpa memberi pakan awal otomatis.
