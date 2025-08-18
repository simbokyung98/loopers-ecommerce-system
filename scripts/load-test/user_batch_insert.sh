#!/usr/bin/env bash
set -euo pipefail

# ===== 환경 변수 =====
COMPOSE_FILE=${COMPOSE_FILE:-./docker/infra-compose.yml}
SERVICE=${SERVICE:-mysql}
DB=${DB:-loopers}
USER=${USER:-root}
PASS=${PASS:-root}

echo "[INFO] Seed 10 users into ${DB}.tb_user (idempotent)"

# 테이블 존재/접속 체크 (간단 count)
user_cnt=$(docker compose -f "$COMPOSE_FILE" exec -T "$SERVICE" \
  sh -c "MYSQL_PWD=$PASS mysql -u$USER -Nse 'SELECT COUNT(*) FROM $DB.tb_user;'") || {
  echo "[ERROR] cannot query ${DB}.tb_user (check DB/container)."
  exit 1
}
echo "[INFO] current tb_user count: ${user_cnt}"

# 10명 시드 (u1..u10) — login_id UNIQUE 충돌 시에도 안전하게 동작
docker compose -f "$COMPOSE_FILE" exec -T "$SERVICE" \
  mysql -u"$USER" -p"$PASS" -D "$DB" <<'SQL'
INSERT INTO tb_user (login_id, gender, brith, email)
VALUES
  ('u1','M','1990-01-01','u1@example.com'),
  ('u2','F','1990-01-02','u2@example.com'),
  ('u3','M','1990-01-03','u3@example.com'),
  ('u4','F','1990-01-04','u4@example.com'),
  ('u5','M','1990-01-05','u5@example.com'),
  ('u6','F','1990-01-06','u6@example.com'),
  ('u7','M','1990-01-07','u7@example.com'),
  ('u8','F','1990-01-08','u8@example.com'),
  ('u9','M','1990-01-09','u9@example.com'),
  ('u10','F','1990-01-10','u10@example.com')
ON DUPLICATE KEY UPDATE
  email = email; -- idempotent no-op (login_id UNIQUE일 때 재실행해도 에러 없이 통과)
SQL

echo "[INFO] Done. Current 10 users:"
docker compose -f "$COMPOSE_FILE" exec -T "$SERVICE" \
  sh -c "MYSQL_PWD=$PASS mysql -u$USER -e \"SELECT id,login_id,gender,brith,email FROM $DB.tb_user WHERE login_id REGEXP '^u(10|[1-9])$' ORDER BY id;\""
