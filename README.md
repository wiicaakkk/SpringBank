# 🏦 Core Banking System - Pendaftaran Nasabah & Generator Nomor CIF

Selamat datang di proyek **Core Banking System (Pendaftaran Nasabah & CIF Generator)** berbasis **Java 21**, **Spring Boot 3**, **Spring Data JPA**, **H2 Database (In-Memory)**, **Lombok**, dan **Validation**.

---

## 🔑 Konsep Utama CIF (Customer Information File)

Di dalam perbankan, **CIF (Customer Information File)** adalah nomor identifikasi unik utama bagi setiap nasabah.
Format penomoran CIF otomatis pada aplikasi ini:
`CIF` + `YYYYMMDD` + `4-Digit Sequence` (Contoh: `CIF202607260001`).

Setiap data CIF menyimpan data sensitif & penting perbankan:
- **NIK (16 Digit KTP)** & Nama Lengkap
- **Tempat / Tanggal Lahir** & Jenis Kelamin
- **Nama Ibu Kandung** *(Bidang Keamanan Wajib Perbankan)*
- **Alamat Domisili**, Nomor HP, & Email
- **Pekerjaan** & **Penghasilan Bulanan**
- **Status Nasabah** (`AKTIF`, `PENDING_VERIFIKASI`, `BLOCKED`)

---

## 🏗️ Struktur Proyek (Clean Layered Architecture)

```
com.belajar.springboot/
├── BelajarSpringbootApplication.java  # Main Entry Point Aplikasi
├── config/
│   └── DataInitializer.java           # Seeder 3 Nasabah resmi pertama dengan CIF
├── controller/
│   └── NasabahController.java         # REST Endpoints (/api/nasabah)
├── dto/
│   ├── RegisterNasabahRequest.java    # Input Pendaftaran dengan Validasi
│   ├── UpdateNasabahRequest.java      # Input Update Profil
│   ├── NasabahResponse.java           # Format Output Response Nasabah + CIF
│   └── WebResponse.java               # Standard JSON Response Wrapper
├── entity/
│   ├── Nasabah.java                   # Entity JPA Table 'nasabah'
│   ├── JenisKelamin.java              # Enum LAKI_LAKI / PEREMPUAN
│   └── StatusNasabah.java             # Enum AKTIF / PENDING_VERIFIKASI / BLOCKED
├── exception/
│   └── GlobalExceptionHandler.java    # Error Handler untuk NIK ganda & Validasi
├── repository/
│   └── NasabahRepository.java         # JpaRepository (CIF Lookup & Search Query)
└── service/
    └── NasabahService.java            # Algoritma Auto CIF Generator & Logika Bisnis Bank
```

---

## 🚀 Cara Menjalankan Server

```bash
# 1. Load SDKMAN (Java 21 & Maven)
source "$HOME/.sdkman/bin/sdkman-init.sh"

# 2. Jalankan Spring Boot
./mvnw spring-boot:run
```

Aplikasi dapat diakses di: **`http://localhost:8080`**

---

## 🖥️ Interactive Web Dashboard & H2 Console

1. **Dashboard Perbankan Interaktif**:
   Buka browser ke **`http://localhost:8080`**
   - Form Pendaftaran Nasabah Baru
   - Notifikasi Penerbitan Nomor CIF Resmi secara otomatis
   - Pencarian Nasabah berdasarkan CIF, NIK, atau Nama
   - Tombol blokir / aktifkan status nasabah

2. **H2 Console Database**:
   Buka **`http://localhost:8080/h2-console`**
   - **JDBC URL**: `jdbc:h2:mem:belajardb`
   - **Username**: `sa`
   - **Password**: *(kosongkan)*

---

## 📡 REST API Documentation

### 1. Register Nasabah Baru (Auto Generate CIF)
- **Method**: `POST`
- **URL**: `http://localhost:8080/api/nasabah/register`
- **Body JSON**:
```json
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

### 2. Get Seluruh Data Nasabah
- **Method**: `GET`
- **URL**: `http://localhost:8080/api/nasabah`

### 3. Cari Nasabah Berdasarkan CIF
- **Method**: `GET`
- **URL**: `http://localhost:8080/api/nasabah/CIF202607260001`

### 4. Search Nasabah (Kata Kunci: CIF / NIK / Nama)
- **Method**: `GET`
- **URL**: `http://localhost:8080/api/nasabah/search?keyword=Santoso`

### 5. Update Status Nasabah (Block / Aktifkan)
- **Method**: `PATCH`
- **URL**: `http://localhost:8080/api/nasabah/CIF202607260001/status?status=BLOCKED`
