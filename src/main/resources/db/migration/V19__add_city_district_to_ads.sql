-- İlanlara il/ilçe: kartlarda ham koordinat ("39.9334, 32.8597") yerine
-- okunur konum göstermek ve ileride şehirle aramaya temel olmak için.
-- Kayıt anında form beyanından ya da koordinattan (ters geokodlama) dolar;
-- mevcut kayıtlar dağıtımdan sonra bir kez çağrılacak yönetici ucuyla dolar:
--   POST /api/admin/ads/backfill-location
ALTER TABLE ads ADD COLUMN city varchar(100);
ALTER TABLE ads ADD COLUMN district varchar(100);
