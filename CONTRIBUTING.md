# Nasıl çalışıyoruz

Tek depo, tek dal (`main`). Herkes kendi dosyasında çalışır, doğrudan main'e gönderir.

## Kim hangi dosyada

| Kişi | Dosyalar |
|---|---|
| Mohammed | `database/01_schema.sql`, `docs/diagrams/`, `docs/business-rules.md` |
| Humam | `database/02_seed.sql` … `database/05_transactions.sql` |
| Erdem | `app/`, dokümanın birleştirilmesi |

Başkasının dosyasında değişiklik gerekiyorsa önce grupta yaz.

## Her çalışmada

```bash
git pull                      # başlamadan önce
# ... çalış ...
git add .
git commit -m "db: add Products table"
git push                      # bitince
```

Push reddedilirse önce `git pull`, sonra tekrar `git push`.

## Tek kritik kural

**19 Kasım'dan sonra `01_schema.sql` içindeki tablo ve sütun isimleri gruba sormadan değişmez.** Sorgular ve arayüz bu isimlere bağlı.

## Birleştirme

21–22 Kasım: boş veritabanında `01` → `05` scriptleri çalıştırılır, arayüz bağlanıp her rolle denenir.
