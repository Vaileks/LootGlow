"""Verify the shipped intermediary mixin and the 1.21.11 outline render path.
Usage: python3 tests/verify_render_path.py <named minecraft jar>
Requires a JDK (JAVA_HOME or javap on PATH); run after ./gradlew build.
"""
from pathlib import Path
import json, os, subprocess, sys, zipfile
root = Path(__file__).resolve().parents[1]
javap = str(Path(os.environ["JAVA_HOME"]) / "bin/javap") if "JAVA_HOME" in os.environ else "javap"
def dump(jar, cls, verbose=False):
    return subprocess.check_output([javap, "-p", "-v" if verbose else "-c", "-classpath", str(jar), cls], text=True)
jar = root / "build/libs/LootGlow.jar"
with zipfile.ZipFile(jar) as z:
    assert z.testzip() is None
    mixins = json.loads(z.read("lootglow.mixins.json"))
    assert "ItemEntityRendererMixin" in mixins["client"]
    assert json.loads(z.read("fabric.mod.json"))["authors"] == ["Vaileks"]
mixin = dump(jar, "ru.bloodstone.lootglow.mixin.ItemEntityRendererMixin", True)
assert "net/minecraft/client/render/entity/ItemEntityRenderer" not in mixin, "Mixin target not remapped"
assert "updateRenderState(" not in mixin, "Injection selector not remapped"
assert 'value="TAIL"' in mixin
assert "LootGlowClient.outlineColor" in mixin
assert "putfield" in mixin and "ior" in mixin and "-16777216" in mixin, "Missing opaque ARGB outline write"
world = dump(sys.argv[1], "net.minecraft.client.render.WorldRenderer")
start = world.index("getAndUpdateRenderState:")
assert start < world.index("EntityRenderState.hasOutline:", start), "World pass must inspect updated item state"
item = dump(sys.argv[1], "net.minecraft.client.render.entity.ItemEntityRenderer")
assert "updateRenderState(net.minecraft.entity.ItemEntity, net.minecraft.client.render.entity.state.ItemEntityRenderState, float)" in item
assert "outlineColor:I" in item and "ItemRenderState.render:" in item
print("PASS shipped renderer mixin, intermediary remapping, opaque outline and world outline-pass ordering")
