#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Backend enum'larının değer listesi TAAHHÜT EDİLEN dosyayla aynı mı?

NEDEN VAR: Ön yüz ve arka yüz aynı kavramlar için ayrı değer listeleri tutuyor.
19.08 taramasında 13 enum'un 6'sında ayrışma ölçüldü; kullanıcıya yansıyan
sonuçlar somuttu (sahiplendirme ilanında yaşlı hayvan seçilemiyor, backend'in
tanımadığı desen değerleri gönderiliyor, ilan 8'de tasma "YES" iken arayüz
"bilinmiyor" gösteriyor). Elle senkron tutma bu turda başarısız oldu ve
başarısızlık HİÇBİR YERDE görünmedi.

NEDEN İKİ TARAFLI DEĞİL: üç depo da private ⇒ ön yüz CI'ı arka yüz enum'larını
depolar arası jeton olmadan okuyamaz. Jeton yerine TAAHHÜT EDİLEN ANLIK GÖRÜNTÜ
seçildi: bu betik backend enum'larını kendi kaynağından üretir ve depodaki
`enum-aynasi.json` ile karşılaştırır. Böylece bir enum'a değer eklendiğinde,
silindiğinde ya da yeniden adlandırıldığında bu TEK dosyanın diff'i isteğin
içinde GÖRÜNÜR olur — sessiz hata gürültülü hataya döner. Ön yüz aynı dosyanın
kopyasını kendi tarafında sınar (`client/src/services/enum-aynasi.json`).

KULLANIM:  python scripts/enum-aynasi.py            # denetle (CI bunu koşar)
           python scripts/enum-aynasi.py --uret     # dosyayı yeniden üret
           python scripts/enum-aynasi.py --kendini-sina   # çözümleyiciyi sına
ÇIKIŞ   :  0 = temiz · 1 = ayna bayat · 2 = ölçüm yapılamadı
"""
import json
import os
import re
import sys

KOK = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
KAYNAK_KOKU = os.path.join(KOK, "src", "main", "java")
AYNA_DOSYASI = os.path.join(KOK, "enum-aynasi.json")

# `enum Ad {` ile `enum AdType {` ayrımı için sınır gerekiyor; ayrıca
# `implements` / `<...>` taşıyan bildirimler de tutulmalı.
ENUM_BILDIRIMI = re.compile(r"\benum\s+([A-Z][A-Za-z0-9_]*)\s*(?:implements[^{]*)?\{")

JAVA_KIMLIGI = re.compile(r"[A-Za-z_$][A-Za-z0-9_$]*")


def yorumlari_sil(kaynak):
    """Yorumları siler ama dizgi sabitlerinin İÇİNE dokunmaz.

    Düz `re.sub` yeterli değil: `"http://..."` gibi bir dizgi satır yorumu
    sanılıp satırın kalanı yutulurdu. Enum gövdelerinde gerçekten dizgi var
    (`SENT("gönderildi")`), bu yüzden küçük bir tarayıcı yazılıyor.
    """
    sonuc = []
    i, n = 0, len(kaynak)
    while i < n:
        c = kaynak[i]
        if c == '"' or c == "'":
            tirnak = c
            sonuc.append(c)
            i += 1
            while i < n:
                sonuc.append(kaynak[i])
                if kaynak[i] == "\\":
                    if i + 1 < n:
                        sonuc.append(kaynak[i + 1])
                        i += 2
                        continue
                elif kaynak[i] == tirnak:
                    i += 1
                    break
                i += 1
            continue
        if c == "/" and i + 1 < n and kaynak[i + 1] == "/":
            while i < n and kaynak[i] != "\n":
                i += 1
            continue
        if c == "/" and i + 1 < n and kaynak[i + 1] == "*":
            i += 2
            while i + 1 < n and not (kaynak[i] == "*" and kaynak[i + 1] == "/"):
                i += 1
            i += 2
            continue
        sonuc.append(c)
        i += 1
    return "".join(sonuc)


def sabit_bolumu(kaynak, acilis):
    """Enum gövdesinin SABİT listesi: `{`ten sonraki ilk 1. seviye `;`ye kadar.

    `;` yoksa (yalnız sabit taşıyan enum) gövdenin sonuna kadar. Derinlik
    sayılmazsa iç içe gövdeli sabitler (`A { ... }`) listeyi erken keserdi.
    """
    derinlik = 0
    i = acilis
    n = len(kaynak)
    baslangic = acilis + 1
    while i < n:
        c = kaynak[i]
        if c in "{([":
            derinlik += 1
        elif c in "})]":
            derinlik -= 1
            if derinlik == 0:
                return kaynak[baslangic:i]
        elif c == ";" and derinlik == 1:
            return kaynak[baslangic:i]
        i += 1
    return None


def sabitleri_ayikla(bolum):
    """Virgülle ayrılmış sabitlerin adlarını verir; yapıcı argümanlarını atar."""
    adlar = []
    derinlik = 0
    parca = []
    for c in bolum:
        if c in "{([":
            derinlik += 1
        elif c in "})]":
            derinlik -= 1
        if c == "," and derinlik == 0:
            adlar.append("".join(parca))
            parca = []
        else:
            parca.append(c)
    adlar.append("".join(parca))

    temiz = []
    for ham in adlar:
        # Açıklama notlarını (@Deprecated gibi) at, kalan ilk kimliği al.
        ham = re.sub(r"@[A-Za-z0-9_.]+(\([^)]*\))?", " ", ham).strip()
        if not ham:
            continue
        eslesme = JAVA_KIMLIGI.match(ham)
        if eslesme:
            temiz.append(eslesme.group(0))
    return temiz


def enumlari_topla(kok):
    """`src/main` altındaki TÜM enum bildirimlerini tarar (sabit liste değil)."""
    bulunan = {}
    cakisan = []
    for dizin, _, dosyalar in os.walk(kok):
        for dosya in dosyalar:
            if not dosya.endswith(".java"):
                continue
            yol = os.path.join(dizin, dosya)
            with open(yol, encoding="utf-8") as f:
                kaynak = yorumlari_sil(f.read())

            for eslesme in ENUM_BILDIRIMI.finditer(kaynak):
                ad = eslesme.group(1)
                bolum = sabit_bolumu(kaynak, eslesme.end() - 1)
                if bolum is None:
                    continue
                degerler = sabitleri_ayikla(bolum)
                if not degerler:
                    # 0 sonuç ŞÜPHEDİR: değersiz enum bir çözümleme hatasıdır.
                    print("HATA: %s enum'undan hiç değer çıkmadı (%s)"
                          % (ad, os.path.relpath(yol, kok)), file=sys.stderr)
                    sys.exit(2)
                if ad in bulunan and bulunan[ad]["degerler"] != degerler:
                    cakisan.append(ad)
                bulunan[ad] = {
                    "kaynak": os.path.relpath(yol, KOK).replace("\\", "/"),
                    "degerler": degerler,
                }

    if cakisan:
        print("HATA: aynı adlı iki enum farklı değer taşıyor: %s"
              % ", ".join(sorted(set(cakisan))), file=sys.stderr)
        sys.exit(2)
    return dict(sorted(bulunan.items()))


def ayna_uret():
    return {
        "_aciklama": (
            "Backend enum'larının değer listesi. ELLE DÜZENLENMEZ — "
            "scripts/enum-aynasi.py --uret ile üretilir, CI bayatlığını denetler. "
            "Ön yüz bu dosyanın kopyasını client/src/services/enum-aynasi.json "
            "altında tutar ve kendi tiplerini ona karşı sınar."
        ),
        "enumlar": enumlari_topla(KAYNAK_KOKU),
    }


def yaz(ayna):
    with open(AYNA_DOSYASI, "w", encoding="utf-8", newline="\n") as f:
        json.dump(ayna, f, ensure_ascii=False, indent=2, sort_keys=False)
        f.write("\n")


def denetle():
    if not os.path.exists(AYNA_DOSYASI):
        print("HATA: enum-aynasi.json yok. `--uret` ile oluştur.", file=sys.stderr)
        return 1

    with open(AYNA_DOSYASI, encoding="utf-8") as f:
        depodaki = json.load(f)

    guncel = ayna_uret()
    eski = depodaki.get("enumlar", {})
    yeni = guncel["enumlar"]

    if eski == yeni:
        print("TEMİZ: %d enum, ayna güncel." % len(yeni))
        return 0

    print("AYNA BAYAT — backend enum'ları değişmiş ama enum-aynasi.json güncellenmemiş.\n")
    for ad in sorted(set(eski) | set(yeni)):
        if ad not in eski:
            print("  + YENİ ENUM   %s: %s" % (ad, ", ".join(yeni[ad]["degerler"])))
        elif ad not in yeni:
            print("  - SİLİNEN ENUM %s" % ad)
        elif eski[ad]["degerler"] != yeni[ad]["degerler"]:
            a, b = eski[ad]["degerler"], yeni[ad]["degerler"]
            eklenen = [d for d in b if d not in a]
            silinen = [d for d in a if d not in b]
            print("  ~ %s" % ad)
            if eklenen:
                print("      eklenen: %s" % ", ".join(eklenen))
            if silinen:
                print("      silinen: %s" % ", ".join(silinen))
            if not eklenen and not silinen:
                print("      sıra değişti: %s -> %s" % (", ".join(a), ", ".join(b)))
    print("\nÇÖZÜM: python scripts/enum-aynasi.py --uret  (ve ön yüzdeki kopyayı da güncelle)")
    return 1


def kendini_sina():
    """Çözümleyicinin KENDİSİNİ sınar; sınanmamış bekçi teslim edilmez."""
    vakalar = [
        ("yalnız sabit",
         "public enum A { BLACK, WHITE, GRAY; }", ["BLACK", "WHITE", "GRAY"]),
        ("yapıcı argümanlı — PushResult sınıfı",
         'public enum A { SENT("gönderildi"), FAILED("olmadı"); }',
         ["SENT", "FAILED"]),
        ("araya yorum girmiş",
         "public enum A { /** not */ X, // satır\n Y; }", ["X", "Y"]),
        ("dizgi içinde // var",
         'public enum A { X("http://a"), Y("b"); }', ["X", "Y"]),
        ("gövdeli sabit",
         "public enum A { X { void f() { } }, Y; }", ["X", "Y"]),
        ("noktalı virgülsüz (yalnız sabit)",
         "public enum A { X, Y }", ["X", "Y"]),
        ("implements taşıyan",
         "public enum A implements Serializable { X, Y; }", ["X", "Y"]),
        ("son sabitten sonra virgül",
         "public enum A { X, Y, ; }", ["X", "Y"]),
    ]

    hata = 0
    for ad, kaynak, beklenen in vakalar:
        temiz = yorumlari_sil(kaynak)
        eslesme = ENUM_BILDIRIMI.search(temiz)
        if not eslesme:
            print("  ✗ %-38s bildirim BULUNAMADI" % ad)
            hata += 1
            continue
        bulunan = sabitleri_ayikla(sabit_bolumu(temiz, eslesme.end() - 1))
        if bulunan == beklenen:
            print("  ✓ %-38s %s" % (ad, ", ".join(bulunan)))
        else:
            print("  ✗ %-38s beklenen %s, bulunan %s" % (ad, beklenen, bulunan))
            hata += 1

    # Negatif kontrol: karşılaştırma GERÇEKTEN ayırt ediyor mu? Doğru cevabı
    # bilerek eksik yazıp eşleşmemesi gerekiyor — yoksa yukarıdaki sekiz ✓
    # "her şeye evet diyen" bir karşılaştırmadan da gelebilirdi.
    temiz = yorumlari_sil("public enum A { X, Y; }")
    eslesme = ENUM_BILDIRIMI.search(temiz)
    if sabitleri_ayikla(sabit_bolumu(temiz, eslesme.end() - 1)) == ["X"]:
        print("  ✗ negatif kontrol: eksik beklenti GEÇTİ — karşılaştırma sağır")
        hata += 1
    else:
        print("  ✓ %-38s eksik beklenti reddedildi" % "negatif kontrol")

    print("\n%d/%d vaka geçti." % (len(vakalar) - hata, len(vakalar)))
    return 1 if hata else 0


def main():
    if "--kendini-sina" in sys.argv:
        return kendini_sina()
    if "--uret" in sys.argv:
        ayna = ayna_uret()
        yaz(ayna)
        print("ÜRETİLDİ: %d enum -> %s"
              % (len(ayna["enumlar"]), os.path.relpath(AYNA_DOSYASI, KOK)))
        return 0
    return denetle()


if __name__ == "__main__":
    sys.exit(main())
