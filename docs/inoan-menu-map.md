# Peta Menu Sistem InoAn (WinVader CBS, Pro*C / Tuxedo)

Rangkuman struktur menu pada proyek C (InoAn) sebagai acuan implementasi menu di Java Spring Boot.

> Catatan dulu: menu di InoAn **tidak di-hardcode** di source. Struktur menu (grup, sub-grup, nama layar, kode transaksi, URL layar, urutan) tersimpan di **database Oracle** pada tabel `ACOM_COMH_MENU`. Source hanya berisi layar per transaksi (`htm/<modul>/<KODE>.html`) dan program layanannya (`src/<modul>/<kode>.pc`). Jadi nama menu yang paling asli hanya bisa ditarik dari DB sistem berjalan, bukan dari kode. Dokumen ini menyusun ulang inventori dari kode + dokumentasi modul yang ada di repo.

## Mekanisme Menu (dari kode)

- `htm/Menu.html`: menggambar menu dari section `MENUDATA` (hasil query DB). Field tiap record menu:
  - `FOCM02_MENU_ID` (id menu / node tree)
  - `FOCM02_MENU_NAME` (nama yang tampil)
  - `FOCM02_UPMU_CODE` (kode transaksi, mis. `DP01`, `RC11`)
  - `FOCM02_SRC_URL` (URL layar HTML)
  - `FOCM02_MENU_PATH` (jalur tree, dipisah `|`, contoh `Group|Sub|Screen`)
- Tampilan menu punya 3 tab: **Menu** (tree default), **Scenario** (group skenario bisnis, tabel `ACOM_SCNRO_BASE` / `ACOM_SCNRO_DTLS`), **Favorite** (favorit per operator).
- Hak akses menu per operator (dari `menulist.html`):
  - Grup: `ACOM_COMH_USER` (op, GRP_NO) join `ACOM_COMH_GRPGRANT` join `ACOM_COMH_MENU`
  - Per-user: `ACOM_COMH_USERGRANT`
  - Syarat tampil: `ACOM_COMH_MENU.STS = '0'`, `SRC_URL != '.'`, urut `SORT_SEQ`
- Klik leaf -> `top.MAIN.GoWork(menuCd, "EMPTY")` -> aplikasi memuat `htm/<modul>/<menuCd>.html` dan memanggil service Tuxedo (program `.pc`) dengan `TransCode=<menuCd>`.

## Konvensi Kode

| Pola | Arti |
|---|---|
| `DP01` / `RC11` dst. | Kode transaksi layar (htm) dan menu (UPMU_CODE) |
| `rco0100.pc` | Program online: `o` = entry/transaksi, `q` = inquiry, `g` = fungsi bersama |
| `FIDP00_*`, `FIRC11_*` | Field FML milik layar (prefix `FI<MODUL><nn>` / `FO<MODUL>`) |

## Inventaris Modul (dari code)

Jumlah = file program `.pc` di `src/<modul>/` dan layar HTML di `htm/<modul>/`. Bidang usaha diambil dari dokumentasi repo (rc, dp, at) dan label layar sampel; yang belum pasti diberi tanda `?`.

| Modul | Program | Layar | Bidang / dugaan fungsi | Layar sampel |
|---|---|---|---|---|
| rc | 21 | 30 | Rekonsiliasi rekening Nostro/Vostro (FX, MT940/942/950, SWIFT) terdokumentasi | RC01..RC23, RC0001..0005 |
| dp | 168 | 58 | Deposit / rekening tabungan & transfer OTC terdokumentasi (DP01+DP02) | DP01..DP99, DPTP |
| at | 24 | 14 | Auto Transfer (reguler, massal internal/interbank, upload FTP, ARO deposito, outward remittance) | AT01, AT11, AT12, AT31..35, AT50, AT51, AT60, AT70 |
| re | 320 | 43 | Modul terbesar; transaksi harian settlement (isi belum terkonfirmasi) ? | RE0010..RE6902 |
| cm | 130 | 38 | Customer (nasabah) management / master | CM11..CM71 |
| ln | 114 | 58 | Loan (kredit) | LN11, LN56, LN72 |
| ch | 112 | 99 | Clearing / cash management besar ? | CH06, CH09, CH35 |
| iq | 80 | 76 | Inquiry (semua bentuk) | IQ04, IQ15, IQ63 |
| ec | 78 | 24 | E-channel / external channel ? | EC11, EC34, EC61 |
| rt | 72 | 43 | Remittance transfer (outward, BIC/bene) | RT20, RT31, RT92 |
| dt | 70 | 40 | Settlement / LLG ? (DT51 tampilkan llg_temp_group) | DT0023, DT51, DT92 |
| rl | 54 | 50 | Overdue / aging bucket (M-1..M-11, days past due) | RL2105, RL2701, RL54 |
| ot | 49 | 30 | Other transactions ? | OT39, OT66, OT71 |
| la | 44 | 20 | Loan admin (facility, batch) | LA03, LA43, LA52 |
| eb | 43 | 36 | Electronic banking / e-banking | EB06, EB31, EB41 |
| cg | 39 | 37 | (OP/leave kind dari CG18) ? | CG13, CG18 |
| fe | 30 | 27 | Foreign exchange dealing (convert, rate) | FE41, FE50, FE99 |
| ck | 29 | 32 | Check / cek | CK23, CK45, CK53 |
| ob | 25 | 15 | Interest/advance-deferred ? | OB13, OB14, OB22 |
| ei | 25 | 4 | Interface / message interchange (send/receive, response code) | EI03, EI04, EI31 |
| fa | 22 | 13 | ? | FA01, FA11, FA14 |
| fb | 23 | 19 | Bond / securities (issue, securities ID) | FB03, FB10, FB21 |
| ff | 20 | 14 | Foreign funds ? | FF05, FF12, FF18 |
| pf | 20 | 20 | Product factory / produk ? | PF01, PF23, PF33 |
| mm | 20 | 19 | Money market (borrow/loan, broker) | MM02, MM11, MM42 |
| dm | 16 | 14 | Collateral / appraisal | DM13, DM31, DM3101 |
| fn | 16 | 22 | ? | FN07, FN21, FN71 |
| ma | 16 | 22 | Money market dealing / broker (BIC, appr user) | MA14, MA21, MA31 |
| aw | 15 | 9 | Housing subscription (AYDA grade) ? | AW02, AW14, AW21 |
| qr | 14 | 4 | QR payment (merchant ID, response) | QR03, QR04, QR31 |
| va | 11 | 8 | Billing / collection (bill type, contract) | VA01, VA03, VA12 |
| rp | 11 | 5 | ? | RP01, RP02, RP11 |
| sb | 9 | 6 | Safe deposit box (box no, maturity) | SB03, SB11, SB41 |
| sp | 8 | 5 | Standing payment / auto debit (auto acct, cycle) | SP11, SP12, SP31 |
| tr | 7 | 8 | ? | TR11, TR22 |
| ca | 6 | 5 | Current account / CA ? | CA02, CA11, CA12 |
| mc | 4 | 6 | ? | MC01, MC12, MC1101 |
| mt | 6 | 7 | ? | MT02, MT15 |
| bp | 6 | 0 | ? (batch) | - |
| bf | 17 | 10 | ? | BF01, BF03, BF04 |
| si | 1 | 1 | ? | SI11 |
| lr | 4 | 7 | Recovery ? | LR01, LR14, LR1401 |
| rm | 7 | 7 | Remittance message / MT | RM02, RM07, RM08 |
| hf | 5 | 0 | ? | - |
| gt | 8 | 1 | ? | GT11 |
| hq | 9 | 9 | Head office / accounting (HQBIC, HQBIX, HQDOC) | HQBIC, HQBIX, HQDOC |
| co | 17 | 14 | ? | CO11, CO81, CO88 |
| ep | 19 | 13 | Budget / expense (BUDG AMT, Busi Cd) | EP02, EP03, EP14 |
| em, eo, er, es, et, eu, sw, wf, yi | ~30 | 0 | Util / batch kecil | - |

## Menu Terdokumentasi (nama bisnis asli)

### Modul RC - Rekonsiliasi Valuta Asing (21 program)
Alur: Shadow (internal) vs Actual (SWIFT MT940/942/950) -> buat Pending -> konfirmasi -> auto/manual rekonsiliasi -> laporan MT950 ke bank koresponden.

| Layer | Program | Fungsi |
|---|---|---|
| Inquiry | rco0100 | Penulisan/Inquiry Pending Ledger (Shadow & Actual) |
| Inquiry | rco0200 | Inquiry Balance Ledger (dengan average) |
| Inquiry | rco0300 | Inquiry informasi rekonsiliasi (UNION + GROUP BY) |
| Inquiry | rco0400 | Inquiry detail pending ledger |
| Inquiry | rco0500 | Inquiry adjustment statement / rekonsiliasi |
| Inquiry | rco0600 | Inquiry MT942 / MT900 / MT910 |
| Inquiry | rco0700 | Inquiry jumlah Shadow & Actual (dashboard) |
| Inquiry | rco0900 | Inquiry balance ledger lengkap (Shadow/Actual/Vostro) |
| Entry | rco1000 | Update/koreksi REF-NO Actual |
| Entry | rco1100 | Penulisan Pending Ledger ACTUAL (INSERT/DELETE) |
| Entry | rco1200 | Konfirmasi / batal konfirmasi ACTUAL |
| Entry | rco1300 | Non Ref-No processing & pembuatan MT299 |
| Entry | rco1400 | Penulisan Balance Ledger harian |
| Entry | rco1500 | Pembatalan Balance Ledger |
| Entry | rco1900 | Konfirmasi ACTUAL (versi baru) |
| Rekon | rco2000 | Auto rekonsiliasi (pencocokan otomatis) |
| Rekon | rco2100 | Manual rekonsiliasi v1 |
| Rekon | rco2111 | Incoming remittance MT950 parser |
| Rekon | rco2200 | Pembatalan rekonsiliasi |
| Rekon | rco2300 | Manual rekonsiliasi lanjutan v2 |
| Rekon | rco3000 | Pembuatan MT950 (laporan SWIFT outgoing) |

Tabel: `AFEX_RCH_BASE`, `AFEX_RCH_PEND`, `AFEX_RCH_RECON`, `AFEX_RCH_BAL`, `AFEX_RCH_SEQNO`, `AFEX_RCH_VOSTRO`.

### Modul DP - Deposit & Rekening (168 program, 58 layar)
Layar utama (kode transaksi `DPxx`): DP01..DP13 (pembukaan/mutasi rekening, DP01 buka tabungan, DP02 transfer OTC), DP21, DP31..33, DP41, DP50..53, DP61..63, DP71..88 (beragam transaksi deposit), DP91..99, DPTP. Field FML berprefix `FIDP00_*` / `FODP00_*`.

### Modul AT - Auto Transfer (24 program, 14 layar)
Layar: AT01 (reguler entry), AT02, AT11/AT12 (File upload Excel map), AT31..35 (massal internal / interbank / FTP), AT3301 (CCY inquiry), AT50/AT51 (eksekusi on-demand / batch), AT60, AT70 (outward remittance, ARO deposito).

Tabel: `ACOM_ATB_BASE` (instruksi), `ACOM_ATB_UPLOAD` (staging file), `ACOM_ATB_HIS` (histori).

## Catatan Implementasi di Java

1. **Model menu** sebaiknya meniru `ACOM_COMH_MENU`: `MenuGroup` + `MenuItem(code, name, url, parent_path, sort_seq, active)` dengan path `Group|Sub|Screen` dan kode transaksi sebagai kunci akses.
2. **Keamanan**: replikasi model grant (`role -> menu` setara `ACOM_COMH_GRPGRANT` dan `ACOM_COMH_USERGRANT`); navbar dirender sesuai grant role (di app Java saat ini: TELLER/SUPERVISOR/ADMIN, RC13 dibatasi Supervisor/Admin).
3. **Modul yang sudah ada di Java** (`belajar-springboot`): RC (CIF registrasi/inquiry/audit & maintenance) dan DP (buka rekening + transfer) adalah versi mini dari modul RC/DP InoAn.
4. **Nama menu asli yang persis** (untuk seed data menu), query DB Oracle sistem InoAn:
   ```sql
   SELECT MENU_ID, UPMU_CODE, MENU_NAME, SRC_URL, SORT_SEQ, STS
     FROM ACOM_COMH_MENU
    WHERE STS = '0'
    ORDER BY SORT_SEQ;
   ```
5. **Data sumber** dokumen ini tersimpan lokal: `/tmp/inoan_tree.txt`, `/tmp/htm_screens.txt`, `/tmp/src_programs.txt`, `/tmp/inoan_ref/README_rc.md`, `/tmp/inoan_htm/md/at/README.md`.