#!/usr/bin/env bash
# =============================================================================
# graphify.sh — สร้าง/อัปเดต "แผนที่โค้ด" (knowledge graph) ของโปรเจกต์
#
# ผลลัพธ์อยู่ใน graphify-out/
#   graph.html       เปิดในเบราว์เซอร์ → เห็นไฟล์/คลาส/ฟังก์ชันและเส้นเชื่อมว่าใครเรียกใคร
#   GRAPH_REPORT.md  สรุป: จุดศูนย์กลางของโค้ด, กลุ่มโค้ดที่เกี่ยวข้องกัน
#   graph.json       ข้อมูลดิบ (ใช้กับคำสั่ง graphify query/explain/path)
#
# ติดตั้งครั้งแรก:   pip install --user graphifyy
# ใช้งาน:          ./scripts/graphify.sh
# ถามแผนที่:        graphify explain "GameActivity"
#                   graphify path "LibraryScreen" "GameFileResolver"
# =============================================================================
set -euo pipefail
cd "$(dirname "$0")/.."

if command -v graphify >/dev/null 2>&1; then G=(graphify); else G=(python -m graphify); fi

# update = อ่านโค้ดด้วย AST (ไม่ต้องใช้ AI/API key) แล้วสร้าง graph ใหม่
"${G[@]}" update . --force
echo "เปิดดู: graphify-out/graph.html  และ  graphify-out/GRAPH_REPORT.md"
