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
            '  <p>Modul Pendaftaran Nasabah (RC), Pengelolaan CIF, dan Rekening Dana (DP).</p>',
            '  <p>Pilih menu pada folder di sebelah kiri untuk membuka layar transaksi.</p>',
            '  <p>Teller: RC11, RC12, RC14, DP01, DP02. Supervisor/Admin: tambahan RC13 (audit trail).</p>',
            '</div>'
        ].join('\n');
        return;
    }

    var m = String(code).match(/^(RC|DP)(\d{2})$/);
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