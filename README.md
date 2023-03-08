# TravellersToasts 
Vanilla Minecraft features a plethora of biomes, yet many go overlooked due to upfront visual similarities with other mods. TravellersToasts is a small mod which aims to better  express the personalities of each biome by displaying a *toast*[^1]  when you begin '***exploring***' a new biome—take a look!

![Biome Toast Demo](https://files.catbox.moe/n4tz8u.gif "Biome Toast Demo")
<sup>**·** If you were curious, the shaders used this for demo are [Complementary Shaders](https://www.complementary.dev).

A longstanding issue I've had with vanilla Minecraft is how unrewarding it can feel to visit new biomes. I hope this small addition could help incentivize/reward aimless traveling—even if just a bit more.

## Features (Technical Information)
...Well, the singular feature of this mod fairly evident lol, so I'll use this section to talk about how the issue of ***toast spam*** was tackled—that is, the issue of having the biome toast display on ***every*** biome transition:

* **Movement Checks** *(biome toasts will not be immediately shown when entering a new biome until the player's future position (based on current velocity) remains within the said biome for a set amount of time).*
	* This prevents biome toasts from showing up when 'skimming' a new biome or passing a biome quickly (e.g. riding a boat on ice).
- **Cooldown Checks** *(a biome toast for a given biome will never show up twice within a configurable cooldown period).*
	- This prevents biome toasts from showing in bases or commonly explored areas.
* **Location Checks** *(biome toasts will not be shown when underground, unless in a cave biome (e.g. lush caves)).*
	* This prevents biome toasts from showing up when—for example—strip mining (as opposed to the default behavior of the underground biome mirroring the surface biome).
	* Additionally, for ocean biomes, toasts will not show if the player is *not* swimming (e.g. riding a boat).
- **Inhabited Time Checks** *(biome toasts will never be shown when entering a chunk with an inhabited time past a \*configurable\* threshold).*
	- This prevents biome toasts from showing in bases or commonly explored areas.
  
Summarizing, to begin '***exploring***' a new biome is to enter a biome whilst passing all the above checks (probably with a few more I forgot to list). Hopefully this results in a smoother experience using the mod—thanks for reading!

Download the latest version over at [Modrinth](https://modrinth.com/mod/TravellersToasts)!
	
[^1]: Icons were scraped from the [Biomes](https://minecraft.fandom.com/wiki/Biome) page on the Minecraft Wiki. I am not sure if the icons presented are 'official' but they look really good!
