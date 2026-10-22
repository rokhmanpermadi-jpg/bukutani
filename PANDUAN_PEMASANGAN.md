# Buku Kas UMKM — Panduan Pemasangan di HP Android

Aplikasi pembukuan gratis untuk semua jenis UMKM (produksi makanan/minuman, warung, jasa, kerajinan, pertanian).
Berjalan **tanpa internet**, tanpa akun, tanpa iklan. Data tersimpan hanya di HP pengguna.

## Dua cara memasang (pilih salah satu)

### Cara 1 — File APK (paling mudah dibagikan ke mitra)

- Tautan unduh APK terbaru:
  **https://github.com/rokhmanpermadi-jpg/bukukas-umkm/releases/latest/download/BukuKasUMKM.apk**
- Atau kirim file `BukuKasUMKM.apk` (±2,7 MB) ke HP mitra lewat WhatsApp / Bluetooth / kabel.

Langkah di HP mitra:
1. Buka file `BukuKasUMKM.apk` (dari WhatsApp: ketuk file → *Buka*).
2. Bila muncul "Demi keamanan, ponsel Anda tidak diizinkan memasang aplikasi dari sumber ini",
   ketuk **Setelan → Izinkan dari sumber ini**, lalu kembali dan ketuk **Pasang**.
3. Bila Play Protect menanyakan "Pindai aplikasi?", pilih **Pasang saja / Lanjutkan**
   (aplikasi ini dibangun dari kode sumber di repositori di atas, tidak ada akses internet maupun data pribadi).
4. Ikon **Buku Kas UMKM** muncul di layar utama. Buka, isi nama dan jenis usaha, mulai mencatat.

Mode offline otomatis; tidak perlu dibuka dengan internet terlebih dahulu.

### Cara 2 — Tanpa file, dari Chrome (PWA)

1. Di Chrome Android buka: **https://rokhmanpermadi-jpg.github.io/bukukas-umkm/**
2. Tunggu halaman terbuka penuh (±5 detik, perlu internet sekali ini saja).
3. Ketuk menu **⋮ → Tambahkan ke layar utama / Instal aplikasi**.
4. Setelah itu aplikasi berjalan tanpa sinyal, sama seperti versi APK.

Catatan: data versi APK dan versi PWA terpisah (masing-masing menyimpan di tempatnya sendiri).
Gunakan **Cadangkan (JSON)** di menu Laporan untuk memindahkan data antar HP atau antar versi.

## Kebiasaan yang perlu diajarkan ke mitra

- **Cadangkan data** rutin (mis. setiap akhir pekan): *Laporan → Cadangkan (JSON)* → bagikan ke WhatsApp sendiri.
  Data hilang bila aplikasi dihapus atau HP diganti tanpa cadangan.
- **Kirim laporan ke pendamping**: *Laporan → Salin ke WA* lalu tempel di WhatsApp.
- **Unduh CSV** bila laporan perlu dibuka di Excel untuk laporan program.
- *Pengaturan → Isi data contoh* berguna saat pelatihan; hapus lagi lewat *Hapus semua data*.

## Fitur

1. **Beranda** — saldo kas, omzet/modal keluar/keuntungan bulan ini dengan perbandingan bulan lalu, aksi cepat,
   peringatan kasbon dan stok, grafik 6 bulan, transaksi terakhir.
2. **Catat** — pemasukan/pengeluaran, nominal cepat, tanggal, kategori, produk + jumlah unit (nominal otomatis,
   stok berkurang), cara bayar Tunai/QRIS/Transfer/Kasbon, pelanggan, catatan.
3. **Riwayat** — filter Semua/Pemasukan/Pengeluaran/Piutang, per bulan, pencarian; kelompok per hari;
   kartu **kasbon per pelanggan** dengan tombol Lunas; ketuk transaksi untuk ubah/hapus.
4. **Produk & Stok** — kartu produk, kalkulator **HPP dan harga jual** (margin %, bulat Rp500), stok minimum,
   catat produksi/barang masuk.
5. **Laporan** — keuntungan bersih dan margin, grafik (omzet vs modal, tren untung, per produk, cara bayar),
   komposisi pengeluaran, produk paling laris, rincian buku kas, stok; Salin ke WA, CSV, cadangan/pulihkan JSON.
6. **Pengaturan** — data usaha, jenis usaha (menentukan kategori bawaan), kategori sendiri, kesehatan pencatatan,
   bantuan, data contoh, hapus semua data.

## Memperbarui aplikasi (untuk tim pelaksana)

Repositori: https://github.com/rokhmanpermadi-jpg/bukukas-umkm

- Aplikasi web ada di `app/src/main/assets/www/index.html` (APK) dan `docs/index.html` (PWA); keduanya harus sama.
- Setiap *push* ke cabang `main` otomatis membangun APK baru di tab **Releases** (±2 menit).
- Untuk PWA, naikkan nomor `bukukas-v5` di `docs/sw.js` menjadi `bukukas-v6` dan seterusnya setiap ada perubahan.
- Naikkan `versionCode` dan `versionName` di `app/build.gradle` agar APK baru dikenali sebagai pembaruan.
- Kunci tanda tangan APK dibuat otomatis saat build. Bila nanti ingin masuk Play Store, buat kunci tetap dan simpan
  sebagai *secret* repositori (`KEYSTORE_B64`, `KEYSTORE_PASS`, `KEY_ALIAS`, `KEY_PASS`).
