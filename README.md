# Stored Fall Damage
In Minecraft, sometimes mistakes happen. You might fall off a cliff, maybe misjudge a drop, or perhaps land perfectly on the edge of a block and miss your water bucket because of it! This is where Stored Fall Damage comes in: When you take a lethal fall you'll be saved at half a heart and all the damage that would've killed you is "stored" into your hearts themselves! To repair your hearts you can regen any way you'd like, but be careful! Until you do so you'll be locked at half a heart permanently!

Of course, there are small drops and minor accidents, or maybe you're taking a risk you shouldn't! But don't test your luck! Even this has limits...
But as long as you survive you can heal back up to full health, provided you have enough resources!

Additionally, two effects are introduced (and can be turned on/off by configs):
- Storing Shield, which can protect from more damage for a period after storing damage.
- Unstable Hearts, which serves as a countdown that you need to recover your health by!

### Configuration
Of course, this mod has a few config options to make things a little easier (or harder?) if you so desire. They can be found in `config/storedfalldamage.toml`:

`activation_requirement` - Pick between only storing damage when holding an item that could save you, or always storing fall damage! Or disable it entirely...? By default it requires holding a "clutch" item.

`scaling` - Maybe you think stored damage should be easier to heal, or maybe you want to punish yourself and make it extra hard to recover? You can scale any damage stored, if you're not happy with a 1:1 store! The default is no scaling.

`limit` - Perhaps you think dropping from orbit is fun, or maybe you think stored damage should only save you from minor mistakes! Raise or lower the limit of stored damage as you like! The default limit is 120 stored damage, equal to falling 143 blocks from full health.

Should be fairly compatible with other mods that render custom heart overlays (for example, Mantle's signature colored hearts), as Stored Fall Damage only attempts to render when you're actually storing damage and lets other mods do the work otherwise. Yay for compatibility!

### Datapacks
Stored Fall Damage's features are heavily built around datapacks. Not only are the items used to save yourself determined by them, but the types of damage you'll be saved from are also controlled by them! This means that you can save yourself from things other than fall damage if you so desire.

The datapack structure works as follows:
```
data/
└── storedfalldamage/
    ├── damage_type/
    │   └── unstable_hearts_timeout.json
    └── tags/
        ├── damage_type/
        │   ├── storing_shield_nulls/
        │   │   └── <namespace>/
        │   │       └── <damage_type>.json
        │   └── stored_damage_types.json
        └── items/
            └── storing_items/
                └── <namespace>/
                    └── <damage_type>.json
```
- `stored_damage_types.json` defines what damage types can trigger stored damage
- Clutch items for a specific damage type are defined in `storing_items/`. For example, if you wanted to add an item that protects from explosions, it'd go in `storing_items/minecraft/explosion.json`. Vanilla damage types can be found [here](https://minecraft.wiki/w/Damage_type#List_of_damage_types).
  - `fall.json` is defined by default.
- Storing Shield protects from future damage for a short duration based on the damage type that triggered stored damage. For example, if you wanted players to be immune to fall damage after an explosion you'd add `minecraft:fall` to `storing_shield_nulls/minecraft/explosion.json`
  - `fall.json` is defined by default.