import os
import io
from urllib.request import Request, urlopen
import numpy
from PIL import Image
from bs4 import BeautifulSoup
from multiprocessing.pool import ThreadPool
import multiprocessing
from oxipng import StripChunks, Deflaters, oxipng
import json

def get_request(url):
    return Request(url=''.join(url.splitlines()), headers={'User-Agent': 'Mozilla/6.0'})

def optimize_image(image_array, iterations=1):
    image_bytes = io.BytesIO()
    Image.fromarray(image_array).save(image_bytes, format='PNG')
    return oxipng.optimize_from_memory(image_bytes.getvalue(),
        level=6,
        optimize_alpha=True,
        strip=StripChunks.all(),
        deflate=Deflaters.zopfli(iterations))

def generate_icon(icon_url, biome_id):
    with Image.open(urlopen(get_request(icon_url))) as icon:
        icon_pixels = numpy.array(icon.convert('RGBA'))
        
    # Sheer corners by one
    icon_pixels[0, 0] = icon_pixels[0, 15] = icon_pixels[15, 0] = icon_pixels[15, 15] = (0, 0, 0, 0)
    # Sheer corners by two
    # icon_pixels[0, 0:2] = icon_pixels[0, 14:16] = icon_pixels[15, 0:2] = icon_pixels[15, 14:16] = (0, 0, 0, 0)

    # Optimize icon image using oxipng
    with open(os.path.join(icon_path, f'{biome_id}.png'), 'wb') as icon:
        icon.write(optimize_image(icon_pixels, iterations=255))
    print(f'Generated: {biome_id}.png')

if __name__ == '__main__':
    mod_id = 'travellerstoasts'
    target_request = get_request('https://minecraft.wiki/w/Biome#Biome_IDs')
    base_path = f'src/main/resources/assets/{mod_id}'
    icon_path = 'textures/gui/sprites/biome/minecraft'
    atlas_path = 'atlases'
    pool = ThreadPool(multiprocessing.cpu_count())
    
    os.chdir(os.path.normpath(os.path.join(os.path.dirname(__file__), base_path)))
    with urlopen(target_request) as response:
        HTML = response.read()

    # Getting the next table present after the 'Biome IDs' section.
    biome_table = BeautifulSoup(HTML, features='html.parser') \
        .find('span', {'class': 'mw-headline', 'id': 'Biome_IDs'}) \
        .find_next('table', {'data-description': 'Java Biome IDs'})

    # Parsing and getting the biome icons main image.
    entries = biome_table.find_all('td')
    atlas_data = {'sources': [
        {'type': 'single', 'resource': f'{mod_id}:toast/plaque'},
        {'type': 'single', 'resource': f'{mod_id}:toast/plaque_rounded'}
    ]}
    for i in range(0, len(entries) - 1, 3):
        icon_url = f'https://minecraft.wiki{entries[i].find("img")["src"]}'
        biome_id = ''.join(entries[i + 1].find('code').text.splitlines())
        resource = f'{mod_id}:biome/minecraft/{biome_id}'
                
        pool.apply_async(generate_icon, (icon_url, biome_id))
        # Write gui atlas json entry
        atlas_data['sources'].append({'type': 'single', 'resource': resource})
    
    pool.close()
    pool.join()
    
    with open(f'{atlas_path}/gui.json', 'w') as gui_atlas:
        json.dump(atlas_data, gui_atlas, indent=4)
    print('Generated: gui.json')