import random

MIN_LAT, MAX_LAT = 40.1700, 40.2500
MIN_LON, MAX_LON = 28.8500, 29.0000

PHOTO_URLS = {
    'CAT': ["https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=800", "https://images.unsplash.com/photo-1573865526739-10659fec78a5?w=800"],
    'DOG': ["https://images.unsplash.com/photo-1543466835-00a7907e9de1?w=800", "https://images.unsplash.com/photo-1583511655857-d19b40a7a54e?w=800"]
}

def generate_sql():
    lines = [
        "DO $$",
        "DECLARE",
        "    demo_user_id BIGINT;",
        "    new_ad_id BIGINT;",
        "BEGIN",
        "    SELECT uid INTO demo_user_id FROM users LIMIT 1;",
        "    IF demo_user_id IS NULL THEN",
        "        RAISE EXCEPTION 'HATA: Veritabaninda hic kullanici yok!';",
        "    END IF;"
    ]

    for i in range(1, 401):
        lat, lon = round(random.uniform(MIN_LAT, MAX_LAT), 6), round(random.uniform(MIN_LON, MAX_LON), 6)
        ad_type = random.choice(['LOST', 'FOUND', 'ADOPTION'])
        species = random.choice(['CAT', 'DOG'])
        photo_url = random.choice(PHOTO_URLS[species])

        lines.append(f"    INSERT INTO ads (title, description, ad_type, species, breed, gender, age_group, coat_pattern, collar_status, eye_color, ear_tag_status, ear_notch_status, ai_status, location, city, district, user_id, active, resolution_status, created_at, updated_at)")
        lines.append(f"    VALUES ('Demo İlan {i}', 'Bursa Nilüfer demo verisi.', '{ad_type}', '{species}', 'MIXED_OR_UNKNOWN', 'UNKNOWN', 'UNKNOWN', 'UNKNOWN', 'UNKNOWN', 'UNKNOWN', 'UNKNOWN', 'UNKNOWN', 'DONE', ST_SetSRID(ST_MakePoint({lon}, {lat}), 4326), 'Bursa', 'Nilüfer', demo_user_id, true, 'NONE', NOW() - INTERVAL '{random.randint(1, 30)} days', NOW()) RETURNING id INTO new_ad_id;")
        lines.append(f"    INSERT INTO ad_photo_urls (ad_id, photo_url, photo_order) VALUES (new_ad_id, '{photo_url}', 0);")

    for i in range(1, 51):
        lat, lon = round(random.uniform(MIN_LAT, MAX_LAT), 6), round(random.uniform(MIN_LON, MAX_LON), 6)
        photo_url = random.choice(PHOTO_URLS['CAT'])

        lines.append(f"    INSERT INTO ads (title, description, ad_type, species, ai_status, location, city, district, user_id, active, resolution_status, created_at, updated_at)")
        lines.append(f"    VALUES ('Kavuşan Kayıp {i}', 'Demo', 'LOST', 'CAT', 'DONE', ST_SetSRID(ST_MakePoint({lon}, {lat}), 4326), 'Bursa', 'Nilüfer', demo_user_id, true, 'FOUND', NOW(), NOW()) RETURNING id INTO new_ad_id;")
        lines.append(f"    INSERT INTO ad_photo_urls (ad_id, photo_url, photo_order) VALUES (new_ad_id, '{photo_url}', 0);")

        lines.append(f"    INSERT INTO ads (title, description, ad_type, species, ai_status, location, city, district, user_id, active, resolution_status, created_at, updated_at)")
        lines.append(f"    VALUES ('Kavuşan Bulunan {i}', 'Demo', 'FOUND', 'CAT', 'DONE', ST_SetSRID(ST_MakePoint({lon}, {lat}), 4326), 'Bursa', 'Nilüfer', demo_user_id, true, 'FOUND', NOW(), NOW()) RETURNING id INTO new_ad_id;")
        lines.append(f"    INSERT INTO ad_photo_urls (ad_id, photo_url, photo_order) VALUES (new_ad_id, '{photo_url}', 0);")

    lines.append("END $$;")

    with open("demo_veri.sql", "w", encoding="utf-8") as f:
        f.write("\n".join(lines))

generate_sql()