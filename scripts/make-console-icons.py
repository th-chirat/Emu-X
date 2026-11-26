import sys, os
import numpy as np, cv2
from PIL import Image
from scipy import ndimage

SRC = r"D:\For claude\Asset_icon"
OUT = sys.argv[1]
os.makedirs(OUT, exist_ok=True)

def load(name):
    return np.array(Image.open(os.path.join(SRC, name)).convert("RGBA"))

def cut_bg(a, pred):
    """ลบพื้นหลัง: pixel ที่ตรง pred และต่อกับขอบภาพ"""
    m = pred(a[..., :3].astype(int))
    lab, _ = ndimage.label(m)
    edge = set(np.unique(np.concatenate([lab[0], lab[-1], lab[:, 0], lab[:, -1]]))) - {0}
    bg = np.isin(lab, list(edge))
    a = a.copy()
    a[bg, 3] = 0
    # ขอบนุ่มขึ้นเล็กน้อย
    alpha = cv2.GaussianBlur(a[..., 3].astype(np.float32), (3, 3), 0)
    a[..., 3] = np.minimum(a[..., 3], alpha).astype(np.uint8)
    return a

def erase(a, boxes, pick):
    """ลบโลโก้: เลือก pixel ตัวอักษร (pick) ในกรอบ (x0,y0,x1,y1) แล้ว inpaint"""
    rgb = np.ascontiguousarray(a[..., :3])
    mask = np.zeros(rgb.shape[:2], np.uint8)
    f = rgb.astype(int)
    for x0, y0, x1, y1 in boxes:
        sub = f[y0:y1, x0:x1]
        mask[y0:y1, x0:x1] = np.where(pick(sub), 255, 0)
    mask = cv2.dilate(mask, np.ones((5, 5), np.uint8))
    rgb = cv2.inpaint(rgb, mask, 7, cv2.INPAINT_TELEA)
    a = a.copy(); a[..., :3] = rgb
    return a

def save(a, name, size=256):
    ys, xs = np.where(a[..., 3] > 8)
    a = a[ys.min():ys.max() + 1, xs.min():xs.max() + 1]
    h, w = a.shape[:2]
    s = max(h, w)
    canvas = np.zeros((s, s, 4), np.uint8)
    canvas[(s - h) // 2:(s - h) // 2 + h, (s - w) // 2:(s - w) // 2 + w] = a
    Image.fromarray(canvas).resize((size, size), Image.LANCZOS).save(os.path.join(OUT, name), optimize=True)

def near(color, tol):
    c = np.array(color)
    return lambda rgb: np.abs(rgb - c).max(axis=-1) <= tol


lum = lambda rgb: rgb.mean(axis=-1)
sat = lambda rgb: rgb.max(axis=-1) - rgb.min(axis=-1)
dark_or_color = lambda rgb: (lum(rgb) < 170) | (sat(rgb) > 40)

ps1 = cut_bg(load("ps1.webp"), lambda rgb: (rgb[..., 0] - rgb[..., 2]) > 45)
save(erase(ps1, [(695, 410, 805, 445), (712, 630, 800, 710)], dark_or_color), "console_ps1.png")
gb = cut_bg(load("game_boy.jpg"), near((255, 255, 255), 12))
save(erase(gb, [(118, 268, 292, 294)], dark_or_color), "console_gb.png")
gbc = cut_bg(load("GB color.jpg"), near((228, 229, 250), 12))
save(erase(gbc, [(314, 292, 488, 324)], lambda rgb: (lum(rgb) > 80) | (sat(rgb) > 40)), "console_gbc.png")
save(erase(load("nes.png"), [(138, 108, 214, 150)], lambda rgb: (rgb[..., 0] > 140) & (rgb[..., 1] < 110)), "console_nes.png")
save(erase(load("PSP.png"), [(126, 150, 164, 161)], lambda rgb: lum(rgb) > 110), "console_psp.png")
print("ok")

# GBA: ลบโลโก้บนจอ (เติมสีไล่ระดับของจอแทน), ป้ายด้านบน และตัวอักษรใต้จอ
gba = load("gameboyAdvance-icon-17218.png")
for yy in range(187, 319):
    gba[yy, 151:355, :3] = gba[yy, 155, :3]
gba[333:352, 185:320, :3] = 0
save(erase(gba, [(218, 138, 290, 162)], lambda rgb: lum(rgb) >= 0), "console_gba.png")
print("gba ok")

# PS2 (ARMSX2): พื้นหลังโปร่งใสอยู่แล้ว — ลบแค่โลโก้ "PS2", ตัวหนังสือ "PlayStation 2" และชื่อผู้ผลิตด้านบน
# ด้านหน้าตัวเครื่องเป็นสีเทาเข้มเรียบ (51,51,51) → ทาทับกรอบโลโก้ด้วยสีนั้นเลย (inpaint จะทิ้งรอยเงา)
ps2 = load("Standing_Black_PlayStation_2_icon.png")
for x0, y0, x1, y1 in [(71, 96, 128, 142)]:
    ps2[y0:y1, x0:x1, :3] = 51
light_or_color = lambda rgb: (lum(rgb) > 42) | (sat(rgb) > 30)
save(erase(ps2, [(136, 4, 166, 16)], light_or_color), "console_ps2.png")
