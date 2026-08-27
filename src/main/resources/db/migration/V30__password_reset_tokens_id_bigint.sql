-- PasswordResetToken.id entity'de Long (BIGINT) -- V4'te SERIAL (INTEGER)
-- olarak oluşturulmuştu. Hibernate schema-validate bu uyuşmazlıkla
-- uygulamanın AÇILMASINI engelliyor ("Schema-validation: wrong column type
-- encountered in column [id] in table [password_reset_tokens]; found
-- [serial (Types#INTEGER)], but expecting [bigint (Types#BIGINT)]") --
-- taze/boş bir veritabanında canlı olarak doğrulandı. V4 migration'ının
-- kendisi DEĞİŞTİRİLMİYOR (zaten uygulanmış olabilir), yalnızca tip
-- düzeltiliyor.
ALTER TABLE password_reset_tokens ALTER COLUMN id TYPE BIGINT;
