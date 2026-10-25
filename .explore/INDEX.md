# Explore Index

Read before any broad search. Last write wins. Rebuild: python .agents/skills/explore-index/scripts/reindex.py
Validate: python .agents/skills/explore-index/scripts/verify.py

## Absent — proven not to exist

Answer is no. Stop searching; do not retry synonyms.

direct feeding keeper ai :: ABSENT :: src/main/java/net/zootycoon/entity/ZooZookeeperEntity.java :: roadmap lines 36-37; keeper direct feeding not implemented
dynamic visitor scaling :: ABSENT :: src/** :: roadmap lines 40-41; spawner scaling not yet implemented
financial ticket pricing system :: ABSENT :: src/** :: roadmap lines 42-43; staff wages not yet implemented
negative debuff on critical stats :: ABSENT :: src/main/java/net/zootycoon/animal/ZooAnimalHungerManager.java :: roadmap line 36; negative effect on 0 stats not yet implemented
water auto pathing :: ABSENT :: src/main/java/net/zootycoon/animal/ZooAnimalHungerManager.java :: roadmap line 39; auto pathing to water not yet implemented
zoo rating calculation :: ABSENT :: src/** :: roadmap lines 38-39; rating formula not yet implemented

## Regions — a concept lives here

Open the directory; already narrowed.

animal diet registry :: REGION :: src/main/java/net/zootycoon/animal/AnimalDietRegistry.java :: food diets, trough vs thrown meat mapping
animal hunger manager :: REGION :: src/main/java/net/zootycoon/animal/ZooAnimalHungerManager.java :: hunger, thirst, cleanliness, food throwing
builder entity :: REGION :: src/main/java/net/zootycoon/entity/ZooBuilderEntity.java :: builder logic and blueprints
cashier entity :: REGION :: src/main/java/net/zootycoon/entity/ZooCashierEntity.java :: cashier logic and ticket handling
central warehouse supply :: REGION :: src/main/java/net/zootycoon/warehouse :: empty barrel warehouse, recipe resolver, staff supply requests
claim system :: REGION :: src/main/java/net/zootycoon/claim :: zoo claim management and boundaries
feeding trough block :: REGION :: src/main/java/net/zootycoon/block/ZooFeedingTroughBlock.java :: trough block and known troughs registry
feeding trough entity :: REGION :: src/main/java/net/zootycoon/block/ZooFeedingTroughBlockEntity.java :: trough inventory, water, food storage
hologram docs :: REGION :: docs/hologram :: rules and progress for hologram system
network payloads :: REGION :: src/main/java/net/zootycoon/network :: packets for dialogs, trough sync, zookeeper
zookeeper entity :: REGION :: src/main/java/net/zootycoon/entity/ZooZookeeperEntity.java :: keeper logic, follow animal, claim stroll, refilling

## Rules — how this repo works

Follow it before writing code here.

build output destination :: RULE :: final mod jar :: output destination to ModrinthApp profiles Fresh Smooth
no bold or emoji :: RULE :: agent output :: no bold font, no emoji in responses or dialogs
no visual regressions :: RULE :: entity rendering :: do not replace 3D entity renders with 2D items
scope guard strict :: RULE :: staff classes :: do not touch unmentioned staff/builder/cashier classes
zookeeper initial food :: RULE :: zookeeper entity :: never give initial food; player must supply keeper first

## Real paths — repo map missed these

Read the file.

animal list docx :: REAL :: Daftar_Satwa_Kebun_Binatang.docx :: curated zoo animal collection
zootycoon rules file :: REAL :: .agents/rules/zootycoonrule.md :: central instructions and active roadmap

---
entries: 24 journalled
cap: 40 per bucket
