const oopen = XMLHttpRequest.prototype.open;
XMLHttpRequest.prototype.open = function (method, url, ...rest) {
    this.addEventListener('load', function () {
        if(url.indexOf('/vt/pb') > 0)
        {
        debugger;
        }
    });
    console.log(`M: ${method} U:${url}`);
    console.trace();
    return oopen.call(this, method, url, ...rest);
};

https://www.google.at/maps/vt/pb=!1m4!1m3!1i11!2i1480!3i949!2m3!1e0!2sm!3i543267038!3m7!2sde!5e1105!12m4!1e68!2m2!1sset!2sRoadmap!4e1!5m4!1e4!8m2!1e0!1e1!6m7!1e12!2i2!26m1!4b1!39b1!44e1!50e0!23i1381174!23i1382412!23i10203575!23i1381938!23i1381033!23i1368782!23i1368785!23i1375246!23i1385853!23i46990830!28i54377
    const origOpen = XMLHttpRequest.prototype.open;
XMLHttpRequest.prototype.open = function () {
    debugger;
    this.addEventListener('load', function () {
        console.debug('hu');
    });
    origOpen.apply(this, arguments);
};



const OListener = XMLHttpRequest.addEventListener;
XMLHttpRequest.addEventListener = function () {
    debugger;
    OListener(arguments);
}

(function (original) {
    XMLHttpRequest.prototype.addEventListener = function (type, listener, useCapture) {
        console.log('addEventListener ' + type, listener, useCapture);

        L = function () {
            debugger;
            listener();
        }

        return original.apply(this, [type, L, useCapture]);
    }
})(XMLHttpRequest.prototype.addEventListener);


var mke = function (a) {
    if (!a.qk(2))
        return null;
    if (!a.V) {
        var b = kT(a)
            , c = a.H
            , d = null;
        if (b && _.H(b, 4) || c.Nb())
            d = new _.wv,
            b && _.H(_.hke(b), 0) && d.Ja(new _.wv(_.hke(b).$[0])),
            !_.H(d, 0) && c.Nb() && _.yv(d, c.Nb()),
            _.H(d, 3) || (b = c.NN()) && _.Awb(_.Cv(d), b);
        a.V = d
    }
    return a.V
};

_.nnc = function (a) {
    if ("undefined" != typeof _.OC && a instanceof _.OC) {
        var b = new Rmc;
        a.zx(Nlc) && Xmc(b, new _.Gkc(Tkc(a.jz(Nlc))));
        a.zx(Qlc) && Ymc(b, new _.Hkc(Zkc(a.jz(Qlc))));
        a.zx(zlc) && Wmc(b, new _.lI(vlc(a.jz(zlc))));
        a.zx(Olc) && Zmc(b, new _.Fkc(Jkc(a.jz(Olc))));
        a.zx(Hlc) && hnc(b, new rkc(wkc(a.jz(Hlc))));
        a = b
    } else
        "undefined" != typeof _.MC && a instanceof _.MC ? (b = new Rmc,
        a.vj(_.Rgc) && Smc(b, new _.Nmc(nkc(a.Oo(_.Rgc)))),
        a.vj(flc) && Tmc(b, new _.Imc(dlc(a.Oo(flc)))),
        a.vj(umc) && Umc(b, new _.Lmc(qmc(a.Oo(umc)))),
        a.vj(llc) && Vmc(b, new _.Hmc(jlc(a.Oo(llc)))),
        a.vj(Alc) && Wmc(b, new _.lI(vlc(a.Oo(Alc)))),
        a.vj(Flc) && gnc(b, new _.Omc(Dlc(a.Oo(Flc)))),
        a.vj(hmc) && $mc(b, new _.qkc(fmc(a.Oo(hmc)))),
        a.vj(zmc) && anc(b, new _.Mmc(xmc(a.Oo(zmc)))),
        a.vj(Mlc) && cnc(b, new _.Gmc(Klc(a.Oo(Mlc)))),
        a.vj(Glc) && hnc(b, new rkc(wkc(a.Oo(Glc)))),
        a.vj(_.Ngc) && bnc(b, new _.Pmc(mkc(a.Oo(_.Ngc)))),
        a.vj(_.Dmc) && inc(b, new _.Fmc(Bmc(a.Oo(_.Dmc)))),
        a.vj(_.Zlc) && jnc(b, new _.Emc(Xlc(a.Oo(_.Zlc)))),
        a.vj(Plc) && Ymc(b, new _.Hkc(Zkc(a.Oo(Plc)))),
        (a = a.vj(_.GC) && a.Oo(_.GC)) && !_.H(b, 4) && _.Av(new _.wv(_.Q(new _.lI(_.Q(b, 4)), 0)), a.jg()),
            a = b) : "undefined" != typeof _.PC && a instanceof _.PC ? (b = new Rmc,
        a.Kv(vmc) && Umc(b, new _.Lmc(qmc(a.TA(vmc)))),
        a.Kv(Blc) && Wmc(b, new _.lI(vlc(a.TA(Blc)))),
        a.Kv(plc) && dnc(b, new _.Jmc(nlc(a.TA(plc)))),
        a.Kv(lmc) && enc(b, new _.Kmc(jmc(a.TA(lmc)))),
        a.Kv(dmc) && fnc(b, new Qmc(bmc(a.TA(dmc)))),
        a.Kv(Ilc) && hnc(b, new rkc(wkc(a.TA(Ilc)))),
            a = b) : a = "undefined" != typeof _.yAb && a instanceof _.yAb ? new Rmc : null;
    return a
}

// XHR1 catched hrere on success, breakpoint conditions

var Dg = function(a) {
    if (a.i && "undefined" != typeof Oa && (!a.T[1] || 4 != Eg(a) || 2 != Fg(a)))
        if (a.o && 4 == Eg(a))
            tg(a.ya, 0, a);
        else if (a.dispatchEvent(ca),
        4 == Eg(a)) {
            a.i = !1;
            try {
                Gg(a) ? (a.dispatchEvent(ba), // a.g.responseText.startsWith('XHR1')
                    a.dispatchEvent("success")) : Bg(a) // a.g.responseURL.indexOf('1m7') >= 0
            } finally {
                Cg(a)
            }
        }
}

http://kh.google.com/rt/earth/BulkMetadata/pb=!1m21s305170607352!2u859
// 10, 8, 8, 13, 16, 149, 46, 24, 234, 29, 18, 4, 8, 3, 16, 1, 34, 220, 29, 10, 180, 27, 137, 80, 78, 71, 13, 10, 26, 10, 0, 0, 0, 13, 73, 72, 68, 82, 0, 0, 3, 241, 0, 0, 0, 20, 8, 3, 0, 0, 0, 242, 126, 77, 11, 0, 0, 0, 9, 112, 72, 89, 115, 0, 0, 0, 28, 0, 0, 0, 28, 0, 15, 1, 185, 143, 0, 0, 0, 51, 80, 76, 84, 69, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0

//    4 classes, in each element 3 is variable
// 1    !1m4!1m3!1i15!2i26892!3i17144!
// 3   !1m4!1m3!1i13!2i6721!3i4286!
// 4   !1m4!1m3!1i13!2i6721!3i4287!
// 2   !1m4!1m3!1i15!2i26898!3i17145!
//
//    another class
//    !1m5!1m4!1i12!2i6723!3i4286!