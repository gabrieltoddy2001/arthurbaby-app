"""Gera a logo circular do app a partir de drawable/logo_arthurbaby.png.
- estica a elipse original para um círculo exato;
- remove o contorno branco original: as faixas verde/rosa passam a encostar no anel;
- aplica anel verde fino e uniforme."""
import math, sys
from PIL import Image, ImageDraw
res = sys.argv[1]
VERDE = (0x37, 0xB6, 0xB0, 255)
src = Image.open(res + '/drawable/logo_arthurbaby.png').convert('RGBA')
D = max(src.size)
logo = src.resize((D, D), Image.LANCZOS)
base = Image.new('RGBA', (D, D), (255, 255, 255, 255)); base.alpha_composite(logo)
px = base.load(); orig = logo.load()
c = (D - 1) / 2; R = D / 2; ZONA = 16

TEAL = (2, 178, 176); PINK = (252, 92, 164)
def perto(p, ref, tol=40): return p[3] > 200 and all(abs(p[i] - ref[i]) <= tol for i in range(3))
def faixa(p):
    if perto(p, TEAL): return TEAL
    if perto(p, PINK): return PINK
    return None

def claro(p): return p[3] < 200 or min(p[:3]) > 190
def dentro(x, y): return math.hypot(x - c, y - c) <= R

# 1) Descobre o bloco contínuo de linhas de cada faixa (linhas quase todas da cor da faixa)
fracao = {}
for y in range(D):
    cont = {TEAL: 0, PINK: 0}; tot = 0
    for x in range(0, D, 2):
        if not dentro(x, y): continue
        tot += 1
        cf = faixa(orig[x, y])
        if cf: cont[cf] += 1
    fracao[y] = {k: (v / tot if tot else 0) for k, v in cont.items()}

linha_faixa = {}
for cor in (TEAL, PINK):
    fortes = [y for y in range(D) if fracao[y][cor] > 0.8]
    # agrupa em blocos (tolerando linhas de texto no meio da faixa)
    blocos, atual = [], []
    for y in fortes:
        if atual and y - atual[-1] > 40: blocos.append(atual); atual = []
        atual.append(y)
    if atual: blocos.append(atual)
    if not blocos: continue
    maior = max(blocos, key=lambda b: b[-1] - b[0])
    for y in range(maior[0], maior[-1] + 1): linha_faixa[y] = cor
    print('faixa', cor, maior[0], maior[-1])

# 2) Nessas linhas, o contorno branco nas pontas recebe a cor da faixa
for y, cf in linha_faixa.items():
    for lado in (1, -1):
        dy = y - c
        meia = math.sqrt(max(0.0, R * R - dy * dy))
        borda = int(math.floor(c - meia)) if lado == 1 else int(math.ceil(c + meia))
        for k in range(0, 80):
            x = borda + lado * k
            if not (0 <= x < D): continue
            p = orig[x, y]
            if all(faixa(orig[min(D-1, max(0, x + lado * j)), y]) == cf for j in range(8)):
                for j in range(-10, k):
                    xx = borda + lado * j
                    if 0 <= xx < D: px[xx, y] = (p[0], p[1], p[2], 255)
                break

# 3) Embaixo da faixa rosa (borda quase horizontal): completa na vertical
ys_rosa = [y for y, cf in linha_faixa.items() if cf == PINK]
if ys_rosa:
    for x in range(D):
        dx = x - c
        if abs(dx) >= R: continue
        borda = int(math.ceil(c + math.sqrt(R * R - dx * dx)))
        for k in range(0, 80):
            y = borda - k
            if not (0 <= y < D): continue
            p = orig[x, y]
            if all(faixa(orig[x, max(0, y - j)]) == PINK for j in range(8)):
                for j in range(-10, k):
                    yy = borda - j
                    if 0 <= yy < D: px[x, yy] = (p[0], p[1], p[2], 255)
                break

def mask(size, ss=4):
    m = Image.new('L', (size * ss, size * ss), 0)
    ImageDraw.Draw(m).ellipse((0, 0, size * ss - 1, size * ss - 1), fill=255)
    return m.resize((size, size), Image.LANCZOS)

def redonda(d):
    l = base.resize((d, d), Image.LANCZOS); l.putalpha(mask(d)); return l

PROP = 0.965
def completo(size):
    img = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    v = Image.new('RGBA', (size, size), VERDE); v.putalpha(mask(size)); img.alpha_composite(v)
    d = round(size * PROP); d -= (size - d) % 2; o = (size - d) // 2
    img.alpha_composite(redonda(d), (o, o)); return img

completo(600).save(res + '/drawable-nodpi/logo_circular.png', optimize=True)
for k, f in {'mdpi': 1, 'hdpi': 1.5, 'xhdpi': 2, 'xxhdpi': 3, 'xxxhdpi': 4}.items():
    fg = round(108 * f); d = round(69.5 * f); d -= (fg - d) % 2; o = (fg - d) // 2
    im = Image.new('RGBA', (fg, fg), (0, 0, 0, 0)); im.alpha_composite(redonda(d), (o, o))
    im.save(f'{res}/mipmap-{k}/ic_launcher_foreground.webp', lossless=True)
    l = completo(round(48 * f))
    l.save(f'{res}/mipmap-{k}/ic_launcher.webp', lossless=True)
    l.save(f'{res}/mipmap-{k}/ic_launcher_round.webp', lossless=True)
print('ok')
