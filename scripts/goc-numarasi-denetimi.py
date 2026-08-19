#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Bu daldaki bir göç numarası ORTAK DALDA başka bir dosyayla alınmış mı?

NEDEN VAR: 19.08'de aynı gün iki kez aynı göç numarası iki ayrı dalda kullanıldı
(önce V12, çözülür çözülmez V13). Flyway aynı numaradan iki dosya görünce göç
zincirini hiç başlatmaz ve UYGULAMA AÇILMAZ — üstelik hata birleştirme anında
değil, DAĞITIM anında ve her ortamda birden çıkar.

NEDEN TESTTEN AYRI: GocNumarasiCakismasiTest yalnız BU AĞAÇTAKİ dosyalara bakar.
Bir dal, ortak dalda zaten alınmış bir numarayı kullanıyorsa kendi ağacında
çakışma görünmez — özellikle isteğin hedefi ortak dal değilse. 19.08'de bekleyen
istek tam olarak buydu: hedefi main'di, main'de öteki V13 yoktu, dolayısıyla
ağaç-içi denetim yeşil yanardı. Bu betik git'e bakarak o boşluğu kapatır.

KULLANIM:  python scripts/goc-numarasi-denetimi.py [ortak-dal]
           (varsayılan ortak dal: origin/develop)
ÇIKIŞ   :  0 = temiz · 1 = çakışma · 2 = ölçüm yapılamadı (ortak dal okunamadı)
"""
import os
import re
import subprocess
import sys

DIZIN = "src/main/resources/db/migration"
SURUMLU = re.compile(r"^V([0-9][0-9_.]*)__.+\.sql$")


def surumu_normalize(ham):
    """Flyway '_' ile '.'yı aynı sayar, baştaki sıfırları yok sayar.
    V1_1 · V1.1 · V01.1 hepsi AYNI sürümdür."""
    parcalar = [p for p in ham.replace("_", ".").split(".") if p]
    return ".".join(str(int(p)) for p in parcalar)


def kabuk(*komut):
    s = subprocess.run(komut, capture_output=True, text=True)
    return s.returncode, s.stdout, s.stderr


def ortak_daldaki_gocler(ortak):
    kod, cikti, hata = kabuk("git", "ls-tree", "-r", "--name-only", ortak, "--", DIZIN)
    if kod != 0:
        return None, hata.strip()
    surumler = {}
    for yol in cikti.split():
        ad = os.path.basename(yol)
        m = SURUMLU.match(ad)
        if m:
            surumler[surumu_normalize(m.group(1))] = ad
    return surumler, None


def bu_daldaki_gocler():
    surumler = {}
    if not os.path.isdir(DIZIN):
        return surumler
    for ad in sorted(os.listdir(DIZIN)):
        m = SURUMLU.match(ad)
        if m:
            surumler[surumu_normalize(m.group(1))] = ad
    return surumler


def main():
    ortak = sys.argv[1] if len(sys.argv) > 1 else "origin/develop"
    print("GOC NUMARASI DENETIMI — ortak dal:", ortak)
    print("=" * 70)

    bizim = bu_daldaki_gocler()
    print("  bu dalda sürümlü göç : %d" % len(bizim))
    if not bizim:
        print("  KARSILASTIRMA BOS: '%s' altinda hic goc bulunamadi." % DIZIN)
        print("  Bu, 'cakisma yok' demek DEGIL — dizin yolu degismis olabilir.")
        return 2

    onlarin, hata = ortak_daldaki_gocler(ortak)
    if onlarin is None:
        print("  ORTAK DAL OKUNAMADI:", hata)
        print("  Denetim yapilamadi. CI'da 'fetch-depth: 0' ve ortak dalin")
        print("  cekilmis olmasi gerekiyor. Yesil saymak YANLIS olur.")
        return 2
    print("  ortak dalda sürümlü göç: %d" % len(onlarin))

    cakisan = []
    for surum, ad in sorted(bizim.items(), key=lambda x: [int(p) for p in x[0].split(".")]):
        oteki = onlarin.get(surum)
        if oteki and oteki != ad:
            cakisan.append((surum, ad, oteki))

    print()
    if not cakisan:
        print("SONUC: YESIL — bu dalin numaralari ortak dalla cakismiyor.")
        return 0

    for surum, bizimki, otekisi in cakisan:
        print("  CAKISMA  V%s" % surum)
        print("     bu dalda   : %s" % bizimki)
        print("     ortak dalda: %s" % otekisi)
    bos = max([int(s.split(".")[0]) for s in list(bizim) + list(onlarin)]) + 1
    print()
    print("  Ilk BOS numara: V%d" % bos)
    print("  Bu daldaki dosyayi o numaraya tasi. Tasimadan once numarayi ekibe")
    print("  duyur — yoksa ayni cakisma bir sonraki dalda tekrar dogar.")
    print()
    print("SONUC: KIRMIZI — %d cakisma. Birlesirse UYGULAMA ACILMAZ." % len(cakisan))
    return 1


if __name__ == "__main__":
    sys.exit(main())
