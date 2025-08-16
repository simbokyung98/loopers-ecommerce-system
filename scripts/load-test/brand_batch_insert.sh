#!/usr/bin/env bash
set -euo pipefail

# ===== 환경 변수(필요하면 수정) =====
COMPOSE_FILE=./docker/infra-compose.yml
SERVICE=mysql                # docker compose 서비스명
DB=loopers
USER=root
PASS=root

TOTAL=1500                   # 총 삽입 건수
BATCH=300                    # 배치당 삽입 건수
RUNS=$(( (TOTAL + BATCH - 1) / BATCH ))

echo "[INFO] Insert brands in batches: total=${TOTAL}, batch=${BATCH}, runs=${RUNS}"

for i in $(seq 1 $RUNS); do
  echo "[INFO] Batch ${i}/${RUNS} - inserting up to ${BATCH} rows..."

  docker compose -f "$COMPOSE_FILE" exec -T "$SERVICE" \
    mysql -u"$USER" -p"$PASS" -D "$DB" <<SQL
-- 현재 최대 id를 시작점으로 사용(접미사 고유성 확보)
SET @start := (SELECT COALESCE(MAX(id),0) FROM tb_brand);
SET @rownum := 0;

INSERT INTO tb_brand (name, created_at, updated_at, deleted_at)
SELECT
  CONCAT(
    ELT(FLOOR(1 + RAND()*45),
      'Samsung','LG','Sony','Apple','Xiaomi','Panasonic','Philips','Nike','Adidas','Zara',
      'Uniqlo','H&M','Puma','Levis','IKEA','Muji','Nespresso','Dyson','KitchenAid','Fissler',
      'LocknLock','Bosch','Whirlpool','Miele','Under Armour','New Balance','Reebok','Columbia',
      'North Face','Patagonia','Converse','Crocs','Asics','Timberland','Fila','Gap','Banana Republic',
      'Coach','Gucci','Prada','Louis Vuitton','Chanel','Rolex','Casio','Seiko','Citizen'
    ),
    ' ',
    ELT(FLOOR(1 + RAND()*4), 'Korea','Global','Life','Fashion'),
    ' ',
    LPAD(@start + (@rownum := @rownum + 1), 6, '0')  -- 중복 완화용 접미사
  ) AS name,

  -- created_at: 최근 3년
  (@c := TIMESTAMP(DATE_SUB(NOW(), INTERVAL FLOOR(RAND()*1095) DAY))) AS created_at,

  -- updated_at: created_at 이후(최대 1년), NOW() 초과 금지
  (@u := LEAST(DATE_ADD(@c, INTERVAL FLOOR(RAND()*365) DAY), NOW())) AS updated_at,

  -- deleted_at: 5% 확률, updated_at 이후 30일 내, NOW() 초과 금지
  CASE
    WHEN RAND() < 0.05
      THEN LEAST(DATE_ADD(@u, INTERVAL FLOOR(RAND()*30) DAY), NOW())
    ELSE NULL
  END AS deleted_at

FROM (
  -- 더미 로우 BATCH개 생성 (여기선 300개)
  SELECT 1
  FROM (SELECT 0 UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3) a
  CROSS JOIN (SELECT 0 UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3) b
  CROSS JOIN (SELECT 0 UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3) c
  CROSS JOIN (SELECT 0 UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3) d
  CROSS JOIN (SELECT 0 UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3) e
  LIMIT ${BATCH}
) t;
SQL

done

echo "[INFO] Done. Current count:"
docker compose -f "$COMPOSE_FILE" exec -T "$SERVICE" \
  mysql -u"$USER" -p"$PASS" -D "$DB" -e "SELECT COUNT(*) AS brand_cnt FROM tb_brand;"
