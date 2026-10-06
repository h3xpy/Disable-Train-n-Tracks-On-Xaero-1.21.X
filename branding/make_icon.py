# Generates the mod icon as 64x64 pixel art, upscaled with nearest neighbour.
# Run from this folder: python make_icon.py
from PIL import Image, ImageDraw
import random

N = 64
img = Image.new("RGBA", (N, N), (0, 0, 0, 0))
d = ImageDraw.Draw(img)
rnd = random.Random(7)

# Map tile: terrain pixels in the style of Xaero's World Map.
grass = [(94, 140, 58), (104, 152, 64), (86, 128, 52), (112, 158, 70)]
water = [(52, 96, 170), (58, 104, 178), (48, 90, 160)]
sand = [(214, 200, 140), (204, 190, 130)]
for y in range(2, 62):
    for x in range(2, 62):
        dx, dy = x - 54, y - 54
        r = dx * dx + dy * dy
        c = rnd.choice(water) if r < 130 else rnd.choice(sand) if r < 175 else rnd.choice(grass)
        img.putpixel((x, y), c + (255,))

# Create's train map: thick pink lines with a dark edge, plus stations.
track, edge = (236, 70, 120, 255), (120, 24, 60, 255)
paths = [[(2, 12), (20, 12), (30, 2)], [(20, 12), (20, 30)], [(44, 2), (44, 20), (61, 20)],
         [(2, 52), (12, 52), (12, 61)], [(52, 30), (61, 30)]]
for w, col in [(6, edge), (4, track)]:
    for p in paths:
        d.line(p, fill=col, width=w, joint="curve")
for (x, y) in [(20, 12), (44, 20)]:
    d.rectangle([x - 3, y - 3, x + 3, y + 3], fill=(250, 250, 250, 255), outline=edge)

# Central badge: dark plate with a crossed-out eye.
plate, outline, white = (24, 27, 38, 255), (12, 13, 20, 255), (245, 245, 245, 255)
d.rounded_rectangle([9, 17, 54, 47], radius=6, fill=plate, outline=outline, width=2)
d.ellipse([14, 23, 49, 41], fill=white, outline=outline, width=1)
d.ellipse([25, 24, 38, 40], fill=(64, 108, 196, 255))
d.ellipse([28, 27, 35, 37], fill=outline)
d.rectangle([29, 28, 30, 29], fill=white)
slash = [(16, 43), (20, 46), (48, 21), (44, 18)]
d.polygon([(14, 44), (20, 49), (50, 22), (44, 16)], fill=outline)
d.polygon(slash, fill=(236, 52, 52, 255))

# Outer frame with notched corners.
frame = (28, 31, 42, 255)
d.rectangle([0, 0, 63, 63], outline=frame, width=2)
for (x, y) in [(0, 0), (63, 0), (0, 63), (63, 63), (1, 0), (0, 1), (62, 0), (63, 1), (0, 62), (1, 63), (63, 62), (62, 63)]:
    img.putpixel((x, y), (0, 0, 0, 0))

img.resize((512, 512), Image.NEAREST).save("icon_512.png")
img.resize((128, 128), Image.NEAREST).save("../src/main/resources/hidetrainmap.png")
