"""Read existing local plugin sources/presets; never writes to a plugin or resource pack."""
from pathlib import Path
import re,json
R=Path(__file__).resolve().parent;P=R.parent
full=P/'GitHub/RunesLegendary/RunesLegendary/src/main'
framework=P/'GitHub/LegendaryDopPlugin/src/main/resources/presets'
csi=P/'LegendaryPlugins-release/sources/CustomSpeedItems'
models=set()
for root in (full,framework):
 for path in root.rglob('*'):
  if path.suffix not in ('.java','.yml'):continue
  text=path.read_text(errors='replace')
  models.update(re.findall(r'[a-z0-9_.-]+:tools/[a-z0-9_./-]+',text))
  for namespace,key in re.findall(r'new NamespacedKey\(\s*(?:\(String\))?"([a-z0-9_.-]+)",\s*(?:\(String\))?"(tools/[a-z0-9_./-]+)"\s*\)',text):models.add(namespace+':'+key)
# Only tool families actually referenced by RunesLegendary, not every resource-pack tool or item model.
prefixes={m.split('/')[0]+'/'+m.split('/')[1]+'/':'LEGENDARY' for m in models if len(m.split('/'))>=2}
# Both namespace variants occur in RunesLegendary generations and the supplied resource pack.
for prefix in list(prefixes):
 family=prefix.split(':',1)[1]
 for namespace in ('bloodstone','civilization'):prefixes[namespace+':'+family]='LEGENDARY'
exact={m:'LEGENDARY' for m in sorted(models)}
exact['fullleg_dark:fx/wand']='LEGENDARY'
# The supplied Ballista RP defines this whole-machine root model. Do not match
# every ModelEngine item: other model parts and mobs are not dropped epic loot.
exact['modelengine:blood_ballista_pack_v2/root']='EPIC'
# Extract per-material CMD defaults from the supplied original CSI item factory + its config.
config={};section=None
for line in (csi/'src/main/resources/config.yml').read_text().splitlines():
 m=re.match(r'^(\w+):\s*$',line)
 if m:section=m[1]
 m=re.match(r'^  (\w+):\s*(\d+)',line)
 if m and section:config[section+'.'+m[1]]=int(m[2])
source=(csi/'src/main/java/dark/hopliteWeapons/CustomWeapons.java').read_text()
classic={'MidasSword','ArtemisBowDisabled','ArtemisBowEnabled','ReaperScythe','SonicCrossbow','Excalibur','DragonKatana','EmeraldBlade','Mjolnir','ShadowBlade','CloudSword','Laser'}
rules=[]
for name,body in re.findall(r'private static ItemStack (\w+)\(\)\s*\{(.*?)(?=\n   (?:private|public) static|\Z)',source,re.S):
 material=re.search(r'new ItemStack\(Material\.(\w+)\)',body)
 if not material:continue
 vals=set()
 # getInt calls used for the CMD variable or directly inside setCustomModelData only.
 for key,default in re.findall(r'(?:int custommodeldata\s*=\s*|setCustomModelData\()config.getInt\("([^\"]+)"(?:,\s*(\d+))?\)',body):
  value=config.get(key,int(default) if default else None)
  if value is not None:vals.add(value)
  if default:vals.add(int(default))
 vals.update(map(int,re.findall(r'setCustomModelData\((\d+)\)',body)))
 if vals:rules.append({'item':'minecraft:'+material[1].lower(),'values':sorted(vals),'kind':'LEGENDARY' if name in classic else 'EPIC','label':'CSI '+name})
# Current and prior RunesLegendary spell-wand variants; model rules cover the dark wand too.
rules += [
 {'item':'minecraft:blaze_rod','values':[1,24001],'kind':'LEGENDARY','label':'RunesLegendary Elder Wand'},
 {'item':'minecraft:stick','values':[24001],'kind':'LEGENDARY','label':'RunesLegendary prior Elder Wand'},
 {'item':'minecraft:bow','values':[5555],'kind':'LEGENDARY','label':'CSI TimeBow'},
 {'item':'minecraft:netherite_sword','values':[7492],'kind':'LEGENDARY','label':'RunesLegendary CloudSword'},
]
# The integrated End Staff uses the configured material + CMD, without item_model.
# Read all shipped profiles so meetups/default/smp stay covered after edits.
for profile in ('smp','default_presets','meetups'):
 path=framework/profile/'SpecialItems/EndStaff.yml'
 text=path.read_text()
 material=re.search(r'^material:\s*([A-Z_]+)\s*$',text,re.M)
 cmd=re.search(r'^custom-model-data:\s*(\d+)\s*$',text,re.M)
 model=re.search(r"^item_model:\s*['\"]?([a-z0-9_.-]+:[a-z0-9_./-]+)['\"]?\s*$",text,re.M)
 if model:exact[model[1]]='LEGENDARY'
 if material and cmd:
  rule={'item':'minecraft:'+material[1].lower(),'values':[int(cmd[1])],'kind':'LEGENDARY','label':'RunesLegendary End Staff'}
  if rule not in rules:rules.append(rule)
# Resource-pack-only old variants are still identifiable by their original item+CMD pair.
data={'models':exact,'modelPrefixes':dict(sorted(prefixes.items())),'customModelData':rules}
(R/'src/main/resources/lootglow-rules.json').write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n')
print(len(prefixes),'RunesLegendary model families;',len(rules),'CSI/legacy CMD rules;',sum(len(r['values']) for r in rules),'CMD values')
