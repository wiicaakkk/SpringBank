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
| GET | `/api/rekening/cif/{cif}` | Rekening milik nasabah |
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

Paket per domain: `rc` (nasabah/CIF), `dp` (rekening & transfer), `auth` (login & role), `config` (Redis, seeder), `common` (response wrapper, error handler). Frontend statis di `src/main/resources/static` dikirim langsung oleh Spring Boot.