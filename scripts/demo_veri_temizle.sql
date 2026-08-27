-- Demo verisini KOMPLE kaldırır. Anahtar: demo hesabının e-postası
-- (demo_veri_uret.py içindeki DEMO_EPOSTA ile aynı olmalı).
--
-- ads'e bağlı tabloların hemen hepsi ON DELETE CASCADE ya da SET NULL —
-- tek istisna potential_matches (FK'sinde ON DELETE yok → RESTRICT).
-- Bu yüzden önce o temizleniyor; yoksa demo ilan bir eşleştirmeye girmişse
-- ads silme adımı FK hatasıyla durur.
DO $$
DECLARE
    demo_user_id BIGINT;
BEGIN
    SELECT uid INTO demo_user_id FROM users WHERE email = 'demo-veri@patimati.me';
    IF demo_user_id IS NULL THEN
        RAISE NOTICE 'Demo hesabı yok; silinecek bir şey bulunamadı.';
        RETURN;
    END IF;

    DELETE FROM potential_matches
    WHERE ad_a_id IN (SELECT id FROM ads WHERE user_id = demo_user_id)
       OR ad_b_id IN (SELECT id FROM ads WHERE user_id = demo_user_id);

    DELETE FROM ads WHERE user_id = demo_user_id;
    DELETE FROM users WHERE uid = demo_user_id;
END $$;
