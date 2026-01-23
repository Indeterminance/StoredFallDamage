# Stored Fall Damage
In Minecraft, sometimes mistakes happen. You might fall off a cliff, maybe misjudge a drop, or perhaps land perfectly on the edge of a block and miss your water bucket because of it! This is where Stored Fall Damage comes in: When you take a lethal fall you'll be saved at half a heart and all the damage that would've killed you is "stored" into your hearts themselves! To repair your hearts you can regen any way you'd like, but be careful! Until you do so you'll be locked at half a heart permanently!

Of course, there are small drops and minor accidents, or maybe you're taking a risk you shouldn't! But don't test your luck! Even this has limits...
But as long as you survive you can heal back up to full health, provided you have enough resources!

### Configuration
Of course, this mod has a few config options to make things a little easier (or harder?) if you so desire. They can be found in `config/storedfalldamage.toml`:

`activation_requirement` - Pick between only storing damage when holding an item that could save you, or always storing fall damage! Or disable it entirely...? By default it requires holding a "clutch" item.

`scaling` - Maybe you think stored damage should be easier to heal, or maybe you want to punish yourself and make it extra hard to recover? You can scale any damage stored, if you're not happy with a 1:1 store! The default is no scaling.

`limit` - Perhaps you think dropping from orbit is fun, or maybe you think stored damage should only save you from minor mistakes! Raise or lower the limit of stored damage as you like! The default limit is 120 stored damage, equal to falling 143 blocks from full health.

Should be fairly compatible with other mods that render custom heart overlays (for example, Mantle's signature colored hearts), as Stored Fall Damage only attempts to render when you're actually storing damage and lets other mods do the work otherwise. Yay for compatibility!