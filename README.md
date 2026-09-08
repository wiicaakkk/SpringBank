# SpringBank

Core banking backend: pendaftaran nasabah (CIF), rekening, dan transfer OTC. Java 21, Spring Boot, JPA, PostgreSQL, Redis. Frontend gaya UI bank legacy (WinVader).

## Fitur

- Registrasi nasabah, nomor CIF digenerate otomatis: `CIF` + `YYYYMMDD` + 4 digit urut (contoh `CIF202607260001`)
- Pencarian nasabah berdasarkan CIF, NIK, atau nama
- Update data dan status nasabah (AKTIF, PENDING_VERIFIKASI, BLOCKED), plus riwayat audit tiap perubahan
- Buka rekening tabungan dan transfer OTC
- Login 3 role: TELLER, SUPERVISOR, ADMIN. Layar RC13 (maintenance CIF update) hanya untuk SUPERVISOR dan ADMIN

## Menjalankan

Prasyarat: Java 21, PostgreSQL (schema `bankdb`), Redis. Konfigurasi DB di `src/main/resources/application.properties` atau lewat env (`SPRING_DATASOURCE_*`, `SPRING_DATA_REDIS_*`).

```bash
./mvnw spring-boot:run
```

App di `http://localhost:8080`. User demo: `teller1`, `spv1`, `admin1` (password `password123`), dibuat otomatis oleh `DataInitializer`.

## API

Semua endpoint selain login butuh header `Authorization: Bearer <token>` dari `POST /api/auth/login`.

| Method | Path | Fungsi |
|---|---|---|
| POST | `/api/auth/login` | Login, dapat token |
| POST | `/api/auth/logout` | Logout, token di-blacklist (Redis) |
| POST | `/api/nasabah/register` | Daftar nasabah baru, CIF digenerate otomatis |
| GET | `/api/nasabah` | Semua nasabah |
| GET | `/api/nasabah/search?keyword=` | Cari by CIF / NIK / nama |
| GET | `/api/nasabah/{cif}` | Detail satu nasabah |
| PUT | `/api/nasabah/{cif}` | Update data atau status nasabah |
| GET | `/api/nasabah/{cif}/history` | Riwayat perubahan satu nasabah |
| GET | `/api/nasabah/history/all` | Riwayat perubahan semua nasabah |
| POST | `/api/rekening/create` | Buka rekening tabungan |
| POST | `/api/rekening/transfer` | Transfer OTC antar rekening (transaksional) |
| POST | `/api/rekening/setor` | Setoran tunai (DP03) |
| POST | `/api/rekening/tarik` | Tarik tunai (DP04) |
| POST | `/api/rekening/{no}/penutupan` | Penutupan rekening, syarat saldo nol (DP05) |
| POST | `/api/rekening/{no}/blokir` | Blokir rekening (DP06) |
| POST | `/api/rekening/{no}/buka-blokir` | Buka blokir rekening (DP06) |
| GET | `/api/rekening/cif/{cif}` | Rekening milik nasabah |
| POST | `/api/auto-transfer/register` | Registrasi instruksi auto transfer (AT02) |
| GET | `/api/auto-transfer?status=` | Daftar instruksi, filter status AKTIF/BERHENTI (AT01) |
| POST | `/api/auto-transfer/{kode}/stop` | Hentikan instruksi |
| POST | `/api/auto-transfer/{kode}/start` | Aktifkan kembali instruksi |
| POST | `/api/auto-transfer/execute` | Eksekusi jatuh tempo (bisa per kode via body `kodeInstruksi`) |
| GET | `/api/auto-transfer/{kode}/history` | Riwayat eksekusi instruksi |
| GET | `/api/neraca?tanggal=` | Neraca per tanggal: daftar akun aktiva & pasiva, total, selisih (harus nol) |
| GET | `/api/coa` | Daftar akun (chart of accounts) beserta saldo |
| POST | `/api/coa` | Tambah akun GL baru |
| POST | `/api/jurnal` | Posting jurnal umum (debet = kredit) untuk pendapatan/beban |
| GET | `/api/laba-rugi?tanggal=` | Laporan laba rugi: pendapatan, beban, laba bersih |
| GET | `/api/buku-besar?kodeAkun=&dari=&sampai=` | Ledger per akun GL dengan saldo berjalan |
| GET | `/api/rekening/{nomorRekening}/mutasi?dari=&sampai=` | Laporan mutasi per rekening |
| GET | `/api/v1/transaksi/summary` | Rekap transaksi |
| POST | `/api/v1/transaksi` | Transaksi umum (debit/kredit) |

Contoh registrasi nasabah:

```json
POST /api/nasabah/register
{
  "nik": "3171012005900004",
  "namaLengkap": "Rina Melati",
  "tempatLahir": "Jakarta",
  "tanggalLahir": "1995-03-25",
  "jenisKelamin": "PEREMPUAN",
  "ibuKandung": "Siti Rahmah",
  "alamat": "Jl. Gatot Subroto No. 88, Jakarta",
  "nomorHp": "081299887766",
  "email": "rina.melati@bank.com",
  "pekerjaan": "Product Manager",
  "penghasilanBulanan": 25000000.00
}
```

## Struktur

Paket per domain: `rc` (nasabah/CIF), `dp` (rekening, transfer, kas), `at` (auto transfer & scheduler), `gl` (chart of accounts, jurnal, neraca, laba rugi), `auth` (login & role), `config` (Redis, seeder), `common` (response wrapper, error handler). Scheduler auto transfer berjalan tiap hari 07:15 WIB (`AutoTransferScheduler`). Setiap transaksi uang diposting otomatis ke jurnal GL (`gl_entry`) dan neraca selalu seimbang; pendapatan/beban dibukukan lewat jurnal umum (GL03) dan tampil sebagai "Laba / Rugi Berjalan" di neraca serta laporan laba rugi (GL04). Frontend statis di `src/main/resources/static` dikirim langsung oleh Spring Boot.

## Deploy Homelab

Build jar, lalu docker image `corebanking:latest` dan jalankan di LXC docker (Proxmox), exposed port `8090 -> 8080`. Konfigurasi via env: `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `SPRING_DATA_REDIS_HOST`, `SPRING_DATA_REDIS_PORT`, `SPRING_DATA_REDIS_PASSWORD` (nilai kredensial dipegang terpisah dari repo).

Catatan implementasi & peta menu sistem acuan InoAn ada di `docs/inoan-menu-map.md`.