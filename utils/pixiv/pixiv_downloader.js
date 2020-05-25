const path = require('path');

const lpad3 = (val) => {
    return "000".substring(`${val}`.length) + `${val}`
};

const jsonString = require('fs').readFileSync(0).toString("utf8");
try {
    const json = JSON.parse(jsonString);
    const urlsList = json.body.map((image) => {
        return image.urls.original;
    });
    const seriesId = process.argv[2];
    const seriesName = process.argv[3];
    let downloadTask = "";
    urlsList.forEach((f, id) => {

        const outFileName = `${seriesName}_p${lpad3(id)}${path.extname(f)}`;

        const url = `url = "${f}"`;
        const output = `output = "${seriesName}/${outFileName}"`;
        const referer = `referer = "https://www.pixiv.net/artworks/${seriesId}"`;
        downloadTask += [url, output, referer].join("\n");
        downloadTask += "\n\n";
    });
    console.log(downloadTask);
} catch (e) {
    console.error(e);
    process.exit(1);
}