const fs = require('fs');
const path = require('path');
let html = fs.readFileSync('html.html');


const cheerio = require('cheerio');
const $ = cheerio.load(html);
console.log($('a.gtm-expand-full-size-illust img').map((i, el) => {
    const url = $(el).attr('src');
    return `url = ${url}
output = "${path.basename(url)}"
referer = "https://www.pixiv.net/artworks/71408439"`
}).get().join('\n\n'));