/* ============================================================
   Core Banking System - common.js
   Lapisan data dan perilaku layar legacy banking.
   Dipakai oleh index.html (shell) dan seluruh modul fragment.
   ============================================================ */

var CB = (function () {

    var session = null;

    function storeSession(s) {
        session = s;
        try { sessionStorage.setItem('cbs_session', JSON.stringify(s)); }
        catch (e) { /* abaikan */ }
    }

    function loadSession() {
        if (session) return session;
        try {
            var raw = sessionStorage.getItem('cbs_session');
            if (raw) session = JSON.parse(raw);
        } catch (e) { session = null; }
        return session;
    }

    function clearSession() {
        session = null;
        try { sessionStorage.removeItem('cbs_session'); } catch (e) { /* abaikan */ }
    }

    /* Header operator mengikuti sesi login aktif */
    function operatorId() {
        var s = loadSession();
        return (s && (s.nip || s.username)) || 'TELLER1';
    }

    function parseError(body) {
        if (body && body.message) return body.message;
        if (body && body.errors) return Object.values(body.errors).join('; ');
        return 'Terjadi kesalahan pada server.';
    }

    /* Wrapper fetch: mengembalikan data WebResponse jika sukses,
       melempar Error dengan pesan dari server saat gagal. */
    function api(path, opts) {
        opts = opts || {};
        var url = (opts.base ? opts.base : '') + path;

        var headers = {
            'Content-Type': 'application/json'
        };
        if (!opts.noOperator) headers['X-Operator-Id'] = operatorId();

        var init = {
            method: opts.method || 'GET',
            headers: headers
        };
        if (opts.body !== undefined) init.body = JSON.stringify(opts.body);

        return fetch(url, init).then(function (res) {
            return res.json().catch(function () { return null; }).then(function (body) {
                if (!res.ok) {
                    var err = new Error(parseError(body));
                    err.status = res.status;
                    throw err;
                }
                return body;
            });
        });
    }

    function fmtNum(v) {
        if (v === null || v === undefined || v === '') return '-';
        var n = Number(v);
        if (isNaN(n)) return String(v);
        return n.toLocaleString('id-ID', { maximumFractionDigits: 2 });
    }

    function fmtDate(s) {
        if (!s) return '-';
        var m = String(s).match(/^(\d{4})-(\d{2})-(\d{2})/);
        if (m) return m[3] + '/' + m[2] + '/' + m[1];
        return String(s);
    }

    function msg(elmId, type, text) {
        var el = document.getElementById(elmId);
        if (!el) return;
        el.className = 'cbs-msg' + (type ? ' ' + type : '');
        el.textContent = text || '';
    }

    function hideMsg(elmId) {
        var el = document.getElementById(elmId);
        if (el) el.className = 'cbs-msg';
    }

    function nowClock() {
        var d = new Date();
        function p(x) { return (x < 10 ? '0' : '') + x; }
        return p(d.getDate()) + '/' + p(d.getMonth() + 1) + '/' + d.getFullYear() +
            ' ' + p(d.getHours()) + ':' + p(d.getMinutes()) + ':' + p(d.getSeconds());
    }

    function procDate() {
        var d = new Date();
        function p(x) { return (x < 10 ? '0' : '') + x; }
        return p(d.getDate()) + '/' + p(d.getMonth() + 1) + '/' + d.getFullYear();
    }

    function esc(s) {
        if (s === null || s === undefined) return '';
        return String(s).replace(/[&<>"']/g, function (c) {
            return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c];
        });
    }

    function statusClass(v) {
        var s = String(v || '').toUpperCase();
        if (s === 'AKTIF' || s === 'ACTIVE' || s === 'SUCCESS') return 'st-aktif';
        if (s.indexOf('BLOCK') >= 0) return 'st-blocked';
        return 'st-pending';
    }

    /* Otorisasi menu: RC13 hanya untuk Supervisor / Admin */
    function auth(code) {
        var s = loadSession();
        var role = s ? String(s.role).toUpperCase() : '';
        if (code === 'RC13') {
            return role === 'SUPERVISOR' || role === 'ADMIN';
        }
        return true;
    }

    function logJson(label, obj) {
        var box = document.getElementById('jsonOutput');
        if (!box) return;
        box.textContent = '> ' + label + '\n' + JSON.stringify(obj, null, 2);
    }

    return {
        session: loadSession,
        storeSession: storeSession,
        clearSession: clearSession,
        operatorId: operatorId,
        api: api,
        fmtNum: fmtNum,
        fmtDate: fmtDate,
        msg: msg,
        hideMsg: hideMsg,
        nowClock: nowClock,
        procDate: procDate,
        esc: esc,
        statusClass: statusClass,
        auth: auth,
        logJson: logJson
    };
})();

/* ------------------------------------------------------------
   Kursor jam / tanggal pada status bar
   ------------------------------------------------------------ */
function cbTickClock() {
    var el = document.getElementById('statusClock');
    if (el) el.textContent = CB.nowClock();
}

/* ------------------------------------------------------------
   Shell: login / logout / menu
   ------------------------------------------------------------ */
function cbLoginSubmit(e) {
    if (e && e.preventDefault) e.preventDefault();
    var u = document.getElementById('loginUsername').value.trim();
    var p = document.getElementById('loginPassword').value;
    var errEl = document.getElementById('loginError');
    if (errEl) errEl.style.display = 'none';

    CB.api('/api/auth/login', { method: 'POST', body: { username: u, password: p } })
        .then(function (res) {
            CB.storeSession(res.data);
            cbEnterShell();
        })
        .catch(function (err) {
            if (errEl) {
                errEl.textContent = err.message;
                errEl.style.display = 'block';
            }
        });
}

function cbQuickFill(user) {
    document.getElementById('loginUsername').value = user;
    document.getElementById('loginPassword').value = 'password123';
}

function cbLogout() {
    CB.api('/api/auth/logout', { method: 'POST' }).catch(function () { /* abaikan */ });
    CB.clearSession();
    cbShowLogin();
}

function cbShowLogin() {
    var s = CB.session();
    document.getElementById('loginModal').style.display = s ? 'none' : 'flex';
    if (!s) document.getElementById('shellView').style.display = 'none';
}

function cbEnterShell() {
    var s = CB.session();
    if (!s) return cbShowLogin();

    document.getElementById('loginModal').style.display = 'none';
    document.getElementById('shellView').style.display = 'flex';

    var name = s.namaLengkap || '-';
    var nip = s.nip || '-';
    var role = s.role || '-';
    document.getElementById('badgeNama').textContent = name;
    document.getElementById('badgeNama2').textContent = name;
    document.getElementById('badgeNip').textContent = nip;
    document.getElementById('badgeRole').textContent = role;

    cbMenu('HOME');
}

function cbToggleGroup(id, headEl) {
    var g = document.getElementById(id);
    if (!g) return;
    var open = g.style.display !== 'none';
    g.style.display = open ? 'none' : 'block';
    var glyph = headEl.querySelector('.glyph');
    if (glyph) glyph.textContent = open ? '[+]' : '[-]';
}

function cbGoMenu(inputEl) {
    var code = inputEl.value.trim().toUpperCase();
    if (code) cbMenu(code);
}

function cbMenu(code) {
    var container = document.getElementById('moduleContainer');
    if (!container) return;

    if (code === 'HOME') {
        container.innerHTML = [
            '<div class="cbs-welcome">',
            '  <h2>Core Banking System</h2>',
            '  <p>Modul Pendaftaran Nasabah (RC), Pengelolaan CIF, Rekening Dana (DP), Auto Transfer (AT), dan General Ledger / Neraca (GL).</p>',
            '  <p>Pilih menu pada folder di sebelah kiri untuk membuka layar transaksi.</p>',
            '  <p>Teller: RC11, RC12, RC14, DP01-DP07, AT01, AT02. Supervisor/Admin: tambahan RC13 (audit trail) dan GL01-GL05 (neraca, jurnal, laba rugi, buku besar).</p>',
            '</div>'
        ].join('\n');
        return;
    }

    var m = String(code).match(/^(RC|DP|AT|GL)(\d{2})$/);
    if (!m) {
        CB.msg('shellMsg', 'err', 'Kode menu tidak dikenal: ' + code);
        return;
    }

    if (!CB.auth(code)) {
        CB.msg('shellMsg', 'err', 'Menu ' + code + ' hanya untuk Supervisor atau Admin.');
        return;
    }

    var group = m[1].toLowerCase();
    var file = code.toLowerCase();
    var path = '/modules/' + group + '/' + file + '.html';

    fetch(path)
        .then(function (res) {
            if (!res.ok) throw new Error('File modul tidak ditemukan: ' + path);
            return res.text();
        })
        .then(function (html) {
            container.innerHTML = html;
            window.scrollTo(0, 0);
            cbModuleReady(code);
        })
        .catch(function (err) {
            CB.msg('shellMsg', 'err', err.message);
        });
}

/* Inisialisasi konten setelah modul selesai dimuat */
function cbModuleReady(code) {
    switch (code) {
        case 'RC12':
            cbRC12Load();
            break;
        case 'RC13':
            cbRC13Load();
            break;
        case 'AT01':
            cbAT01Load();
            break;
        case 'GL01':
            cbGL01Load();
            break;
        case 'GL02':
            cbGL02Load();
            break;
        case 'GL03':
            cbGL03Load();
            break;
        case 'GL04':
            cbGL04Load();
            break;
        case 'GL05':
            cbGL05Load();
            break;
        case 'DP07':
            cbDP07Load();
            break;
        default:
            break;
    }
}

/* ------------------------------------------------------------
   Proxy CIF lookup (dipakai DP01 dan RC14)
   ------------------------------------------------------------ */
var cbLookupTarget = null;

function cbLookupOpen(inputId) {
    cbLookupTarget = inputId;
    document.getElementById('lookupModal').style.display = 'flex';
    document.getElementById('lookupSearchKeyword').value = '';
    document.getElementById('lookupResultBox').innerHTML =
        '<div class="cbs-empty">Masukkan kata kunci lalu tekan Cari.</div>';
    var kw = document.getElementById('lookupSearchKeyword');
    kw.focus();
}

function cbLookupClose() {
    cbLookupTarget = null;
    document.getElementById('lookupModal').style.display = 'none';
}

function cbLookupSearch() {
    var kw = document.getElementById('lookupSearchKeyword').value.trim();
    var box = document.getElementById('lookupResultBox');

    if (!kw) {
        box.innerHTML = '<div class="cbs-empty">NIK, nama, atau nomor CIF wajib diisi.</div>';
        return;
    }

    box.innerHTML = '<div class="cbs-empty">Mencari data CIF...</div>';

    CB.api('/api/nasabah/search?keyword=' + encodeURIComponent(kw))
        .then(function (res) {
            var rows = res.data || [];
            if (!rows.length) {
                box.innerHTML = '<div class="cbs-empty">TIDAK ADA DATA. Tidak ada nasabah cocok dengan: ' + CB.esc(kw) + '</div>';
                return;
            }
            var html = '<table class="cbs-grid"><thead><tr><th>CIF</th><th>NIK</th><th>Nama</th><th>Status</th></tr></thead><tbody>';
            rows.forEach(function (r) {
                html += '<tr style="cursor:pointer" tabindex="0" ' +
                    'onclick="cbLookupPick(\'' + CB.esc(r.cif) + '\')" ' +
                    'onkeydown="if(event.key===\'Enter\'||event.key===\' \')cbLookupPick(\'' + CB.esc(r.cif) + '\')">' +
                    '<td>' + CB.esc(r.cif) + '</td>' +
                    '<td>' + CB.esc(r.nik) + '</td>' +
                    '<td>' + CB.esc(r.namaLengkap) + '</td>' +
                    '<td class="' + CB.statusClass(r.statusNasabah) + '">' + CB.esc(r.statusNasabah) + '</td>' +
                    '</tr>';
            });
            html += '</tbody></table>';
            box.innerHTML = html;
        })
        .catch(function (err) {
            box.innerHTML = '<div class="cbs-empty">' + CB.esc(err.message) + '</div>';
        });
}

function cbLookupPick(cif) {
    if (cbLookupTarget) {
        var input = document.getElementById(cbLookupTarget);
        if (input) input.value = cif;
    }
    cbLookupClose();
}

/* ------------------------------------------------------------
   RC11: pendaftaran nasabah baru
   ------------------------------------------------------------ */
function cbSubmitRC11(e) {
    if (e && e.preventDefault) e.preventDefault();
    CB.hideMsg('rc11Msg');

    var body = {
        nik: document.getElementById('rc11Nik').value.trim(),
        namaLengkap: document.getElementById('rc11Nama').value.trim(),
        tempatLahir: document.getElementById('rc11Tempat').value.trim(),
        tanggalLahir: document.getElementById('rc11Tgl').value,
        jenisKelamin: document.getElementById('rc11Kelamin').value,
        ibuKandung: document.getElementById('rc11Ibu').value.trim(),
        alamat: document.getElementById('rc11Alamat').value.trim(),
        nomorHp: document.getElementById('rc11Hp').value.trim(),
        email: document.getElementById('rc11Email').value.trim(),
        pekerjaan: document.getElementById('rc11Pekerjaan').value.trim(),
        penghasilanBulanan: Number(document.getElementById('rc11Penghasilan').value) || 0
    };

    var submitBtn = document.getElementById('rc11Submit');
    if (submitBtn) submitBtn.disabled = true;

    CB.api('/api/nasabah/register', { method: 'POST', body: body })
        .then(function (res) {
            var d = res.data;
            CB.msg('rc11Msg', 'ok',
                'SELESAI. CIF ' + (d && d.cif ? d.cif : '-') + ' diterbitkan untuk ' +
                (d ? d.namaLengkap : 'nasabah baru') + '.');
            CB.logJson('POST /api/nasabah/register', res);
            if (e && e.target) e.target.reset();
        })
        .catch(function (err) {
            CB.msg('rc11Msg', 'err', 'REGISTRASI GAGAL: ' + err.message);
            CB.logJson('error', err.message);
        })
        .finally(function () {
            if (submitBtn) submitBtn.disabled = false;
        });
}

/* ------------------------------------------------------------
   RC12: inquiry database CIF
   ------------------------------------------------------------ */
function cbRC12Load() {
    var box = document.getElementById('rc12Body');
    if (!box) return;
    box.innerHTML = '<tr><td colspan="6" class="cbs-empty">MEMUAT DATA...</td></tr>';

    CB.api('/api/nasabah')
        .then(function (res) {
            cbsDrawRC12(res.data || []);
        })
        .catch(function (err) {
            box.innerHTML = '<tr><td colspan="6" class="cbs-empty">' + CB.esc(err.message) + '</td></tr>';
        });
}

function cbRC12Search() {
    var kw = document.getElementById('rc12Keyword').value.trim();
    var box = document.getElementById('rc12Body');
    if (!box) return;

    if (!kw) return cbRC12Load();

    box.innerHTML = '<tr><td colspan="6" class="cbs-empty">MENCARI DATA...</td></tr>';

    CB.api('/api/nasabah/search?keyword=' + encodeURIComponent(kw))
        .then(function (res) {
            var rows = res.data || [];
            if (!rows.length) {
                box.innerHTML = '<tr><td colspan="6" class="cbs-empty">TIDAK ADA DATA. ' +
                    'Tidak ada nasabah cocok dengan: ' + CB.esc(kw) + '</td></tr>';
                return;
            }
            cbsDrawRC12(rows);
        })
        .catch(function (err) {
            box.innerHTML = '<tr><td colspan="6" class="cbs-empty">' + CB.esc(err.message) + '</td></tr>';
        });
}

function cbsDrawRC12(rows) {
    var box = document.getElementById('rc12Body');
    if (!rows.length) {
        box.innerHTML = '<tr><td colspan="6" class="cbs-empty">TIDAK ADA DATA.</td></tr>';
        return;
    }
    var html = '';
    rows.forEach(function (r) {
        html += '<tr>' +
            '<td>' + CB.esc(r.cif) + '</td>' +
            '<td>' + CB.esc(r.nik) + '</td>' +
            '<td>' + CB.esc(r.namaLengkap) + '</td>' +
            '<td>' + CB.esc(r.tempatLahir) + ' / ' + CB.fmtDate(r.tanggalLahir) + '</td>' +
            '<td>' + CB.esc(r.pekerjaan) + '</td>' +
            '<td class="' + CB.statusClass(r.statusNasabah) + '">' + CB.esc(r.statusNasabah) + '</td>' +
            '</tr>';
    });
    box.innerHTML = html;
}

/* ------------------------------------------------------------
   RC13: audit trail history (Supervisor / Admin)
   ------------------------------------------------------------ */
function cbRC13Load() {
    var box = document.getElementById('rc13Body');
    if (!box) return;
    box.innerHTML = '<tr><td colspan="5" class="cbs-empty">MEMUAT DATA...</td></tr>';

    CB.api('/api/nasabah/history/all')
        .then(function (res) {
            var rows = res.data || [];
            if (!rows.length) {
                box.innerHTML = '<tr><td colspan="5" class="cbs-empty">TIDAK ADA DATA.</td></tr>';
                return;
            }
            var html = '';
            rows.forEach(function (h) {
                html += '<tr>' +
                    '<td>' + CB.fmtDate(h.createdAt) + '</td>' +
                    '<td>' + CB.esc(h.cif) + '</td>' +
                    '<td>' + CB.esc(h.actionType) + '</td>' +
                    '<td>' + CB.esc(h.keterangan) + '</td>' +
                    '<td>' + CB.esc(h.updatedBy) + '</td>' +
                    '</tr>';
            });
            box.innerHTML = html;
        })
        .catch(function (err) {
            box.innerHTML = '<tr><td colspan="5" class="cbs-empty">' + CB.esc(err.message) + '</td></tr>';
        });
}

/* ------------------------------------------------------------
   RC14: pemeliharaan profil CIF
   ------------------------------------------------------------ */
function cbRC14LoadCif() {
    var cif = document.getElementById('rc14Cif').value.trim();
    var form = document.getElementById('rc14Form');
    CB.hideMsg('rc14Msg');

    if (!cif) {
        CB.msg('rc14Msg', 'err', 'Nomor CIF wajib diisi.');
        return;
    }

    if (form) form.style.display = 'none';

    CB.api('/api/nasabah/' + encodeURIComponent(cif))
        .then(function (res) {
            cbsFillRC14(res.data);
            if (form) form.style.display = 'block';
        })
        .catch(function (err) {
            CB.msg('rc14Msg', 'err', err.message);
        });
}

function cbsFillRC14(d) {
    document.getElementById('rc14CifRo').value = d.cif || '';
    document.getElementById('rc14NikRo').value = d.nik || '';
    document.getElementById('rc14Nama').value = d.namaLengkap || '';
    document.getElementById('rc14Alamat').value = d.alamat || '';
    document.getElementById('rc14Hp').value = d.nomorHp || '';
    document.getElementById('rc14Email').value = d.email || '';
    document.getElementById('rc14Pekerjaan').value = d.pekerjaan || '';
    document.getElementById('rc14Penghasilan').value = d.penghasilanBulanan != null ? d.penghasilanBulanan : '';
}

function cbSubmitRC14(e) {
    if (e && e.preventDefault) e.preventDefault();
    var cif = document.getElementById('rc14Cif').value.trim();
    CB.hideMsg('rc14Msg');

    if (!cif) {
        CB.msg('rc14Msg', 'err', 'Nomor CIF wajib diisi.');
        return;
    }

    var body = {
        namaLengkap: document.getElementById('rc14Nama').value.trim(),
        alamat: document.getElementById('rc14Alamat').value.trim(),
        nomorHp: document.getElementById('rc14Hp').value.trim(),
        email: document.getElementById('rc14Email').value.trim(),
        pekerjaan: document.getElementById('rc14Pekerjaan').value.trim(),
        penghasilanBulanan: Number(document.getElementById('rc14Penghasilan').value) || 0
    };

    CB.api('/api/nasabah/' + encodeURIComponent(cif), { method: 'PUT', body: body })
        .then(function (res) {
            CB.msg('rc14Msg', 'ok', 'SELESAI. Profil CIF ' + cif + ' berhasil diperbarui.', res);
            CB.logJson('PUT /api/nasabah/' + cif, res);
        })
        .catch(function (err) {
            CB.msg('rc14Msg', 'err', 'PEMBARUAN GAGAL: ' + err.message);
        });
}

/* ------------------------------------------------------------
   DP01: pembukaan rekening tabungan
   ------------------------------------------------------------ */
function cbSubmitDP01(e) {
    if (e && e.preventDefault) e.preventDefault();
    CB.hideMsg('dp01Msg');

    var body = {
        cif: document.getElementById('dp01Cif').value.trim(),
        jenisTabungan: document.getElementById('dp01Jenis').value,
        setoranAwal: Number(document.getElementById('dp01Setoran').value) || 0
    };

    CB.api('/api/rekening/create', { method: 'POST', body: body })
        .then(function (res) {
            var d = res.data;
            CB.msg('dp01Msg', 'ok',
                'SELESAI. Rekening ' + (d && d.nomorRekening ? d.nomorRekening : '-') +
                ' terbuka atas nama ' + (d ? d.namaNasabah : '-') + '. CIF: ' +
                (d ? d.cif : '-') + '.');
            CB.logJson('POST /api/rekening/create', res);
        })
        .catch(function (err) {
            CB.msg('dp01Msg', 'err', 'PEMBUKAAN GAGAL: ' + err.message);
        });
}

/* ------------------------------------------------------------
   DP02: transfer antar rekening
   ------------------------------------------------------------ */
function cbSubmitDP02(e) {
    if (e && e.preventDefault) e.preventDefault();
    CB.hideMsg('dp02Msg');

    var body = {
        nomorRekeningAsal: document.getElementById('dp02Asal').value.trim(),
        nomorRekeningTujuan: document.getElementById('dp02Tujuan').value.trim(),
        nominal: Number(document.getElementById('dp02Nominal').value) || 0,
        beritaTransfer: document.getElementById('dp02Berita').value.trim()
    };

    CB.api('/api/rekening/transfer', { method: 'POST', body: body })
        .then(function (res) {
            CB.msg('dp02Msg', 'ok', res.message);
            CB.logJson('POST /api/rekening/transfer', res);
        })
        .catch(function (err) {
            CB.msg('dp02Msg', 'err', 'TRANSFER GAGAL: ' + err.message);
        });
}

/* ------------------------------------------------------------
   DP03 Setoran tunai / DP04 Tarik tunai
   ------------------------------------------------------------ */
function cbSubmitDP03(e) {
    if (e && e.preventDefault) e.preventDefault();
    CB.hideMsg('dp03Msg');

    var url = '/api/rekening/setor';
    var msgId = 'dp03Msg';
    var label = 'SETORAN';

    CB.api(url, { method: 'POST', body: {
        nomorRekening: document.getElementById('dp03Norek').value.trim(),
        nominal: Number(document.getElementById('dp03Nominal').value) || 0
    } })
        .then(function (res) {
            CB.msg(msgId, 'ok', res.message);
            CB.logJson('POST ' + url, res);
        })
        .catch(function (err) {
            CB.msg(msgId, 'err', label + ' GAGAL: ' + err.message);
        });
}

function cbSubmitDP04(e) {
    if (e && e.preventDefault) e.preventDefault();
    CB.hideMsg('dp04Msg');

    CB.api('/api/rekening/tarik', { method: 'POST', body: {
        nomorRekening: document.getElementById('dp04Norek').value.trim(),
        nominal: Number(document.getElementById('dp04Nominal').value) || 0
    } })
        .then(function (res) {
            CB.msg('dp04Msg', 'ok', res.message);
            CB.logJson('POST /api/rekening/tarik', res);
        })
        .catch(function (err) {
            CB.msg('dp04Msg', 'err', 'PENARIKAN GAGAL: ' + err.message);
        });
}

/* ------------------------------------------------------------
   DP05 Penutupan rekening
   ------------------------------------------------------------ */
function cbSubmitDP05(e) {
    if (e && e.preventDefault) e.preventDefault();
    CB.hideMsg('dp05Msg');

    var norek = document.getElementById('dp05Norek').value.trim();
    if (!norek) { CB.msg('dp05Msg', 'err', 'Nomor rekening wajib diisi.'); return; }

    CB.api('/api/rekening/' + encodeURIComponent(norek) + '/penutupan', { method: 'POST' })
        .then(function (res) {
            CB.msg('dp05Msg', 'ok', res.message);
            CB.logJson('POST /api/rekening/{no}/penutupan', res);
        })
        .catch(function (err) {
            CB.msg('dp05Msg', 'err', 'PENUTUPAN GAGAL: ' + err.message);
        });
}

/* ------------------------------------------------------------
   DP06 Blokir / buka blokir rekening
   ------------------------------------------------------------ */
function cbSubmitDP06(mode) {
    CB.hideMsg('dp06Msg');

    var norek = document.getElementById('dp06Norek').value.trim();
    if (!norek) { CB.msg('dp06Msg', 'err', 'Nomor rekening wajib diisi.'); return; }

    var url = mode === 'BLOCK'
        ? '/api/rekening/' + encodeURIComponent(norek) + '/blokir'
        : '/api/rekening/' + encodeURIComponent(norek) + '/buka-blokir';

    CB.api(url, { method: 'POST' })
        .then(function (res) {
            CB.msg('dp06Msg', 'ok', res.message);
            CB.logJson('POST ' + url, res);
        })
        .catch(function (err) {
            CB.msg('dp06Msg', 'err', 'OPERASI GAGAL: ' + err.message);
        });
}

/* ------------------------------------------------------------
   AT01 Inquiry daftar auto transfer
   ------------------------------------------------------------ */
function cbAT01Load() {
    var body = document.getElementById('at01Body');
    var historyBox = document.getElementById('at01History');
    if (!body) return;
    if (historyBox) historyBox.innerHTML = '';
    CB.hideMsg('at01Msg');

    body.innerHTML = '<tr><td colspan="9" class="cbs-empty">MEMUAT DATA...</td></tr>';

    var status = '';
    var sel = document.getElementById('at01Status');
    if (sel) status = sel.value;

    var url = '/api/auto-transfer' + (status ? '?status=' + encodeURIComponent(status) : '');

    CB.api(url, { method: 'GET' })
        .then(function (res) {
            var rows = res.data || [];
            if (!rows.length) {
                body.innerHTML = '<tr><td colspan="9" class="cbs-empty">TIDAK ADA DATA.</td></tr>';
                return;
            }
            var h = '';
            for (var i = 0; i < rows.length; i++) {
                var r = rows[i];
                var stClass = CB.statusClass(r.statusAt);
                var aksi = '';
                if (r.statusAt === 'AKTIF') {
                    aksi = '<button class="cbs-btn cbs-btn-sm" onclick="cbATStop(\'' + r.kodeInstruksi + '\', this)">Stop</button>'
                        + ' <button class="cbs-btn cbs-btn-sm" onclick="cbATExecOne(\'' + r.kodeInstruksi + '\', this)">Eksekusi</button>';
                } else {
                    aksi = '<button class="cbs-btn cbs-btn-sm cbs-btn-ok" onclick="cbATStart(\'' + r.kodeInstruksi + '\', this)">Aktifkan</button>';
                }
                aksi += ' <button class="cbs-btn cbs-btn-sm" onclick="cbATHistory(\'' + r.kodeInstruksi + '\')">Riwayat</button>';

                h += '<tr>'
                    + '<td class="cbs-mono">' + CB.esc(r.kodeInstruksi) + '</td>'
                    + '<td class="cbs-mono">' + CB.esc(r.nomorRekeningDebit) + '</td>'
                    + '<td class="cbs-mono">' + CB.esc(r.nomorRekeningKredit) + '</td>'
                    + '<td class="cbs-num">Rp ' + CB.fmtNum(r.nominal) + '</td>'
                    + '<td>' + CB.esc(r.periode) + '</td>'
                    + '<td class="cbs-date">' + CB.fmtDate(r.tanggalMulai) + '</td>'
                    + '<td class="cbs-date">' + CB.fmtDate(r.tanggalBerikutnya) + '</td>'
                    + '<td><span class="' + stClass + '">' + CB.esc(r.statusAt) + '</span></td>'
                    + '<td>' + aksi + '</td>'
                    + '</tr>';
            }
            body.innerHTML = h;
        })
        .catch(function (err) {
            body.innerHTML = '<tr><td colspan="9" class="cbs-empty">' + CB.esc(err.message) + '</td></tr>';
        });
}

function cbATAction(url, msgId, okText, btn) {
    CB.hideMsg(msgId);
    if (btn) { btn.disabled = true; }
    CB.api(url, { method: 'POST' })
        .then(function (res) {
            if (btn) btn.disabled = false;
            CB.msg(msgId, 'ok', okText + ' ' + res.message);
            cbAT01Load();
        })
        .catch(function (err) {
            if (btn) btn.disabled = false;
            CB.msg(msgId, 'err', err.message);
        });
}

function cbATStop(kode, btn) {
    cbATAction('/api/auto-transfer/' + encodeURIComponent(kode) + '/stop', 'at01Msg', 'TERHENTI.', btn);
}

function cbATStart(kode, btn) {
    cbATAction('/api/auto-transfer/' + encodeURIComponent(kode) + '/start', 'at01Msg', 'AKTIF.', btn);
}

function cbATExecOne(kode, btn) {
    CB.hideMsg('at01Msg');
    if (btn) btn.disabled = true;
    CB.api('/api/auto-transfer/execute', { method: 'POST', body: { kodeInstruksi: kode } })
        .then(function (res) {
            if (btn) btn.disabled = false;
            CB.msg('at01Msg', 'ok', res.message);
            cbAT01Load();
        })
        .catch(function (err) {
            if (btn) btn.disabled = false;
            CB.msg('at01Msg', 'err', err.message);
        });
}

function cbATExecAll(btn) {
    CB.hideMsg('at01Msg');
    if (btn) btn.disabled = true;
    CB.api('/api/auto-transfer/execute', { method: 'POST' })
        .then(function (res) {
            if (btn) btn.disabled = false;
            CB.msg('at01Msg', 'ok', res.message);
            cbAT01Load();
        })
        .catch(function (err) {
            if (btn) btn.disabled = false;
            CB.msg('at01Msg', 'err', err.message);
        });
}

function cbATHistory(kode) {
    var box = document.getElementById('at01History');
    if (!box) return;

    box.innerHTML = '<div class="cbs-panel"><div class="cbs-panel-title">RIWAYAT EKSEKUSI ' + CB.esc(kode) + '</div>'
        + '<p class="cbs-empty">MEMUAT...</p></div>';

    CB.api('/api/auto-transfer/' + encodeURIComponent(kode) + '/history', { method: 'GET' })
        .then(function (res) {
            var rows = res.data || [];
            if (!rows.length) {
                box.innerHTML = '<div class="cbs-panel"><div class="cbs-panel-title">RIWAYAT EKSEKUSI ' + CB.esc(kode) + '</div>'
                    + '<p class="cbs-empty">BELUM ADA EKSEKUSI.</p></div>';
                return;
            }
            var h = '<div class="cbs-panel"><div class="cbs-panel-title">RIWAYAT EKSEKUSI ' + CB.esc(kode) + '</div>'
                + '<table class="cbs-grid"><thead><tr><th>Waktu</th><th>Status</th><th>Operator</th><th>Keterangan</th></tr></thead><tbody>';
            for (var i = 0; i < rows.length; i++) {
                var r = rows[i];
                h += '<tr>'
                    + '<td class="cbs-date">' + (r.tanggalEksekusi || '-') + '</td>'
                    + '<td><span class="' + CB.statusClass(r.statusEksekusi) + '">' + CB.esc(r.statusEksekusi) + '</span></td>'
                    + '<td>' + CB.esc(r.operatorEksekusi) + '</td>'
                    + '<td>' + CB.esc(r.keterangan) + '</td>'
                    + '</tr>';
            }
            h += '</tbody></table></div>';
            box.innerHTML = h;
        })
        .catch(function (err) {
            box.innerHTML = '<div class="cbs-panel"><div class="cbs-panel-title">RIWAYAT EKSEKUSI</div>'
                + '<p class="cbs-empty">' + CB.esc(err.message) + '</p></div>';
        });
}

/* ------------------------------------------------------------
   AT02 Registrasi auto transfer
   ------------------------------------------------------------ */
function cbSubmitAT02(e) {
    if (e && e.preventDefault) e.preventDefault();
    CB.hideMsg('at02Msg');

    var body = {
        nomorRekeningDebit: document.getElementById('at02Debit').value.trim(),
        nomorRekeningKredit: document.getElementById('at02Kredit').value.trim(),
        nominal: Number(document.getElementById('at02Nominal').value) || 0,
        periode: document.getElementById('at02Periode').value,
        tanggalMulai: document.getElementById('at02Mulai').value || null
    };

    CB.api('/api/auto-transfer/register', { method: 'POST', body: body })
        .then(function (res) {
            CB.msg('at02Msg', 'ok', res.message);
            CB.logJson('POST /api/auto-transfer/register', res);
            document.getElementById('at02Submit').disabled = true;
        })
        .catch(function (err) {
            CB.msg('at02Msg', 'err', 'REGISTRASI GAGAL: ' + err.message);
        });
}

/* ------------------------------------------------------------
   GL01 Inquiry neraca
   ------------------------------------------------------------ */
function cbGL01Load() {
    CB.hideMsg('gl01Msg');

    var aktBox = document.getElementById('gl01Aktiva');
    var pasBox = document.getElementById('gl01Pasiva');
    var ringkas = document.getElementById('gl01Ringkas');
    if (!aktBox || !pasBox) return;

    aktBox.innerHTML = '<tr><td colspan="3" class="cbs-empty">MEMUAT DATA...</td></tr>';
    pasBox.innerHTML = '<tr><td colspan="3" class="cbs-empty">MEMUAT DATA...</td></tr>';

    var tglEl = document.getElementById('gl01Tanggal');
    var tgl = tglEl && tglEl.value ? '?tanggal=' + encodeURIComponent(tglEl.value) : '';

    CB.api('/api/neraca' + tgl, { method: 'GET' })
        .then(function (res) {
            renderPane(aktBox, res.data.aktiva || []);
            renderPane(pasBox, res.data.pasiva || []);

            var status = Number(res.data.selisih) === 0 ? 'st-aktif' : 'st-blocked';
            var label = Number(res.data.selisih) === 0 ? 'SEIMBANG' : 'TIDAK SEIMBANG (' + CB.fmtNum(res.data.selisih) + ')';
            ringkas.innerHTML = 'Total Aktiva: <b>Rp ' + CB.fmtNum(res.data.totalAktiva)
                + '</b> &nbsp;|&nbsp; Total Pasiva: <b>Rp ' + CB.fmtNum(res.data.totalPasiva)
                + '</b> &nbsp;|&nbsp; <span class="' + status + '">' + label + '</span>'
                + ' &nbsp;|&nbsp; Neraca per: ' + CB.fmtDate(res.data.tanggal);
        })
        .catch(function (err) {
            aktBox.innerHTML = '<tr><td colspan="3" class="cbs-empty">' + CB.esc(err.message) + '</td></tr>';
            pasBox.innerHTML = '';
            ringkas.innerHTML = '';
        });
}

function renderPane(box, rows) {
    if (!rows.length) {
        box.innerHTML = '<tr><td colspan="3" class="cbs-empty">TIDAK ADA AKUN.</td></tr>';
        return;
    }
    var h = '';
    for (var i = 0; i < rows.length; i++) {
        var r = rows[i];
        h += '<tr>'
            + '<td class="cbs-mono">' + CB.esc(r.kodeAkun) + '</td>'
            + '<td>' + CB.esc(r.namaAkun) + '</td>'
            + '<td class="cbs-num">' + CB.fmtNum(r.saldo) + '</td>'
            + '</tr>';
    }
    box.innerHTML = h;
}

/* ------------------------------------------------------------
   GL02 Daftar akun (chart of accounts)
   ------------------------------------------------------------ */
function cbGL02Load() {
    CB.hideMsg('gl02Msg');

    var body = document.getElementById('gl02Body');
    if (!body) return;

    body.innerHTML = '<tr><td colspan="6" class="cbs-empty">MEMUAT DATA...</td></tr>';

    CB.api('/api/coa', { method: 'GET' })
        .then(function (res) {
            var rows = res.data || [];
            if (!rows.length) {
                body.innerHTML = '<tr><td colspan="6" class="cbs-empty">TIDAK ADA AKUN.</td></tr>';
                return;
            }
            var h = '';
            for (var i = 0; i < rows.length; i++) {
                var r = rows[i];
                var stClass = r.aktif ? 'st-aktif' : 'st-blocked';
                h += '<tr>'
                    + '<td class="cbs-mono">' + CB.esc(r.kodeAkun) + '</td>'
                    + '<td>' + CB.esc(r.namaAkun) + '</td>'
                    + '<td>' + CB.esc(r.jenisAkun) + '</td>'
                    + '<td>' + CB.esc(r.posisiNormal) + '</td>'
                    + '<td class="cbs-num">' + CB.fmtNum(r.saldo) + '</td>'
                    + '<td><span class="' + stClass + '">' + (r.aktif ? 'AKTIF' : 'NON-AKTIF') + '</span></td>'
                    + '</tr>';
            }
            body.innerHTML = h;
        })
        .catch(function (err) {
            body.innerHTML = '<tr><td colspan="6" class="cbs-empty">' + CB.esc(err.message) + '</td></tr>';
        });
}

/* ------------------------------------------------------------
   GL03 Jurnal umum (posting manual debet = kredit)
   ------------------------------------------------------------ */
function cbGL03Load() {
    CB.hideMsg('gl03Msg');

    var debSel = document.getElementById('gl03Debit');
    var krdSel = document.getElementById('gl03Kredit');
    if (!debSel || !krdSel) return;

    debSel.innerHTML = '<option value="">MEMUAT...</option>';
    krdSel.innerHTML = '<option value="">MEMUAT...</option>';

    CB.api('/api/coa', { method: 'GET' })
        .then(function (res) {
            var rows = (res.data || []).filter(function (a) { return a.aktif; });
            var opts = '';
            for (var i = 0; i < rows.length; i++) {
                opts += '<option value="' + CB.esc(rows[i].kodeAkun) + '">'
                    + CB.esc(rows[i].kodeAkun) + ' - ' + CB.esc(rows[i].namaAkun)
                    + ' (' + CB.esc(rows[i].jenisAkun) + ')</option>';
            }
            debSel.innerHTML = opts;
            krdSel.innerHTML = opts;
        })
        .catch(function (err) {
            debSel.innerHTML = '<option value="">' + CB.esc(err.message) + '</option>';
            krdSel.innerHTML = '<option value="">' + CB.esc(err.message) + '</option>';
        });
}

function cbSubmitGL03(e) {
    if (e && e.preventDefault) e.preventDefault();
    CB.hideMsg('gl03Msg');

    var body = {
        kodeAkunDebit: document.getElementById('gl03Debit').value,
        kodeAkunKredit: document.getElementById('gl03Kredit').value,
        nominal: Number(document.getElementById('gl03Nominal').value) || 0,
        keterangan: document.getElementById('gl03Keterangan').value.trim()
    };

    CB.api('/api/jurnal', { method: 'POST', body: body })
        .then(function (res) {
            CB.msg('gl03Msg', 'ok', res.message);
            CB.logJson('POST /api/jurnal', res);
            document.getElementById('gl03Submit').disabled = true;
        })
        .catch(function (err) {
            CB.msg('gl03Msg', 'err', 'JURNAL GAGAL: ' + err.message);
        });
}

/* ------------------------------------------------------------
   GL04 Laporan laba rugi
   ------------------------------------------------------------ */
function cbGL04Load() {
    CB.hideMsg('gl04Msg');

    var pendBox = document.getElementById('gl04Pendapatan');
    var bebanBox = document.getElementById('gl04Beban');
    var ringkas = document.getElementById('gl04Ringkas');
    if (!pendBox || !bebanBox) return;

    pendBox.innerHTML = '<tr><td colspan="3" class="cbs-empty">MEMUAT DATA...</td></tr>';
    bebanBox.innerHTML = '<tr><td colspan="3" class="cbs-empty">MEMUAT DATA...</td></tr>';

    var tglEl = document.getElementById('gl04Tanggal');
    var tgl = tglEl && tglEl.value ? '?tanggal=' + encodeURIComponent(tglEl.value) : '';

    CB.api('/api/laba-rugi' + tgl, { method: 'GET' })
        .then(function (res) {
            renderPane(pendBox, res.data.pendapatan || []);
            renderPane(bebanBox, res.data.beban || []);

            var laba = Number(res.data.labaBersih);
            var status = laba >= 0 ? 'st-aktif' : 'st-blocked';
            var label = (laba >= 0 ? 'LABA' : 'RUGI') + ': Rp ' + CB.fmtNum(Math.abs(laba));
            ringkas.innerHTML = 'Total Pendapatan: <b>Rp ' + CB.fmtNum(res.data.totalPendapatan)
                + '</b> &nbsp;|&nbsp; Total Beban: <b>Rp ' + CB.fmtNum(res.data.totalBeban)
                + '</b> &nbsp;|&nbsp; <span class="' + status + '">' + label + '</span>'
                + ' &nbsp;|&nbsp; Per: ' + CB.fmtDate(res.data.tanggal);
        })
        .catch(function (err) {
            pendBox.innerHTML = '<tr><td colspan="3" class="cbs-empty">' + CB.esc(err.message) + '</td></tr>';
            bebanBox.innerHTML = '';
            ringkas.innerHTML = '';
        });
}

/* ------------------------------------------------------------
   GL05 Buku besar (ledger per akun GL)
   ------------------------------------------------------------ */
function cbGL05Load() {
    CB.hideMsg('gl05Msg');

    var akunSel = document.getElementById('gl05Akun');
    var body = document.getElementById('gl05Body');
    var foot = document.getElementById('gl05Foot');
    var ringkas = document.getElementById('gl05Ringkas');
    if (!body) return;

    if (!akunSel.dataset.loaded) {
        CB.api('/api/coa', { method: 'GET' }).then(function (res) {
            var opts = '<option value="">SEMUA AKUN</option>';
            (res.data || []).forEach(function (a) {
                if (!a.aktif) return;
                opts += '<option value="' + CB.esc(a.kodeAkun) + '">'
                    + CB.esc(a.kodeAkun) + ' - ' + CB.esc(a.namaAkun)
                    + ' (' + CB.esc(a.jenisAkun) + ')</option>';
            });
            akunSel.innerHTML = opts;
            akunSel.dataset.loaded = '1';
        }).catch(function () { /* biarkan pilihan kosong */ });
    }

    body.innerHTML = '<tr><td colspan="8" class="cbs-empty">MEMUAT DATA...</td></tr>';
    foot.innerHTML = '';

    var params = '?dari=' + encodeURIComponent(gl05Val('gl05Dari')) + '&sampai=' + encodeURIComponent(gl05Val('gl05Sampai'));
    var kode = akunSel.value;
    if (kode) params += '&kodeAkun=' + encodeURIComponent(kode);

    CB.api('/api/buku-besar' + params, { method: 'GET' })
        .then(function (res) {
            var rows = res.data.baris || [];
            if (!rows.length) {
                body.innerHTML = '<tr><td colspan="8" class="cbs-empty">TIDAK ADA TRANSAKSI pada rentang tanggal tersebut.</td></tr>';
            } else {
                var html = '';
                for (var i = 0; i < rows.length; i++) {
                    var r = rows[i];
                    var cls = r.sisi === 'DEBIT' ? 'st-aktif' : '';
                    html += '<tr>'
                        + '<td class="cbs-date">' + CB.fmtDate(r.tanggal) + '</td>'
                        + '<td class="cbs-mono">' + CB.esc(r.kodeAkun) + '</td>'
                        + '<td><span class="' + (r.sisi === 'DEBIT' ? 'st-aktif' : 'st-blocked') + '">'
                        + CB.esc(r.sisi) + '</span></td>'
                        + '<td class="cbs-num ' + cls + '">' + (r.sisi === 'DEBIT' ? '' : '-') + CB.fmtNum(r.nominal) + '</td>'
                        + '<td class="cbs-mono">' + CB.esc(r.refTransaksi || '-') + '</td>'
                        + '<td>' + CB.esc(r.keterangan || '-') + '</td>'
                        + '<td>' + CB.esc(r.operatorId || '-') + '</td>'
                        + '<td class="cbs-num">' + CB.fmtNum(r.saldo) + '</td>'
                        + '</tr>';
                }
                body.innerHTML = html;
            }

            if (res.data.saldoAkhir !== undefined && res.data.saldoAkhir !== null) {
                foot.innerHTML = '<tr>'
                    + '<td colspan="3">SALDO AWAL TIDAK DITAMPILKAN (terbawa dari sebelumnya)</td>'
                    + '<td class="cbs-num">Total Debet ' + CB.fmtNum(res.data.totalDebit) + '</td>'
                    + '<td colspan="2">Total Kredit ' + CB.fmtNum(res.data.totalKredit) + '</td>'
                    + '<td class="cbs-num">Saldo Akhir</td>'
                    + '<td class="cbs-num">' + CB.fmtNum(res.data.saldoAkhir) + '</td>'
                    + '</tr>';
            }
            ringkas.innerHTML = (res.data.kodeAkun || 'SEMUA AKUN')
                + (res.data.namaAkun ? ' - ' + CB.esc(res.data.namaAkun) : '')
                + ' &nbsp;|&nbsp; ' + CB.fmtDate(res.data.tanggalDari)
                + ' s/d ' + CB.fmtDate(res.data.tanggalSampai)
                + ' &nbsp;|&nbsp; ' + rows.length + ' baris';
        })
        .catch(function (err) {
            body.innerHTML = '<tr><td colspan="8" class="cbs-empty">' + CB.esc(err.message) + '</td></tr>';
            foot.innerHTML = '';
            ringkas.innerHTML = '';
        });
}

function gl05Val(id) {
    var el = document.getElementById(id);
    return el && el.value ? el.value : '';
}

/* ------------------------------------------------------------
   DP07 Mutasi rekening (laporan per rekening)
   ------------------------------------------------------------ */
function cbDP07Load() {
    CB.hideMsg('dp07Msg');

    var noRek = document.getElementById('dp07NoRek');
    var body = document.getElementById('dp07Body');
    var foot = document.getElementById('dp07Foot');
    var info = document.getElementById('dp07Info');
    if (!body) return;

    var params;
    if (noRek && noRek.value.trim()) {
        params = '/' + encodeURIComponent(noRek.value.trim())
            + '/mutasi?dari=' + encodeURIComponent(dp07Val('dp07Dari')) + '&sampai=' + encodeURIComponent(dp07Val('dp07Sampai'));
    } else {
        info.innerHTML = 'Masukkan nomor rekening (contoh: 3436196555) lalu klik Tampilkan.';
        foot.innerHTML = '';
        return;
    }

    info.innerHTML = 'MEMUAT MUTASI...';
    body.innerHTML = '<tr><td colspan="5" class="cbs-empty">MEMUAT DATA...</td></tr>';
    foot.innerHTML = '';

    CB.api('/api/rekening' + params, { method: 'GET' })
        .then(function (res) {
            var d = res.data;
            var rows = d.baris || [];
            info.innerHTML = 'Rekening: <b>' + CB.esc(d.nomorRekening)
                + '</b> &nbsp;|&nbsp; Nasabah: <b>' + CB.esc(d.namaNasabah || '-')
                + '</b> &nbsp;|&nbsp; Periode: ' + CB.fmtDate(d.tanggalDari) + ' s/d ' + CB.fmtDate(d.tanggalSampai)
                + ' &nbsp;|&nbsp; <b>Saldo akhir: Rp ' + CB.fmtNum(d.saldoAkhir) + '</b>';

            if (!rows.length) {
                body.innerHTML = '<tr><td colspan="5" class="cbs-empty">TIDAK ADA MUTASI pada periode tersebut.</td></tr>';
                foot.innerHTML = '';
                return;
            }

            var html = '';
            for (var i = 0; i < rows.length; i++) {
                var r = rows[i];
                html += '<tr>'
                    + '<td class="cbs-date">' + CB.fmtDate(r.tanggal) + '</td>'
                    + '<td><span class="' + (r.debit ? 'st-blocked' : 'st-aktif') + '">'
                    + CB.esc(r.tipeTransaksi) + '</span></td>'
                    + '<td>' + CB.esc(r.deskripsi || '-') + '</td>'
                    + '<td class="cbs-num">' + (r.debit ? CB.fmtNum(r.debit) : '-') + '</td>'
                    + '<td class="cbs-num">' + (r.kredit ? CB.fmtNum(r.kredit) : '-') + '</td>'
                    + '</tr>';
            }
            body.innerHTML = html;
            foot.innerHTML = '<tr>'
                + '<td colspan="3">TOTAL</td>'
                + '<td class="cbs-num">' + CB.fmtNum(d.totalDebit) + '</td>'
                + '<td class="cbs-num">' + CB.fmtNum(d.totalKredit) + '</td>'
                + '</tr>';
        })
        .catch(function (err) {
            info.innerHTML = '';
            body.innerHTML = '<tr><td colspan="5" class="cbs-empty">' + CB.esc(err.message) + '</td></tr>';
            foot.innerHTML = '';
        });
}

function dp07Val(id) {
    var el = document.getElementById(id);
    return el && el.value ? el.value : '';
}

/* ------------------------------------------------------------
   Kunci keyboard: Escape menutup window lookup
   ------------------------------------------------------------ */
document.addEventListener('keydown', function (ev) {
    if (ev.key === 'Escape') {
        var lookup = document.getElementById('lookupModal');
        if (lookup && lookup.style.display === 'flex') cbLookupClose();
    }
});

/* ------------------------------------------------------------
   Inisialisasi saat dokumen shell siap
   ------------------------------------------------------------ */
document.addEventListener('DOMContentLoaded', function () {
    if (CB.session()) {
        cbEnterShell();
    } else {
        cbShowLogin();
    }

    setInterval(cbTickClock, 1000);
    cbTickClock();

    document.getElementById('procDate').textContent = CB.procDate();
});