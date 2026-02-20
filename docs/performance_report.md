# Performance Report (Sederhana)

Dokumen ini fokus pada perubahan yang berdampak ke performa server tick (AI + interaksi item).

## Perubahan Utama
- Capture cage: dari multi-slot + UI release menjadi 1-slot toggle. Dampak: NBT lebih kecil dan jalur eksekusi lebih pendek saat interaksi.
- Tagging: menambah raycast saat klik kanan udara (hanya saat item tag digunakan), dan event intercept untuk memastikan prioritas tag.
- Toilet: penambahan block entity + seat entity + tick ringan untuk queue/door. Tick berjalan hanya pada block toilet yang ada.
- Hunter: penghapusan breaking dan penambahan placement dirt sementara saat pathfinding macet (placement dibatasi jumlah + TTL).

## Metrik yang Dianjurkan Untuk Validasi
- TPS/lag saat 20+ visitor + 3 hunter + 30 hewan bertag.
- Rata-rata waktu tick (Spark/VisualVM jika tersedia).
- Jumlah blok dirt sementara yang aktif (harus turun otomatis setelah TTL).

## Catatan
- Tidak dilakukan benchmark “before vs after” yang terinstrumentasi otomatis di repo ini; evaluasi performa disediakan sebagai checklist dan batasan mekanisme (cooldown/TTL/bounds) untuk mencegah lag.
