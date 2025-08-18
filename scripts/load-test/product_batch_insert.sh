#!/usr/bin/env bash
set -euo pipefail

# ===== 환경 변수 =====
COMPOSE_FILE=./docker/infra-compose.yml
SERVICE=mysql
DB=loopers
USER=root
PASS=root

TOTAL=100000        # 총 삽입 수
BATCH=10000         # 배치당 삽입 수(= 1만 x 10회)
RUNS=$(( (TOTAL + BATCH - 1) / BATCH ))

echo "[INFO] Insert products in batches: total=${TOTAL}, batch=${BATCH}, runs=${RUNS}"

# 사전 체크: 브랜드가 1개 이상 있어야 함
brand_cnt=$(docker compose -f "$COMPOSE_FILE" exec -T "$SERVICE" \
  sh -c "MYSQL_PWD=$PASS mysql -u$USER -Nse 'SELECT COUNT(*) FROM $DB.tb_brand;'")
if [ "$brand_cnt" -lt 1 ]; then
  echo "[ERROR] tb_brand 가 비어있음. 먼저 브랜드를 넣어주세요."
  exit 1
fi

for i in $(seq 1 $RUNS); do
  echo "[INFO] Batch ${i}/${RUNS} - inserting ${BATCH} rows..."

  docker compose -f "$COMPOSE_FILE" exec -T "$SERVICE" \
    mysql -u"$USER" -p"$PASS" -D "$DB" <<'SQL'
-- 브랜드 개수/순번 준비
SET @brand_cnt := (SELECT COUNT(*) FROM tb_brand);
SET @r := 0;

-- 1만 건 생성 & 브랜드 균등 매핑
INSERT INTO tb_product
  (name, stock, price, status, brand_id, like_count, created_at, updated_at, deleted_at)
SELECT
  -- 이름: [형용사] [명사] [번호]
  CONCAT(
    ELT(FLOOR(1 + RAND()*20),
      'Ultra','Smart','Eco','Pro','Max','Mini','Lite','Prime','Quantum','Swift',
      'Neo','Hyper','Aero','Crystal','Dynamic','Fusion','Nova','Omega','Aqua','Terra'
    ),
    ' ',
    ELT(FLOOR(1 + RAND()*20),
      'Phone','Watch','Speaker','Camera','Laptop','Keyboard','Mouse','Headset','Monitor','Charger',
      'Backpack','Shoes','Jacket','Bottle','Lamp','Scooter','Drone','Glasses','Router','Tablet'
    ),
    ' ',
    LPAD(FLOOR(1 + RAND()*9999), 4, '0')
  ) AS name,

  -- 재고 0~10,000
  FLOOR(RAND()*10001) AS stock,

  -- 가격 분포 (천/만 단위 라운딩)
  CASE FLOOR(1 + RAND()*3)
    WHEN 1 THEN (FLOOR((1000 + RAND()*98000)/100)*100)          -- 1천 ~ 9.9만 (100원 단위)
    WHEN 2 THEN (FLOOR((100000 + RAND()*800000)/1000)*1000)     -- 10만 ~ 90만 (1천원 단위)
    ELSE      (FLOOR((1000000 + RAND()*8900000)/1000)*1000)     -- 100만 ~ 990만 (1천원 단위)
  END AS price,

  -- 상태 (Enum: SELL / OUT_OF_STOCK / DISCONTINUED / HIDDEN)
  CASE
    WHEN RAND() < 0.70 THEN 'SELL'
    WHEN RAND() < 0.90 THEN 'OUT_OF_STOCK'
    WHEN RAND() < 0.95 THEN 'DISCONTINUED'
    ELSE 'HIDDEN'
  END AS status,

  -- 브랜드: 1..@brand_cnt 를 순환 매핑
  b.id AS brand_id,

  -- 좋아요 0~50,000
  FLOOR(RAND()*50001) AS like_count,

  -- 날짜: created <= updated <= deleted(옵션) <= NOW()
  (@c := TIMESTAMP(DATE_SUB(NOW(), INTERVAL FLOOR(RAND()*1095) DAY))) AS created_at,
  (@u := LEAST(DATE_ADD(@c, INTERVAL FLOOR(RAND()*365) DAY), NOW())) AS updated_at,
  CASE WHEN RAND() < 0.03
       THEN LEAST(DATE_ADD(@u, INTERVAL FLOOR(RAND()*30) DAY), NOW())
       ELSE NULL
  END AS deleted_at

FROM
  (
    -- 10,000행 더미 + 연속 번호 idx (1..10000)
    SELECT (@i := @i + 1) AS idx
    FROM (
      SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
      UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
    ) a
    CROSS JOIN (
      SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
      UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
    ) b
    CROSS JOIN (
      SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
      UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
    ) c
    CROSS JOIN (
      SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
      UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
    ) d,
    (SELECT @i := 0) init_i
  ) n
  JOIN (
    -- 브랜드에 1..brand_cnt 순번 부여
    SELECT id, (@r := @r + 1) AS rn
    FROM tb_brand, (SELECT @r := 0) r
    ORDER BY id
  ) b
  ON b.rn = 1 + MOD(n.idx - 1, @brand_cnt);  -- 1..brand_cnt 반복 매핑
SQL

done

echo "[INFO] Done. Current counts:"
docker compose -f "$COMPOSE_FILE" exec -T "$SERVICE" \
  sh -c "MYSQL_PWD=$PASS mysql -u$USER -Nse 'SELECT COUNT(*) AS products FROM $DB.tb_product;'"
