require 'pathname'
require 'json'

if ARGV.count != 3
  puts "Usage: <id-prefix> <base-block> <english-prefix>"
  puts "Example: dark_oak dark_oak_planks \"Dark Oak\""
  exit 1
end

type = "acacia"
base_block = "acacia_planks"
english_prefix = "Acacia"

type, base_block, english_prefix = ARGV

full_id = "#{type}_shelf"
full_localized_name = "#{english_prefix} Shelf"

puts full_id, base_block, full_localized_name

lang_file = Pathname("assets/shelf/lang/en_us.json")
lang_json = JSON.parse(lang_file.read)
lang_json["block.shelf.#{full_id}"] = full_localized_name
lang_file.write JSON.pretty_generate(lang_json)

Pathname("assets/shelf/blockstates/#{full_id}.json").write <<~EOS.chomp
{
  "variants": {
    "facing=east": {
      "model": "shelf:block/#{full_id}",
      "y": 0,
      "uvlock": true
    },
    "facing=north": {
      "model": "shelf:block/#{full_id}",
      "y": 270,
      "uvlock": true
    },
    "facing=south": {
      "model": "shelf:block/#{full_id}",
      "y": 90,
      "uvlock": true
    },
    "facing=west": {
      "model": "shelf:block/#{full_id}",
      "y": 180,
      "uvlock": true
    }
  }
}
EOS

Pathname("assets/shelf/models/block/#{full_id}.json").write <<~EOS.chomp
{
  "parent": "shelf:block/shelf",
  "textures": {
    "bottom": "minecraft:block/#{base_block}",
    "top": "minecraft:block/#{base_block}",
    "side": "minecraft:block/#{base_block}"
  }
}
EOS

Pathname("assets/shelf/models/item/#{full_id}.json").write <<~EOS.chomp
{
  "parent": "shelf:block/#{full_id}"
}
EOS

Pathname("data/shelf/loot_tables/blocks/#{full_id}.json").write <<~EOS.chomp
{
"type": "minecraft:block",
"pools": [
  {
    "rolls": 1,
    "entries": [
      {
        "type": "minecraft:item",
        "functions": [
          {
            "function": "minecraft:copy_name",
            "source": "block_entity"
          }
        ],
        "name": "shelf:#{full_id}"
      }
    ],
    "conditions": [
      {
        "condition": "minecraft:survives_explosion"
      }
    ]
  }
]
}
EOS

Pathname("data/shelf/recipes/#{full_id}.json").write <<~EOS.chomp
{
  "type": "minecraft:crafting_shaped",
  "pattern": [
    "# ",
    "##"
  ],
  "key": {
    "#": {
      "item": "minecraft:#{base_block}"
    }
  },
  "result": {
    "item": "shelf:#{full_id}"
  }
}
EOS