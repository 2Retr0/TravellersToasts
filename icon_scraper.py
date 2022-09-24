import os
import fandom
import urllib.request
import numpy
from PIL import Image
from bs4 import BeautifulSoup

if __name__ == '__main__':
    target_path = 'src/main/resources/assets/travellerstoasts/textures/gui/icons/'
    os.chdir(os.path.normpath(os.path.join(os.path.dirname(__file__), target_path)))

    HTML = fandom.page("Biome", wiki='Minecraft').html
    # Getting the next table present after the 'Biome IDs' section.
    biome_table = BeautifulSoup(HTML, features='html.parser') \
        .find('span', {'class': 'mw-headline', 'id': 'Biome_IDs'}) \
        .find_next('table', {'class': 'wikitable stikitable sortable'})

    # Parsing and getting the biome icons main image.
    image_url = biome_table.find_next('span').get('style').split('(')[1].split(')')[0]
    main_image = Image.open(urllib.request.urlopen(image_url))

    entries = biome_table.find_all('td')
    for i in range(len(entries) - 1):
        if (tag := str(entries[i].find('span'))) and (identifier := entries[i + 1].find('code')):
            identifier = identifier.text.strip()

            offset = tag.split('background-position:')[1].split('"')[0]
            offset = offset.replace('-', '').replace('px', '')

            x, y = map(int, offset.split(' '))
            icon = main_image.crop((x, y, x + 16, y + 16)).convert('RGBA')

            pixels = numpy.array(icon)
            # Sheer corners by one
            pixels[0, 0] = pixels[0, 15] = pixels[15, 0] = pixels[15, 15] = (0, 0, 0, 0)

            # Sheer corners by two
            # pixels[0, 0:2] = pixels[0, 14:16] = pixels[15, 0:2] = pixels[15, 14:16] = (0, 0, 0, 0)

            Image.fromarray(pixels).save(os.path.join('minecraft', identifier + '.png'))
            print("Generated: " + identifier + '.png')
